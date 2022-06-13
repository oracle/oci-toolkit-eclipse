/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.editor;

import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.ADMINPASSWORD;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.CHANGE_WORKLOAD_TYPE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.COPY_ADMIN_PASSWORD;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.CREATECLONE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.CREATECONNECTION;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.DOWNLOAD_CLIENT_CREDENTIALS;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.GET_CONNECTION_STRINGS;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.RESTART;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.RESTORE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.SCALEUPDOWN;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.SERVICE_CONSOLE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.START;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.STOP;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.TERMINATE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.UPDATELICENCETYPE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.UPDATE_ADB_ACCESS_CONTROL;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.UPGRADE_INSTANCE_TO_PAID;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.getSupportedADBActions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.DbWorkload;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.database.ADBConstants;
import com.oracle.oci.eclipse.ui.explorer.database.actions.ADBInstanceAction;
import com.oracle.oci.eclipse.ui.explorer.database.actions.CreateADBInstanceAction;
import com.oracle.oci.eclipse.ui.explorer.database.actions.DetailsADBInstanceAction;
import com.oracle.oci.eclipse.ui.explorer.database.actions.RefreshADBInstanceAction;
import com.oracle.oci.eclipse.ui.explorer.database.actions.RegisterDriverAction;

public class ADBInstanceTable  extends BaseTable {

    private int tableDataSize = 0;

    private static final int DISPLAY_NAME_COL = 0;
    private static final int DATABASE_NAME_COL = 1;
    private static final int STATE_COL = 2;
    private static final int ALWAYS_FREE_COL = 3;
    private static final int DEDICATED_INFRA_COL = 4;
    private static final int CPU_CORE_COUNT_COL = 5;
    private static final int STORAGE_IN_TB_COL= 6;
    private static final int WORKLOAD_TYPE_COL = 7;
    private static final int CREATED_DATE_COL = 8;

    private static final String WORKLOAD_DW = "Data Warehouse";
    private static final String WORKLOAD_OLTP = "Transaction Processing";
    private static final String WORKLOAD_AJD = "JSON Database";
    private static final String WORKLOAD_ALL = "All";
    Combo combo;

    private boolean disableCreateConnectionOption=true;

    List<AutonomousDatabaseSummary> instanceList = new ArrayList<AutonomousDatabaseSummary>();

    public ADBInstanceTable(Composite parent, int style) {
        super(parent, style);

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());

        try {
            Class.forName("org.eclipse.datatools.connectivity.ui.dse.views.DataSourceExplorerView");
            disableCreateConnectionOption = false;
            ErrorHandler.logInfo("Enabled create connection option because eclipse data tools plugin is installed.");
        } catch (ClassNotFoundException e) {
            disableCreateConnectionOption = true;
            ErrorHandler.logInfo("Disabled create connection option because eclipse data tools plugin is not installed.");
        }
    }

    @Override
    public List<AutonomousDatabaseSummary> getTableData() {
        return getTableData(combo.getText());
    }

    @Override
    public List<AutonomousDatabaseSummary> getTableCachedData() {
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
                final AutonomousDatabaseSummary s = (AutonomousDatabaseSummary) element;
                final boolean isFreeTier = s.getIsFreeTier() !=null && s.getIsFreeTier();

                switch (columnIndex) {
                case DISPLAY_NAME_COL:
                    return s.getDisplayName();
                case DATABASE_NAME_COL:
                    return s.getDbName();
                case STATE_COL:
                    return s.getLifecycleState().getValue();
                case ALWAYS_FREE_COL:
                    return isFreeTier ? "Yes" : "No";
                case DEDICATED_INFRA_COL:
                    return (s.getIsDedicated() !=null && s.getIsDedicated()) ? "Yes" : "No";
                case CPU_CORE_COUNT_COL:
                    return String.valueOf(s.getCpuCoreCount());
                case STORAGE_IN_TB_COL:
                    return isFreeTier ? ADBConstants.ALWAYS_FREE_STORAGE_TB : String.valueOf(s.getDataStorageSizeInTBs());
                case WORKLOAD_TYPE_COL:
                    return s.getDbWorkload().getValue();
                case CREATED_DATE_COL:
                    return s.getTimeCreated().toGMTString();
                }
            } catch (Exception e) {
                ErrorHandler.logErrorStack(e.getMessage(), e);
            }
            return "";
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            AutonomousDatabaseSummary s = (AutonomousDatabaseSummary) element;
            switch (columnIndex) {
            case STATE_COL:
                if (s.getLifecycleState().equals(LifecycleState.Available) || s.getLifecycleState().equals(LifecycleState.ScaleInProgress))
                    return Activator.getImage(Icons.DATABASE_AVAILABLE_STATE.getPath());
                else if(s.getLifecycleState().equals(LifecycleState.Terminated) || s.getLifecycleState().equals(LifecycleState.Unavailable))
                    return Activator.getImage(Icons.DATABASE_UNAVAILABLE_STATE.getPath());
                else
                    return Activator.getImage(Icons.DATABASE_INPROGRESS_STATE.getPath());
            }

            return null;
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
        createColumn(tableColumnLayout,tree, "Name", 18);
        createColumn(tableColumnLayout,tree, "Database Name", 15);
        createColumn(tableColumnLayout,tree, "State", 12);
        createColumn(tableColumnLayout,tree, "Always Free", 10);
        createColumn(tableColumnLayout,tree, "Dedicated Infrastructure", 14);
        createColumn(tableColumnLayout,tree, "CPU-Core", 10);
        createColumn(tableColumnLayout,tree, "Storage (TB)", 10);
        createColumn(tableColumnLayout,tree, "WorkLoad Type", 13);
        createColumn(tableColumnLayout,tree, "Created", 18);
    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        manager.add(new RefreshADBInstanceAction(ADBInstanceTable.this));
        manager.add(new Separator());

        manager.add(new CreateADBInstanceAction(ADBConstants.CREATE_ADW_INSTANCE, CreateAutonomousDatabaseBase.DbWorkload.Dw, ADBInstanceTable.this));
        manager.add(new CreateADBInstanceAction(ADBConstants.CREATE_ATP_INSTANCE, CreateAutonomousDatabaseBase.DbWorkload.Oltp, ADBInstanceTable.this));
        manager.add(new CreateADBInstanceAction(ADBConstants.CREATE_AJD_INSTANCE, CreateAutonomousDatabaseBase.DbWorkload.Ajd, ADBInstanceTable.this));
        manager.add(new Separator());

        if (getSelectedObjects().size() ==1) {
            final AutonomousDatabaseSummary db = (AutonomousDatabaseSummary)getSelectedObjects().get(0);
            final LifecycleState lifeCycleState = db.getLifecycleState();
            final boolean isDedicated = db.getIsDedicated() !=null && db.getIsDedicated();
            final boolean isFreeTier = db.getIsFreeTier() != null && db.getIsFreeTier();
            final boolean isAjd = db.getDbWorkload() == AutonomousDatabaseSummary.DbWorkload.Ajd;
            for (String action : getSupportedADBActions()) {
                if(enableAction(action, lifeCycleState, isDedicated, isFreeTier, isAjd))
                    manager.add(new ADBInstanceAction(ADBInstanceTable.this, action));
            }
            manager.add(new Separator());
            manager.add(new DetailsADBInstanceAction(ADBInstanceTable.this));
        }

        if(!disableCreateConnectionOption) {
            manager.add(new Separator());
            manager.add(new RegisterDriverAction(ADBConstants.REGISTER_DRIVER));
        }
    }

    @Override
    protected void addTableLabels(FormToolkit toolkit, Composite left, Composite right) {

        Label label = new Label(right, SWT.NULL);
        label.setText("Workload Type");
        toolkit.adapt(label, true, false);

        combo = new Combo(right, SWT.DROP_DOWN | SWT.READ_ONLY);
        toolkit.adapt(combo, true, false);
        combo.add(WORKLOAD_ALL);
        combo.add(WORKLOAD_DW);
        combo.add(WORKLOAD_OLTP);
        combo.add(WORKLOAD_AJD);

        // default value
        combo.select(0);

        combo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setInput(getTableData(combo.getText()));
                viewer.setItemCount(getTableDataSize());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Button refreshADBListButton = toolkit.createButton(right, "Refresh ADB List", SWT.PUSH);
        refreshADBListButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new RefreshADBInstanceAction(ADBInstanceTable.this).run();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }

    private List<AutonomousDatabaseSummary> getTableData(final String workloadType) {
        final AutonomousDatabaseSummary.DbWorkload workload;
        if(WORKLOAD_DW.equals(workloadType)) {
            workload = DbWorkload.Dw;
        } else if(WORKLOAD_OLTP.equals(workloadType)) {
            workload = DbWorkload.Oltp;
        } else if(WORKLOAD_AJD.equals(workloadType)) {
            workload = DbWorkload.Ajd;
        } else {
            workload = DbWorkload.UnknownEnumValue;
        }
        Job instanceListJob = new Job("Get ADB Instances") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    monitor.beginTask("Requesting ADB Instance", 100);
                    ADBInstanceClient oci = ADBInstanceClient.getInstance();
                    monitor.worked(5);
                    instanceList = oci.getInstances(workload);
                    monitor.worked(90);
                    tableDataSize = instanceList.size();
                    refresh(false);
                    monitor.worked(5);
                } catch (Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                finally {
                    if (!monitor.isCanceled()) {
                        monitor.done();
                    }
                }
                return Status.OK_STATUS;
            }

            @Override
            protected void canceling() {
                ErrorHandler.logInfo("User cancelled ADB instance list request");
            }
            
            
        };

        instanceListJob.schedule();
        return instanceList;
    }

    private boolean enableAction(final String action, final LifecycleState lifeCycleState,
            final boolean isDedicated, final boolean isFreeTier, boolean isAjd) {
        switch (action) {
        case START:
            return LifecycleState.Stopped.equals(lifeCycleState) ? true : false;
        case STOP:
            return LifecycleState.Stopped.equals(lifeCycleState) ? false : true;
        case CREATECLONE:
            return isDedicated ? false : true;
        case SCALEUPDOWN:
            return isFreeTier ? false : true;
        case UPDATELICENCETYPE:
            return (isFreeTier || isDedicated || isAjd) ? false : true;
        case ADMINPASSWORD:
            return true;
        case TERMINATE:
            return true;
        case DOWNLOAD_CLIENT_CREDENTIALS:
            return true;
        case CREATECONNECTION:
            return (disableCreateConnectionOption || LifecycleState.Stopped.equals(lifeCycleState)) ? false : true;
        case RESTORE:
            return isFreeTier ? false : true;
        case UPGRADE_INSTANCE_TO_PAID:
        	return isFreeTier ? true : false;
        case SERVICE_CONSOLE:
        	return true;
        case CHANGE_WORKLOAD_TYPE:
        	return isAjd ? true : false;
        case RESTART:
        	return true;
        case COPY_ADMIN_PASSWORD:
            return true;
        case GET_CONNECTION_STRINGS:
        	return true;
        case UPDATE_ADB_ACCESS_CONTROL:
            return true;
        }
        return false;
    }

}
