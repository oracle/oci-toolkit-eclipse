package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DownloadADBWalletWizardPage extends WizardPage {

    private Text walletPasswordText;
    private Text confirmWalletPasswordText;
    private Text walletDirectoryPathText;

    public DownloadADBWalletWizardPage() {
    	super("wizardPage");
        setTitle("Download Wallet");
        setDescription("Provide a password for downloading the wallet and click Download button.\n"
        		+ "Note that this password is used only while using Java Key Store (JKS) to connect. Oracle Wallets use auto-login.");
    }

    @Override
    public void createControl(Composite parent) {

   	 Composite topLevelContainer = new Composite(parent, SWT.NULL);
        GridLayout topLevelLayout = new GridLayout();
        topLevelContainer.setLayout(topLevelLayout);

        /* Top Section */
        Group group = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        group.setText("Choose wallet download location");
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setLayout(new GridLayout(1, true));

        Composite innerTopContainer = new Composite(group, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 3;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(innerTopContainer, SWT.NULL);
        label.setText("&Wallet Location:     ");

        walletDirectoryPathText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        walletDirectoryPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        walletDirectoryPathText.setEditable(false);

        Button walletDirButton = new Button(innerTopContainer, SWT.PUSH);
        walletDirButton.setText("Browse...");
        walletDirButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
           	 walletDirectoryPathText.setText(handleBrowse(innerTopContainer.getShell()));
            }
        });
        
       group = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
       group.setText("Enter password");
       group.setLayoutData(new GridData(GridData.FILL_BOTH));
       group.setLayout(new GridLayout(1, true));

       Composite innerContainer = new Composite(group, SWT.NONE);
       GridLayout innerLayout = new GridLayout();
       innerLayout.numColumns = 2;
       innerTopLayout.verticalSpacing = 9;
       innerContainer.setLayout(innerLayout);
       innerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
       
       Label label1 = new Label(innerContainer, SWT.NULL);
       label1.setText("&Password:");
       walletPasswordText = new Text(innerContainer, SWT.BORDER | SWT.PASSWORD);
       GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
       walletPasswordText.setLayoutData(gd1);
       
       Label label2 = new Label(innerContainer, SWT.NULL);
       label2.setText("&Confirm Password:");
       confirmWalletPasswordText = new Text(innerContainer, SWT.BORDER | SWT.PASSWORD);
       GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
       confirmWalletPasswordText.setLayoutData(gd2);
       
       Label passwordRule = new Label(innerContainer, SWT.NULL);
       Label passwordRule1 = new Label(innerContainer, SWT.NULL);
       passwordRule1.setText(
				"Password must be 8 to 60 characters and contain at least 1 alphabetic and 1 numeric character.");
       passwordRule1.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

       setControl(innerContainer);
    }
    
    private String handleBrowse(Shell shell) {
    	DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
        return dialog.open();
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getPassword() {
        return walletPasswordText.getText();
    }
    
    public String getConfirmPassword() {
    	return confirmWalletPasswordText.getText();
    }
    
    public String getWalletDirectoryPath() {
    	return walletDirectoryPathText.getText();
    }

}
