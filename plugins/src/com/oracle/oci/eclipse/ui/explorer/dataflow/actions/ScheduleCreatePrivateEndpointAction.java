package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.bmc.dataflow.model.CreatePrivateEndpointDetails;
import com.oracle.bmc.dataflow.requests.CreatePrivateEndpointRequest;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class ScheduleCreatePrivateEndpointAction implements IRunnableWithProgress{
	
	private Object[] obj;
	private Map<String,Map<String,Object>> OT;
	private Map<String,String> FT;
	private ArrayList<String> nsgs;
	private String compid,errorMessage=null;
	
    public ScheduleCreatePrivateEndpointAction(Object[] obj,Map<String,Map<String,Object>> OT,Map<String,String> FT,ArrayList<String> nsgs,String compid)
    {
    	this.obj=obj;
    	this.OT=OT;
    	this.FT=FT;
    	this.compid=compid;
    	this.nsgs=nsgs;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
    	try {
        // Tell the user what you are doing
        monitor.beginTask("Create Private Endpoint request processing", IProgressMonitor.UNKNOWN);

        // Do your work
        	System.out.println(compid);
        	
        CreatePrivateEndpointDetails createPrivateEndpointDetails = CreatePrivateEndpointDetails.builder()
				.compartmentId(compid)
				.definedTags(OT)
				.displayName((String)obj[3])
				.dnsZones(Arrays.asList((String[])obj[4]))
				.freeformTags(FT)
				.maxHostCount((int)obj[9])
				.nsgIds(nsgs)
				.subnetId((String)obj[8]).build();
		
		CreatePrivateEndpointRequest createPrivateEndpointRequest = CreatePrivateEndpointRequest.builder()
				.createPrivateEndpointDetails(createPrivateEndpointDetails)
				.build();
    // Send request to the Client 
		DataflowClient.getInstance().getDataFlowClient().createPrivateEndpoint(createPrivateEndpointRequest);
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

