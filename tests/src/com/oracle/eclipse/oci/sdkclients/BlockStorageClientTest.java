package com.oracle.eclipse.oci.sdkclients;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.oracle.bmc.core.model.Volume;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.BlockStorageClient;

import tests.utils.SetupConfig;

public class BlockStorageClientTest {

    BlockStorageClient blockStorageClient;
    @Before
    public void setup() {
        SetupConfig.init();
        blockStorageClient = BlockStorageClient.getInstance();
        AuthProvider.getInstance().setCompartmentName(SetupConfig.COMPARTMENT_NAME);
        AuthProvider.getInstance().updateCompartmentId(SetupConfig.COMPARTMENT_ID);
    }

    @Test
    public void testUpdateClient() throws Exception {
        BlockStorageClient.getInstance().updateClient();
    }

    @Test
    public void testGetVolumes() throws Exception {
        List<Volume> result = blockStorageClient.getVolumes();
        //assertTrue(result.size() > 0);
    }
}