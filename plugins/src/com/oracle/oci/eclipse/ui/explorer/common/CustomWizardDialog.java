package com.oracle.oci.eclipse.ui.explorer.common;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class CustomWizardDialog extends WizardDialog {

	String finishButtonText;
	String cancelButtonText;
	boolean isVisibleFinishButton = true;

	public CustomWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	public void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		
		Button finishButton = getButton(IDialogConstants.FINISH_ID);
		if (finishButtonText != null) 
			finishButton.setText(finishButtonText);
		finishButton.setVisible(isVisibleFinishButton);
		
		Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
		if (cancelButtonText != null) 
			cancelButton.setText(cancelButtonText);
		
	}
	
	public void setFinishButtonText(String finishButtonText) {
		this.finishButtonText = finishButtonText;
	}
	
	public void setVisibleFinishButton(boolean isVisibleFinishButton) {
		this.isVisibleFinishButton =  isVisibleFinishButton;
		
	}
	
	public void setCancelButtonText(String cancelButtonText) {
		this.cancelButtonText = cancelButtonText;
	}
}
