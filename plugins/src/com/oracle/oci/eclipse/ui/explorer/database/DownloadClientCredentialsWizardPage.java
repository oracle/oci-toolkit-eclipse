/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;

public class DownloadClientCredentialsWizardPage extends WizardPage {
    private ISelection selection;
    AutonomousDatabaseSummary instance;
    Map<String, AutonomousDatabaseWallet> walletTypeMap;
    private Combo walletTypeList;
    private boolean isDedicatedInstance;

	public DownloadClientCredentialsWizardPage(ISelection selection, AutonomousDatabaseSummary instance,
			Map<String, AutonomousDatabaseWallet> walletTypeMap) {
        super("wizardPage");
        setTitle("Download Client Credentials (Wallet)");
        String description = "You will need the client credentials and connection information to connect to your database." + "\n"
                             +"The client credentials include the wallet, which is required for all types of connections.";
        setDescription(description);
        this.selection = selection;
        this.instance = instance;
        this.walletTypeMap = walletTypeMap;
        this.isDedicatedInstance = (instance.getIsDedicated() != null && instance.getIsDedicated());
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.verticalSpacing = 9;
        
        final String message = "To download your client credentials, select the type of wallet, then click Download Wallet." + "\n"
                               +"You will be asked to create a password for the wallet.";
        Label label = new Label(container, SWT.NULL);
        label.setText(message);
        
        if(!isDedicatedInstance) {
        	Label walletTypeLabel = new Label(container, SWT.NULL);
    		walletTypeLabel.setText("&WALLET TYPE:");
    		walletTypeList = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
    		GridData gd0 = new GridData(GridData.FILL_HORIZONTAL);
    		walletTypeList.setLayoutData(gd0);
    		walletTypeList.add(ADBConstants.INSTANCE_WALLET);
    		walletTypeList.add(ADBConstants.REGIONAL_WALLET);
    		walletTypeList.select(0);
        }
		
		Composite innerTopContainer = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 4;
        innerTopLayout.verticalSpacing = 9;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Button downloadButton = new Button(innerTopContainer, SWT.PUSH);
		downloadButton.setText("Download Wallet");
		
		if(!isDedicatedInstance) {
			final Button rotateButton = new Button(innerTopContainer, SWT.PUSH);
			rotateButton.setText("Rotate Wallet");
			
			final AutonomousDatabaseWallet wallet = walletTypeMap.get(walletTypeList.getText());
			final String rotationTimeStr = (wallet != null && wallet.getTimeRotated() != null) ? wallet.getTimeRotated().toGMTString() : "";
			
			final Label rotatedTimeLabel = new Label(container, SWT.NULL);
			rotatedTimeLabel.setText("Wallet Last Rotated : " + rotationTimeStr);
			
			final AutonomousDatabaseWallet instanceWallet = walletTypeMap.get(ADBConstants.INSTANCE_WALLET);
			final AutonomousDatabaseWallet regionalWallet = walletTypeMap.get(ADBConstants.REGIONAL_WALLET);
			
			if ((instanceWallet != null && instanceWallet.getLifecycleState().equals(AutonomousDatabaseWallet.LifecycleState.Updating))
					|| (regionalWallet != null && regionalWallet.getLifecycleState().equals(AutonomousDatabaseWallet.LifecycleState.Updating))) {
				// disable download and rotate buttons when wallet rotation is in progress and display rotation message
				downloadButton.setEnabled(false);
				rotateButton.setEnabled(false);
				
				final String rotationInProgressMsg = 
						"The wallet rotation process takes a few minutes. During the wallet rotation, a new wallet is generated."
						+ "\n" + "You cannot perform a wallet download during the rotation process. Existing connections to database"
						+ "\n" + "will be terminated, and will need to be reestablished using the new wallet.";
				
			    Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		        group.setText("Rotation in Progress");
		        group.setLayoutData(new GridData(GridData.FILL_BOTH));
		        group.setLayout(new GridLayout(1, true));
		        
		        Composite innerContainer = new Composite(group, SWT.NONE);
		        GridLayout innerLayout = new GridLayout();
		        innerContainer.setLayout(innerLayout);
		        innerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						
				final Label rotatationInProgressLabel = new Label(innerContainer, SWT.NULL);
				rotatationInProgressLabel.setText(rotationInProgressMsg);
			}
			
			walletTypeList.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					final AutonomousDatabaseWallet wallet = walletTypeMap.get(walletTypeList.getText());
					final String rotationTimeStr = (wallet != null && wallet.getTimeRotated() != null) ? wallet.getTimeRotated().toGMTString() : "";
					rotatedTimeLabel.setText("Wallet Last Rotated : " + rotationTimeStr);
					container.layout();
				};
			});
			
			rotateButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					final String walletType = walletTypeList.getText();
					final AutonomousDatabaseSummary adbInstance = instance;
					((WizardDialog) getWizard().getContainer()).close(); 
					// open a new wizard to rotate wallet
					ADBInstanceClient.getInstance().rotateWallet(adbInstance, walletType);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
				}
			});
		}
		
		downloadButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				final String walletType;
				if (isDedicatedInstance)
					walletType = null;
				else
					walletType = walletTypeList.getText();
				final AutonomousDatabaseSummary adbInstance = instance;
				((WizardDialog) getWizard().getContainer()).close(); 
				// open a new wizard to download wallet
				ADBInstanceClient.getInstance().downloadWallet(adbInstance, walletType);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
		
        setControl(innerTopContainer);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
    
}
