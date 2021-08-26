package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.EditPrivateEndpointWizard;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;

public class EditPrivateEndpointAction extends BaseAction {
	
	private final PrivateEndpointTable pepTable;
	private final PrivateEndpointSummary pepSum;

    public EditPrivateEndpointAction (PrivateEndpointSummary pepSum,PrivateEndpointTable table){
        pepTable = table;
        this.pepSum=pepSum;
    }

    @Override
    public String getText() {
        return "Edit Private Endpoint";
    }

    @Override
    protected void runAction() {
    	WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), new EditPrivateEndpointWizard(pepSum,pepTable));
        if (Window.OK == dialog.open()) {
        }
    }
}
