/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.oracle.bmc.containerengine.model.ClusterSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ContainerClustersClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.container.actions.ContainerClustersAction;
import com.oracle.oci.eclipse.ui.explorer.container.wizard.ContainersWizardLaunch;

public class ContainerClustersTable extends BaseTable {
    private int tableDataSize = 0;

    private static final int NAME_COL = 0;
    private static final int STATE_COL = 1;
    private static final int VCN_COL = 2;

    public ContainerClustersTable(Composite parent, int style) {
        super(parent, style);

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
    }
    List<ClusterSummary> clustersList = new ArrayList<ClusterSummary>();
    @Override
    public List<ClusterSummary> getTableData() {
        new Job("Get Clusters") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    clustersList = ContainerClustersClient.getInstance().listClusters();
                    for (Iterator<ClusterSummary> it = clustersList.iterator(); it.hasNext(); ) {
                        ClusterSummary instance = it.next();
                        if (instance.getLifecycleState().getValue().toLowerCase().equals("deleted")) {
                            it.remove();
                        }
                    }
                    tableDataSize = clustersList.size();
                } catch (Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                refresh(false);
                return Status.OK_STATUS;
            }
        }.schedule();
        return clustersList;
    }
    @Override
    public List<ClusterSummary> getTableCachedData() {
        return clustersList;
    }

    @Override
    public int getTableDataSize() {
        return tableDataSize;
    }

    /* Label provider */
    private final class TableLabelProvider extends BaseTableLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                ClusterSummary s = (ClusterSummary) element;

                switch (columnIndex) {
                case NAME_COL:
                    return s.getName();
                case STATE_COL:
                    return s.getLifecycleState().name();
                case VCN_COL:
                    return s.getVcnId();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
        createColumn(tableColumnLayout,tree, "Cluster Name", 10);
        createColumn(tableColumnLayout,tree, "State", 4);
        createColumn(tableColumnLayout,tree, "VCN", 10);
    }

    @Override
    protected void fillMenu(IMenuManager manager) {


        if (getSelectedObjects().size() > 0) {
            manager.add(new ContainersWizardLaunch(ContainerClustersTable.this, "Deploy Docker Image to OKE"));
            manager.add(new Separator());
            manager.add(new ContainerClustersAction(ContainerClustersTable.this, "Download Kubernetes Config", false));
        }
    }
}
