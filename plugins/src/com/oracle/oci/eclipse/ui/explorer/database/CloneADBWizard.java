/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseCloneDetails;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;

public class CloneADBWizard extends Wizard implements INewWizard {

    private CloneADBWizardPage page;
    private ISelection selection;
    private AutonomousDatabaseSummary instance;

	public CloneADBWizard(final AutonomousDatabaseSummary instance) {
		super();
		this.instance = instance;
		setNeedsProgressMonitor(true);
	}

    @Override
    public void addPages() {
        page = new CloneADBWizardPage(selection, instance);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
    	
    	if(!isValidPassword())
    		return false;
    	
    	final String compartmentId = page.getSelectedCompartmentId();
    	final CreateAutonomousDatabaseDetails.DbWorkload workloadType;
		if (instance.getDbWorkload() == AutonomousDatabaseSummary.DbWorkload.Dw) {
			workloadType = CreateAutonomousDatabaseDetails.DbWorkload.Dw;
		} else {
			workloadType = CreateAutonomousDatabaseDetails.DbWorkload.Oltp;
		}
		
		final boolean isFreeTier = (instance.getIsFreeTier() != null) && instance.getIsFreeTier()
				&& page.isAlwaysFreeInstance();
        
    	CreateAutonomousDatabaseCloneDetails cloneRequest = 
    	CreateAutonomousDatabaseCloneDetails.builder()
        .compartmentId(compartmentId)
        .cpuCoreCount(Integer.valueOf(page.getCPUCoreCount()))
        .dataStorageSizeInTBs(Integer.valueOf(page.getStorageInTB()))
        .displayName(page.getDisplayName())
        .adminPassword(page.getAdminPassword())
        .dbName(page.getDatabaseName())
        .dbWorkload(workloadType)
        .isAutoScalingEnabled(page.isAutoScalingEnabled())
        .licenseModel(page.getLicenseModel())
        .sourceId(instance.getId())
        .cloneType(page.getCloneType())
        .isFreeTier(isFreeTier)
        .build();
    	
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                ADBInstanceClient.getInstance().createClone(cloneRequest);
                monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to clone ADB instance : "+instance.getDbName(), realException.getMessage());
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
        this.selection = selection;
    }
    
    private boolean isValidPassword() {
    	final String adminPassword = page.getAdminPassword();
        final String confirmAdminPassword = page.getConfirmAdminPassword();
        
        if(adminPassword == null || adminPassword.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Admin password required error",
					"Admin password cannot be empty");
			return false;
        	
        } else if (!adminPassword.equals(confirmAdminPassword)) {
			MessageDialog.openError(getShell(), "Admin password mismatch error",
					"Confirm Admin password must match Admin password");
			return false;
		}
		
		return true;
    }
}