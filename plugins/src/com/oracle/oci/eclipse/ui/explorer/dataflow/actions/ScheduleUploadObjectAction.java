package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

public class ScheduleUploadObjectAction implements IRunnableWithProgress { 
	private String bucketName;
	private File applicationFile;
	private String errorMessage=null;
	
	public ScheduleUploadObjectAction(String bucketName , File applicationFile){
	    	this.bucketName = bucketName;
	    	this.applicationFile = applicationFile;
	    }
	   @Override
	    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	    {
		   try {
	        // Tell the user what you are doing
	        monitor.beginTask("Uploading in Progress", IProgressMonitor.UNKNOWN);
	        // Do your work
	    
			ObjStorageClient.getInstance().uploadObject(bucketName, applicationFile);				
		
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
