package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class CreateDataflowServicePolicyPage extends WizardPage{
	
	public CreateDataflowServicePolicyPage(ISelection selection) {
		super("wizardPage");
		setTitle("Identity: Data Flow service policy setup");
		setDescription("This page created the required IAM policies to allow Data Flow service to perform actions on behalf of the user or group on objects within the tenancy");
	}
	
	@Override
    public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        
        Composite messageContainer = new Composite(container, SWT.NONE);
        GridLayout messageLayout = new GridLayout();
        messageLayout.numColumns = 1;
        messageContainer.setLayout(messageLayout);
        messageContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label messageLabel = new Label(messageContainer, SWT.NULL);
		messageLabel.setText("&Below IAM policies will be created:"); 
		messageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		messageLabel.setLocation(100, 100);
        
		Composite policyContainer = new Composite(container, SWT.NONE);
        GridLayout policyLayout = new GridLayout();
        policyLayout.numColumns = 1;
        policyContainer.setLayout(policyLayout);
        policyContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		Label policy1 = new Label(policyContainer, SWT.NULL);
		policy1.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy1.setText("ALLOW SERVICE dataflow TO READ objects IN tenancy WHERE target.bucket.name='dataflow-logs'");
		policy1.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
        
        setControl(container);
        setPageComplete(true);
	}
}
