package com.oracle.eclipse.oci.sdkclients;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.sdkclients.IdentClient;

import tests.utils.SetupConfig;

public class IdentClientTest {

    @Before
    public void setup() {
        SetupConfig.setProxy();
        SetupConfig.init();
    }

    private IdentClient createTestSubject() {
        return IdentClient.getInstance();
    }

    @Test
    public void testGetInstance() throws Exception {
        IdentClient result = IdentClient.getInstance();
        assertNotNull(result);
    }

    @Test
    public void testUpdateClient() throws Exception {
        IdentClient.getInstance().updateClient();
    }

    @Test
    public void testGetCompartmentList() throws Exception {
        IdentClient testSubject = createTestSubject();
        List<Compartment> result = testSubject.getCompartmentList();
        assertTrue(result.size() > 0);
    }

    @Test
    public void testGetCurrentCompartmentName() throws Exception {
        IdentClient testSubject = createTestSubject();
        String result = testSubject.getCurrentCompartmentName();
        assertFalse(result.isEmpty());
    }
}