/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.editor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.DeleteBucketAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.DeleteObjectAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.DetailsBucketAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.DetailsObjectAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.DownloadObjectAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.RefreshObjectAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.UploadObjectAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.dragdrop.ObjectDragSource;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.dragdrop.ObjectDropTarget;

public class ObjectsTable extends BaseTable {
    private String bucketName;
    private int objectsListSize;
    private final String NotesText = "Drag and drop files or right click below to upload objects.";
    private List<ObjectSummary> objectsList = new ArrayList<ObjectSummary>();

    private static final int NAME_COL = 0;
    private static final int SIZE_COL = 1;
    private static final int DATE_COL = 2;



    public ObjectsTable(Composite parent, int style, String bucketName) {
        super(parent, style);
        this.bucketName = bucketName;

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());

        setupDragAndDrop();
    }

    @Override
    protected List<?> getTableData() {
        new Job("Get Objects") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    ObjStorageClient oci = ObjStorageClient.getInstance();
                    objectsList = oci.getBucketObjects(bucketName);
                    objectsListSize = objectsList.size();
                } catch (Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                refresh(false);
                return Status.OK_STATUS;
            }
        }.schedule();

        return objectsList;
    }

    @Override
    protected List<?> getTableCachedData() {
        return objectsList;
    }

    @Override
    protected int getTableDataSize() {
        return objectsListSize;
    }

    public String getBucketName() {
        return bucketName;
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
        createColumn(tableColumnLayout,tree, "Object Name", 25);
        createColumn(tableColumnLayout,tree, "Size", 15);
        createColumn(tableColumnLayout,tree, "Time created", 22);
    }

    @Override
    protected void fillMenu(IMenuManager manager) {

        manager.add(new RefreshObjectAction(ObjectsTable.this));
        manager.add(new Separator());
        manager.add(new UploadObjectAction(ObjectsTable.this));
        if (getSelectedObjects().size() > 0) {
            manager.add(new DownloadObjectAction(ObjectsTable.this));
            manager.add(new DeleteObjectAction(ObjectsTable.this));
            manager.add(new Separator());
            manager.add(new DetailsObjectAction(ObjectsTable.this));
        }

    }

    protected void setupDragAndDrop() {
        new ObjectDragSource(ObjectsTable.this, viewer);
        new ObjectDropTarget(ObjectsTable.this, viewer);
    }

    /* Label provider */
    private final class TableLabelProvider extends BaseTableLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                ObjectSummary s = (ObjectSummary) element;
                switch (columnIndex) {
                case NAME_COL:
                    return s.getName();
                case SIZE_COL:
                    if (s.getSize() != null) {
                        return getReadableFileSize(s.getSize());
                    }
                case DATE_COL:
                    if (s.getTimeCreated() != null)
                        return s.getTimeCreated().toString();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }

    }

    @Override
    protected void addTableLabels(FormToolkit toolkit, Composite left, Composite right) {

        Button editBucketAclButton = toolkit.createButton(right, "Delete Bucket", SWT.PUSH);
        editBucketAclButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new DeleteBucketAction(getBucketName()).run();;
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        Button deatilsBucketAclButton = toolkit.createButton(right, "Bucket Details", SWT.PUSH);
        deatilsBucketAclButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new DetailsBucketAction(getBucketName()).run();;
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        toolkit.createLabel(left, NotesText);
    }

    public static String getReadableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
