package com.oracle.eclipse.oci.sdkclients;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.oracle.oci.eclipse.sdkclients.IdentClient;

import tests.utils.SetupConfig;

public class IdentClientTest {

    @Before
    public void setup() {
        SetupConfig.setProxy();
        SetupConfig.init();
    }

    @SuppressWarnings("unused")
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
}