package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class CreatingDataflowUserPoliciesPageUsers extends WizardPage{

	public CreatingDataflowUserPoliciesPageUsers(ISelection selection, String COMPARTMENT_ID) {
		super("wizardPage");
		setTitle("Identity: Data Flow user policies setup");
		setDescription("This page creates the required IAM policies to allow Dataflow users to manage their own Applications and Runs");
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
		policy1.setText("ALLOW GROUP dataflow-users TO READ buckets IN tenancy");
		policy1.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		Label policy2 = new Label(policyContainer, SWT.NULL);
		policy2.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy2.setText("ALLOW GROUP dataflow-users TO USE dataflow-family IN tenancy");
		policy2.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		Label policy3 = new Label(policyContainer, SWT.NULL);
		policy3.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy3.setText("ALLOW GROUP dataflow-users TO MANAGE dataflow-family IN tenancy WHERE ANY \n"
				+ "{request.user.id = target.user.id, request.permission = 'DATAFLOW_APPLICATION_CREATE', \n"
				+ "request.permission = 'DATAFLOW_RUN_CREATE'}");
		policy3.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		Label policy4 = new Label(policyContainer, SWT.NULL);
		policy4.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy4.setText("ALLOW GROUP dataflow-users TO MANAGE objects IN tenancy WHERE ALL \n"
				+ "{target.bucket.name='dataflow-logs', any {request.permission='OBJECT_CREATE', \n"
				+ "request.permission='OBJECT_INSPECT'}}");
		policy4.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		Composite noteContainer = new Composite(container, SWT.NONE);
        GridLayout noteLayout = new GridLayout();
        noteLayout.numColumns = 1;
        noteContainer.setLayout(noteLayout);
        noteContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		Label note = new Label(noteContainer, SWT.NULL);
		note.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		note.setText("Note: Make sure dataflow-users user group already exists");
		note.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
        setControl(container);
        setPageComplete(true);
	}
}
