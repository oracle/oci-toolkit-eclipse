package com.oracle.oci.eclipse.ui.explorer.database;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceWrapper;
import com.oracle.oci.eclipse.sdkclients.NetworkClient;
import com.oracle.oci.eclipse.ui.explorer.common.Utils;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlRowHolder;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlType.Category;
import com.oracle.oci.eclipse.ui.explorer.database.model.OcidBasedAccessControlType;
import com.oracle.oci.eclipse.ui.explorer.database.provider.EditingSupportFactory;
import com.oracle.oci.eclipse.ui.explorer.database.provider.PropertyListeningArrayList;
import com.oracle.oci.eclipse.ui.explorer.database.provider.VCNTableLabelProvider;

public class UpdateADBAccessControlWizardPage extends WizardPage {

    private final AutonomousDatabaseSummary instance;

    private static final String HTTP_MORE_INFO_LINK = "https://docs.oracle.com/en/cloud/paas/autonomous-database/adbsa/access-control-rules-autonomous.html#GUID-483CD2B4-5898-4D27-B74E-6735C32CB58C";

    private PropertyListeningArrayList<AccessControlRowHolder> vcnConfigs = new PropertyListeningArrayList<>();

    private Table configureAnywhereTable;
    private TableColumn privateVCN;
    private TableColumn privateVCNCompartment;
    private Table privateEndpointTable;

    private Button configureSecurityCheckbox;
    private Button btnIsMLTSRequired;
    private ToolBar actionPanelVCN;
    private TableViewer vcnAclTableViewer;

    private TableViewerColumn vcnDisplayNameColumn;
    private TableViewerColumn vcnOcidColumn;
    private TableViewerColumn vcnIPRestrictions;

    private IPAddressPanel ipAddressPanel;

    public UpdateADBAccessControlWizardPage(AutonomousDatabaseSummary instance) {
        super("wizardPage");
        setTitle("Update Autonomous Database Access Control");
        setDescription("");
        this.instance = instance;
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
        networkSetupContainer
                .setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        TabFolder connectionTypeFolder = new TabFolder(networkSetupContainer, SWT.TOP | SWT.MULTI);
        connectionTypeFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem secureFromEverywhere = new TabItem(connectionTypeFolder, SWT.NONE);
        secureFromEverywhere.setText("Secure From Anywhere");
        Composite secureFromEverywhereTabComp = new Composite(connectionTypeFolder, SWT.BORDER);

        secureFromEverywhereTabComp
                .setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        secureFromEverywhere.setControl(secureFromEverywhereTabComp);
        createSecureFromEverywhere(secureFromEverywhereTabComp);

//        TabItem privateEndpoint = new TabItem(connectionTypeFolder, SWT.NONE);
//        privateEndpoint.setText("Private Endpoint");
//        Composite privateNetworkTabComp = new Composite(connectionTypeFolder, SWT.BORDER);
//        privateNetworkTabComp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
//        privateEndpoint.setControl(privateNetworkTabComp);
//        createPrivateEndpoint(privateNetworkTabComp);

        setControl(container);

    }

    private void createSecureFromEverywhere(final Composite secureFromEverywhereTabComp) {
        secureFromEverywhereTabComp.setLayout(new GridLayout(2, false));
        Label explainMlsLabel = new Label(secureFromEverywhereTabComp, SWT.WRAP);
        explainMlsLabel.setText(
                "Specify the IP addresses and VCNs allowed to access this database. An access control list blocks all IP addresses that are not in the list from accessing the database.");
        GridDataFactory.defaultsFor(explainMlsLabel).span(2, 1).applyTo(explainMlsLabel);

        Link moreInfoLink = new Link(secureFromEverywhereTabComp, SWT.NONE);
        moreInfoLink.setText(
            String.format("For additional information (opens browser): <a href=\"%s\">See here</a>", HTTP_MORE_INFO_LINK));
        GridDataFactory.defaultsFor(explainMlsLabel).span(2, 1).applyTo(moreInfoLink);
        moreInfoLink.addSelectionListener(new SelectionAdapter()  {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IWebBrowser browser;
                try {
                    browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
                    browser.openURL(new URL(HTTP_MORE_INFO_LINK));
                } catch (PartInitException | MalformedURLException e1) {
                    ErrorHandler.logErrorStack("Error opening browser", e1);
                }
            }
        });
        this.configureSecurityCheckbox = new Button(secureFromEverywhereTabComp, SWT.CHECK);
        configureSecurityCheckbox.setText("Configure access control rules");
        GridDataFactory.defaultsFor(configureSecurityCheckbox).span(2, 1).applyTo(configureSecurityCheckbox);

        this.btnIsMLTSRequired = new Button(secureFromEverywhereTabComp, SWT.CHECK);
        btnIsMLTSRequired.setText("Require mutual TLS (mTLS) authentication");
        btnIsMLTSRequired.setToolTipText(
                "If you select this option, mTLS will be required to authenticate connections to your Autonomous Database.");

        final SashForm sashForm = new SashForm(secureFromEverywhereTabComp, SWT.BORDER | SWT.VERTICAL | SWT.SMOOTH);
        GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(sashForm);
        sashForm.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

        // create toolbar and table for ip address acls
        createVCNPanel(sashForm);

        createIPAddressPanel(sashForm);

        populateAccessFromAnywhere(this.instance);

        // add listener after initialization.
        configureSecurityCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateAclUpdate(configureSecurityCheckbox.getSelection());
            }
        });

        btnIsMLTSRequired.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateStatus();
            }
        });
    }

    private void createIPAddressPanel(final Composite secureFromEverywhereTabComp) {
        this.ipAddressPanel = new IPAddressPanel();
        this.ipAddressPanel.createControls(secureFromEverywhereTabComp);
    }

    private void createVCNPanel(final Composite parent) {
        Group ipAddressPanel = new Group(parent, SWT.NONE);
        ipAddressPanel.setText("Secure By VCN");
        ipAddressPanel.setLayout(new GridLayout(2, false));
        GridData layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        layoutData.horizontalSpan = 2;
        ipAddressPanel.setLayoutData(layoutData);

        this.actionPanelVCN = new ToolBar(ipAddressPanel, SWT.NONE);
        GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.END).span(2, 1).applyTo(actionPanelVCN);
        ToolItem addItem = new ToolItem(actionPanelVCN, SWT.PUSH);
        addItem.setImage(Activator.getImage(Icons.ADD.getPath()));
        addItem.setToolTipText("Add");
        ToolItem rmItem = new ToolItem(actionPanelVCN, SWT.PUSH);
        rmItem.setImage(Activator.getImage(Icons.DELETE.getPath()));
        rmItem.setToolTipText("Remove");

        Composite tableLayout = new Composite(ipAddressPanel, SWT.NONE);
        TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableLayout.setLayout(tableColumnLayout);
        tableLayout.setLayoutData(layoutData);

        this.vcnAclTableViewer = new TableViewer(tableLayout, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        this.configureAnywhereTable = vcnAclTableViewer.getTable();
        configureAnywhereTable.setHeaderVisible(true);
        configureAnywhereTable.setLinesVisible(true);
        configureAnywhereTable.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create());

        this.vcnDisplayNameColumn = Utils.createColumn(vcnAclTableViewer, "Display Name");
        Utils.setColumnLayout(vcnDisplayNameColumn, tableColumnLayout, 25);
        vcnDisplayNameColumn
            .setEditingSupport(new EditingSupportFactory.VcnDisplayNameColumnEditingSupport(vcnAclTableViewer));

        this.vcnIPRestrictions = Utils.createColumn(vcnAclTableViewer, "IP Restrictions (Optional)");
        Utils.setColumnLayout(vcnIPRestrictions, tableColumnLayout, 25);
        this.vcnIPRestrictions
            .setEditingSupport(new EditingSupportFactory.VcnIPRestrictionColumnEditingSupport(vcnAclTableViewer));

        this.vcnOcidColumn = Utils.createColumn(vcnAclTableViewer, "Ocid");
        Utils.setColumnLayout(vcnOcidColumn, tableColumnLayout, 50);
        this.vcnOcidColumn.setEditingSupport(new EditingSupportFactory.VcnOcidColumnEditingSupport(vcnAclTableViewer));

        vcnAclTableViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List<?>) {
                    return ((List<?>) inputElement).toArray();
                }
                return new Object[0];
            }
        });

        vcnAclTableViewer.setLabelProvider(new VCNTableLabelProvider());
        addItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                OcidBasedAccessControlType addressType = new OcidBasedAccessControlType("", Collections.emptyList());
                AccessControlRowHolder e2 = new AccessControlRowHolder(addressType, false);
                e2.setNew(true);
                vcnConfigs.add(e2);
                vcnAclTableViewer.refresh();
                e2.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("ocid".equals(evt.getPropertyName())) {
                            NetworkClient networkClient = new NetworkClient();
                            Job.create("Load VCN", new ICoreRunnable() {
                                @Override
                                public void run(IProgressMonitor monitor) throws CoreException {
                                    Vcn vcn = networkClient.getVcn((String) evt.getNewValue());
                                    if (vcn != null) {
                                        ((OcidBasedAccessControlType) evt.getSource()).setVcn(vcn);
                                    }
                                }
                            }).schedule();
                            ;
                        }
                    }
                });
            }
        });

        rmItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = vcnAclTableViewer.getStructuredSelection();
                Object firstElement = selection.getFirstElement();
                vcnConfigs.remove(firstElement);
                vcnAclTableViewer.refresh();
            }
        });
    }

    private void updateStatus() {
        getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                MultiStatus result = ipAddressPanel.validate();
                result.merge(validateIsMTLSRequired());
                if (!result.isOK()) {
                    setErrorMessage(result.getChildren()[0].getMessage());
                    setPageComplete(false);
                } else {
                    setErrorMessage(null);
                    setPageComplete(true);
                }
                ipAddressPanel.refresh(true);
                vcnAclTableViewer.refresh(true);
            }
        });
    }

    private IStatus validateIsMTLSRequired() {
        if (!btnIsMLTSRequired.getSelection()) {
            if (ipAddressPanel.getIpConfigs().isEmpty() && vcnConfigs.isEmpty()) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Must have at least one ACL to disable mTLS");
            }
        }
        return Status.OK_STATUS;
    }

    private void updateAclUpdate(boolean enabled) {
        ipAddressPanel.setEnabled(enabled);
        configureAnywhereTable.setEnabled(enabled);
        btnIsMLTSRequired.setEnabled(enabled);
        actionPanelVCN.setEnabled(enabled);
    }

    private void populateAccessFromAnywhere(AutonomousDatabaseSummary instance2) {
        ADBInstanceWrapper wrapper = new ADBInstanceWrapper(instance);
        List<String> whiteListedIps = wrapper.getWhiteListedIps();
        if (whiteListedIps == null)
        {
            whiteListedIps = Collections.emptyList();
        }
        boolean aclEnabled;
        if (!whiteListedIps.isEmpty()) {
            aclEnabled = true;
        } else {
            aclEnabled = false;
        }
        this.configureSecurityCheckbox.setSelection(aclEnabled);

        this.btnIsMLTSRequired.setSelection(wrapper.isMTLSRequired());

        updateAclUpdate(aclEnabled);
        PropertyListeningArrayList<AccessControlRowHolder> ipConfigs = AccessControlRowHolder
                .parseAclsFromText(whiteListedIps, Category.IP_BASED);
        ipConfigs.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateStatus();
            }
        });
        this.ipAddressPanel.setInput(ipConfigs);

        this.vcnConfigs = AccessControlRowHolder.parseAclsFromText(whiteListedIps, Category.VCN_BASED);
        vcnConfigs.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateStatus();
            }
        });

        // this.vcnInfos = new PropertyListeningArrayList<VcnWrapper>();
        final NetworkClient networkClient = new NetworkClient();
        for (final AccessControlRowHolder aclHolder : this.vcnConfigs) {
            if (!aclHolder.isFullyLoaded()) {
                Job.create("Load vcn info for acl", new ICoreRunnable() {
                    @Override
                    public void run(IProgressMonitor monitor) throws CoreException {
                        final OcidBasedAccessControlType aclType = (OcidBasedAccessControlType) aclHolder.getAclType();
                        String ocid = aclType.getOcid();
                        Vcn vcn = networkClient.getVcn(ocid);
                        if (vcn != null) {
                            System.out.println(vcn.getDisplayName());
                            aclType.setVcn(vcn);
                            aclHolder.setFullyLoaded(true);
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    vcnAclTableViewer.refresh();
                                }

                            });
                        }
                    }
                }).schedule();
                ;
            }
        }
        List<Vcn> listVcns = networkClient.listVcns();
        for (Vcn vcn : listVcns) {
            System.out.printf("id=%s, compartment=%s, displayName=%s\n", vcn.getId(), vcn.getCompartmentId(),
                    vcn.getDisplayName());
        }
        this.vcnAclTableViewer.setInput(this.vcnConfigs);
    }

    @SuppressWarnings("unused")
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

        Group networkSecGroups = new Group(privateNetworkTabComp, SWT.NONE);
        networkSecGroups.setText("Network Security Groups");
        networkSecGroups.setLayout(new GridLayout(1, false));
        GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        layoutData.horizontalSpan = 3;
        networkSecGroups.setLayoutData(layoutData);

        TableViewer viewer = new TableViewer(networkSecGroups, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        this.privateEndpointTable = viewer.getTable();
        privateEndpointTable.setHeaderVisible(true);
        privateEndpointTable.setLinesVisible(true);
        privateEndpointTable.setLayoutData(GridDataFactory.defaultsFor(configureAnywhereTable).align(SWT.FILL, SWT.FILL)
                .grab(true, true).create());
        this.privateVCN = new TableColumn(configureAnywhereTable, SWT.NONE);
        privateVCN.setText("Virtual Cloud Network");
        this.privateVCNCompartment = new TableColumn(configureAnywhereTable, SWT.NONE);
        privateVCNCompartment.setText("Compartment");

        this.privateVCN.pack();
        this.privateVCNCompartment.pack();

        viewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List<?>) {
                    return ((List<?>) inputElement).toArray();
                }
                return new Object[0];
            }
        });
    }

    private static class CompartmentLabelLink {
        private Link compartmentLink;

        public CompartmentLabelLink(final Composite parent) {
            Composite linkComposite = new Composite(parent, SWT.NONE);
            linkComposite.setLayout(new GridLayout(1, false));
            this.compartmentLink = new Link(linkComposite, SWT.NONE);
        }

        public void setText(String text) {
            this.compartmentLink.setText(text);
        }
    }

    public boolean isMTLSRequired() {
        return this.btnIsMLTSRequired.getSelection();
    }

    public List<String> getWhitelistedIps() {
        List<String> whitelistedIps = new ArrayList<>();
        for (AccessControlRowHolder holder : this.ipAddressPanel.getIpConfigs()) {
            whitelistedIps.add(holder.getAclType().getValue());
        }
        for (AccessControlRowHolder holder : this.vcnConfigs) {
            whitelistedIps.add(holder.getAclType().getValue());
        }
        return whitelistedIps;
    }

    static class UpdateState {
        private List<String> whitelistedIps;
        private boolean isMtlsConnectionRequired;

        public UpdateState(AutonomousDatabaseSummary instance) {
            this(instance.getWhitelistedIps(), 
               instance.getIsMtlsConnectionRequired() != null ? instance.getIsMtlsConnectionRequired() : false);
        }

        public UpdateState(List<String> whitelistIps, boolean isMtlsConnectionRequired) {
            this.whitelistedIps = new ArrayList<>(whitelistIps != null ? whitelistIps : Collections.emptyList());
            Collections.sort(this.whitelistedIps);
            this.isMtlsConnectionRequired = isMtlsConnectionRequired;
        }

        public boolean isAclChanged(UpdateState otherState) {
            if (otherState.whitelistedIps.size() != whitelistedIps.size()) {
                return true;
            }
            for (int i = 0; i < whitelistedIps.size(); i++) {
                String str1 = whitelistedIps.get(i);
                assert str1 != null;
                String str2 = otherState.whitelistedIps.get(i);
                assert str2 != null;
                // neither of these should be null;
                if (!str1.equals(str2)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isMtlsConnectionRequiredChanged(UpdateState otherState) {
            return isMtlsConnectionRequired != otherState.isMtlsConnectionRequired;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other instanceof UpdateState) {
                UpdateState otherState = (UpdateState) other;
                if (isAclChanged(otherState)) {
                    return false;
                }
                return !isMtlsConnectionRequiredChanged(otherState);
            }
            return false;
        }

        public int hashCode() {
            HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
            hashCodeBuilder.append(isMtlsConnectionRequired);
            hashCodeBuilder.append(whitelistedIps.toArray());
            return hashCodeBuilder.toHashCode();
        }
    }

    public boolean getConfigureAccess() {
        return configureSecurityCheckbox.getSelection();
    }
}
