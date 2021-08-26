package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.bmc.objectstorage.requests.ListBucketsRequest;
import com.oracle.bmc.objectstorage.responses.ListBucketsResponse;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

public class GetBuckets implements IRunnableWithProgress {
	
	private ObjectStorage objectStorageClient = ObjStorageClient.getInstance().getObjectStorageClient();
	private String compartmentId;
	private String page=null;
	public List<BucketSummary> bucketSummaryList = new ArrayList<BucketSummary>();
	public ListBucketsResponse listBucketsResponse;
	private String errorMessage=null;

	   public GetBuckets(String givenCompartmentId,String page)
	    {
	        this.compartmentId=givenCompartmentId;
	        this.page=page;
	    }
	   
	    @Override
	    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException{
	    	try {
	        monitor.beginTask("Getting Buckets", IProgressMonitor.UNKNOWN);
	        bucketSummaryList = new ArrayList<BucketSummary>();	   		
	        String namespace = ObjStorageClient.getInstance().getNamespace(objectStorageClient) ;
	        if (namespace != null) {
	        	ListBucketsRequest.Builder listBucketsBuilder =  ListBucketsRequest.builder()
		        		 .compartmentId(compartmentId)
		        		 .limit(20)
		                 .namespaceName(namespace)
		        		 .page(page);
		             try {
		            	 listBucketsResponse =
		            			 objectStorageClient.listBuckets(listBucketsBuilder.build());
		            	 bucketSummaryList.addAll(listBucketsResponse.getItems());
		             } catch(Exception e) {            	 
		            	 MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to get list buckets: ",e.getMessage());
		             }
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
