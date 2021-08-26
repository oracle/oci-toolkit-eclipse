package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.UpdatePrivateEndpointDetails;
import com.oracle.bmc.dataflow.requests.UpdatePrivateEndpointRequest;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class ScheduleEditPrivateEndpointAction implements IRunnableWithProgress{
	
	private Map<String,Map<String,Object>> OT;
	private Map<String,String> FT;
	private String name,pepid;
	private String[] dnszones;
	private String errorMessage=null;
	
    public ScheduleEditPrivateEndpointAction(Map<String,Map<String,Object>> OT,Map<String,String> FT,String[] dnszones,String name,String pepid)
    {
    	this.OT=OT;
    	this.FT=FT;
    	this.name=name;
    	this.pepid=pepid;
    	this.dnszones=dnszones;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
    	try {
        // Tell the user what you are doing
        monitor.beginTask("Edit Private Endpoint request processing", IProgressMonitor.UNKNOWN);

        // Do your work
        DataFlowClient client=DataflowClient.getInstance().getDataFlowClient();
        
        UpdatePrivateEndpointDetails updatePrivateEndpointDetails = UpdatePrivateEndpointDetails.builder()
        		.freeformTags(FT)
        		.definedTags(OT)
        		.displayName(name)
        		.dnsZones(Arrays.asList(dnszones))
        		.build();

        UpdatePrivateEndpointRequest updatePrivateEndpointRequest = UpdatePrivateEndpointRequest.builder()
        		.updatePrivateEndpointDetails(updatePrivateEndpointDetails)
        		.privateEndpointId(pepid).build();

        client.updatePrivateEndpoint(updatePrivateEndpointRequest);

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

