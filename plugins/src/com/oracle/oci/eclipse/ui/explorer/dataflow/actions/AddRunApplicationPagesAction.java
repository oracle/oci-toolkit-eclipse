package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.CreateRunWizard;

public class AddRunApplicationPagesAction implements IRunnableWithProgress {

	private CreateRunWizard  wizard;
	private String errorMessage=null;
	
    public AddRunApplicationPagesAction(CreateRunWizard wizard)
    {
    		this.wizard=wizard;
    }
    
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
    	try {
        // Tell the user what you are doing
        monitor.beginTask("Opening Run Application Wizard", IProgressMonitor.UNKNOWN);

        // Do your work
        wizard.addPagesWithProgress(monitor);

        // You are done
        monitor.done();
    	}
    	catch (Exception e) {
    		errorMessage=e.getMessage();
    	}
    }
    public String getErrorMessage() {
    	return errorMessage;
    }
}
