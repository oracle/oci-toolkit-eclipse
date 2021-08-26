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

import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddEditPrivateEndpointPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.ScheduleEditPrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;


public class EditPrivateEndpointWizard extends Wizard implements INewWizard {
    private EditPrivateEndpointPage page;
    private TagsPage page2;
    private ISelection selection;
	private PrivateEndpointSummary pepSum;
	private PrivateEndpointTable pepTable;

    public EditPrivateEndpointWizard(PrivateEndpointSummary pepSum,PrivateEndpointTable pepTable) {
        super();
        setNeedsProgressMonitor(true);
		this.pepSum=pepSum;
		this.pepTable=pepTable;
    }
    
    @Override
    public void addPages() {
    	
    	try {
         	IRunnableWithProgress op = new AddEditPrivateEndpointPagesAction(this);
             new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
            String errorMessage=((AddEditPrivateEndpointPagesAction)op).getErrorMessage();
            if(errorMessage!=null)
            	throw new Exception(errorMessage);
         } catch (Exception e) {
         	MessageDialog.openError(getShell(), "Unable to add pages to Edit Private Endpoint wizard", e.getMessage());
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
        	
        	Object[] validObjects=new Object[] {page.getName(),page.getDNS()};
        	String[] objType=new String[] {"name","dnszones"};
        	String message=Validations.check(validObjects, objType);
        	if(!message.isEmpty()) {
        		open("Validation errors",message);
        		return false;
        	}
        
        	IRunnableWithProgress op = new ScheduleEditPrivateEndpointAction(page2.getOT(),page2.getFT(),page.getDNS(),page.getName(),pepSum.getId());
            new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
            
            String errorMessage=((ScheduleEditPrivateEndpointAction)op).getErrorMessage();
            if(errorMessage!=null)
            	throw new Exception(errorMessage);
            
		pepTable.refresh(true);
        }
        catch (Exception e) {
        	MessageDialog.openError(getShell(), "Error while creating private endpoint", e.getMessage());
        	return false;
        }
        return true;
    }

    void open(String h,String m) {
    	MessageDialog.openInformation(getShell(), h, m);
    }
    
    public void addPagesWithProgress(IProgressMonitor monitor) {
    	monitor.subTask("Adding Main page");
    	page=new EditPrivateEndpointPage(selection,pepSum);
        addPage(page);
        monitor.subTask("Adding Tags page");
        page2=new TagsPage(selection,pepSum.getCompartmentId(),pepSum.getDefinedTags(),pepSum.getFreeformTags());
        addPage(page2);
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
    
	 public TagsPage getTagsPage() {
		 return page2;
	 }
}