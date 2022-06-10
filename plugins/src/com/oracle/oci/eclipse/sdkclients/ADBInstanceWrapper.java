/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.oracle.bmc.database.model.AutonomousDatabaseConnectionStrings;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;

public class ADBInstanceWrapper {

	private AutonomousDatabaseSummary instance;

	public ADBInstanceWrapper(AutonomousDatabaseSummary instance) {
		this.instance = instance;
	}

	public AutonomousDatabaseSummary getInstance() {
		return this.instance;
	}
	
	public String getOCID() {
		return instance.getId();
	}

	public String getDatabaseName() {
		return instance.getDbName();
	}

	public String getDisplayName() {
		return instance.getDisplayName();
	}

	public String getWorkloadType() {
		return instance.getDbWorkload().getValue();
	}

	public String getCompartment() {
		return instance.getCompartmentId();
	}

	public String getTimeCreated() {
		return instance.getTimeCreated().toGMTString();
	}

	public String getCPUCoreCount() {
		return instance.getCpuCoreCount().toString();
	}

	public String getDataStorageSizeInTBs() {
		return instance.getDataStorageSizeInTBs().toString();
	}

	public String getLicenseType() {
		if(instance.getIsDedicated() != null && instance.getIsDedicated())
			return null;
		
		return instance.getLicenseModel().getValue();
	}

	public String getDatabaseVersion() {
		return instance.getDbVersion();
	}

	public String getAutoScaling() {
		return instance.getIsAutoScalingEnabled() ? "Enabled" : "Disabled";
	}
	public com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState getLifecycleStateEnum() {
	    return instance.getLifecycleState();
	}
	public String getLifeCycleState() {
		return instance.getLifecycleState().getValue();
	}
	
	public Map<String, String> getFreeformTags() {
		return instance.getFreeformTags();
	}
	
	public String getDedicatedInfra() {
		return (instance.getIsDedicated() != null && instance.getIsDedicated()) ? "Yes" : "No";
	}
	
	public String getAutonomousContainerDatabaseId() {
		return instance.getAutonomousContainerDatabaseId();
	}
	
	public boolean isFreeTierInstance() {
		return instance.getIsFreeTier() != null && instance.getIsFreeTier();
	}
	
	public String getInstanceType() {
		return isFreeTierInstance() ? "Free" : "Paid";
	}
	
	public String getDatabaseConnectionStrings()
	{
		AutonomousDatabaseConnectionStrings connectionStrings = instance.getConnectionStrings();
		return connectionStrings.getAllConnectionStrings().toString();
	}

	public boolean isMTLSRequired()
	{
		 Boolean isMtlsConnectionRequired = instance.getIsMtlsConnectionRequired();
		 return isMtlsConnectionRequired == null ? false : isMtlsConnectionRequired.booleanValue();
	}
	
	public String isMTLSRequiredAsYesNo()
	{
		return isMTLSRequired() ? "Yes" : "No";
	}
	
	public boolean isAclEnabled()
	{
	    Boolean isAccessControlEnabled = instance.getIsAccessControlEnabled();
	    return isAccessControlEnabled != null && isAccessControlEnabled.booleanValue();
	}
	
	public String isAclEnabledYesNo()
	{
	    return isAclEnabled() ? "Yes" : "No";
	}

	public boolean isWhiteListedIps()
	{
	    return instance.getArePrimaryWhitelistedIpsUsed() != null 
	            && instance.getArePrimaryWhitelistedIpsUsed().booleanValue();
	}

	public String isWhiteListedIpsYesNo() {
        return isWhiteListedIps() ? "Yes" : "No";
    }

	public List<String> getWhiteListedIps()
	{
	    List<String> whitelistedIps = instance.getWhitelistedIps();
	    if (whitelistedIps != null)
	    {
	        return whitelistedIps;
	    }
	    return Collections.emptyList();
	}

}
