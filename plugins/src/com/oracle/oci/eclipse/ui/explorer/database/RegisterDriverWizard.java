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

public class RegisterDriverWizard extends Wizard implements INewWizard {

    private RegisterDriverWizardPage page;
    private ISelection selection;

    public RegisterDriverWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        page = new RegisterDriverWizardPage(selection);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        final String driverJarPath = page.getDriverJarName();
        
        if(!isValidJarPath(driverJarPath))
        	return false;
        
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ConfigureADBConnectionProfile.registerDriver(driverJarPath);
				monitor.done();
			}
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to Register JDBC Driver: ", realException.getMessage());
            return false;
        }

        return true;
    }
    
    private boolean isValidJarPath(final String driverJarPath) {
    	if(driverJarPath == null || driverJarPath.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Invalid JDBC Jar location",
					"JDBC jar file path cannot be empty");
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
