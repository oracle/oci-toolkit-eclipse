/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

public class ErrorHandler {

    public static String getErrorMsg(String errorMessage, Throwable e) {
        try {
            if(e.toString().contains("BmcException")) {
                if (errorMessage.contains("NotAuthenticated")) {
                    return "Authentication Error: " + errorMessage.substring(errorMessage.indexOf(")") + 1, errorMessage.indexOf(".")+1);
                }
                else if (errorMessage.contains("Timed out while communicating")) {
                    return "Error: Timed out while communicating. Check your internet connection or check your Eclipse proxy settings if you are behind a firewall.";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return errorMessage;
    }

    public static IStatus reportException(String errorMessage, Throwable e) {
        errorMessage = getErrorMsg(errorMessage,  e);
        IStatus status = new Status(IStatus.ERROR, Activator.getDefault().PLUGIN_ID, errorMessage, e);
        return status;
    }
    public static IStatus reportAndShowException(String errorMessage, Throwable e) {
        errorMessage = getErrorMsg(errorMessage,  e);
        IStatus status = new Status(IStatus.ERROR, Activator.getDefault().PLUGIN_ID, errorMessage, e);
        StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG );
        return status;
    }

    public static void logInfo(String message) {
        Activator.getDefault().logMsg(IStatus.INFO, message, null);
    }
    public static void logError(String message) {
        Activator.getDefault().logMsg(IStatus.ERROR, message, null);
    }
    public static void logErrorStack(String message, Throwable th) {
        Activator.getDefault().logMsg(IStatus.ERROR, message, th);
    }
}
