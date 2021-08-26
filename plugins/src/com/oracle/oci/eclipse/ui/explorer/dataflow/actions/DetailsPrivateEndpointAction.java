package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.model.PrivateEndpoint;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable.TablePair;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;

public class DetailsPrivateEndpointAction extends BaseAction {

    private final PrivateEndpointTable table;
    private final List<PrivateEndpointSummary> privateEndpointSelectionList;
    private String pepID;
    private String title = "Private Endpoint Details";
	private PrivateEndpoint privateEndpointObject;

    public DetailsPrivateEndpointAction (PrivateEndpointTable table){
        this.table = table;
        privateEndpointSelectionList = (List<PrivateEndpointSummary>) table.getSelectedObjects();
    }

    @Override
    public String getText() {
        if ( privateEndpointSelectionList.size() == 1 ) {
            return "PrivateEndpoint Details";
        }
        return "";
    }

    @Override
    protected void runAction() {
        if (privateEndpointSelectionList.size() > 0) {
            PrivateEndpointSummary object = privateEndpointSelectionList.get(0);
            
            pepID = object.getId();
            
			try {
				privateEndpointObject=DataflowClient.getInstance().getPrivateEndpointDetails(pepID);
			} 
			catch (Exception e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to get private endpoint details : ",e.getMessage());
			}
        }
        else {
        	return;
        }
        new Job("Get PrivateEndpoint Details") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            List<TablePair> dataList = createDataList(privateEndpointObject);
                            DetailsTable detailsTable= new DetailsTable(title, dataList);
                            detailsTable.openTable();
                        }
                    });

                }
                catch (Exception e) {
                	MessageDialog.openError(Display.getDefault().getActiveShell(),"Error while getting private endpoint details :",e.getMessage());
                }
                return Status.OK_STATUS;
            }
        }.schedule();


    }

    protected List<TablePair> createDataList(PrivateEndpoint obj) {
        List<TablePair> data = new ArrayList<TablePair>();
        data.add(new TablePair("Description:", obj.getDescription()));
		data.add(new TablePair("Subnet:", obj.getSubnetId()));
        data.add(new TablePair("DNS Zones to Resolve:", String.join(",",obj.getDnsZones())));
		data.add(new TablePair("State Details:", obj.getLifecycleDetails()));
		data.add(new TablePair("Number of Hosts to Access:", obj.getMaxHostCount().toString()));
		if(obj.getNsgIds()!=null) 
			data.add(new TablePair("Network Security Groups:", String.join(",",obj.getNsgIds())));
		else 
			data.add(new TablePair("Network Security Groups:",""));
		data.add(new TablePair("OCID:", obj.getId()));
		data.add(new TablePair("Defined Tags:", obj.getDefinedTags().toString()));
		data.add(new TablePair("Free Form Tags:", obj.getFreeformTags().toString()));
        return data;
    }

}
