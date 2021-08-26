package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.DataTransferObject;

public class CreateFiles implements IRunnableWithProgress{
	private int workload;

    public CreateFiles(int workload)
    {
        this.workload = workload;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
        // Tell the user what you are doing
        monitor.beginTask("Creating Files", workload);

        // Do your work
        try {
        String tempDir=System.getProperty("java.io.tmpdir");
		File theDir = new File(tempDir+"\\dataflowtempdir");
		byte[] buffer = new byte[1024];
		File dff=File.createTempFile("dataflowtempdir\\dflib-",".zip",theDir);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dff));
		   
		   
		StringBuffer mcp=new StringBuffer("");
		FileInputStream in=null;
		
		int i=1;
		for (String classpathEntry : new HashSet<String>(DataTransferObject.jarList)) {
			monitor.subTask("Zipping file " + (i++) + " of "+ (workload-1) + "...");
		    if (classpathEntry.endsWith(".jar")) {
		    	File jar = new File(classpathEntry);
		        String p=jar.getAbsolutePath();
		        String n=p.substring(p.lastIndexOf('\\')+1),n0=p.substring(0,p.lastIndexOf('\\'));
		        mcp.append("java" + "/" + n+" ");
		        ZipEntry e = new ZipEntry("java" + "/" + n);
		        out.putNextEntry(e);
		        in = new FileInputStream(n0 + "/" + n);
	            int len;
	            while ((len = in.read(buffer)) > 0) {
	                out.write(buffer, 0, len);
	            }
		        if(in!=null) in.close();
		        out.closeEntry();
		    }
		    monitor.worked(1);
            if(monitor.isCanceled()){
            	monitor.done();
                return;
            }
		}
		out.close();
		DataTransferObject.archivedir=dff.getAbsolutePath();
		monitor.worked(1);
		monitor.done();
        }
        catch (Exception e) {
        	MessageDialog.openError(Display.getDefault().getActiveShell(), "Error generating archive zip", e.getMessage());
        }

        // You are done
        monitor.done();
    }
}
