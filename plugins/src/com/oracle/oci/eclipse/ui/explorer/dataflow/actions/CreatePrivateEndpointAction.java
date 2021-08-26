package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.CreatePrivateEndpointWizard;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;

public class CreatePrivateEndpointAction extends BaseAction {
    
	private PrivateEndpointTable pepTable;
	
	public CreatePrivateEndpointAction(PrivateEndpointTable pepTable) {
        setText("Create Private Endpoint");
		this.pepTable=pepTable;
    }

    @Override
    protected void runAction() {
    	WizardDialog dialog;
        dialog = new WizardDialog(Display.getDefault().getActiveShell(), new CreatePrivateEndpointWizard(pepTable));
        if (Window.OK == dialog.open()) {
        }
    }
}