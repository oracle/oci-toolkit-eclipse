package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;

import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.core.requests.GetVcnRequest;
import com.oracle.bmc.core.requests.ListVcnsRequest;
import com.oracle.bmc.core.responses.GetVcnResponse;
import com.oracle.bmc.core.responses.ListVcnsResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class NetworkClient extends BaseClient {

    private VirtualNetworkClient virtualNetworkClient;

    public NetworkClient() {
        if (virtualNetworkClient == null) {
            virtualNetworkClient = createADBInstanceClient();
        }

    }

    @Override
    public void updateClient() {
        close();
        createADBInstanceClient();
    }

    private VirtualNetworkClient createADBInstanceClient() {
        virtualNetworkClient = new VirtualNetworkClient(AuthProvider.getInstance().getProvider());
        virtualNetworkClient.setRegion(AuthProvider.getInstance().getRegion());
        return virtualNetworkClient;
    }

    @Override
    public void close() {
        try {
            if (virtualNetworkClient != null) {
                virtualNetworkClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        virtualNetworkClient.setRegion(evt.getNewValue().toString());
    }

    public List<Vcn> listVcns()
    {
        String compartmentId = AuthProvider.getInstance().getCompartmentId();
        return listVcns(compartmentId);
    }

    public List<Vcn> listVcns(String compartmentId)
    {
        ListVcnsRequest request = 
            ListVcnsRequest.builder().compartmentId(compartmentId).build();
        
        List<Vcn> instances = Collections.emptyList();

        if(virtualNetworkClient == null) {
            return instances;
        }
        
        ListVcnsResponse response = null;
        try {
            response = this.virtualNetworkClient.listVcns(request);
        } catch(Throwable e) {
            // To handle forbidden error
            ErrorHandler.logError("Unable to list Autonomous Databases: "+e.getMessage());
        }

        if (response == null) {
            return instances;
        }

        return response.getItems();
    }
    
    public Vcn getVcn(String vcnId)
    {
        if (virtualNetworkClient == null)
        {
            return null;
        }
        GetVcnRequest request = GetVcnRequest.builder().vcnId(vcnId).build();
        
        GetVcnResponse response = null;
        try {
            response = this.virtualNetworkClient.getVcn(request);
        } catch(Throwable e) {
            // To handle forbidden error
            ErrorHandler.logError("Unable to list Autonomous Databases: "+e.getMessage());
        }

        if (response == null) {
            return null;
        }
        return response.getVcn();
    }
}
