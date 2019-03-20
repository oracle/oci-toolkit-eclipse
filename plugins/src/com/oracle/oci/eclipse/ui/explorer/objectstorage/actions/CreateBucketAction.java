/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.CreateBucketWizard;

public class CreateBucketAction extends BaseAction {
    public CreateBucketAction() {
        setText("Create New Bucket");
    }

    @Override
    protected void runAction() {
        WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), new CreateBucketWizard());
        if (Window.OK == dialog.open()) {
        }
    }
}
