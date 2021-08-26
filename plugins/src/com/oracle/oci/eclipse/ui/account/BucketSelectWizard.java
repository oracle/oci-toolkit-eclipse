package com.oracle.oci.eclipse.ui.account;

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;

public class BucketSelectWizard extends Wizard implements INewWizard  {
	
    private BucketSelectWizardPage page;
    private ISelection selection;
    private Consumer<String> objectUri;
    private  String compartmentId;
    private ApplicationLanguage language;
    
	public BucketSelectWizard(Consumer<String> objectUri,String CompartmentId,ApplicationLanguage language) {
		super();
		setNeedsProgressMonitor(true);
		this.objectUri = objectUri;
		this.language = language;
		this.compartmentId = CompartmentId;

	}

    @Override
    public void addPages() {
        page = new BucketSelectWizardPage(selection,compartmentId,language);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
    	String FileUri = page.getObjectSelected();
    	objectUri.accept(FileUri);
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
