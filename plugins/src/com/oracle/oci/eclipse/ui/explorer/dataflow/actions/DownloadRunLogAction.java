package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class DownloadRunLogAction implements IRunnableWithProgress{
	
	private String errorMessage=null;
	private List<String> name;
	private String fullFilePath,runId;
	
	 public DownloadRunLogAction(List<String> name,String fullFilePath,String runId)
	    {
	    		this.name=name;
	    		this.fullFilePath=fullFilePath;
	    		this.runId=runId;
	    }
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		
		try {
	        // Tell the user what you are doing
			int work=name.size();
	        monitor.beginTask("Downloading Logs...", work);

	        // Do your work
	        File f=new File(fullFilePath);
	        if(f.exists())
	        	f.delete();
	        byte[] buffer = new byte[1024];
	        String fileName=fullFilePath.substring(fullFilePath.lastIndexOf(File.separator));
	        if(fileName.length()>2&&fileName.endsWith(".gz"))
	        	fileName=fileName.substring(0,fileName.lastIndexOf(".gz"));
	        
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fullFilePath));
	        int i=1;
	        for(String n:name) {
	        	monitor.subTask("Downloading "+n+" log file("+(i++)+" of "+(work)+" files)...");
	    		InputStream in=DataflowClient.getInstance().getLogResponse(runId, n).getInputStream();
	    		ZipEntry e = new ZipEntry(fileName + "/" + n);
		        out.putNextEntry(e);
		        int len;
	            while ((len = in.read(buffer)) > 0) {
	                out.write(buffer, 0, len);
	            }
		        if(in!=null) in.close();
		        out.closeEntry();
		        monitor.worked(1);
		        if(monitor.isCanceled()){
	            	monitor.done();
	                return;
	            }
	    	}
	        out.close();
	        

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