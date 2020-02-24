/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;

public class RestoreADBWizard extends Wizard implements INewWizard {

    private RestoreADBWizardPage page;
    private ISelection selection;
    private AutonomousDatabaseSummary instance;
    private List<AutonomousDatabaseBackupSummary> backupList;

    public RestoreADBWizard(final AutonomousDatabaseSummary instance, List<AutonomousDatabaseBackupSummary> backupList) {
        super();
        setNeedsProgressMonitor(true);
        this.instance = instance;
        this.backupList = backupList;
    }

    @Override
    public void addPages() {
        page = new RestoreADBWizardPage(selection, instance, backupList);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        final Date restoreTimeStamp=page.getRestoreTimeStamp();
        
        if (restoreTimeStamp == null) {
			MessageDialog.openError(getShell(), "Restore Autonomous Database error",
					"Please select a backup from list and then restore the database");
			return false;
		}
        
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ADBInstanceClient.getInstance().restore(instance.getId(), restoreTimeStamp);
				monitor.done();
			}
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to restore ADB instance : "+instance.getDbName(), realException.getMessage());
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

}
