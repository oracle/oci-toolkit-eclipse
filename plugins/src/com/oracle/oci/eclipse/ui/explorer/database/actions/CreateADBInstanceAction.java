/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.DbWorkload;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.database.CreateADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.editor.ADBInstanceTable;

public class CreateADBInstanceAction extends BaseAction {
	DbWorkload workloadType;
	ADBInstanceTable table;
    public CreateADBInstanceAction(String text, DbWorkload workloadType) {
        setText(text);
        this.workloadType = workloadType;
    }
    
    public CreateADBInstanceAction(String text, DbWorkload workloadType, ADBInstanceTable table) {
        this(text, workloadType);
        this.table = table;
    }

    @Override
    protected void runAction() {
        CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new CreateADBWizard(workloadType));
        dialog.setFinishButtonText("Create");
        if (Window.OK == dialog.open()) {
        }
        if(table != null) {
        	Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                	table.refresh(true);
                }
            });
        }
    }
}
