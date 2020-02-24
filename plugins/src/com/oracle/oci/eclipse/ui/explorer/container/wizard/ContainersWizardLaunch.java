/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.wizard;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.containerengine.model.ClusterSummary;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersTable;

public class ContainersWizardLaunch extends BaseAction {

    private final ContainerClustersTable table;
    private final List<ClusterSummary> clusterSelectionList;
    private final String actionName;

    public ContainersWizardLaunch (ContainerClustersTable table, String action){
        this.table = table;
        this.clusterSelectionList = (List<ClusterSummary>)table.getSelectedObjects();
        this.actionName = action;
    }

    @Override
    public String getText() {
        return actionName;
    }

    @Override
    protected void runAction() {
        if(!table.getSelectedObjects().isEmpty()){
            WizardDialog dlg = new WizardDialog(Display.getDefault()
                    .getActiveShell(),  new ContainersConfigurationWizard(table));
            if (dlg.open() != Dialog.OK) {}
        }
    }
}
