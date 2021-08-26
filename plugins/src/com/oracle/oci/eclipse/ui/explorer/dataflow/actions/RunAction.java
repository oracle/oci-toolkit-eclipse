package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.RunWizard;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunTable;

public class RunAction extends BaseAction {
    
	private RunSummary runSum;
	private RunTable runTable;
	
	public RunAction(RunSummary runSum,RunTable runTable) {
        setText("Re-Run");
		this.runSum=runSum;
		this.runTable=runTable;
    }

    @Override
    protected void runAction() {
    	WizardDialog dialog;
        dialog = new WizardDialog(Display.getDefault().getActiveShell(), new RunWizard(runSum,runTable));
        if (Window.OK == dialog.open()) {}
    }
}