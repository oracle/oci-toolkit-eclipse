/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.dragdrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsTable;

public class ObjectDropTarget implements DropTargetListener {

    private List<File> filesToUpload = new ArrayList<File>();
    private final ObjectsTable bucketObjectTable;
    private final TableViewer viewer;
    private final String bucketName;

    public ObjectDropTarget(ObjectsTable table, TableViewer viewer) {
        this.viewer = viewer;
        this.bucketName = table.getBucketName();
        this.bucketObjectTable = table;

        DropTarget dropTarget =  new DropTarget(viewer.getTable(), DND.DROP_COPY | DND.DROP_DEFAULT | DND.DROP_MOVE);
        dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance() });
        dropTarget.addDropListener(this);
    }

    @Override
    public void drop(DropTargetEvent event) {
        // Upload files from IDE
        if (LocalSelectionTransfer.getTransfer().getSelection() != null) {
            IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().nativeToJava(
                    event.currentDataType);
            for (Object obj : selection.toList() ) {
                IResource resource = (IResource) obj;
                File ideFile = resource.getLocation().toFile();
                if(ideFile.isFile()) {
                    filesToUpload.add(ideFile);
                }
            }
        }
        // Upload files from Desktop
        else if (FileTransfer.getInstance()!= null) {
            String[] files = (String[]) FileTransfer.getInstance().nativeToJava(event.currentDataType);
            for(String fileName : files) {
                File file = new File(fileName);
                if (file.isFile()) {
                    filesToUpload.add(file);
                }
            }
        }
        if (filesToUpload.isEmpty()) {
            return;
        }

        new Job("Upload Object") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    int uploadedFilesNumber = 0;
                    int filesNumber = filesToUpload.size();
                    monitor.beginTask("Uploading", filesNumber);
                    for(File file : filesToUpload) {
                        try {
                            ObjStorageClient.getInstance().uploadObject(bucketName, file);
                            monitor.worked(uploadedFilesNumber++);

                        } catch (Exception e) {
                            ErrorHandler.logErrorStack(e.getMessage(), e);
                        }
                    }
                    monitor.done();

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            bucketObjectTable.refresh(true);
                        }
                    });

                    return Status.OK_STATUS;
                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to upload object: " + e.getMessage(), e);
                }
            }
        }.schedule();
    }

    @Override
    public void dragEnter(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dragLeave(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dragOperationChanged(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dragOver(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dropAccept(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }
}
