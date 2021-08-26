package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.util.List;

import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.NetworkSecurityGroup;
import com.oracle.bmc.core.requests.ListNetworkSecurityGroupsRequest;
import com.oracle.bmc.core.responses.ListNetworkSecurityGroupsResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class VirtualnetworkClient extends BaseClient {
	private static VirtualnetworkClient single_instance = null;
    private static VirtualNetworkClient virtualnetworkClient;

    private VirtualnetworkClient() {
        if (virtualnetworkClient == null) {
        	virtualnetworkClient = createVirtualNetworkClient();
        }
    }

    public static VirtualnetworkClient getInstance() {
        if (single_instance == null) {
            single_instance = new VirtualnetworkClient();
        }
        return single_instance;
    }

    private VirtualNetworkClient createVirtualNetworkClient(){
    	virtualnetworkClient = new VirtualNetworkClient(AuthProvider.getInstance().getProvider());
    	virtualnetworkClient.setRegion(AuthProvider.getInstance().getRegion());
        return virtualnetworkClient;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	virtualnetworkClient.setRegion(evt.getNewValue().toString());
    }

    
    @Override
    public void updateClient() {
        close();
        createVirtualNetworkClient();
    }
   
    public VirtualNetworkClient getVirtualNetworkClient() {
        return virtualnetworkClient;
    }
    
    @Override
    public void close() {
        try {
            if (virtualnetworkClient != null) {
            	virtualnetworkClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }
    
    public List<NetworkSecurityGroup> getNetworkSecurityGroupList(String compid) throws Exception{
    	ListNetworkSecurityGroupsRequest listNetworkSecurityGroupsRequest = ListNetworkSecurityGroupsRequest.builder().compartmentId(compid).build();

        ListNetworkSecurityGroupsResponse response = virtualnetworkClient.listNetworkSecurityGroups(listNetworkSecurityGroupsRequest);
        
        return response.getItems();
    }
}
