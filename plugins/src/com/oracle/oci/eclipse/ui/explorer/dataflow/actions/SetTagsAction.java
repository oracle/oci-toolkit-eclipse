package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.TagsPage;

public class SetTagsAction implements IRunnableWithProgress{
	private TagsPage  tagsPage;
	private Map<String,Map<String,Object>> defMap;
    private Map<String,String> freeMap;
    private String errorMessage;
    
    public SetTagsAction(TagsPage tagsPage,Map<String,Map<String,Object>> defMap,Map<String,String> freeMap)
    {
    		this.tagsPage=tagsPage;
    		this.defMap=defMap;
            this.freeMap=freeMap;
    }
    
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
    	try {
        // Tell the user what you are doing
        monitor.beginTask("Fetching already set tags", IProgressMonitor.UNKNOWN);

        // Do your work
        tagsPage.getTags(defMap, freeMap);

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

