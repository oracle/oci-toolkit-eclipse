/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;


import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsTable;

public class DownloadObjectAction extends BaseAction {

    private final ObjectsTable bucketObjectTable;
    private final List<ObjectSummary> objectSelectionList;
    private final String bucketName;
    // Dialog text
    String message = "File already exists. Do you want to replace it?";
    String title = "Confirm File Replace";

    public DownloadObjectAction (ObjectsTable table){
        bucketObjectTable = table;
        objectSelectionList = (List<ObjectSummary>) bucketObjectTable.getSelectedObjects();
        bucketName = bucketObjectTable.getBucketName();
    }

    @Override
    public String getText() {
        if ( objectSelectionList.size() > 1 ) {
            return "Download Objects";
        } else {
            return "Download Object";
        }
    }
    String downloadPath = null;
    String fileName = null;
    @Override
    protected void runAction() {
        try {
            // For multiple file download, the dialog allows to select the save folder.
            // It will overwrite existing files with similar names
            if ( objectSelectionList.size() > 1 ) {
                DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
                downloadPath = dialog.open();
            }
            // For single file download, the dialog can change the download fileName.
            else if ( objectSelectionList.size() == 1 ){
                ObjectSummary object = objectSelectionList.get(0);
                FileDialog dialog = new FileDialog ( Display.getDefault().getActiveShell(), SWT.SAVE );
                dialog.setFileName(object.getName());
                String result = dialog.open();
                if (result == null) {
                    return;
                }
                File file = new File(result);
                if (file.exists()) {
                    Dialog confirmDialog =  new MessageDialog(Display.getDefault().getActiveShell(), title , null, message , MessageDialog.WARNING, new String[] {"Yes","No"}, 1);
                    if (confirmDialog.open() != Dialog.OK) {
                        return;
                    }
                }
                downloadPath = file.getParent();
                fileName = file.getName();
            }
        } catch(Exception e) {
            ErrorHandler.reportException(e.getMessage(), e);
        }
        // Canceled
        if (downloadPath == null) {
            return;
        }

        new Job("Download Object") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                for ( ObjectSummary object : objectSelectionList ) {
                    try {
                        String fullFilePath = downloadPath + File.separator + object.getName();
                        // For single file the user can change the download fileName.
                        if (fileName != null && !fileName.equals(object.getName())) {
                            fullFilePath = downloadPath + File.separator + fileName;
                            fileName = null;
                        }

                        ObjStorageClient.getInstance().downloadObject(bucketName, object.getName(), fullFilePath);
                    } catch (Exception e) {
                        return ErrorHandler.reportException("Unable to download object: " + object.getName() + ". "+ e.getMessage(), e);
                    }
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }
}
