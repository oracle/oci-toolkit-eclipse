package com.oracle.oci.eclipse.ui.explorer.database;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.DbWorkload;

public class CreateADBAdvancedWizardPage extends WizardPage {

    private List<IPAddressType> ipConfigs = new ArrayList<>();
    private TableColumn ipTypeColumn;
    private TableColumn valuesColumn;
    private TableColumn optionValuesColumn;
    private Table configureAnywhereTable;
    private TableColumn privateVCN;
    private TableColumn privateVCNCompartment;
    private Table privateEndpointTable;

    public CreateADBAdvancedWizardPage(ISelection selection, DbWorkload workloadType) {
        super("advancedWizardPage");
        setTitle("Advanced Autonomous Database Create Options");
        setDescription("Set advanced configurations (defaults are usually fine to start)");
        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);

        Composite networkSetupContainer = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 2;
        networkSetupContainer.setLayout(innerTopLayout);
        networkSetupContainer.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        
        TabFolder connectionTypeFolder = new TabFolder(networkSetupContainer, SWT.TOP | SWT.MULTI);
        connectionTypeFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        TabItem secureFromEverywhere = new TabItem(connectionTypeFolder, SWT.NONE);
        secureFromEverywhere.setText("Secure From Anywhere");
        Composite secureFromEverywhereTabComp = new Composite(connectionTypeFolder, SWT.BORDER);
        secureFromEverywhereTabComp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        secureFromEverywhere.setControl(secureFromEverywhereTabComp);
        createSecureFromEverywhere(secureFromEverywhereTabComp);
        
        TabItem privateEndpoint = new TabItem(connectionTypeFolder, SWT.NONE);
        privateEndpoint.setText("Private Endpoint");
        Composite privateNetworkTabComp = new Composite(connectionTypeFolder, SWT.BORDER);
        privateNetworkTabComp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        privateEndpoint.setControl(privateNetworkTabComp);
        createPrivateEndpoint(privateNetworkTabComp);
        
        setControl(container);

    }

    private void createSecureFromEverywhere(final Composite secureFromEverywhereTabComp) {
        secureFromEverywhereTabComp.setLayout(new GridLayout(2, false));
        Label explainMlsLabel = new Label(secureFromEverywhereTabComp, SWT.NONE);
        explainMlsLabel.setText("To enable one-way TLS (walletless mode) you must click on 'Configure access control rules'");
        GridDataFactory.defaultsFor(explainMlsLabel).span(2,1).applyTo(explainMlsLabel);

        Button configureSecurityCheckbox = new Button(secureFromEverywhereTabComp, SWT.CHECK);
        configureSecurityCheckbox.setText("Configure access control rules");
        GridDataFactory.defaultsFor(configureSecurityCheckbox).span(2,1).applyTo(configureSecurityCheckbox);
        
        ToolBar actionPanel = new ToolBar(secureFromEverywhereTabComp, SWT.NONE);
        GridDataFactory.swtDefaults().grab(true,false).align(SWT.END, SWT.END).span(2,1).applyTo(actionPanel);
        ToolItem addItem = new ToolItem(actionPanel, SWT.PUSH);
        addItem.setText("Add");
        ToolItem rmItem = new ToolItem(actionPanel, SWT.PUSH);
        rmItem.setText("Remove");

        TableViewer viewer = new TableViewer(secureFromEverywhereTabComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        this.configureAnywhereTable = viewer.getTable();
        configureAnywhereTable.setHeaderVisible(true);
        configureAnywhereTable.setLinesVisible(true);
        configureAnywhereTable.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2,1).create());
        this.ipTypeColumn = new TableColumn(configureAnywhereTable, SWT.NONE);
        ipTypeColumn.setText("IP Notation");
        this.valuesColumn = new TableColumn(configureAnywhereTable, SWT.NONE);
        valuesColumn.setText("Values");
        this.optionValuesColumn = new TableColumn(configureAnywhereTable, SWT.NONE);
        optionValuesColumn.setText("Optional");

        viewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List<?>)
                {
                    return ((List<?>)inputElement).toArray();
                }
                return new Object[0];
            }
        });

        viewer.setLabelProvider(new LabelProvider());
        addItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IPAddressType addressType = new IPAddressType();
                addressType.setIpAddress("IP Address");
                addressType.setValues("");
                ipConfigs.add(addressType);
                viewer.refresh();
            }
        });

        rmItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = viewer.getStructuredSelection();
                Object firstElement = selection.getFirstElement();
                ipConfigs.remove(firstElement);
                viewer.refresh();
            }
        });

        viewer.setInput(ipConfigs);
        this.ipTypeColumn.pack();
        this.valuesColumn.pack();
        this.optionValuesColumn.pack();

        // disable if button not checked
        actionPanel.setEnabled(false);
        configureAnywhereTable.setEnabled(false);
        configureSecurityCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionPanel.setEnabled(configureSecurityCheckbox.getSelection());
                configureAnywhereTable.setEnabled(configureSecurityCheckbox.getSelection());
            }
        });
    }

    private void createPrivateEndpoint(Composite privateNetworkTabComp) {
        privateNetworkTabComp.setLayout(new GridLayout(3, false));
        
        GridData inputGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        
        Label vcnLabel = new Label(privateNetworkTabComp, SWT.NONE);
        vcnLabel.setText("Virtual cloud network:");
        CompartmentLabelLink vcnLink = new CompartmentLabelLink(privateNetworkTabComp);
        vcnLink.setText("Compartment: <a>cbateman</a>");
        Combo vcnCombo = new Combo(privateNetworkTabComp, SWT.DROP_DOWN);
        vcnCombo.setLayoutData(inputGridData);
        
        Label subnetLbl = new Label(privateNetworkTabComp, SWT.NONE);
        subnetLbl.setText("Subnet:");
        CompartmentLabelLink subnetLink = new CompartmentLabelLink(privateNetworkTabComp);
        subnetLink.setText("Compartment: <a>cbateman</a>");
        Combo subnetCombo = new Combo(privateNetworkTabComp, SWT.DROP_DOWN);
        subnetCombo.setLayoutData(inputGridData);
        
        Label hostnamePrefixLbl = new Label(privateNetworkTabComp, SWT.NONE);
        hostnamePrefixLbl.setText("Host name prefix");
        CompartmentLabelLink hostnameLink = new CompartmentLabelLink(privateNetworkTabComp);
        hostnameLink.setText("Compartment: <a>cbateman</a>");
        Text hostnamePrefix = new Text(privateNetworkTabComp, SWT.NONE);
        hostnamePrefix.setLayoutData(inputGridData);

        Group networkSecGroups  = new Group(privateNetworkTabComp, SWT.NONE);
        networkSecGroups.setText("Network Security Groups");
        networkSecGroups.setLayout(new GridLayout(1, false));
        GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL|GridData.FILL_BOTH);
        layoutData.horizontalSpan = 3;
        networkSecGroups.setLayoutData(layoutData);

        TableViewer viewer = new TableViewer(networkSecGroups, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        this.privateEndpointTable = viewer.getTable();
        privateEndpointTable.setHeaderVisible(true);
        privateEndpointTable.setLinesVisible(true);
        privateEndpointTable.setLayoutData(GridDataFactory.defaultsFor(configureAnywhereTable).align(SWT.FILL, SWT.FILL).grab(true, true).create());
        this.privateVCN = new TableColumn(configureAnywhereTable, SWT.NONE);
        privateVCN.setText("Virtual Cloud Network");
        this.privateVCNCompartment = new TableColumn(configureAnywhereTable, SWT.NONE);
        privateVCNCompartment.setText("Compartment");

        this.privateVCN.pack();
        this.privateVCNCompartment.pack();

        viewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List<?>)
                {
                    return ((List<?>)inputElement).toArray();
                }
                return new Object[0];
            }
        });
    }

    private static class LabelProvider implements ITableLabelProvider
    {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IPAddressType)
            {
                switch(columnIndex)
                {
                case 0:
                    return ((IPAddressType)element).getIpAddress();
                case 1:
                    return ((IPAddressType)element).getValues();
                case 2: 
                    return ((IPAddressType)element).getOptional();
                }
            }
            return "";
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void dispose() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // TODO Auto-generated method stub
            
        }
        
    }
    private static class IPAddressType
    {
        private String ipAddress;
        private String values;
        private String optional;
        public String getIpAddress() {
            return ipAddress;
        }
        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
        public String getValues() {
            return values;
        }
        public void setValues(String values) {
            this.values = values;
        }
        public String getOptional() {
            return optional;
        }
        @SuppressWarnings("unused")
        public void setOptional(String optional) {
            this.optional = optional;
        }
    }
    
    private static class CompartmentLabelLink
    {
        private Link compartmentLink;

        public CompartmentLabelLink(final Composite parent)
        {
            Composite linkComposite = new Composite(parent, SWT.NONE);
            linkComposite.setLayout(new GridLayout(1,false));
            this.compartmentLink = new Link(linkComposite, SWT.NONE);
        }
        
        public void setText(String text)
        {
            this.compartmentLink.setText(text);
        }
    }
}
