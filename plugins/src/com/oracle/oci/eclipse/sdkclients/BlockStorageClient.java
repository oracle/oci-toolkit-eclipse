/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
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

public class BlockStorageClient extends BaseClient {

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

    private BlockstorageClient createBlockStorageClient(){
        blockStorageClient = new BlockstorageClient(AuthProvider.getInstance().getProvider());
        blockStorageClient.setRegion(AuthProvider.getInstance().getRegion());
        return blockStorageClient;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        blockStorageClient.setRegion(evt.getNewValue().toString());
    }

    @Override
    public void updateClient() {
        close();
        createBlockStorageClient();
    }
    @Override
    public void close() {
        try {
            if (blockStorageClient != null) {
                blockStorageClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
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
