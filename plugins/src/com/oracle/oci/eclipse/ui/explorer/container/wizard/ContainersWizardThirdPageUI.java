/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.wizard;

import static com.oracle.bmc.util.internal.FileUtils.expandUserHome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.containerengine.model.ClusterSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersTable;
import com.oracle.oci.eclipse.ui.explorer.container.wizard.CommandUtils.CommandUtilsInstance;

public class ContainersWizardThirdPageUI {

    private Combo deploymentKindCombo;
    private Combo dockerImageCombo;
    private Combo serviceTypeCombo;

    private Text appName;
    private Text keyFileText;
    private Text portNumberText;
    private Text kubeFilePathField;
    private Text kubeSpecFileText;
    private Button saveButton;
    private String clusterName;
    private String kubeFileExt = "_KubeSpec.yaml";
    Boolean fileUpdated = false;

    static Pattern pv=Pattern.compile("\\$\\{(\\w+)\\}");

    public ContainersWizardThirdPageUI(ContainerClustersTable table) {
        if (table.getSelectedObjects().size() > 0) {
            clusterName = ((ClusterSummary)table.getSelectedObjects().get(0)).getName();
        }
    }

    // os.path.expanduser
    public static String expanduser(String path) {
        String user=System.getProperty("user.home");

        return path.replaceFirst("~", user);
    }

    // os.path.expandvars
    public static String expandvars(String path) {
        String result=new String(path);

        Matcher m=pv.matcher(path);
        while(m.find()) {
            String var=m.group(1);
            String value=System.getenv(var);
            if (value!=null)
                result=result.replace("${"+var+"}", value);
        }
        return result;
    }

    public String getProfileName() {
        return deploymentKindCombo.getText();
    }

    public String getRegionName() {
        return dockerImageCombo.getText();
    }

    public String getApplicationName() {
        return appName.getText();
    }

    public String getKeyFileName() {
        return keyFileText.getText();
    }

    public String getTenancyOcid() {
        return portNumberText.getText();
    }

    public String getKubeFileName() {
        return kubeFilePathField.getText();
    }

    public boolean saveProfile(Shell shell) {
        if (!saveButton.isEnabled())
            return true;

        File file = new File(expandUserHome(kubeFilePathField.getText()));
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                ErrorHandler.reportException("Filed to create file " + file.getAbsolutePath(), e);
            }
        }
        Path path = Paths.get(file.getPath());
        try (BufferedWriter writer = Files.newBufferedWriter(path))
        {
            writer.write(kubeSpecFileText.getText());
        } catch (IOException e) {
            ErrorHandler.reportException("Filed to save file " + file.getAbsolutePath(), e);
        }

        return true;
    }

    static String readFile(String path) {
        List<String> lines = Collections.emptyList();
        try
        {
            lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            ErrorHandler.reportException("Filed to read file " + path, e);
        }

        StringBuilder sb = new StringBuilder();
        lines.forEach(line-> sb.append(line + "\n"));
        return sb.toString();
    }

    public String getFileName() {
        final File projectPath = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
        return projectPath.getPath() + File.separator + clusterName + kubeFileExt;
    }

    public Control drawElements(Composite parent) {

        // Create layout
        Composite topLevelContainer = new Composite(parent, SWT.NULL);
        GridLayout topLevelLayout = new GridLayout();
        topLevelContainer.setLayout(topLevelLayout);

        /* Top Section */
        createTopSection(topLevelContainer);

        /* Middle Section */
        createMiddleSection(topLevelContainer);

        /* Bottom Section */
        createBottomSection(topLevelContainer);

        return topLevelContainer;
    }

    public void createTopSection(Composite topLevelContainer) {
        Group topGroup = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        topGroup.setText("Kubernetes Spec. File Location");
        topGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        topGroup.setLayout(new GridLayout(1, true));

        Composite innerTopContainer = new Composite(topGroup, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 3;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(innerTopContainer, SWT.NULL);
        label.setText("&FileName: ");

        kubeFilePathField = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        kubeFilePathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        kubeFilePathField.setText(getFileName());
        kubeFilePathField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                kubeSpecFileText.setText(readFile(kubeFilePathField.getText()));
                System.out.println("Modified kubeFilePathField " + kubeFilePathField.getText());
            }
        });

        Button button = new Button(innerTopContainer, SWT.PUSH);
        button.setText("Browse...");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                kubeFilePathField.setText(handleBrowse(innerTopContainer.getShell(), kubeFilePathField.getText(), null));
            }
        });
    }

    public void createMiddleSection(Composite topLevelContainer) {
        Group parametersGroup = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        parametersGroup.setText("Parameters");
        parametersGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersGroup.setLayout(new GridLayout(1, true));

        Composite innerContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 2;
        innerContainer.setLayout(innerLayout);
        innerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(innerContainer, SWT.NULL);
        label.setText("&Kind:");

        deploymentKindCombo = new Combo(innerContainer, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        deploymentKindCombo.add("Deployment");
        deploymentKindCombo.add("ReplicationController");
        deploymentKindCombo.select(0);
        deploymentKindCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                kubeSpecFileText.setText(generateFile());
            }
        });

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Docker Images:");

        dockerImageCombo = new Combo(innerContainer, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        dockerImageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dockerImageCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                kubeSpecFileText.setText(generateFile());
            }
        });

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Application name:");

        appName = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        appName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        appName.setEditable(true);

        appName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                kubeSpecFileText.setText(generateFile());

            }
        });

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Port Number:");

        portNumberText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        portNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        portNumberText.setEditable(true);
        portNumberText.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                kubeSpecFileText.setText(generateFile());
            }
        });
        portNumberText.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                e.doit = false;
                char myChar = e.character;
                if (Character.isDigit(myChar))
                    e.doit = true;
            }
        });

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Service Type:");

        serviceTypeCombo = new Combo(innerContainer, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        serviceTypeCombo.add("LoadBalancer");
        serviceTypeCombo.add("NodePort");
        serviceTypeCombo.add("ClusterIP");
        serviceTypeCombo.add("ExternalName");
        serviceTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serviceTypeCombo.select(0);
        serviceTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                kubeSpecFileText.setText(generateFile());
            }
        });

        label = new Label(innerContainer, SWT.NULL);
        label.setText("Save kubernetes Spec. to file:");

        saveButton = new Button(innerContainer, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveProfile(innerContainer.getShell());
            }
        });
        saveButton.setEnabled(true);
    }

    public void createBottomSection(Composite topLevelContainer) {
        Group bottomGroup = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        bottomGroup.setText("Generated Kubernetes Spec. File");
        bottomGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        bottomGroup.setLayout(new GridLayout(1, true));

        Composite innerBottomContainer = new Composite(bottomGroup, SWT.NONE);
        GridLayout innerBottomLayout = new GridLayout();
        innerBottomLayout.numColumns = 1;
        innerBottomContainer.setLayout(innerBottomLayout);
        GridData innerGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        innerGridData.heightHint = 320;
        innerBottomContainer.setLayoutData(innerGridData);

        kubeSpecFileText = new Text(innerBottomContainer, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        kubeSpecFileText.setLayoutData(new GridData(GridData.FILL_BOTH));
        File expandedFileLoad = new File(getFileName());
        if (expandedFileLoad.exists()) {
            kubeSpecFileText.setText(readFile(expandedFileLoad.getPath()));
        } else {
            kubeSpecFileText.setText(generateFile());
        }

        Label label = new Label(innerBottomContainer, SWT.NULL);
        label.setText("&Click 'Next' to save the generated file and deploy it to the Kubernetes Cluster");
    }

    public String getKubeSpecFileText() {
        return kubeSpecFileText.getText();
    }

    public void fillDockerImageCombo() {
        if (dockerImageCombo != null) {
            for (String s : CommandUtilsInstance.getDockerImages()) {
                // Only add tagged OCIR images, exclude local ones
                if(s.contains(".ocir.io")) {
                    dockerImageCombo.add(s);
                }
            }
            dockerImageCombo.select(0);
        }
    }

    String generateFile() {
        fileUpdated = true;
        String x = "apiVersion: apps/v1\n" +
                "kind: " + deploymentKindCombo.getText() + "\n" +
                "metadata:\n" +
                "  name: " + appName.getText() + "\n" +
                "spec:\n" +
                "  selector:\n" +
                "    matchLabels:\n" +
                "      app: " + appName.getText() + "\n" +
                "  replicas: 1\n" +
                "  template:\n" +
                "    metadata:\n" +
                "      labels:\n" +
                "        app: " + appName.getText() + "\n" +
                "    spec:\n" +
                "      containers:\n" +
                "      - name: " + appName.getText() + "\n" +
                "        image: " + dockerImageCombo.getText() + "\n" +
                "        ports:\n" +
                "        - containerPort: " + portNumberText.getText() + "\n" +
                "      imagePullSecrets:\n" +
                "      - name: oci-registry\n" +
                "---\n" +
                "apiVersion: v1\n" +
                "kind: Service\n" +
                "metadata:\n" +
                "  name: " + appName.getText() + "\n" +
                "  labels:\n" +
                "    app: " + appName.getText() + "\n" +
                "spec:\n" +
                "  type: "+ serviceTypeCombo.getText() + "\n" +
                "  selector:\n" +
                "    app: " + appName.getText() + "\n" +
                "  ports:\n" +
                "  - protocol: TCP\n" +
                "    port: " + portNumberText.getText() + "\n" +
                "    name: http";
        return x;
    }

    private boolean validateValues(Shell shell, String keyFileText) {
        // Key file name is a valid file
        File keyFile = new File(keyFileText);
        if (!keyFile.exists()) {
            MessageDialog.openError(shell, "Form Error ", "Enter valid key file. The provided file does not exist: " + keyFileText);
            return false;
        }
        return true;
    }

    private String handleBrowse(Shell shell, String path, String filter) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        if (filter != null)
            dialog.setFilterExtensions(new String [] {filter});
        dialog.setFilterPath(path);
        return dialog.open();
    }
}
