/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.actions;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.oracle.bmc.containerengine.model.ClusterSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.SystemPropertiesUtils;
import com.oracle.oci.eclipse.sdkclients.ContainerClustersClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersTable;


public class ContainerClustersAction extends BaseAction {

    private final ContainerClustersTable table;
    private final List<ClusterSummary> clusterSelectionList;
    private final String action;

    String fullFilePath = null;
    String downloadPath = null;
    String fileName = null;
    ClusterSummary object = null;

    Boolean override = false;

    // Dialog text
    String message = "File already exists. Do you want to replace it?";
    String title = "Confirm File Replace";

    public ContainerClustersAction (ContainerClustersTable table, String action, Boolean override){
        this.table = table;
        this.action = action;
        this.override = override;
        this.clusterSelectionList = (List<ClusterSummary>)table.getSelectedObjects();
    }

    @Override
    public String getText() {
        return action;
    }

    public static final String DEFAULT_KUBE_CONFIG_FOLDER_PATH = ".kube";
    public static String getKubeConfigFilePath() {
        return SystemPropertiesUtils.userHome()+ File.separator + DEFAULT_KUBE_CONFIG_FOLDER_PATH;
    }


    @Override
    protected void runAction() {

        try {
            // Download selected cluster, the dialog can change the download fileName.
            if ( clusterSelectionList.size() >= 1 ){
                object = clusterSelectionList.get(0);
                String pathResult = getKubeConfigFilePath() + File.separator + "config";
                if(!override) {
                    FileDialog dialog = new FileDialog ( Display.getDefault().getActiveShell(), SWT.SAVE );
                    dialog.setFilterPath(getKubeConfigFilePath());
                    dialog.setFileName("config");
                    pathResult = dialog.open();
                    if (pathResult == null) {
                        return;
                    }
                }
                File file = new File(pathResult);
                if (file.exists() && !override) {
                    Dialog confirmDialog =  new MessageDialog(Display.getDefault().getActiveShell(), title , null, message , MessageDialog.WARNING, new String[] {"Yes","No"}, 1);
                    if (confirmDialog.open() != Dialog.OK) {
                        return;
                    }
                }
                downloadPath = file.getParent();
                fileName = file.getName();
                fullFilePath = downloadPath + File.separator + fileName;
            }
        } catch(Exception e) {
            ErrorHandler.reportException(e.getMessage(), e);
        }
        // Canceled
        if (object == null || downloadPath == null) {
            return;
        }

        new Job("Cluster Action") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    ContainerClustersClient.getInstance().createKubeconfig(object.getId(), fullFilePath);
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