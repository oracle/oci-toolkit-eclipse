/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.account;

import org.eclipse.core.runtime.Platform;

public class SystemPropertiesUtils {
	
	public static String currentUser() {
        return System.getProperty("user.name");
    }

    public static String userHome() {
        return System.getProperty("user.home");
    }

    public static String lineSeparator() {
        return System.getProperty("line.separator");
    }


    public static boolean isWindows() {
        return Platform.getOS().equals(Platform.OS_WIN32);
    }

    public static boolean isMac() {
        return Platform.getOS().equals(Platform.OS_MACOSX);
    }

    public static boolean isLinux() {
        return Platform.getOS().equals(Platform.OS_LINUX);
    }
}
