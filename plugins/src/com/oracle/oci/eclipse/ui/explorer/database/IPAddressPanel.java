package com.oracle.oci.eclipse.ui.explorer.database;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.ui.explorer.common.Utils;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlRowHolder;
import com.oracle.oci.eclipse.ui.explorer.database.model.IPAddressType;
import com.oracle.oci.eclipse.ui.explorer.database.provider.AclTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.database.provider.EditingSupportFactory.IPTypeColumnEditingSupport;
import com.oracle.oci.eclipse.ui.explorer.database.provider.EditingSupportFactory.IPValueColumnEditingSupport;
import com.oracle.oci.eclipse.ui.explorer.database.provider.PropertyListeningArrayList;

public class IPAddressPanel {

    private ToolBar actionPanelIpAddress;
    private TableViewer ipAddressAclTableViewer;
    private Table configureAnywhereTable;
    private TableViewerColumn ipTypeColumn;
    private TableViewerColumn valuesColumn;
    private PropertyListeningArrayList<AccessControlRowHolder> ipConfigs;
    private Group parentControl;
 
    public Control createControls(Composite parent)
    {
        this.parentControl = new Group(parent, SWT.NONE);
        parentControl.setText("Secure By IP Address");
        parentControl.setLayout(new GridLayout(2, false));
        GridData layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        layoutData.horizontalSpan = 2;
        parentControl.setLayoutData(layoutData);

        this.actionPanelIpAddress = new ToolBar(parentControl, SWT.NONE);
        GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.END).span(2, 1)
                .applyTo(actionPanelIpAddress);
        ToolItem addItem = new ToolItem(actionPanelIpAddress, SWT.PUSH);
        addItem.setToolTipText("Add");
        addItem.setImage(Activator.getImage(Icons.ADD.getPath()));
        ToolItem rmItem = new ToolItem(actionPanelIpAddress, SWT.PUSH);
        rmItem.setImage(Activator.getImage(Icons.DELETE.getPath()));
        rmItem.setToolTipText("Remove");

        Composite tableLayout = new Composite(parentControl, SWT.NONE);
        TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableLayout.setLayout(tableColumnLayout);
        tableLayout.setLayoutData(layoutData);

        this.ipAddressAclTableViewer = new TableViewer(tableLayout, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        this.configureAnywhereTable = ipAddressAclTableViewer.getTable();
        configureAnywhereTable.setHeaderVisible(true);
        configureAnywhereTable.setLinesVisible(true);
        configureAnywhereTable.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create());
        this.ipTypeColumn = Utils.createColumn(this.ipAddressAclTableViewer, "IP Notation");
        Utils.setColumnLayout(ipTypeColumn, tableColumnLayout, 30);
        ipTypeColumn.setEditingSupport(new IPTypeColumnEditingSupport(ipTypeColumn.getViewer()));

        this.valuesColumn = Utils.createColumn(ipAddressAclTableViewer, "Value");
        Utils.setColumnLayout(valuesColumn, tableColumnLayout, 70);
        valuesColumn.setEditingSupport(new IPValueColumnEditingSupport(ipAddressAclTableViewer));

        ipAddressAclTableViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List<?>) {
                    return ((List<?>) inputElement).toArray();
                }
                return new Object[0];
            }
        });

        ipAddressAclTableViewer.setLabelProvider(new AclTableLabelProvider());

        addItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IPAddressType addressType = new IPAddressType("");
                ipConfigs.add(new AccessControlRowHolder(addressType, true));
                ipAddressAclTableViewer.refresh(true);
            }
        });

        rmItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = ipAddressAclTableViewer.getStructuredSelection();
                Object firstElement = selection.getFirstElement();
                ipConfigs.remove(firstElement);
                ipAddressAclTableViewer.refresh(true);
            }
        });
        
        return parentControl;
    }

    public void setEnabled(boolean enabled) {
        actionPanelIpAddress.setEnabled(enabled);
        this.ipAddressAclTableViewer.getTable().setEnabled(enabled);
    }


    public void refresh(boolean updateLabels) {
        this.ipAddressAclTableViewer.refresh(updateLabels);
    }

    public void setInput(PropertyListeningArrayList<AccessControlRowHolder> ipConfigs) {
        this.ipConfigs = ipConfigs;  
        this.ipAddressAclTableViewer.setInput(ipConfigs);
    }

    public PropertyListeningArrayList<AccessControlRowHolder> getIpConfigs() {
        return this.ipConfigs;
    }

    public Control getControl() {
        return this.parentControl;
    }
    
    public MultiStatus validate() {
        MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, -1, null, null);
        for (AccessControlRowHolder source : this.getIpConfigs()) {
            String validation = source.getAclType().isValueValid();
            if (validation != null) {
                IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, validation, null);
                multiStatus.add(status);
            }
        }
        return multiStatus;
    }
}
