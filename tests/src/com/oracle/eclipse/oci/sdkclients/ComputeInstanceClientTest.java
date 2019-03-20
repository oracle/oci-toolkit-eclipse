package com.oracle.eclipse.oci.sdkclients;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.model.VnicAttachment;
import com.oracle.oci.eclipse.sdkclients.ComputeInstanceClient;

import tests.utils.SetupConfig;

public class ComputeInstanceClientTest {
    String instanceName  = "eclipse-test-instance";
    String instanceID  = "ocid1.instance.oc1.phx.abyhqljrkye6o6rtjluhz5zniddkaxlccpqxmfrwyrzyklpupjkwjr6qknya";

    @Before
    public void setup() {
        SetupConfig.init();
    }

    private ComputeInstanceClient createTestSubject() {
        return ComputeInstanceClient.getInstance();
    }

    @Test
    public void testGetInstance() throws Exception {
        ComputeInstanceClient result = ComputeInstanceClient.getInstance();
        assertNotNull(result);
    }

    @Test
    public void testUpdateClientRegion() throws Exception {
        ComputeInstanceClient.getInstance().updateClient();
    }

    @Test
    public void testGetComputeInstances() throws Exception {
        ComputeInstanceClient testSubject = createTestSubject();
        List<Instance> instances = testSubject.getComputeInstances();
        assertTrue(instances.size() > 0);
    }

    @Test
    public void testContainsTestComputeInstances() throws Exception {
        Boolean found = false;
        // check if the test instance exist
        ComputeInstanceClient testSubject = createTestSubject();
        List<Instance> instances = testSubject.getComputeInstances();
        for (Instance instance: instances) {
            if (instance.getDisplayName().toLowerCase().equals(instanceName)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testRunInstanceActionStop() throws Exception {
        ComputeInstanceClient testSubject = createTestSubject();
        testSubject.runInstanceAction(instanceID, "STOP");
    }

    @Test
    public void testListVnicAttachmentsOnInstance() throws Exception {
        ComputeInstanceClient testSubject = createTestSubject();
        Iterable<VnicAttachment> result = testSubject.listVnicAttachmentsOnInstance(instanceID);
        assertTrue(result.iterator().hasNext());
    }

}