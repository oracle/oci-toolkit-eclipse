/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.compute.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.model.Vnic;
import com.oracle.bmc.core.model.VolumeAttachment;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ComputeInstanceClient;
import com.oracle.oci.eclipse.sdkclients.InstanceWrapper;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable.TablePair;
import com.oracle.oci.eclipse.ui.explorer.compute.editor.InstanceTable;
public class DetailsInstanceAction extends BaseAction {

    private final InstanceTable table;
    private final List<Instance> instanceSelectionList;
    private String instanceName;
    private String instanceID;
    private String title = "Instance Details";

    public DetailsInstanceAction (InstanceTable table){
        this.table = table;
        instanceSelectionList = (List<Instance>) table.getSelectedObjects();
    }

    @Override
    public String getText() {
        if ( instanceSelectionList.size() == 1 ) {
            return "Instance Details";
        }
        return "";
    }

    @Override
    protected void runAction() {
        if (instanceSelectionList.size() > 0) {
            Instance object = instanceSelectionList.get(0);
            instanceName = object.getDisplayName();
            instanceID = object.getId();
        }
        new Job("Get Instance Details") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    InstanceWrapper instance = ComputeInstanceClient.getInstance().getInstanceDetails(instanceID);

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            List<TablePair> dataList = createDataList(instanceName, instance);
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

    protected List<TablePair> createDataList(String name, InstanceWrapper instance) {
        List<TablePair> data = new ArrayList<TablePair>();
        data.add(new TablePair("Instance Name", name));
        data.add(new TablePair("Instance OCID", instance.getInstance().getId()));

        List<VolumeAttachment> volAttachIterable = instance.getVolumeAttachments();
        for (VolumeAttachment volumeAttachment : volAttachIterable) {
            data.add(new TablePair("Attached Volume", volumeAttachment.getDisplayName()));
        }
        try {
            Vnic vnic = instance.getVnic();
            if(vnic != null) {
                data.add(new TablePair("Public IP", vnic.getPublicIp()));
                data.add(new TablePair("Private IP", vnic.getPrivateIp()));
            }
        }
        catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);;
        }
        return data;
    }


}
