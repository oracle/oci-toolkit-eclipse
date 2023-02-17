/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class ADBConstants {
	public static final String ALWAYS_FREE_STORAGE_TB = "0.02";
	public static final int ALWAYS_FREE_CPU_CORE_COUNT = 1;
	public static final String ALWAYS_FREE_STORAGE_TB_DUMMY = "1";
	
	public static final int CPU_CORE_COUNT_MIN = 1;
	public static final int CPU_CORE_COUNT_MAX = 128;
	public static final int CPU_CORE_COUNT_DEFAULT = 1;
	public static final int CPU_CORE_COUNT_INCREMENT = 1;
	
	public static final int STORAGE_IN_TB_MIN = 1;
	public static final int STORAGE_IN_TB_MAX = 128;
	public static final int STORAGE_IN_TB_DEFAULT = 1;
	public static final int STORAGE_IN_TB_INCREMENT = 1;
	
	public static final String INSTANCE_WALLET = "Instance Wallet";
	public static final String REGIONAL_WALLET = "Regional Wallet";
	
	/* ADB Actions */
	
	public static final String CREATE_ADW_INSTANCE = "Create ADW Instance";
	public static final String CREATE_ATP_INSTANCE = "Create ATP Instance";
	public static final String CREATE_AJD_INSTANCE = "Create AJD Instance";
	public static final String CREATE_APEX_INSTANCE = "Create APEX Instance";

	public static final String REGISTER_DRIVER = "Register JDBC Driver";
			
	public static final String START = "Start";
	public static final String STOP = "Stop";
	public static final String CREATECLONE = "Create Clone";
	public static final String SCALEUPDOWN = "Scale Up/Down";
	public static final String UPDATELICENCETYPE = "Update Licence Type";
	public static final String ADMINPASSWORD = "Admin Password";
	public static final String TERMINATE = "Terminate";
	public static final String DOWNLOAD_CLIENT_CREDENTIALS = "Download Client Credentials (Wallet)";
	public static final String CREATECONNECTION = "Create Connection";
	public static final String RESTORE = "Restore";
	public static final String UPGRADE_INSTANCE_TO_PAID = "Upgrade Instance to Paid";
	public static final String SERVICE_CONSOLE = "Service Console";
	public static final String CHANGE_WORKLOAD_TYPE = "Change Workload Type";
	public static final String RESTART = "Restart";
	public static final String COPY_ADMIN_PASSWORD = "Copy Admin Password";
	public static final String GET_CONNECTION_STRINGS = "Get TNS Connection Strings";
	public static final String UPDATE_ADB_ACCESS_CONTROL = "Update Access Control";
	
	private static final Set<String> ACTION_SET = new TreeSet<String>();
	static {
		ACTION_SET.add(START);
		ACTION_SET.add(STOP);
		ACTION_SET.add(CREATECLONE);
		ACTION_SET.add(SCALEUPDOWN);
		ACTION_SET.add(UPDATELICENCETYPE);
		ACTION_SET.add(ADMINPASSWORD);
		ACTION_SET.add(COPY_ADMIN_PASSWORD);
		ACTION_SET.add(GET_CONNECTION_STRINGS);
		ACTION_SET.add(UPDATE_ADB_ACCESS_CONTROL);
		ACTION_SET.add(TERMINATE);
		ACTION_SET.add(DOWNLOAD_CLIENT_CREDENTIALS);
		ACTION_SET.add(CREATECONNECTION);
		ACTION_SET.add(RESTORE);
		ACTION_SET.add(UPGRADE_INSTANCE_TO_PAID);
		ACTION_SET.add(SERVICE_CONSOLE);
		ACTION_SET.add(CHANGE_WORKLOAD_TYPE);
		ACTION_SET.add(RESTART);
	}
	
	public static Set<String> getSupportedADBActions() {
		return Collections.unmodifiableSet(ACTION_SET);
	}

}
