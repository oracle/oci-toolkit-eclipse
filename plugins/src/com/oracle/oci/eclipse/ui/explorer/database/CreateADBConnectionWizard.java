/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.ErrorHandler;

public class CreateADBConnectionWizard  extends Wizard implements INewWizard {

    private CreateADBConnectionWizardPage page;
    private AutonomousDatabaseSummary adbInstance;

	public CreateADBConnectionWizard(AutonomousDatabaseSummary adbInstance) {
		super();
		this.adbInstance = adbInstance;
		setNeedsProgressMonitor(true);
	}

    @Override
    public void addPages() {
        page = new CreateADBConnectionWizardPage(adbInstance);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
    	final String user = page.getUserName();
    	final String password = page.getPassword();
    	final String walletLocation = page.getWalletDirectory();
    	final String aliasName = page.getSelectedAlias();
    	final boolean autoConnect = page.isAutoConnectProfile();  // no need to validate
    	
    	if(!validateInput(user, password, walletLocation, aliasName)) {
    		return false;
    	}
    	
    	IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					ConfigureADBConnectionProfile.createConnectionProfile(monitor, adbInstance, user, password,walletLocation, aliasName, autoConnect);
				} catch (Exception e) {
					ErrorHandler.logErrorStack("Error occured while creating connection to database: " + adbInstance.getDbName(), e);
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
        };
        
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error occured while creating connection to database: "+adbInstance.getDbName(), realException.getMessage());
            return false;
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Error occured while creating connection to database: "+adbInstance.getDbName(), e.getMessage());
            return false;
        }

        return true;
    }
    
    private boolean validateInput(final String user, final String password, final String walletLocation, final String aliasName) {
    	if(user == null || user.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Database user required error",
					"Database user name cannot be empty");
			return false;
        }
    	
    	if(password == null || password.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Password required error",
					"User's password cannot be empty");
			return false;
        }
    	
    	if(walletLocation == null || walletLocation.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Wallet location required error",
					"Wallet location cannot be empty");
			return false;
        }
    	
    	if(aliasName == null || aliasName.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Alias name required error",
					"Unable to find matching tnsnames alias for the database, make sure wallet location is correct");
			return false;
        }
    	
    	return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // DO NOTHING
    }
}
