/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.NavigatorDoubleClick;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.ObjStorageContentProvider;

public class DeleteBucketAction extends BaseAction {
    private String bucketName;

    public DeleteBucketAction(String bucketName) {
        setText("Delete Bucket");
        this.bucketName = bucketName;
    }

    @Override
    protected void runAction() {
        String title = "Delete Object Storage Bucket";
        String message = "Are you sure you want to delete Bucket: " + bucketName + " and its contents?";
        Dialog dialog =  new MessageDialog(Display.getDefault().getActiveShell(), title, null, message, MessageDialog.QUESTION, new String[] {"Yes","No"}, 1);
        if (dialog.open() != Dialog.OK) {
            return;
        }

        new Job("Deleting Bucket") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    ObjStorageClient.getInstance().deleteBucket(bucketName);
                    ObjStorageContentProvider.getInstance().getBucketsAndRefresh();
                    NavigatorDoubleClick.closeBucketWindow(bucketName);
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    return ErrorHandler.reportException("Error: Unable to delete bucket. " + e.getMessage(), e);
                }
            }
        }.schedule();
    }
}
