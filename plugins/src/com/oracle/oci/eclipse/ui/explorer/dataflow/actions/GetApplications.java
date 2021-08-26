package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.dataflow.responses.ListApplicationsResponse;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class GetApplications implements IRunnableWithProgress{

	private DataFlowClient dataflowClient = DataflowClient.getInstance().getDataFlowClient();
	private String compartmentId;
	private ListApplicationsRequest.SortBy sortBy;
	private ListApplicationsRequest.SortOrder sortOrder;
	private String page=null;
	public List<ApplicationSummary> applicationSummaryList = new ArrayList<ApplicationSummary>();
	public ListApplicationsResponse listApplicationsResponse;
	private String errorMessage=null;
	
	   public GetApplications(String givenCompartmentId,ListApplicationsRequest.SortBy sortBy,ListApplicationsRequest.SortOrder sortOrder,String page)
	    {
	        this.compartmentId=givenCompartmentId;
	        this.sortBy=sortBy;
	        this.sortOrder=sortOrder;
	        this.page=page;
	    }
	   
	    @Override
	    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException{
	    	
	    	try {
	        monitor.beginTask("Getting Applications", IProgressMonitor.UNKNOWN);
	   		applicationSummaryList = new ArrayList<ApplicationSummary>();	   			   		
	   		ListApplicationsRequest.Builder listApplicationsBuilder =  ListApplicationsRequest.builder()
	        		 .compartmentId(compartmentId)
	        		 .sortBy(sortBy)
	        		 .sortOrder(sortOrder)
	        		 .limit(20)
	        		 .page(page);
	             try {
	            	 listApplicationsResponse =
	                 dataflowClient.listApplications(listApplicationsBuilder.build());
	                 applicationSummaryList.addAll(listApplicationsResponse.getItems());
	             } catch(Exception e) {            	 
	            	 MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to get list applications: ",e.getMessage());
	             }
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
