/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.account;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.identity.model.AuthToken;
import com.oracle.oci.eclipse.ErrorHandler;

public class PreferencesWrapper {

    private final static String PREFERENCES_LOCATION = "oci-ide-prefs";
    public static final String SECURE_STORAGE_KEY_PATH = "com.oracle.oci.eclipse/";
    static String pathName = SECURE_STORAGE_KEY_PATH + PREFERENCES_LOCATION;
    private static Preferences systemPrefs = Preferences.userRoot().node(PREFERENCES_LOCATION);
    private static ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault().node(pathName);
    private final static String VERSION = "1.3.4";

    private final static String OCI_IDE_DEBUG_CLEAR_PREFS_KEY = "oracle.debug.oci-ide-prefs.clear";
    
    static
    {
        if (System.getProperty(OCI_IDE_DEBUG_CLEAR_PREFS_KEY) != null)
        {
            try {
                systemPrefs.clear();
                ErrorHandler.logInfo("Global user preferences clear");
            } catch (BackingStoreException e) {
                ErrorHandler.logErrorStack("Error clearing system prefs", e);
            }
        }
    }
    public static void setRegion(String regionId) {
        systemPrefs.put("region", regionId);
        ErrorHandler.logInfo("Setting the region to: "+ regionId);
    }

    public static void setProfile(String profileName) {
        systemPrefs.put("profile", profileName);
        ErrorHandler.logInfo("Setting the profile to: "+ profileName);
    }

    public static void setConfigFileName(String configFileName) {
        systemPrefs.put("configfile", configFileName);
        ErrorHandler.logInfo("Setting the config file to: "+ configFileName);
    }

    public static void setAuthToken(AuthToken authToken) {
        try {
            if(authToken != null) {
                securePreferences.put("authtoken", authToken.getToken(), true);
            } else {
                // Delete token when it is null
                securePreferences.put("authtoken", "", false);
            }
        } catch (StorageException e) {
            try {
                // If secure storage fails, try to store without encryption
                securePreferences.put("authtoken", authToken.getToken(), false);
                ErrorHandler.logInfo("save token in NON secure storage ");
            } catch (StorageException e1) {
                e1.printStackTrace();
            }
            ErrorHandler.logErrorStack("Error saving authtoken ", e);
        }
    }

    public static String getAuthToken() {
        try {
            return securePreferences.get("authtoken", "");
        } catch (StorageException e) {
            ErrorHandler.logErrorStack("Error getting authtoken ", e);
        }
        return "";
    }

    public static String getRegion() {
        return systemPrefs.get("region", "us-phoenix-1");
    }

    public static String getProfile() {
        return systemPrefs.get("profile", "DEFAULT");
    }

    public static String getConfigFileName() {
        return systemPrefs.get("configfile", ConfigFileOperations.getConfigFilePath());
    }

    public static String getUserAgent() {
        return String.format("Oracle-EclipseToolkit/%s", VERSION);
    }

    public static ISecurePreferences getSecurePreferences() {
        return securePreferences;
    }

    public static String createSecurePreferenceKey(String compartmentId, String databaseId) {
        return createSecurePreferenceKey("ADMIN", compartmentId, databaseId);
    }

    public static String createSecurePreferenceKey(String userName, String compartmentId, String databaseId) {
        return String.format("%s_PASS_%s_%s", userName, compartmentId, databaseId);
    }

    public static String createSecurePreferenceKey(AutonomousDatabaseSummary instance) {
        return createSecurePreferenceKey(instance.getCompartmentId(), instance.getId());
    }
    
    public static String createSecurePreferenceKeyForOCISecret(String userName, String compartmentId, String databaseId) {
        return String.format("%s_VAULTKEY_%s_%s", userName, compartmentId, databaseId);
    }
}
