package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.ApplicationTable;

public class DeleteApplicationAction extends BaseAction{
	
	   	private final ApplicationTable table;
	    private final List<ApplicationSummary> applicationSelectionList;
	    private String applicationID;
	    private String title = "Delete Application";
	    
		public DeleteApplicationAction (ApplicationTable table){
	        this.table = table;
	        applicationSelectionList = (List<ApplicationSummary>)table.getSelectedObjects();
	    }

	    @Override
	    public String getText() {
	        if ( applicationSelectionList.size() == 1 ) {
	            return title;
	        }
	        return "";
	    }
	    
	    @Override
	 	protected void runAction() {
	    	ApplicationSummary application;
	    	if (applicationSelectionList.size() > 0) {
	    		application = applicationSelectionList.get(0);
	        	applicationID = application.getId();
	        	final String title = "Delete Dataflow Application";
		    	final String message = "Are you sure you want to delete Application: " + application.getDisplayName();
		    	
		    	Dialog dialog =  new MessageDialog(Display.getDefault().getActiveShell(), title, null, message, MessageDialog.QUESTION, new String[] {"Yes","No"}, 1);
		        	if (dialog.open() != Dialog.OK) {
		        		return;
		        	}
		        	new Job("Deleting Application") {
		            @Override
		            protected IStatus run(IProgressMonitor monitor) {
		                try {
		                    DataflowClient.getInstance().deleteApplication(application.getId());
		                    table.refresh(true);
		                    return Status.OK_STATUS;
		                } catch (Exception e) {
		                	MessageDialog.openError(table.getShell(), "Failed to Delete Application ", e.getMessage());
		                	return Status.CANCEL_STATUS;
		                }
		            }
		        }.schedule();     	
	    	}

	}
}
