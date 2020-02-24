/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.compute.editor;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.oracle.bmc.core.model.Instance;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ComputeInstanceClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.compute.actions.DetailsInstanceAction;
import com.oracle.oci.eclipse.ui.explorer.compute.actions.InstanceAction;
import com.oracle.oci.eclipse.ui.explorer.compute.actions.RefreshInstanceAction;

public class InstanceTable extends BaseTable {
    private int tableDataSize = 0;

    private static final int NAME_COL = 0;
    private static final int STATE_COL = 1;
    private static final int SHAPE_COL = 2;
    private static final int AVL_DOMAIN_COL = 3;
    private static final int FLT_DOMAIN_COL = 4;
    public HashMap<String,String> actionMap = createActionMap();

    public InstanceTable(Composite parent, int style) {
        super(parent, style);

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
    }

    List<Instance> instanceList = new ArrayList<Instance>();
    @Override
    public List<Instance> getTableData() {
        new Job("Get Instances") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    ComputeInstanceClient oci = ComputeInstanceClient.getInstance();
                    instanceList = oci.getComputeInstances();
                    for (Iterator<Instance> it = instanceList.iterator(); it.hasNext(); ) {
                        Instance instance = it.next();
                        if (instance.getLifecycleState().getValue().equals("TERMINATED")) {
                            it.remove();
                        }
                    }
                    tableDataSize = instanceList.size();
                } catch (Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                refresh(false);
                return Status.OK_STATUS;
            }
        }.schedule();

        return instanceList;
    }

    @Override
    public List<Instance> getTableCachedData() {
        return instanceList;
    }

    @Override
    public int getTableDataSize() {
        return tableDataSize;
    }

    private final class TableLabelProvider extends BaseTableLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                Instance s = (Instance) element;

                switch (columnIndex) {
                case NAME_COL:
                    return s.getDisplayName();
                case STATE_COL:
                    return s.getLifecycleState().getValue();
                case SHAPE_COL:
                    return s.getShape();
                case AVL_DOMAIN_COL:
                    return s.getAvailabilityDomain();
                case FLT_DOMAIN_COL:
                    return s.getFaultDomain();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
        createColumn(tableColumnLayout,tree, "Name", 15);
        createColumn(tableColumnLayout,tree, "State", 6);
        createColumn(tableColumnLayout,tree, "Shape", 8);
        createColumn(tableColumnLayout,tree, "Availability Domain", 10);
        createColumn(tableColumnLayout,tree, "Fault Domain", 10);
    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        manager.add(new RefreshInstanceAction(InstanceTable.this));
        manager.add(new Separator());

        if (getSelectedObjects().size() > 0) {
            for (String action: actionMap.keySet()) {
                manager.add(new InstanceAction(InstanceTable.this, action));
            }
        }
        if (getSelectedObjects().size() == 1) {
            manager.add(new Separator());
            manager.add(new DetailsInstanceAction(InstanceTable.this));
        }
    }

    protected HashMap<String,String> createActionMap() {
        actionMap = new HashMap<String,String>();
        actionMap.put("Start","START");
        actionMap.put("Stop","STOP");
        actionMap.put("Soft Reboot","SOFTRESET");
        actionMap.put("Hard Reboot","RESET");
        actionMap.put("Shutdown","SOFTSTOP");
        return actionMap;
    }
}
