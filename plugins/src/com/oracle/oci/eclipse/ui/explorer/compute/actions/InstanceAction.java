/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.compute.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.core.model.Instance;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ComputeInstanceClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.compute.editor.InstanceTable;

public class InstanceAction extends BaseAction {

    private final InstanceTable table;
    private final List<Instance> instanceSelectionList;
    private final String action;
    private final String actionOCIValue;


    public InstanceAction (InstanceTable table, String action){
        this.table = table;
        this.instanceSelectionList = (List<Instance>)table.getSelectedObjects();
        this.action = action;
        this.actionOCIValue = table.actionMap.get(action);
    }

    @Override
    public String getText() {
        return action;
    }

    @Override
    protected void runAction() {

        new Job("Instance Action") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    for ( Instance instance : instanceSelectionList ) {
                        ComputeInstanceClient.getInstance().runInstanceAction(instance.getId(), actionOCIValue);
                    }
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            table.refresh(true);
                        }
                    });
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to do action: " + e.getMessage(), e);
                }
            }
        }.schedule();
    }
}
