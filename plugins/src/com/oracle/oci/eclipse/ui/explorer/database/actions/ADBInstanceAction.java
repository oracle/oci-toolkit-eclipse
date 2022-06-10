/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.database.ADBConstants;
import com.oracle.oci.eclipse.ui.explorer.database.editor.ADBInstanceTable;

public class ADBInstanceAction  extends BaseAction {

    private final ADBInstanceTable table;
    private final List<AutonomousDatabaseSummary> instanceSelectionList;
    private final String action;


    @SuppressWarnings("unchecked")
    public ADBInstanceAction (ADBInstanceTable table, String action){
        this.table = table;
        this.instanceSelectionList = (List<AutonomousDatabaseSummary>)table.getSelectedObjects();
        this.action = action;
    }

    @Override
    public String getText() {
        return action;
    }

    @Override
    protected void runAction() {

        new Job("ADB Instance Action") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    for ( AutonomousDatabaseSummary instance : instanceSelectionList ) {
                        ADBInstanceClient.getInstance().runADBInstanceAction(instance, action);
                    }
                    // No need to refresh the table after download wallet and create connection op
					if (!(action.equals(ADBConstants.DOWNLOAD_CLIENT_CREDENTIALS) 
							|| action.equals(ADBConstants.CREATECONNECTION)
							|| action.equals(ADBConstants.SERVICE_CONSOLE))) {
	                    Display.getDefault().asyncExec(new Runnable() {
	                        @Override
	                        public void run() {
	                            table.refresh(true);
	                        }
	                    });
                    }
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to do action: " + e.getMessage(), e);
                }
            }
        }.schedule();
    }
}
