package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;

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

import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddEditPrivateEndpointPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddPrivateEndpointPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.ScheduleCreatePrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;


public class CreatePrivateEndpointWizard extends Wizard implements INewWizard {
    private CreatePrivateEndpointWizardPage page;
    private NsgPage page2;
    private TagsPage page3;
    private ISelection selection;
	private PrivateEndpointTable pepTable;

    public CreatePrivateEndpointWizard(PrivateEndpointTable pepTable) {
        super();
        setNeedsProgressMonitor(true);
		this.pepTable=pepTable;
    }

    @Override
    public void addPages() {
    	try {
         	IRunnableWithProgress op = new AddPrivateEndpointPagesAction(this);
             new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
             String errorMessage=((AddPrivateEndpointPagesAction)op).getErrorMessage();
             if(errorMessage!=null)
             	throw new Exception(errorMessage);
         } catch (Exception e) {
         	MessageDialog.openError(getShell(), "Unable to add pages to Re-run wizard", e.getMessage());
         }
    	getShell().setSize(1000, 800);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
    	
        try {
        	Object[] obj=page.getDetails();
        	ArrayList<String> nsgl=page2.getnsgs();
        	
        	Object[] validObjects=new Object[] {obj[3],obj[4],obj[8],nsgl};
        	String[] objType=new String[] {"name","dnszones","subnetid","nsgl"};
        	String message=Validations.check(validObjects, objType);
        	if(!message.isEmpty()) {
        		open("Improper Entries",message);
        		return false;
        	}
        	
        	IRunnableWithProgress op = new ScheduleCreatePrivateEndpointAction(obj,page3.getOT(),page3.getFT(),page2.getnsgs(),pepTable.compid);
            new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
            
            String errorMessage=((ScheduleCreatePrivateEndpointAction)op).getErrorMessage();
        	if(errorMessage!=null)
        		throw new Exception(errorMessage);
            
        MessageDialog.openInformation(getShell(),"Succesful","Private Endpoint created successfully.");
        }
        catch (Exception e) {
        	MessageDialog.openError(getShell(), "Error while creating private endpoint ", e.getMessage());
        	return false;
        }
        
		pepTable.refresh(true);
		
        return true;
    }
    
    void open(String h,String m) {
    	MessageDialog.openInformation(getShell(), h, m);
    }
    
    public void addPagesWithProgress(IProgressMonitor monitor) {
    	monitor.subTask("Adding Main page");
    	page = new CreatePrivateEndpointWizardPage(selection,pepTable.compid);
        addPage(page);
        monitor.subTask("Adding Network Security Group(NSG) page");
        page2=new NsgPage(selection,"");
        addPage(page2);
        monitor.subTask("Adding Tags page");
        page3=new TagsPage(selection,pepTable.compid,null,null);
        addPage(page3);
    }
    
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}