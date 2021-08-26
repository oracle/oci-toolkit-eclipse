package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.swt.program.Program;
import com.oracle.bmc.Region;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;

public class OpenInConsoleAction extends BaseAction {

    private String objectID = null;
    private String title = "Open In Console";
    
	public OpenInConsoleAction (String objectId){
        this.objectID = objectId;
    }

	@Override
    public String getText() {
        if ( objectID != null ) {
            return title;
        }
        return "";
    }
    
    @Override
 	protected void runAction() {
    	if (objectID != null) {
    		Region region = AuthProvider.getInstance().getRegion();
    		String type = objectID.split("\\.")[1];
    		if(type.equals("dataflowapplication"))
    			Program.launch("https://console."+region.getRegionId()+".oraclecloud.com/data-flow/apps/details/"+objectID);
    		else if(type.equals("dataflowrun"))
    			Program.launch("https://console."+region.getRegionId()+".oraclecloud.com/data-flow/runs/details/"+objectID);
    		else if(type.equals("dataflowprivateendpoint"))
    			Program.launch("https://console."+region.getRegionId()+".oraclecloud.com/data-flow/privateEndpoint/details/"+objectID);
    		
    	}

    }
}
