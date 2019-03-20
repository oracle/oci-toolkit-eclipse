/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.model.Vnic;
import com.oracle.bmc.core.model.VnicAttachment;
import com.oracle.bmc.core.model.VolumeAttachment;
import com.oracle.bmc.core.requests.GetVnicRequest;
import com.oracle.bmc.core.requests.InstanceActionRequest;
import com.oracle.bmc.core.requests.ListInstancesRequest;
import com.oracle.bmc.core.requests.ListVnicAttachmentsRequest;
import com.oracle.bmc.core.requests.ListVolumeAttachmentsRequest;
import com.oracle.bmc.core.responses.InstanceActionResponse;
import com.oracle.bmc.core.responses.ListInstancesResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class ComputeInstanceClient {

    private static ComputeInstanceClient single_instance = null;
    private static ComputeClient computeClient;
    private static VirtualNetworkClient vcnClient;
    private static HashMap<String, InstanceWrapper> instancesMap = new HashMap<String, InstanceWrapper>();

    private ComputeInstanceClient() {
        if (computeClient == null) {
            computeClient = createComputeInstanceClient();
            vcnClient = createVCNInstanceClient();
        }
    }

    public static ComputeInstanceClient getInstance() {
        if (single_instance == null) {
            single_instance = new ComputeInstanceClient();
        }
        return single_instance;
    }

    public void updateClient() {
        close();
        createComputeInstanceClient();
    }

    private ComputeClient createComputeInstanceClient(){
        computeClient = new ComputeClient(AuthProvider.getInstance().getProvider());
        computeClient.setRegion(AuthProvider.getInstance().getRegion());
        vcnClient = new VirtualNetworkClient(AuthProvider.getInstance().getProvider());
        vcnClient.setRegion(AuthProvider.getInstance().getRegion());
        return computeClient;
    }

    private VirtualNetworkClient createVCNInstanceClient(){
        vcnClient = new VirtualNetworkClient(AuthProvider.getInstance().getProvider());
        vcnClient.setRegion(AuthProvider.getInstance().getRegion());
        return vcnClient;
    }

    public void close() {
        try {
            if (computeClient != null && vcnClient != null) {
                computeClient.close();
                vcnClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }

    @Override
    public void finalize() throws Throwable{
        computeClient.close();
        single_instance = null;
    }


    public List<Instance> getComputeInstances() throws Exception {
        ListInstancesRequest listInstancesRequest = ListInstancesRequest.builder().
                compartmentId(AuthProvider.getInstance().getCompartmentId()).build();
        List<Instance> instances = new ArrayList<Instance>();

        if(computeClient != null) {
            ListInstancesResponse response = computeClient.listInstances(listInstancesRequest);
            if(response != null) {
                instances =response.getItems();
            }
        }

        for (Instance instance : instances) {
            instancesMap.put(instance.getId(), new InstanceWrapper(instance));
            ErrorHandler.logInfo("Found instance: " + instance.getDisplayName());
        }

        return instances;
    }

    public void listVnicAttachments(final String compartmentId) {
        ListVnicAttachmentsRequest req = ListVnicAttachmentsRequest.builder().compartmentId(compartmentId).build();
        List<VnicAttachment> vnics = computeClient.listVnicAttachments(req).getItems();

        for (VnicAttachment vnicAttachment : vnics) {
            Vnic vnic =
                    vcnClient.getVnic(GetVnicRequest.builder().vnicId(vnicAttachment.getVnicId()).build()).getVnic();

            if (vnic != null && vnic.getPublicIp() != null) {
                instancesMap.get(vnicAttachment.getInstanceId()).setVnic(vnic);
            }
            instancesMap.get(vnicAttachment.getInstanceId()).setVnicAttachment(vnicAttachment);
        }
    }

    public void runInstanceAction(String instanceId, String action) throws Exception {

        InstanceActionRequest req = InstanceActionRequest.builder().instanceId(instanceId).action(action).build();
        InstanceActionResponse response = computeClient.instanceAction(req);
        response.builder().build();

        ErrorHandler.logInfo(action + " instanceId: " + instanceId);
    }

    public Iterable<VolumeAttachment> listVolumeAttachments(final String compartmentId) {
        final Iterable<VolumeAttachment> volAttachIterable =
                computeClient
                .getPaginators()
                .listVolumeAttachmentsRecordIterator(
                        ListVolumeAttachmentsRequest.builder()
                        .compartmentId(compartmentId)
                        .build());

        for (VolumeAttachment volumeAttachment : volAttachIterable) {
            ErrorHandler.logInfo(volumeAttachment.toString());
            instancesMap.get(volumeAttachment.getInstanceId()).addVolumeAttachment(volumeAttachment);
        }

        return volAttachIterable;
    }

    public Iterable<VolumeAttachment> listVolumeAttachmentsOnInstance(final String instanceId) {
        final Iterable<VolumeAttachment> volAttachIterable =
                computeClient
                .getPaginators()
                .listVolumeAttachmentsRecordIterator(
                        ListVolumeAttachmentsRequest.builder()
                        .compartmentId(AuthProvider.getInstance().getCompartmentId())
                        .instanceId(instanceId)
                        .build());

        for (VolumeAttachment volumeAttachment : volAttachIterable) {
            ErrorHandler.logInfo(volumeAttachment.toString());
            instancesMap.get(volumeAttachment.getInstanceId()).addVolumeAttachment(volumeAttachment);
        }

        return volAttachIterable;
    }

    public Iterable<VnicAttachment> listVnicAttachmentsOnInstance(final String instanceId) {

        final Iterable<VnicAttachment> vicAttachIterable =
                computeClient
                .getPaginators()
                .listVnicAttachmentsRecordIterator(
                        ListVnicAttachmentsRequest.builder()
                        .compartmentId(AuthProvider.getInstance().getCompartmentId())
                        .instanceId(instanceId)
                        .build());

        for (VnicAttachment vnicAttachment : vicAttachIterable) {
            try {
                Vnic vnic =
                        vcnClient.getVnic(GetVnicRequest.builder().vnicId(vnicAttachment.getVnicId()).build()).getVnic();

                if (vnic != null && vnic.getPublicIp() != null) {
                    instancesMap.get(vnicAttachment.getInstanceId()).setVnic(vnic);
                }
            } catch (Exception ex) {
                ErrorHandler.logError("Error vnic req " + ex.getMessage());
            }
            instancesMap.get(vnicAttachment.getInstanceId()).setVnicAttachment(vnicAttachment);
        }

        return vicAttachIterable;
    }
    public InstanceWrapper getInstanceDetails(final String instanceId) {
        listVolumeAttachmentsOnInstance(instanceId);
        listVnicAttachmentsOnInstance(instanceId);
        return instancesMap.get(instanceId);
    }
}
