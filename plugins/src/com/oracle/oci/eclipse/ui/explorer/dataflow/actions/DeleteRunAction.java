package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.dialogs.MessageDialog;

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.DeleteRunRequest;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunTable;

public class DeleteRunAction extends BaseAction {
    
	private RunTable runTable;
	private RunSummary runSummary;
	
	public DeleteRunAction(RunSummary runSummary,RunTable runTable) {
        setText("Cancel Run");
		this.runTable=runTable;
		this.runSummary=runSummary;
    }

    @Override
    protected void runAction() {
    	
    	boolean result = MessageDialog.openConfirm(runTable.getShell(), "Please Confirm", "Are you sure you wish to cancel the run "+runSummary.getDisplayName()+"?");
		if (result){
			try{
			DataFlowClient client = DataflowClient.getInstance().getDataFlowClient();
			DeleteRunRequest deleteRunRequest = DeleteRunRequest.builder()
					.runId(this.runSummary.getId())
					.opcRequestId(this.runSummary.getOpcRequestId())
					.build();
			        client.deleteRun(deleteRunRequest);
			MessageDialog.openInformation(runTable.getShell(), "Successful", "Successfully cancelled the run");
			runTable.refresh(true);
			}
			catch (Exception e){
				MessageDialog.openError(runTable.getShell(), "Error while cancelling the run "+ this.runSummary.getDisplayName(), e.getMessage());
			}
		} else {
			// Cancel Button selected do something
		}
    }
}
