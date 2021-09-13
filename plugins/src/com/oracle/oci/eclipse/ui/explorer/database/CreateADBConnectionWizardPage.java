/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.PreferencesWrapper;

public class CreateADBConnectionWizardPage extends WizardPage {

	private Combo aliasList;
	private Text userText;
	private Text passwordText;
	private Text walletDirText;
	private AutonomousDatabaseSummary adbInstance;
    private Button autoConnectCheckBox;

	public CreateADBConnectionWizardPage(AutonomousDatabaseSummary adbInstance) {
		super("wizardPage");
		setTitle("Create Connection to Autonomous Database");
		setDescription("This wizard creates a new connection to Autonomous Database. Please enter the required details.");
		this.adbInstance = adbInstance;
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite topLevelContainer = new Composite(parent, SWT.NULL);
        GridLayout topLevelLayout = new GridLayout();
        topLevelContainer.setLayout(topLevelLayout);

        /* Top Section */

        Composite innerTopContainer = new Composite(topLevelContainer, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 2;
        innerTopLayout.verticalSpacing = 9;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label userLabel = new Label(innerTopContainer, SWT.NULL);
		userLabel.setText("&User:");
		userText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		userText.setLayoutData(gd1);
		userText.setText("ADMIN");

		Label adminPasswordLabel = new Label(innerTopContainer, SWT.NULL);
		adminPasswordLabel.setText("&Password:         ");
		passwordText = new Text(innerTopContainer, SWT.BORDER | SWT.PASSWORD);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		passwordText.setLayoutData(gd2);

        Composite innerContainer = new Composite(topLevelContainer, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 3;
        innerContainer.setLayout(innerLayout);
        innerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label label = new Label(innerContainer, SWT.NULL);
        label.setText("&Wallet location: ");

        walletDirText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        walletDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        //walletDirText.setEditable(true);
        
        
        walletDirText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                walletDirectoryChanged();
            }
        });
        
        Button walletDirButton = new Button(innerContainer, SWT.PUSH);
        walletDirButton.setText("Browse...");
        walletDirButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	final String walletDirPath = handleBrowse(innerContainer.getShell(), walletDirText.getText());
            	if(walletDirPath != null) {
            	    IStatus status = validateWalletDirectory(walletDirPath);
            	    updateStatus(status);
            		walletDirText.setText(walletDirPath);
            	}
            }
        });

        Composite innerBottomContainer = new Composite(topLevelContainer, SWT.NONE);
        GridLayout innerBottomLayout = new GridLayout();
        innerBottomLayout.numColumns = 2;
        innerBottomContainer.setLayout(innerBottomLayout);
        innerBottomContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label compartmentLabel = new Label(innerBottomContainer, SWT.NULL);
		compartmentLabel.setText("&Tnsnames alias: ");
		aliasList = new Combo(innerBottomContainer, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		aliasList.setLayoutData(gd3);

		Set<String> aliasSet = getTnsEntries(getWalletDirectory());
		if (aliasSet.size() > 0) {
			Iterator<String> it = aliasSet.iterator();
			while (it.hasNext()) {
				aliasList.add(it.next());
			}
			aliasList.select(0);
		}
		
		this.autoConnectCheckBox = new Button(innerBottomContainer, SWT.CHECK);
		autoConnectCheckBox.setSelection(true); // auto-connect by default
		GridData gdCheckBox = GridDataFactory.copyData(gd3);
		gdCheckBox.horizontalSpan = 2;
        this.autoConnectCheckBox.setLayoutData(gdCheckBox);
		this.autoConnectCheckBox.setText("Automatically connect after profile is created");
        
		final String configFilePath = PreferencesWrapper.getConfigFileName();
        if(configFilePath != null) {
        	final String configFileDirPath = configFilePath.substring(0, configFilePath.lastIndexOf(File.separator));
        	final String dbWalletDirPath = configFileDirPath + File.separator + "Wallet_" + adbInstance.getDbName();
        	if(new File(dbWalletDirPath).exists()) {
        		walletDirText.setText(dbWalletDirPath);
        	}
        }

		setControl(innerContainer);
		
	}
	
	private Set<String> getTnsEntries(String walletLocation) {
    	final Set<String> tnsEntries = new LinkedHashSet<>();
    	final String dbName = adbInstance.getDbName();
    	if(walletLocation == null || walletLocation.trim().equals(""))
    		return tnsEntries;
		try {
			File tnsnamesOraFile = getTnsOraFile(walletLocation);
            
			if (!tnsnamesOraFile.exists()) {
				return tnsEntries;
			}
			List<String> allLines = Files.readAllLines(tnsnamesOraFile.toPath());
			for (String line : allLines) {
				if(line != null && line.trim().startsWith(dbName.toLowerCase()+"_")) {
					int index = line.indexOf("=");
					if (index != -1) {
						String alias = line.substring(0, index).trim();
						tnsEntries.add(alias);
					}
				}
			}
		} catch (Exception e) {
			ErrorHandler.logErrorStack("Error occured while reading tnsnames.ora file for database: " + adbInstance.getDbName(), e);
		}
		return tnsEntries;
    }

    private File getTnsOraFile(String walletLocation) {
        String tnsnamesOraFileLoc = getTnsOraFileLoc(walletLocation);
        return new File(tnsnamesOraFileLoc);
    }

    private String getTnsOraFileLoc(String walletLocation) {
        return walletLocation + File.separator + "tnsnames.ora";
    }
	
	private String handleBrowse(Shell shell, String currentWalletDir) {
    	DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
    	if(currentWalletDir != null && (!currentWalletDir.equals("")))
    		dialog.setFilterPath(currentWalletDir);
        return dialog.open();
    }
	
	private void walletDirectoryChanged() {
	    String walletDirectory = getWalletDirectory();
        IStatus status = validateWalletDirectory(walletDirectory);
        
        if (status.isOK())
        {
            updateStatus((String)null);
            aliasList.setEnabled(true);
    		aliasList.clearSelection();
    		aliasList.removeAll();
    		Set<String> aliasSet = getTnsEntries(walletDirectory);
    		if (aliasSet.size() > 0) {
    			Iterator<String> it = aliasSet.iterator();
    			while (it.hasNext()) {
    				aliasList.add(it.next());
    			}
    			aliasList.select(0);
    		}
        }
        else
        {
            updateStatus(status.getMessage());
            aliasList.setEnabled(false);
        }
	}

    private void updateStatus(IStatus status) {
        updateStatus(status.getMessage());
    }

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getUserName() {
		return userText.getText();
	}

	public String getPassword() {
		return passwordText.getText();
	}

	public String getSelectedAlias() {
		return aliasList.getText();
	}
	
	public String getWalletDirectory() {
		return walletDirText.getText();
	}
	
	public boolean isAutoConnectProfile() {
	    return autoConnectCheckBox.getSelection();
	}

	private IStatus validateWalletDirectory(String walletDirStr)
	{
	    if (walletDirStr == null || walletDirStr.trim().isEmpty())
	    {
	        return new Status(IStatus.ERROR, getClass(), "Wallet Directory cannot be empty");
	    }
	    File walletDir = new File(walletDirStr);
	    if (!walletDir.isDirectory())
	    {
	        return new Status(IStatus.ERROR, getClass(), String.format("%s must be an accessible directory", walletDirStr));
	    }
	    File tnsOraFile = getTnsOraFile(walletDirStr);
	    if (!tnsOraFile.exists()) {
	        return new Status(IStatus.ERROR, getClass(), 
	                String.format("Can't find tnsnames.ora in wallet directory %s", walletDirStr));
	    }
	    return Status.OK_STATUS;
	}
}
