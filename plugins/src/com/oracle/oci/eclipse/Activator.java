/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;
import com.oracle.oci.eclipse.ui.explorer.database.actions.CustomADBInstanceActionFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.oracle.oci.eclipse.plugin";
    public static final String PLUGIN_EXPLORER_ID = "com.oracle.oci.eclipse.view";

    // The shared instance
    private static Activator plugin;
    
    private static Object EXT_POINT_LOCK = new Object();
    private static List<CustomADBInstanceActionFactory> databaseActions;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            ADBInstanceClient.getInstance().dispose();
            AuthProvider.getInstance().dispose();
        } catch (Throwable e) {
            ErrorHandler.logErrorStack("Disposing bundle", e);
        }

        plugin = null;        
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public String getPluginId() {
        return getBundle().getSymbolicName();
    }

    public IStatus logMsg(int logStatus, String infoMessage, Throwable th) {
        IStatus status = new Status(logStatus, getPluginId(), infoMessage, th);
        getLog().log(status);
        return status;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String key) {
        return getDefault().getImageRegistry().getDescriptor(key);
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        for (Icons icon : Icons.values()) {
            ImageDescriptor desc = imageDescriptorFromPlugin(PLUGIN_ID, icon.getPath());
            if (desc == null) {
                ErrorHandler.logError("ERROR LOADING IMAGE " + icon.getPath());
            } else {
                reg.put(icon.getPath(), desc);
            }
        }
    }
    public static Image getImage(String key) {
        return getDefault().getImageRegistry().get(key);
    }

    public static List<CustomADBInstanceActionFactory> getDatabaseActionFactories() {
        synchronized(EXT_POINT_LOCK) {
            if (databaseActions == null) {
                databaseActions = new ArrayList<CustomADBInstanceActionFactory>();
                IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();
                IExtensionPoint extensionPoint = pluginRegistry.getExtensionPoint(
                        "com.oracle.oci.eclipse.plugin", "databaseInstanceActions"); //$NON-NLS-1$ //$NON-NLS-2$
                IExtension[] extensions = extensionPoint.getExtensions();
                for (int extensionIndex = 0; extensionIndex < extensions.length; extensionIndex++) {
                    IConfigurationElement[] configElements = extensions[extensionIndex].getConfigurationElements();
                    for (int elementIndex = 0; elementIndex < configElements.length; ++elementIndex) {
                        if (configElements[elementIndex].getName().equals("actionFactory")) //$NON-NLS-1$
                        {
                            IConfigurationElement configElement = configElements[elementIndex];
                            CustomADBInstanceActionFactory actionFactory = null;
                            try {
                                actionFactory = (CustomADBInstanceActionFactory) configElement
                                        .createExecutableExtension("class"); //$NON-NLS-1$
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack("Create action extension", e);
                            }
                            if (actionFactory != null) {
                                databaseActions.add(actionFactory);
                            }
                        }
                    }
                }
            }
            return databaseActions;
        }
    }
}
