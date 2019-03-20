/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.compute.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.oracle.bmc.core.model.Volume;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.BlockStorageClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;

public class VolumeTable extends BaseTable {
    private int tableDataSize = 0;

    private static final int NAME_COL = 0;
    private static final int STATE_COL = 1;
    private static final int SIZE_COL = 2;
    private static final int AVL_DOMAIN_COL = 3;

    public VolumeTable(Composite parent, int style) {
        super(parent, style);

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
    }
    List<Volume> volumesList = new ArrayList<Volume>();
    @Override
    public List<Volume> getTableData() {
        new Job("Get Volumes") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    volumesList = BlockStorageClient.getInstance().getVolumes();
                    tableDataSize = volumesList.size();
                } catch (Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                refresh(false);
                return Status.OK_STATUS;
            }
        }.schedule();
        return volumesList;
    }
    @Override
    public List<Volume> getTableCachedData() {
        return volumesList;
    }

    @Override
    public int getTableDataSize() {
        return tableDataSize;
    }

    /* Label provider */
    private final class TableLabelProvider implements ITableLabelProvider {

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return true;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                Volume s = (Volume) element;

                switch (columnIndex) {
                case NAME_COL:
                    return s.getDisplayName();
                case STATE_COL:
                    return s.getLifecycleState().getValue();
                case SIZE_COL:
                    return s.getSizeInGBs().toString();
                case AVL_DOMAIN_COL:
                    return s.getAvailabilityDomain();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
        createColumn(tableColumnLayout,tree, "Volume Name", 15);
        createColumn(tableColumnLayout,tree, "State", 6);
        createColumn(tableColumnLayout,tree, "Size (GB)", 8);
        createColumn(tableColumnLayout,tree, "Availability Domain", 10);
    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        // TODO Auto-generated method stub

    }
}
