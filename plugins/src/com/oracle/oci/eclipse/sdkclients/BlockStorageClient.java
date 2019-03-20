/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.core.BlockstorageClient;
import com.oracle.bmc.core.model.Volume;
import com.oracle.bmc.core.model.VolumeBackup;
import com.oracle.bmc.core.requests.ListVolumeBackupsRequest;
import com.oracle.bmc.core.requests.ListVolumesRequest;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class BlockStorageClient {

    private static BlockStorageClient single_instance = null;
    private static BlockstorageClient blockStorageClient;

    private BlockStorageClient() {
        if (blockStorageClient == null) {
            blockStorageClient = createBlockStorageClient();
        }
    }

    public static BlockStorageClient getInstance() {
        if (single_instance == null) {
            single_instance = new BlockStorageClient();
        }
        return single_instance;
    }

    public void updateClient() {
        try {
            if (blockStorageClient != null) {
                blockStorageClient.close();
            }
            createBlockStorageClient();
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }

    private BlockstorageClient createBlockStorageClient(){
        blockStorageClient = new BlockstorageClient(AuthProvider.getInstance().getProvider());
        blockStorageClient.setRegion(AuthProvider.getInstance().getRegion());
        return blockStorageClient;
    }

    @Override
    public void finalize() throws Throwable{
        blockStorageClient.close();
        single_instance = null;
    }

    public List<Volume> getVolumes() throws Exception {
        List<AvailabilityDomain> domains = IdentClient.getInstance().getAvailabilityDomains(
                AuthProvider.getInstance().getProvider(),
                AuthProvider.getInstance().getCompartmentId(),
                AuthProvider.getInstance().getRegion());

        List<Volume> volumes = new ArrayList<Volume>();
        for(AvailabilityDomain domain: domains) {
            ListVolumesRequest req = ListVolumesRequest.builder()
                    .availabilityDomain(domain.getName())
                    .compartmentId(AuthProvider.getInstance().getCompartmentId()).build();

            volumes.addAll(blockStorageClient.listVolumes(req).getItems());
        }
        return volumes;
    }

    public List<VolumeBackup> getVolumeBackups(Volume volume) throws Exception {

        return blockStorageClient.listVolumeBackups(
                ListVolumeBackupsRequest.builder()
                .volumeId(volume.getId())
                .compartmentId(AuthProvider.getInstance().getCompartmentId())
                .build()).getItems();
    }

}
