/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.database.RegisterDriverWizard;

public class RegisterDriverAction extends BaseAction {

	public RegisterDriverAction(String text) {
		setText(text);
	}

	@Override
	protected void runAction() {
		CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new RegisterDriverWizard());
		dialog.setFinishButtonText("Register");
		if (Window.OK == dialog.open()) {
		}
	}

}
