package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;


import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class CreatePrivateEndpointsPoliciesPage extends WizardPage{
	
	public CreatePrivateEndpointsPoliciesPage(ISelection selection, String COMPARTMENT_ID) {
		super("wizardPage");
		setTitle("Identity: Private Endpoint policies setup");
		setDescription("This page creaes the required IAM policies to allow use of the virtual-network-family, access to more specific resources, access to specific operations, and changing of the network configuration");
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
		policy1.setText("allow group dataflow-admin to use virtual-network-family in tenancy");
		policy1.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		
		Composite policyContainer2 = new Composite(container, SWT.NONE);
        GridLayout policyLayout2 = new GridLayout();
        policyLayout2.numColumns = 1;
        policyContainer2.setLayout(policyLayout2);
        policyContainer2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		Label policy2 = new Label(policyContainer2, SWT.NULL);
		policy2.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy2.setText("allow group dataflow-admin to manage vnics in tenancy");
		policy2.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		Label policy3 = new Label(policyContainer2, SWT.NULL);
		policy3.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy3.setText("allow group dataflow-admin to use subnets in tenancy");
		policy3.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		Label policy4 = new Label(policyContainer2, SWT.NULL);
		policy4.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy4.setText("allow group dataflow-admin to use network-security-groups in tenancy");
		policy4.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		
		Composite policyContainer3 = new Composite(container, SWT.NONE);
        GridLayout policyLayout3 = new GridLayout();
        policyLayout3.numColumns = 1;
        policyContainer3.setLayout(policyLayout3);
        policyContainer3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		Label policy5 = new Label(policyContainer3, SWT.NULL);
		policy5.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy5.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy5.setText("allow group dataflow-admin to manage virtual-network-family in tenancy\n"
				+ "   where any {request.operation='CreatePrivateEndpoint',\n"
				+ "              request.operation='UpdatePrivateEndpoint',\n"
				+ "              request.operation='DeletePrivateEndpoint'\n"
				+ "              }");
		policy5.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		
		Composite policyContainer4 = new Composite(container, SWT.NONE);
        GridLayout policyLayout4 = new GridLayout();
        policyLayout4.numColumns = 1;
        policyContainer4.setLayout(policyLayout4);
        policyContainer4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		Label policy6 = new Label(policyContainer4, SWT.NULL);
		policy6.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy6.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy6.setText("allow group dataflow-admin to manage dataflow-private-endpoint in tenancy");
		policy6.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
        
        setControl(container);  
        setPageComplete(true);
	}
}
