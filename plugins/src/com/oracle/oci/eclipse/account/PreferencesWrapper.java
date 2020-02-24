/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.account;

import java.util.prefs.Preferences;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

import com.oracle.bmc.identity.model.AuthToken;
import com.oracle.oci.eclipse.ErrorHandler;

public class PreferencesWrapper {

    private final static String PREFERENCES_LOCATION = "oci-ide-prefs";
    public static final String SECURE_STORAGE_KEY_PATH = "com.oracle.oci.eclipse/";
    static String pathName = SECURE_STORAGE_KEY_PATH + PREFERENCES_LOCATION;
    private static Preferences systemPrefs = Preferences.userRoot().node(PREFERENCES_LOCATION);
    private static ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault().node(pathName);
    private final static String VERSION = "1.2.0";

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
}
