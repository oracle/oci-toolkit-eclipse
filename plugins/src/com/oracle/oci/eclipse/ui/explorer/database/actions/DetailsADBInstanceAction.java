/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceWrapper;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable.TablePair;
import com.oracle.oci.eclipse.ui.explorer.database.ADBConstants;
import com.oracle.oci.eclipse.ui.explorer.database.editor.ADBInstanceTable;

public class DetailsADBInstanceAction extends BaseAction {


    @SuppressWarnings("unused")
	private final ADBInstanceTable table;
    private final List<AutonomousDatabaseSummary> instanceSelectionList;
    private String instanceName;
    private String instanceID;
    private String title = "Autonomous Database Information";

    @SuppressWarnings("unchecked")
	public DetailsADBInstanceAction (ADBInstanceTable table){
        this.table = table;
        instanceSelectionList = (List<AutonomousDatabaseSummary>) table.getSelectedObjects();
    }

    @Override
    public String getText() {
        if ( instanceSelectionList.size() == 1 ) {
            return "Autonomous Database Information";
        }
        return "";
    }

    @Override
    protected void runAction() {
        if (instanceSelectionList.size() > 0) {
        	AutonomousDatabaseSummary object = instanceSelectionList.get(0);
            instanceName = object.getDisplayName();
            instanceID = object.getId();
        }
        new Job("Get ADB Instance Details") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    ADBInstanceWrapper adbInstance = ADBInstanceClient.getInstance().getInstanceDetails(instanceID);

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            List<TablePair> dataList = createDataList(instanceName, adbInstance);
                            DetailsTable detailsTable= new DetailsTable(title, dataList);
                            detailsTable.openTable();
                        }
                    });

                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to get Instance details: " + e.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();

    }

    protected List<TablePair> createDataList(String name, ADBInstanceWrapper instance) {
        List<TablePair> data = new ArrayList<TablePair>();
        data.add(new TablePair("Display Name", name));
        data.add(new TablePair("Database Name", instance.getDatabaseName()));
        data.add(new TablePair("Lifecycle State", instance.getLifeCycleState()));
        data.add(new TablePair("Dedicated Infrastructure", instance.getDedicatedInfra()));
        data.add(new TablePair("CPU Core Count", instance.getCPUCoreCount()));
        
        if(instance.isFreeTierInstance())
        	data.add(new TablePair("Storage (TB)", ADBConstants.ALWAYS_FREE_STORAGE_TB));
        else
        	data.add(new TablePair("Storage (TB)", instance.getDataStorageSizeInTBs()));
        
		if ("No".equals(instance.getDedicatedInfra())) {
			data.add(new TablePair("Auto Scaling", instance.getAutoScaling()));
			data.add(new TablePair("License Type", instance.getLicenseType()));
		} else {
			data.add(new TablePair("Container Database Id", instance.getAutonomousContainerDatabaseId()));
		}
        data.add(new TablePair("Workload Type", instance.getWorkloadType()));
        data.add(new TablePair("Created", instance.getTimeCreated()));
        data.add(new TablePair("Compartment", instance.getCompartment()));
        data.add(new TablePair("OCID", instance.getOCID()));
        data.add(new TablePair("Database Version", instance.getDatabaseVersion()));
        data.add(new TablePair("Tags", instance.getFreeformTags().toString()));
        data.add(new TablePair("Instance Type", instance.getInstanceType()));
        data.add(new TablePair("Is ACL Enabled", instance.isAclEnabledYesNo()));
        data.add(new TablePair("Is IP Allowlist Enabled", instance.isWhiteListedIpsYesNo()));
        data.add(new TablePair("mTLS Connections Required", instance.isMTLSRequiredAsYesNo()));
        data.add(new TablePair("IP Allowlist", StringUtils.join(instance.getWhiteListedIps())));

        return data;
    }
}
