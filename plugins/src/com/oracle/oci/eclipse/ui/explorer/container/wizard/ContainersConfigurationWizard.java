/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.wizard;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersTable;


/**
 * This is the configuration wizard.
 */

public class ContainersConfigurationWizard extends Wizard implements INewWizard {
    private ContainersWizardIntroPage introPage;
    private ContainersWizardFirstPage firstPage;
    private ContainersWizardSecondPage secondPage;
    private ContainersWizardThirdPage thirdPage;
    private ContainersWizardFourthPage finalPage;
    private final ContainerClustersTable table;
    private ISelection selection;

    /**
     * Constructor for ConfigurationWizard.
     */
    public ContainersConfigurationWizard(ContainerClustersTable table) {
        super();
        setNeedsProgressMonitor(true);
        this.table = table;
    }

    /**
     * Adding the page to the wizard.
     */
    @Override
    public void addPages() {
        introPage = new ContainersWizardIntroPage(table);
        addPage(introPage);
        firstPage = new ContainersWizardFirstPage(table);
        addPage(firstPage);
        secondPage = new ContainersWizardSecondPage(table);
        addPage(secondPage);
        thirdPage = new ContainersWizardThirdPage(table);
        addPage(thirdPage);
        finalPage = new ContainersWizardFourthPage(thirdPage);
        addPage(finalPage);
    }


    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page.equals(firstPage)){

        }
        else if (page.equals(thirdPage)){
            thirdPage.getElements().fillDockerImageCombo();
        }
        else if (page.equals(finalPage)){
            boolean saveProfileResult = thirdPage.getElements().saveProfile(getShell());
            finalPage.getOutputLabel().setText("");
            finalPage.executeKubeDeployCommand();
        }
        //finalPage.updateText();
        return super.getNextPage(page);
    }
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {

        final boolean saveProfileResult = thirdPage.getElements().saveProfile(getShell());

        if (!saveProfileResult)
            return saveProfileResult;

        openExplorerView();
        return true;
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
    /*
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}