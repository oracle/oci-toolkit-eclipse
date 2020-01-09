/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.ConfigFileReader.ConfigFile;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.account.PreferencesWrapper;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

/**
 * This is the configuration wizard.
 */

public class ConfigurationWizard extends Wizard implements INewWizard {
    private ConfigurationPage page;
    private ISelection selection;

    /**
     * Constructor for ConfigurationWizard.
     */
    public ConfigurationWizard() {
        super();
        setNeedsProgressMonitor(true);  // TODO: Needed?
    }

    /**
     * Adding the page to the wizard.
     */
    @Override
    public void addPages() {
        page = new ConfigurationPage(selection);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {

        final boolean saveProfileResult = page.getElements().saveProfile(getShell());

        if (!saveProfileResult)
            return saveProfileResult;

        final String profileName = page.getElements().getProfileName();
        final String configFileName = page.getElements().getConfigFileName();

        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    doFinish(profileName, configFileName, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            ErrorHandler.reportAndShowException(realException.getMessage(), realException);
            closeExplorerView();
            return false;
        } catch (Exception e) {
            ErrorHandler.reportAndShowException(e.getMessage(), e);
            return false;
        }
        // If connection is successful, open the Explorer
        openExplorerView();
        return true;
    }

    /**
     * Background thread to establish connection to OCI
     */

    private void doFinish(
            String profileName,
            String configFileName,
            IProgressMonitor monitor)
                    throws CoreException {


        PreferencesWrapper.setConfigFileName(configFileName);
        PreferencesWrapper.setProfile(profileName);
        ConfigFile config = null;
        try {
            config = ConfigFileReader.parse(configFileName, profileName);
        } catch (IOException e1) {
            ErrorHandler.logError("ConfigFileReader parse error:" + e1.getMessage());
        }

        PreferencesWrapper.setRegion(config.get("region"));
        AuthProvider.getInstance().setCompartmentId(config.get("tenancy"));

        ObjStorageClient.getInstance().getNamespace();
        ClientUpdateManager.getInstance().refreshClients();

        monitor.worked(1);
    }

    public void openExplorerView() {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        try {
            activeWindow.getActivePage().showView(Activator.PLUGIN_EXPLORER_ID);
        } catch (PartInitException e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }
    public void closeExplorerView() {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        try {
            IViewPart explorerViewPart = activeWindow.getActivePage().findView(Activator.PLUGIN_EXPLORER_ID);
            activeWindow.getActivePage().hideView(explorerViewPart);
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
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