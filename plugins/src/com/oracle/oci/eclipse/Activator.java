/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.oracle.oci.eclipse.plugin";
    public static final String PLUGIN_EXPLORER_ID = "com.oracle.oci.eclipse.view";

    // The shared instance
    private static Activator plugin;

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
		File theDir = new File(System.getProperty("java.io.tmpdir")+"\\dataflowtempdir");
		if(theDir.exists()) {
			for(String s: theDir.list()){
			    File currentFile = new File(theDir.getPath(),s);
			    currentFile.delete();
			}
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
}
