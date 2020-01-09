/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.containerengine.ContainerEngineClient;
import com.oracle.bmc.containerengine.model.ClusterSummary;
import com.oracle.bmc.containerengine.model.NodePool;
import com.oracle.bmc.containerengine.model.NodePoolSummary;
import com.oracle.bmc.containerengine.requests.CreateKubeconfigRequest;
import com.oracle.bmc.containerengine.requests.GetNodePoolRequest;
import com.oracle.bmc.containerengine.requests.ListClustersRequest;
import com.oracle.bmc.containerengine.requests.ListNodePoolsRequest;
import com.oracle.bmc.containerengine.responses.CreateKubeconfigResponse;
import com.oracle.bmc.containerengine.responses.GetNodePoolResponse;
import com.oracle.bmc.containerengine.responses.ListClustersResponse;
import com.oracle.bmc.containerengine.responses.ListNodePoolsResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class ContainerClustersClient  extends BaseClient {

    private static ContainerClustersClient single_instance = null;
    private static ContainerEngineClient containerEngClient;


    private ContainerClustersClient() {
        if (containerEngClient == null) {
            containerEngClient = createContainerClustersClient();
        }
    }

    public static ContainerClustersClient getInstance() {
        if (single_instance == null) {
            single_instance = new ContainerClustersClient();
        }
        return single_instance;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        containerEngClient.setRegion(evt.getNewValue().toString());
    }

    @Override
    public void updateClient() {
        close();
        createContainerClustersClient();
    }

    private ContainerEngineClient createContainerClustersClient(){
        containerEngClient = new ContainerEngineClient(AuthProvider.getInstance().getProvider());
        containerEngClient.setRegion(AuthProvider.getInstance().getRegion());
        return containerEngClient;
    }

    @Override
    public void close() {
        try {
            if (containerEngClient != null) {
                containerEngClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }
    @Override
    public void finalize() throws Throwable{
        containerEngClient.close();
        single_instance = null;
    }


    public List<ClusterSummary> listClusters() throws Exception {
        ListClustersRequest listClustersRequest = ListClustersRequest.builder().
                compartmentId(AuthProvider.getInstance().getCompartmentId()).build();
        List<ClusterSummary> clusters = new ArrayList<ClusterSummary>();

        if(containerEngClient != null) {
            ListClustersResponse response = containerEngClient.listClusters(listClustersRequest);
            if(response != null) {
                clusters =response.getItems();
            }
        }
        //        for(ClusterSummary cluster: clusters) {
        //            System.out.println("--Found" + cluster.getId());
        //            //createKubeconfig(cluster.getId(), "/tmp/config_" + cluster.getName());
        //        }

        return clusters;
    }

    public boolean createKubeconfig(String clusterId, String path) throws Exception {
        // Config version 2 has a CLI command. It can't be used without CLI
        //CreateClusterKubeconfigContentDetails createClusterKubeconfigContentDetails =
        //        CreateClusterKubeconfigContentDetails.builder().tokenVersion("2.0.0").build();
        CreateKubeconfigRequest createKubeconfigRequest = CreateKubeconfigRequest.builder().
                //createClusterKubeconfigContentDetails(createClusterKubeconfigContentDetails).
                clusterId(clusterId).build();

        if(containerEngClient != null) {
            CreateKubeconfigResponse response = containerEngClient.createKubeconfig(createKubeconfigRequest);
            if(response != null) {
                InputStream configStream = response.getInputStream();
                try {
                    File configFile = new File(path);
                    Files.copy(configStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    configStream.close();
                } catch (IOException ex) {
                    configStream.close();
                    return false;
                }
            }
        }
        return true;
    }

    public NodePool getNodePool(String nodePoolId) throws Exception {
        GetNodePoolRequest getNodePoolRequest = GetNodePoolRequest.builder().nodePoolId(nodePoolId).build();
        NodePool nodePool = null;

        if (containerEngClient != null) {
            GetNodePoolResponse response = containerEngClient.getNodePool(getNodePoolRequest);
            if (response != null) {
                nodePool = response.getNodePool();
            }
        }
        return nodePool;
    }

    public List<NodePoolSummary> listNodePools() throws Exception {
        ListNodePoolsRequest listNodePoolsRequest = ListNodePoolsRequest.builder().
                compartmentId(AuthProvider.getInstance().getCompartmentId()).build();
        List<NodePoolSummary> nodePoolSummary = null;

        if (containerEngClient != null) {
            ListNodePoolsResponse response = containerEngClient.listNodePools(listNodePoolsRequest);
            if (response != null) {
                nodePoolSummary = response.getItems();
            }
        }
        for (NodePoolSummary np: nodePoolSummary ) {
            System.out.println(np.getName() + " - " + np.toString());
        }
        return nodePoolSummary;
    }
}
