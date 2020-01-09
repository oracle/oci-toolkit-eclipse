package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;

public class ChangeAdminPasswordADBWizardPage extends WizardPage {

	private Text adminUserName;
    private Text adminPasswordText;
    private Text confirmAdminPasswordText;
    private ISelection selection;
    AutonomousDatabaseSummary instance;

    public ChangeAdminPasswordADBWizardPage(ISelection selection, AutonomousDatabaseSummary instance) {
        super("wizardPage");
        setTitle("ADB Admin Password Change");
        setDescription("Change the password for your Autonomous Database ADMIN user.");
        this.selection = selection;
        this.instance = instance;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
        
        Label label = new Label(container, SWT.NULL);
        label.setText("&USERNAME:");
        adminUserName = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        adminUserName.setLayoutData(gd);
        adminUserName.setText("ADMIN");
        adminUserName.setEditable(false);
        
        
        Label label1 = new Label(container, SWT.NULL);
        label1.setText("ADMIN PASSWORD:");
        adminPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
        GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
        adminPasswordText.setLayoutData(gd1);
        
        Label label2 = new Label(container, SWT.NULL);
        label2.setText("CONFIRM ADMIN PASSWORD:");
        confirmAdminPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
        GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
        confirmAdminPasswordText.setLayoutData(gd2);
        
        Label passwordRule = new Label(container, SWT.NULL);
        Label passwordRule1 = new Label(container, SWT.NULL);
        passwordRule1.setText(
				"Password must be 12 to 30 characters and contain at least one uppercase letter,\n"
				+ " one lowercase letter, and one number. The password cannot contain the double \n"
				+ "quote (\") character or the username \"admin\". It must be different than the \n"
				+ "last four passwords. You cannot reuse a password within 24 hours. ");
        passwordRule1.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

        setControl(container);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getAdminPassword() {
        return adminPasswordText.getText();
    }
    
    public String getConfirmAdminPassword() {
    	return confirmAdminPasswordText.getText();
    }

}
