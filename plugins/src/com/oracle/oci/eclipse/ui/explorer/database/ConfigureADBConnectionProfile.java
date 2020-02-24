/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.io.File;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.datatools.connectivity.ConnectionProfileException;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.drivers.DriverInstance;
import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.IPropertySet;
import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCConnectionProfileConstants;
import org.eclipse.datatools.connectivity.ui.dse.views.DataSourceExplorerView;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;


public class ConfigureADBConnectionProfile {

	private static final String DATABASE_DEVELOPMENT_VIEW = "org.eclipse.datatools.connectivity.DataSourceExplorerNavigator";
	
	private static final String DRIVER_CLASS_NAME = "oracle.jdbc.OracleDriver";
	private static final String DRIVER_DEFINITION_ID_PROP = "org.eclipse.datatools.connectivity.driverDefinitionID";
	private static final String CONNECTION_PROFILE_PROVIDER = "org.eclipse.datatools.enablement.oracle.connectionProfile";
	private static final String PROP_DEFN_TYPE_VALUE =  "org.eclipse.datatools.enablement.oracle.11.driverTemplate";
	private static final String DATABASE_VENDOR_PROP_ID_VALUE = "Oracle";
	private static final String DATABASE_VERSION_PROP_ID_VALUE = "11";
	
	private static final String ORACLE_CATALOG_TYPE_PROP = "org.eclipse.datatools.enablement.oracle.catalogType";
	private static final String ORACLE_CATALOG_TYPE_PROP_VALUE = "USER";
	
	// comma separated list of name-value pairs of jdbc connection properties(prop1=value1,prop2=value2)
	private static final String JDBC_CONNECTION_PROP = "org.eclipse.datatools.connectivity.db.connectionProperties";
	private static final String ORACLE_NET_KEEPALIVE_PROP = "oracle.net.keepAlive";
	private static final String ORACLE_NET_KEEPALIVE_VALUE = "true";
	
	// use your own JDBC driver instead of using default one.
	private static final String TARGET_DRIVER_INSTANCE_ID = "DriverDefn.oci.toolkit.driver";
	private static final String TARGET_DRIVER_INSTANCE_NAME = "OCI Toolkit Driver";
	
	
	public static void registerDriver(final String driverPath) {
		
		if(driverPath == null || "".equals(driverPath))
			return;
		
		DriverInstance existingDriver = DriverManager.getInstance()
				.getDriverInstanceByID(TARGET_DRIVER_INSTANCE_ID);
		
		if (existingDriver != null) {
			if (driverPath.equalsIgnoreCase(existingDriver.getJarList())) {
				ErrorHandler.logInfo("JDBC driver is already registered");
				return;
			} else {
				ErrorHandler.logInfo("User specified a new location of jdbc jar, so removing already configured Driver instance.");
				DriverManager.getInstance().removeDriverInstance(TARGET_DRIVER_INSTANCE_ID);
			}
		}
		
		// register the driver with given jdbc jar path
		Properties driverProperties = new Properties();
		driverProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, driverPath);
		driverProperties.setProperty(IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID,DRIVER_CLASS_NAME);
		driverProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VENDOR_PROP_ID, DATABASE_VENDOR_PROP_ID_VALUE);
		driverProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VERSION_PROP_ID, DATABASE_VERSION_PROP_ID_VALUE);
		driverProperties.setProperty(IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, String.valueOf(true));
		driverProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, PROP_DEFN_TYPE_VALUE);
		driverProperties.setProperty(ORACLE_CATALOG_TYPE_PROP, ORACLE_CATALOG_TYPE_PROP_VALUE);
		
		IPropertySet propertySet = new PropertySetImpl(TARGET_DRIVER_INSTANCE_NAME, TARGET_DRIVER_INSTANCE_ID);
        propertySet.setBaseProperties(driverProperties);
        DriverInstance driver = new DriverInstance(propertySet);
        DriverManager.getInstance().addDriverInstance(driver);
		
	}
	

	public static void createConnectionProfile(
			IProgressMonitor monitor, AutonomousDatabaseSummary adbInstance, String user,
			String password, String walletLocation, String aliasName) throws ConnectionProfileException {
		
		final String regionName = AuthProvider.getInstance().getRegion().toString();
		final String profileName = user.toUpperCase()+"."+aliasName+"."+regionName;
		walletLocation = walletLocation.replace('\\', '/');
		final String url = "jdbc:oracle:thin:@"+aliasName+"?TNS_ADMIN="+walletLocation;
		
		monitor.beginTask("Configuring database connection", 5);
    	IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 4);
    	subMonitor.beginTask("Creating connection profile", 1);
		
		DriverInstance driverInstance = getDriverInstance();
		IConnectionProfile profile = getConnectionProfile(driverInstance, profileName, user, password, url);
		ErrorHandler.logInfo("Connection profile created successfully for database : " + adbInstance.getDbName());
		subMonitor.worked(1);
		monitor.subTask("Connecting...");
		ConfigureADBConnectionProfile.connectAndRevealConnectionProfile(profile);
		monitor.worked(1);
	}
	
	/**
	 * Create and return connection profile.
	 */
	private static IConnectionProfile getConnectionProfile(
			DriverInstance driverInstance, String profileName, String user,
			String password, String url) throws ConnectionProfileException {
		
		Properties profileProperties = driverInstance.getPropertySet()
				.getBaseProperties();
		
		profileProperties.setProperty(IJDBCConnectionProfileConstants.URL_PROP_ID, url);
		profileProperties.setProperty(IJDBCConnectionProfileConstants.USERNAME_PROP_ID, user);
		profileProperties.setProperty(IJDBCConnectionProfileConstants.PASSWORD_PROP_ID, password);
		profileProperties.setProperty(JDBC_CONNECTION_PROP,ORACLE_NET_KEEPALIVE_PROP+"="+ORACLE_NET_KEEPALIVE_VALUE);
		
		profileProperties.setProperty(DRIVER_DEFINITION_ID_PROP, driverInstance.getId());
		
		IConnectionProfile existingProfile = ProfileManager.getInstance().getProfileByName(profileName);
		
        if (existingProfile != null) {
            existingProfile.setBaseProperties(profileProperties);
            ProfileManager.getInstance().modifyProfile(existingProfile);
            return existingProfile;
        } else {
            IConnectionProfile newProfile = ProfileManager.getInstance().createProfile(
                        profileName, profileName,
                        CONNECTION_PROFILE_PROVIDER,
                        profileProperties);
            return newProfile;
        }
	}
	
	/**
	 * Returns the driver instance to be used by the plugin.
	 * 
	 * @return driver instance to be used by the plugin.
	 */
	private static DriverInstance getDriverInstance() {
		
		DriverInstance existingDriver = DriverManager.getInstance()
				.getDriverInstanceByID(TARGET_DRIVER_INSTANCE_ID);
		
		if(existingDriver == null)
			throw new RuntimeException("Unable to locate jdbc driver jar, please register the driver first and then try creating connection.");
		
		// check if jdbc jar is present in already registered driver
		File f = new File(existingDriver.getJarList());
		if(!f.exists())
			throw new RuntimeException("Jdbc driver jar not found, please re-register the driver and then try creating connection.");
		
		return existingDriver;
		
	}
	
	/**
     * Connects to the specified connection profile, selects and reveals the profile to
     * the Data Source Explorer view.
     *
     * @param connectionProfile
     *            The connection profile to connect and reveal
     */
    public static void connectAndRevealConnectionProfile(final IConnectionProfile connectionProfile) {
        IStatus connectStatus = connectionProfile.connect();
        if (connectStatus.isOK() == false) {
        	try {
				ProfileManager.getInstance().deleteProfile(connectionProfile);
			} catch (Exception e) {
				ErrorHandler.logInfo("Could not delete connection profile: " + e.getMessage());
			}
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Unable to connect to the Autonomous database.  Make sure your password is correct"
					+ "\n and you can access your database through your network and any firewalls you may be connecting through.");
            StatusManager.getManager().handle(status, StatusManager.BLOCK | StatusManager.LOG);
            return;
        }

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DATABASE_DEVELOPMENT_VIEW);
                    if (view instanceof DataSourceExplorerView) {
                        DataSourceExplorerView dse = (DataSourceExplorerView)view;
                        StructuredSelection selection = new StructuredSelection(connectionProfile);
                        dse.getCommonViewer().setSelection(selection, true);
                    }
                } catch (Exception e) {
                	try {
        				ProfileManager.getInstance().deleteProfile(connectionProfile);
        			} catch (Exception ex) {
        				ErrorHandler.logInfo("Could not delete connection profile: " + ex.getMessage());
        			}
                    Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to reveal the connection profile to datasource explorer view: " + e.getMessage(), e);
                    StatusManager.getManager().handle(status, StatusManager.LOG);
                }
            }
        });
    }
    
}
