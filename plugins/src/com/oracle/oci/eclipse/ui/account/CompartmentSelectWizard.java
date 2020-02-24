/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.identity.model.Compartment;

public class CompartmentSelectWizard  extends Wizard implements INewWizard {

    private CompartmentSelectWizardPage page;
    private ISelection selection;
    private Consumer<Compartment> selectedCompartment;
    private boolean showCurrentCompartment;
	
	public CompartmentSelectWizard(Consumer<Compartment> selectedCompartment, boolean showCurrentCompartment) {
		super();
		setNeedsProgressMonitor(true);
		this.selectedCompartment = selectedCompartment;
		this.showCurrentCompartment = showCurrentCompartment;
	}

    @Override
    public void addPages() {
        page = new CompartmentSelectWizardPage(selection, showCurrentCompartment);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
    	Compartment compartment = page.getSelectedCompartment();
    	selectedCompartment.accept(compartment);
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
