package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetApplications;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetRuns;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.SetTagsAction;
import com.oracle.bmc.dataflow.model.PrivateEndpoint;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;


public class EditPrivateEndpointPage extends WizardPage {
	
    protected Text nameText,dnsText;
    private ISelection selection;
	private PrivateEndpoint pep;

    public EditPrivateEndpointPage(ISelection selection,PrivateEndpointSummary pepSum) {
        super("wizardPage");
        setTitle("Edit Private Endpoint");
        setDescription("This wizard edits an existing private endpoint. Please enter the following details.");
        this.selection = selection;
		try {
			this.pep=DataflowClient.getInstance().getPrivateEndpointDetails(pepSum.getId());
		} 
		catch (Exception e) {
			MessageDialog.openError(getShell(), "Error fetching Private Endpoint", e.getMessage());		
			}
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
		
        Label nameLabel = new Label(container, SWT.NULL);
        nameLabel.setText("&Name:");
        nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        nameText.setText(pep.getDisplayName());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        nameText.setLayoutData(gd);
		
		Label dnsLabel = new Label(container, SWT.NULL);
        dnsLabel.setText("&DNS zones to resolve:");
		dnsText = new Text(container, SWT.BORDER);
		dnsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dnsText.setText(String.join(",", pep.getDnsZones()));
		
        setControl(container);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String[] getDNS() {
        
		return dnsText.getText().split(",");
    }
	
	public String getName() {
        
		return nameText.getText();
    }
}