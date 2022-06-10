package com.oracle.oci.eclipse.ui.explorer.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.oracle.bmc.database.model.DatabaseConnectionStringProfile;
import com.oracle.bmc.database.model.DatabaseConnectionStringProfile.TlsAuthentication;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient.DatabaseConnectionProfiles;

public class ListConnectionProfilesDialog extends TrayDialog {

    private DatabaseConnectionProfiles profiles;
    private StyledText mTlsProfilesText;
    private StyledText walletlessProfiles;
    private StyledText springProperties;
    private Button mTlsSaveToProjBtn;
    private Button mTlsSaveToFolderBtn;
    private List<IProject> javaProj;
    private ComboViewer cboViewer;
    @SuppressWarnings("unused")
    private Button doneButton;

    public ListConnectionProfilesDialog(Shell shell, DatabaseConnectionProfiles profiles) {
        super(shell);
        this.profiles = profiles;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        CTabFolder folder = new CTabFolder(composite, SWT.NONE);

        CTabItem springTabItem = maybeAddSpringBootTab(folder);

        if (!this.profiles.getWalletLessProfiles().isEmpty()) {
            CTabItem walletLessProfilesTab = new CTabItem(folder, SWT.NONE);
            walletLessProfilesTab.setText("Walletless (one-way) TLS");

            Composite walletLessProfilePanel = new Composite(folder, SWT.NONE);
            walletLessProfilePanel.setLayout(new GridLayout(1, false));
            walletLessProfilePanel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

            Label walletlessProfilesLbl = new Label(walletLessProfilePanel, SWT.NONE);
            walletlessProfilesLbl.setText(
                    "One-way TLS profiles.  You have set up custom IP or VCN restrictions if you can see these profiles");

            this.walletlessProfiles = new StyledText(walletLessProfilePanel, SWT.H_SCROLL);
            this.walletlessProfiles.setEditable(false);
            for (DatabaseConnectionStringProfile profile : this.profiles.getWalletLessProfiles()) {
                String profileText = String.format("%s = %s", profile.getDisplayName(), profile.getValue());
                walletlessProfiles
                        .setText(walletlessProfiles.getText() + profileText + walletlessProfiles.getLineDelimiter());
            }
            GridDataFactory.defaultsFor(walletlessProfiles).align(SWT.FILL, SWT.END).applyTo(walletlessProfiles);
            walletLessProfilesTab.setControl(walletLessProfilePanel);
        }

        CTabItem mtlsProfilesTab = new CTabItem(folder, SWT.NONE);
        mtlsProfilesTab.setText("mTLS profiles");

        Composite mtlsProfilesTabPanel = new Composite(folder, SWT.NONE);
        mtlsProfilesTabPanel.setLayout(new GridLayout(1, false));
        Label mtlsProfilesLbl = new Label(mtlsProfilesTabPanel, SWT.NONE);
        mtlsProfilesLbl.setText("You will a need valid wallet to use these profiles");
        this.mTlsProfilesText = new StyledText(mtlsProfilesTabPanel, SWT.H_SCROLL);
        mTlsProfilesText.setEditable(false);
        GridDataFactory.defaultsFor(mTlsProfilesText).align(SWT.FILL, SWT.END).applyTo(mTlsProfilesText);
        mtlsProfilesTab.setControl(mtlsProfilesTabPanel);

        for (DatabaseConnectionStringProfile profile : this.profiles.getmTLSProfiles()) {
            String profileText = String.format("%s = %s", profile.getDisplayName(), profile.getValue());
            mTlsProfilesText.setText(mTlsProfilesText.getText() + profileText + mTlsProfilesText.getLineDelimiter());
        }
        GridDataFactory.defaultsFor(mTlsProfilesText).align(SWT.FILL, SWT.BEGINNING).applyTo(mTlsProfilesText);

        Composite mTlsButtonPanel = new Composite(composite, SWT.NONE);
        mTlsButtonPanel.setLayout(new GridLayout(2, false));
        this.mTlsSaveToProjBtn = new Button(mTlsButtonPanel, SWT.PUSH);
        mTlsSaveToProjBtn.setText("Save to project...");
        mTlsSaveToProjBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CTabItem selection = folder.getSelection();
                if (selection == springTabItem) {
                    saveSpringProperties();
                } else {
                    saveProfilesToProject(selection == mtlsProfilesTab ? mTlsProfilesText : walletlessProfiles);
                }

            }
        });
        this.mTlsSaveToFolderBtn = new Button(mTlsButtonPanel, SWT.PUSH);
        mTlsSaveToFolderBtn.setText("Save to file...");

        Shell myShell = getShell();
        int width = convertWidthInCharsToPixels(80);
        myShell.setSize(width, (int) width * 5 / 7);
        Rectangle parentSize = myShell.getParent().getBounds();
        myShell.setLocation((parentSize.width - myShell.getBounds().width) / 2,
                (parentSize.height - myShell.getBounds().height) / 2);
        myShell.pack();

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        this.doneButton = createButton(parent, IDialogConstants.OK_ID, "Done", true);
    }

    @Override
    public void create() {
        super.create();
        getShell().pack(true);
    }

    private CTabItem maybeAddSpringBootTab(CTabFolder folder) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        this.javaProj = new ArrayList<>();
        for (IProject project : root.getProjects()) {
            IFile file = project.getFile(".classpath");
            if (file.exists()) {
                // assume it is a Java project if has classpath; we don't
                // want to force a JDT dep
                javaProj.add(project);
            }
        }

        if (!javaProj.isEmpty()) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText("Spring JPA Properties");

            Composite mtlsProfilesTabPanel = new Composite(folder, SWT.NONE);
            mtlsProfilesTabPanel.setLayout(new GridLayout(2, false));
            Label connProfileCombo = new Label(mtlsProfilesTabPanel, SWT.NONE);
            connProfileCombo.setText("Profile:");
            this.cboViewer = new ComboViewer(mtlsProfilesTabPanel);
            cboViewer.setContentProvider(new IStructuredContentProvider() {

                @Override
                public Object[] getElements(Object inputElement) {
                    if (inputElement instanceof DatabaseConnectionProfiles) {
                        List<DatabaseConnectionStringProfile> list = new ArrayList<>();
                        list.addAll(((DatabaseConnectionProfiles) inputElement).getWalletLessProfiles());
                        list.addAll(((DatabaseConnectionProfiles) inputElement).getmTLSProfiles());
                        return list.toArray();
                    }
                    return new Object[0];
                }
            });
            cboViewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    DatabaseConnectionStringProfile connProfile = (DatabaseConnectionStringProfile) element;
                    TlsAuthentication tlsAuthentication = connProfile.getTlsAuthentication();
                    return String.format("%s [%s]", connProfile.getDisplayName(),
                            tlsAuthentication == TlsAuthentication.Server ? "one-way" : "mTLS");
                }
            });
            cboViewer.setInput(profiles);

            GridDataFactory.defaultsFor(cboViewer.getControl()).align(SWT.FILL, SWT.END)
                    .applyTo(cboViewer.getControl());
            Label textLbl = new Label(mtlsProfilesTabPanel, SWT.NONE);
            textLbl.setText("Add Spring connection parameters to properties file");
            this.springProperties = new StyledText(mtlsProfilesTabPanel, SWT.H_SCROLL);
            springProperties.setEditable(false);
            GridDataFactory.defaultsFor(springProperties).span(2, 1).align(SWT.FILL, SWT.END).applyTo(springProperties);

            cboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    StringBuilder builder = buildJPAProps(
                            (DatabaseConnectionStringProfile) event.getStructuredSelection().getFirstElement());
                    springProperties.setText(builder.toString());
                }
            });
            cboViewer.setSelection(new StructuredSelection(
                    !profiles.getWalletLessProfiles().isEmpty() ? profiles.getWalletLessProfiles().get(0)
                            : profiles.getmTLSProfiles().get(0)));

            item.setControl(mtlsProfilesTabPanel);
            return item;
        }
        return null;
    }

    private StringBuilder buildJPAProps(DatabaseConnectionStringProfile connProfile) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> resolveMap = resolveMap(connProfile);
        for (Map.Entry<String, String> prop : resolveMap.entrySet())
        {
            builder.append(String.format("%s=%s\n", prop.getKey(), prop.getValue()));
        }
        return builder;
    }

    private Map<String,String> resolveMap(DatabaseConnectionStringProfile connProfile) {
        Map<String, String> copy = new HashMap<>(SPRING_JPA_PROPS);
        for (Map.Entry<String, String> keyValue : copy.entrySet()) {
            switch (keyValue.getKey()) {
                case "spring.datasource.url": {
                    String urlVal = keyValue.getValue();
                    urlVal = String.format(urlVal, connProfile.getValue());
                    copy.put(keyValue.getKey(), urlVal);
                }
                    break;
                case "spring.datasource.username": {
                    String user = keyValue.getValue();
                    user = String.format(user, "ADMIN");
                    copy.put(keyValue.getKey(), user);
                }
                    break;
                case "spring.datasource.password": {
                    String password = String.format(keyValue.getValue(), "<notthepassword>");
                    copy.put(keyValue.getKey(), password);
                }
                    break;
            }
        }
        return copy;
    }

    private void saveSpringProperties() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
            
            @Override
            public Object[] getElements(Object inputElement) {
                return ((IWorkspaceRoot)inputElement).getProjects();
            }
        };
        ILabelProvider labelProvider = new WorkbenchLabelProvider();
        ListSelectionDialog rsDialog = 
            new ListSelectionDialog(getShell(), root, contentProvider, labelProvider, "Select project");
        
        int open = rsDialog.open();
        if (open == ListSelectionDialog.OK)
        {
            IStructuredSelection structuredSelection = this.cboViewer.getStructuredSelection();
            DatabaseConnectionStringProfile connProfile = 
                    (DatabaseConnectionStringProfile) structuredSelection.getFirstElement();

            Object[] result = rsDialog.getResult();
            for (Object proj : result)
            {
                IProject iProj = (IProject) proj;
                IFile iFile = iProj.getFile("src/main/resources/application.properties");
                Properties props = new Properties();
                if (iFile.exists())
                {
                    try {
                        InputStream contents = iFile.getContents();
                        props.load(contents);
                    } catch (CoreException | IOException e) {
                        ErrorHandler.logErrorStack("Error loading file: "+iFile.toString(), e);
                    }
                }
                
                for (Map.Entry<String, String> entry : resolveMap(connProfile).entrySet())
                {
                    props.put(entry.getKey(), entry.getValue());
                }

                try 
                {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    props.store(bos, "Updated by Eclipse OCI plugin");
                    IFolder parent = (IFolder) iFile.getParent();
                    if (!parent.exists())
                    {
                        ContainerGenerator generator = new ContainerGenerator(parent.getFullPath());
                        generator.generateContainer(null);
                        parent.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
                    }
                    
                    ByteArrayInputStream source = new ByteArrayInputStream(bos.toByteArray());
                    if (iFile.exists())
                    {
                        iFile.setContents(source, IFile.KEEP_HISTORY, null);

                    }
                    else
                    {
                        iFile.create(source, true, null);
                    }
                }
                catch (Exception e)
                {
                    ErrorHandler.logErrorStack("Error storing application.properties", e);
                }
            }
        }
    }

    private void saveProfilesToProject(StyledText text) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        ContainerSelectionDialog selectionDialog = new ContainerSelectionDialog(getShell(), root, true,
                "Select folder for tnsnames.ora");
        int open = selectionDialog.open();
        if (open == Dialog.OK) {
            Object[] result = selectionDialog.getResult();
            if (result.length > 0) {
                IPath containerPath = (IPath) result[0];
                IResource findMember = root.findMember(containerPath, false);
                IContainer container = null;
                if (findMember instanceof IContainer) {
                    container = (IContainer) findMember;
                }
                if (container == null) {
                    return;
                }
                IFile file = container.getFile(new Path("tnsnames.ora"));
                boolean createFile = true;
                if (file.exists()) {
                    createFile = MessageDialog.openQuestion(getShell(), "File will be overwritten",
                            "File be will be overwritten: " + file.toString());
                }
                if (createFile) {
                    try {
                        file.create(new ByteArrayInputStream(text.getText().getBytes()), true,
                                new NullProgressMonitor());
                    } catch (CoreException e) {
                        ErrorHandler.logErrorStack("Error creating tnsnames.ora", e);
                    }
                }
            }
        }
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    private final static Map<String, String> SPRING_JPA_PROPS;

    static {
        // use linked hash map to maintain this ordering when we iterate into a file or
        // string
        Map<String, String> props = new LinkedHashMap<>();
        props.put("spring.jpa.hibernate.ddl-auto", "update");
        props.put("spring.datasource.url", "jdbc:oracle:thin:@%s");
        props.put("spring.datasource.username", "%s");
        props.put("spring.datasource.password", "%s");
        props.put("spring.datasource.driver-class-name", "oracle.jdbc.OracleDriver");
        props.put("#spring.jpa.show-sql", "true");
        SPRING_JPA_PROPS = Collections.unmodifiableMap(props);
    }
}
