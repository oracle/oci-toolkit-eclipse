/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsTable;

public class UploadObjectAction extends BaseAction {

    private final ObjectsTable bucketObjectTable;
    private final String bucketName;
    private String[] filesList = null;

    public UploadObjectAction (ObjectsTable table){
        bucketObjectTable = table;
        bucketName = bucketObjectTable.getBucketName();
    }

    @Override
    public String getText() {
        return "Upload Objects";
    }

    @Override
    protected void runAction() {
        FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.MULTI);
        String result = dialog.open();
        filesList = dialog.getFileNames();
        String filePath = dialog.getFilterPath();

        if (result == null || filesList == null) {
            return;
        }

        new Job("Upload Object") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    for ( String fileName : filesList ) {
                        File f = new File(filePath + File.separatorChar + fileName);
                        ObjStorageClient.getInstance().uploadObject(bucketName, f);
                    }

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            bucketObjectTable.refresh(true);
                        }
                    });

                    return Status.OK_STATUS;
                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to upload object. " + e.getMessage(), e);
                }
            }
        }.schedule();
    }
}
