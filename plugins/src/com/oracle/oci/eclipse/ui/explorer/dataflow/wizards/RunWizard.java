package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddRunPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.ScheduleRerunAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunTable;


public class RunWizard extends Wizard implements INewWizard {
    private RunWizardPage page;
    private TagsPage page2;
    private AdvancedOptionsPage page3;
    private ISelection selection;
	private RunSummary runSum;
	private ApplicationSummary appSum;
	private RunTable runTable;
	private Object obj;

    public RunWizard(RunSummary runSum,RunTable runTable) {
        super();
        setNeedsProgressMonitor(true);
		this.runSum=runSum;
		this.runTable=runTable;
		this.obj=runSum;
    }

    @Override
    public void addPages() {     
    	 try {
         	IRunnableWithProgress op = new AddRunPagesAction(this);
             new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
             String errorMessage=((AddRunPagesAction)op).getErrorMessage();
             if(errorMessage!=null)
             	throw new Exception(errorMessage);
         } catch (Exception e) {
         	MessageDialog.openError(getShell(), "Unable to add pages to Re-run wizard", e.getMessage());
         }
    	 getShell().setSize(1000, 800);
    }
    
    @Override
    public boolean performFinish() {
    	
        try {
        	Object[] obj;
        	obj=page.getDetails();
        	if(page3.ischecked()) {obj[11]=page3.loguri();obj[15]=page3.buckuri();}
        	
        	Object[] validObjects;
        	String[] objectType;
        	
        	if(page3.ischecked()) {
        		validObjects=new Object[] {obj[6],obj[11],obj[15],page3.getconfig().keySet()};
        		objectType=new String[] {"name","loguri","warehouseuri","sparkprop"+((String)obj[14]).charAt(0)};
        	}
        	else {
        		validObjects=new Object[] {obj[6]};
        		objectType=new String[] {"name"};
        	}
        	
        	String message=Validations.check(validObjects, objectType);
        	
        	if(!message.isEmpty()) {
        		open("Validation errors",message);
        		return false;
        	}

        	IRunnableWithProgress op = new ScheduleRerunAction(obj,page2.getOT(),page2.getFT(),page3.getconfig(),page3.ischecked());
            new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
        	
            String errorMessage=((ScheduleRerunAction)op).getErrorMessage();
            if(errorMessage!=null)
            	throw new Exception(errorMessage);
            
        	MessageDialog.openInformation(getShell(),"Re-Run Succesful","A re-run of application is scheduled.");
        	
        	runTable.setSortBy(ListRunsRequest.SortBy.TimeCreated);
        	runTable.setSortOrder(ListRunsRequest.SortOrder.Desc);
        	runTable.refresh(true);
        }
        catch (Exception e) {
        	MessageDialog.openError(getShell(), "Failed to Create Run ", e.getMessage());
        	return false;
        }
        return true;
    }
    
    void open(String h,String m) {
    	MessageDialog.openInformation(getShell(), h, m);
    }
    
    public void addPagesWithProgress(IProgressMonitor monitor) {
    	monitor.subTask("Adding Main page");
    	page = new RunWizardPage(selection,runSum);
    	monitor.subTask("Adding Tags Page");
        page2=new TagsPage(selection,runSum!=null?runSum.getCompartmentId():appSum.getCompartmentId(),runSum.getDefinedTags(),runSum.getFreeformTags());
        addPage(page);addPage(page2);
        monitor.subTask("Adding Advanced Options page");
        page3=new AdvancedOptionsPage(selection,obj,page);
        addPage(page3);
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