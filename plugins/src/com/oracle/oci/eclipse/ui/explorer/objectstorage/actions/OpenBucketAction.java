/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.ui.explorer.NavigatorDoubleClick;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.EditorInput;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsEditor;

public class OpenBucketAction extends BaseAction {

    private String bucketName;

    public OpenBucketAction(BucketSummary bucket) {
        this.bucketName = bucket.getName();
        setText("Open Bucket");
    }

    @Override
    protected void runAction() {

        final IEditorInput input = new EditorInput(bucketName, ObjectsEditor.HEADER);

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    // Keep a mapping between open windows IDs and the buckets name
                    IEditorPart editorPartBucket = NavigatorDoubleClick.getBucketsEditorsMap().get(bucketName);
                    if(editorPartBucket == null ||
                            activeWindow.getActivePage().findEditor(editorPartBucket.getEditorInput()) == null) {

                        editorPartBucket = activeWindow.getActivePage().openEditor(input, ObjectsEditor.ID);
                        NavigatorDoubleClick.getBucketsEditorsMap().put(bucketName, editorPartBucket);
                    } else {
                        activeWindow.getActivePage().activate(editorPartBucket);
                    }
                } catch (Exception e) {
                    ErrorHandler.reportException(e.getMessage(), e);
                }
            }
        });
    }
}
