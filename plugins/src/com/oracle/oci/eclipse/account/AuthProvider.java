/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.account;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.ClientRuntime;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.BlockStorageClient;
import com.oracle.oci.eclipse.sdkclients.ComputeInstanceClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.account.CompartmentOptions;
import com.oracle.oci.eclipse.ui.account.RegionOptions;
import com.oracle.oci.eclipse.ui.explorer.NavigatorDoubleClick;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.ObjStorageContentProvider;


public class AuthProvider {

    private static AuthProvider single_instance = null;

    private static AuthenticationDetailsProvider provider;
    private String currentProfileName = PreferencesWrapper.getProfile();
    private String currentConfigFileName = PreferencesWrapper.getConfigFileName();
    private String currentRegionName = PreferencesWrapper.getRegion();
    private String currentCompartmentId;

    public static AuthProvider getInstance()
    {
        if (single_instance == null) {
            single_instance = new AuthProvider();
        }
        return single_instance;
    }

    private AuthProvider() {}

    private AuthenticationDetailsProvider createProvider() {
        try {
            provider =
                    new ConfigFileAuthenticationDetailsProvider(currentConfigFileName, currentProfileName);
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
        currentCompartmentId = provider.getTenantId();
        setClientUserAgent();
        return provider;
    }

    public AuthenticationDetailsProvider getProvider() {
        if (provider == null) {
            provider = createProvider();
        }
        else {
            String newProfile = PreferencesWrapper.getProfile();
            String newConfigFileName = PreferencesWrapper.getConfigFileName();
            currentRegionName = PreferencesWrapper.getRegion();

            if ((!newProfile.equals(currentProfileName)) || (!newConfigFileName.endsWith(currentConfigFileName))) {
                currentConfigFileName = newConfigFileName;
                currentProfileName = newProfile;
                try {
                    provider = new ConfigFileAuthenticationDetailsProvider(currentConfigFileName, currentProfileName);
                    ErrorHandler.logInfo(currentProfileName + " " + currentConfigFileName + " " + currentRegionName + " "
                            + currentCompartmentId);
                } catch (Exception e) {
                    ErrorHandler.reportException("Error connecting to: " + currentProfileName + " " + e.getMessage(), e);
                }
            }
        }
        return provider;
    }

    public void refreshClients() {
        // Update Clients
        new Job("Update") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    ObjStorageClient.getInstance().updateClient();
                    ComputeInstanceClient.getInstance().updateClient();
                    BlockStorageClient.getInstance().updateClient();
                    IdentClient.getInstance().updateClient();
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            RegionOptions.refreshRegions();
                            CompartmentOptions.refreshCompartments();
                            NavigatorDoubleClick.closeAllComputeWindows();
                            NavigatorDoubleClick.closeAllBucketWindows();
                        }
                    });

                    ObjStorageContentProvider.getInstance().getBucketsAndRefresh();

                } catch(Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    public void updateCompartmentId(String compartmentId) {
        currentCompartmentId = compartmentId;
    }

    public String getCompartmentId() {
        return currentCompartmentId;
    }

    public Region getRegion() {
        return Region.fromRegionId(currentRegionName);
    }

    // set the plug-in version into the SDK.
    private void setClientUserAgent() {
        ErrorHandler.logInfo("Setting SDK ClientUserAgent to: "+ PreferencesWrapper.getUserAgent());
        ClientRuntime.setClientUserAgent(PreferencesWrapper.getUserAgent());
    }

    @Override
    public void finalize() throws Throwable{
        single_instance = null;
    }

}
