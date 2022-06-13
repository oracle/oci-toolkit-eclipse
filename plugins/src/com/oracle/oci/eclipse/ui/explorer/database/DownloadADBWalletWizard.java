/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.account.PreferencesWrapper;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;

public class DownloadADBWalletWizard  extends Wizard implements INewWizard {

    private DownloadADBWalletWizardPage page;
    private final AutonomousDatabaseSummary instance;
    private final String walletType;

    public DownloadADBWalletWizard(final AutonomousDatabaseSummary instance, final String walletType) {
        super();
        setNeedsProgressMonitor(true);
        this.instance = instance;
        this.walletType = walletType;
    }

    @Override
    public void addPages() {
        page = new DownloadADBWalletWizardPage();
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
    	
    	final String walletPassword = page.getPassword();
    	final String dirPath = page.getWalletDirectoryPath();
    	if(!isValidPassword())
    		return false;
    	
    	if(!isValidWalletDir(dirPath))
    		return false;
    	
    	final String walletDirectory = dirPath+File.separator+"Wallet_"+instance.getDbName();
    	
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ADBInstanceClient.getInstance().downloadWallet(instance, walletType, walletPassword, walletDirectory);
				String passwordKey = PreferencesWrapper.createSecurePreferenceKey(instance);
				Properties props = new Properties();
				props.put(DBUtils.TOOL_PROPERTIES_KEY_ADMIN_PASSWORD_SECURE_KEY_NAME, passwordKey);
				DBUtils.writeToToolProperties(new File(walletDirectory), props);
				monitor.done();
			}
        };
        
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
        	e.printStackTrace();
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to download " + walletType + " for database : "+instance.getDbName(), realException.getMessage());
            return false;
        }

        return true;
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // DO NOTHING
    }
    
    private boolean isValidPassword() {
    	final String password = page.getPassword();
        final String confirmPassword = page.getConfirmPassword();
        
        if(password == null || password.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Wallet password required error",
					"Wallet password cannot be empty");
			return false;
        	
        } else if (!password.equals(confirmPassword)) {
			MessageDialog.openError(getShell(), "Wallet password mismatch error",
					"Confirm wallet password must match wallet password");
			return false;
		}
		
		return true;
    }
    
    private boolean isValidWalletDir(final String dirPath) {
        if(dirPath == null || dirPath.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Wallet directory path required error",
					"Wallet directory path cannot be empty");
			return false;
        }
		return true;
    }

}
