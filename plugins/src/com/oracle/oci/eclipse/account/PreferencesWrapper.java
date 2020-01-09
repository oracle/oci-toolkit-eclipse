/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.account;

import java.util.prefs.Preferences;

import com.oracle.oci.eclipse.ErrorHandler;

public class PreferencesWrapper {

    private final static String PREFERENCES_LOCATION = "oci-ide-prefs";
    private static Preferences systemPrefs = Preferences.userRoot().node(PREFERENCES_LOCATION);
    private final static String VERSION = "1.1.0";

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
