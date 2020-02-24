/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsTable;

public class DeleteObjectAction extends BaseAction {

    private final ObjectsTable bucketObjectTable;
    private final List<ObjectSummary> objectSelectionList;
    private final String bucketName;

    public DeleteObjectAction (ObjectsTable table){
        bucketObjectTable = table;
        objectSelectionList = (List<ObjectSummary>) bucketObjectTable.getSelectedObjects();
        bucketName = bucketObjectTable.getBucketName();
    }

    @Override
    public String getText() {
        if ( objectSelectionList.size() > 1 ) {
            return "Delete Objects";
        } else {
            return "Delete Object";
        }
    }

    @Override
    protected void runAction() {
        String title = "Delete Object from Bucket";
        String message = "";

        if ( objectSelectionList.size() == 1 ) {
            message = "Are you sure you want to delete the selected Object: " + objectSelectionList.get(0).getName()  + "?";
        } else if (objectSelectionList.size() > 1 ){
            message = "Are you sure you want to delete the selected Objects?";
        } else {
            return;
        }

        Dialog dialog =  new MessageDialog(Display.getDefault().getActiveShell(), title, null, message, MessageDialog.QUESTION, new String[] {"Yes","No"}, 1);
        if (dialog.open() != Dialog.OK) {
            return;
        }

        new Job("Deleting Object") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    for ( ObjectSummary object : objectSelectionList ) {
                        ObjStorageClient.getInstance().deleteObject(bucketName, object.getName());
                    }

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            bucketObjectTable.refresh(true);
                        }
                    });

                    return Status.OK_STATUS;
                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to delete object. " + e.getMessage(), e);
                }
            }
        }.schedule();
    }
}
