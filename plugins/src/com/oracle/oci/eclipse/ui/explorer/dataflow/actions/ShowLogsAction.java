package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.RunLogWizard;

public class ShowLogsAction extends BaseAction {
    
	private RunSummary runSum;
	
	public ShowLogsAction(RunSummary runSum) {
        setText("Show Logs");
		this.runSum=runSum;
    }

    @Override
    protected void runAction() {
    	CustomWizardDialog dialog;
        dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new RunLogWizard(runSum));
        dialog.setFinishButtonText("Download");
        dialog.setCancelButtonText("Exit");
        if (Window.OK == dialog.open()) {}
    }
}