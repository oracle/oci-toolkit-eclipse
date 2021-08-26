package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.requests.DeletePrivateEndpointRequest;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;


public class DeletePrivateEndpointAction extends BaseAction {
    
	private PrivateEndpointTable pepTable;
	private PrivateEndpointSummary pep;
	
	public DeletePrivateEndpointAction(PrivateEndpointTable pepTable,PrivateEndpointSummary pep) {
        setText("Delete Private Endpoint");
		this.pepTable=pepTable;
		this.pep=pep;
    }

    @Override
    protected void runAction() {
    	if(pep.getLifecycleState().toString()=="Creating") {
    		
    		MessageDialog.openInformation(pepTable.getShell(), "Failed to Delete Private Endpoint", "Private endpoint is still in creating state and hence cannot be deleted");
    		return;
    	}
    	boolean result = MessageDialog.openConfirm(pepTable.getShell(), "Please Confirm", "Are you sure you want to delete the Private Endpoint named "+pep.getDisplayName()+"?");
    	
		if (result){
			try{
			DataFlowClient client = DataflowClient.getInstance().getDataFlowClient();
			DeletePrivateEndpointRequest deletePrivateEndpointRequest = DeletePrivateEndpointRequest.builder()
			.privateEndpointId(pep.getId())
			.build();

			client.deletePrivateEndpoint(deletePrivateEndpointRequest);
			MessageDialog.openInformation(pepTable.getShell(), "Successful", "Private endpoint successfully deleted");
			pepTable.refresh(true);
			}
			catch (Exception e){
				MessageDialog.openError(pepTable.getShell(), "Error while deleting private endpoint", e.getMessage());
			}
		} else {
			// Cancel Button selected do something
		}
    }
}