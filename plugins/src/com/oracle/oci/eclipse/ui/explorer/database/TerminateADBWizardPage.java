package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;

public class TerminateADBWizardPage  extends WizardPage {

    
    private ISelection selection;
    AutonomousDatabaseSummary instance;
    private Text databaseNameText;

    public TerminateADBWizardPage(ISelection selection, AutonomousDatabaseSummary instance) {
        super("wizardPage");
        setTitle("Terminate Autonomous Database");
        setDescription("");
        this.selection = selection;
        this.instance = instance;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;
        layout.verticalSpacing = 9;
        
        final String terminationMsg = "Are you sure you want to terminate Autonomous Database "+instance.getDbName()+"?"
        		+"\nTerminating the Autonomous Database "+instance.getDbName()+" permanently deletes it."
        		+"\nYou cannot recover a terminated Autonomous Database.";
        Label terminationMsgLabel = new Label(container, SWT.NULL);
        terminationMsgLabel.setText(terminationMsg);
        
        Label licenseTypeLabel = new Label(container, SWT.NULL);
        licenseTypeLabel.setText("TYPE IN THE DATABASE NAME TO CONFIRM THE TERMINATION.");
        
        databaseNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        databaseNameText.setLayoutData(gd);
        
        
        setControl(container);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
    
	public String getDbName() {
		return databaseNameText.getText();
	}

}
