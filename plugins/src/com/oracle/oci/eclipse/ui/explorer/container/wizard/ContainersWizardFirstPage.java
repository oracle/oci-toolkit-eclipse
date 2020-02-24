/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.containerengine.model.ClusterSummary;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.explorer.container.actions.ContainerClustersAction;
import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersTable;
import com.oracle.oci.eclipse.ui.explorer.container.wizard.CommandUtils.CommandUtilsInstance;

public class ContainersWizardFirstPage extends WizardPage {
    private final String TITLE_TEXT = "Oracle Cloud Infrastructure OKE Setup";
    private final String DESCRIPTION_TEXT = "Login to Oracle Cloud Infrastructure";
    Text outputlabel;
    Combo dockerImageCombo;
    Text imageTagText;
    String clusterName;
    private ContainerClustersTable table;

    protected ContainersWizardFirstPage(ContainerClustersTable table) {
        super("firstPage");
        setTitle(TITLE_TEXT);
        setDescription(DESCRIPTION_TEXT);
        this.table = table;
        if (table.getSelectedObjects().size() > 0) {
            clusterName = ((ClusterSummary)table.getSelectedObjects().get(0)).getName();
        }
    }

    @Override
    public void createControl(Composite parent) {
        // Create layout
        Composite topLevelContainer = new Composite(parent, SWT.NULL);
        GridLayout topLevelLayout = new GridLayout();
        topLevelContainer.setLayout(topLevelLayout);
        // Mid Section
        createMiddleSection(topLevelContainer);

        setControl(topLevelContainer);

    }

    public void createMiddleSection(Composite topLevelContainer) {
        Group parametersGroup = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        parametersGroup.setText("Authentication");
        parametersGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersGroup.setLayout(new GridLayout(1, true));

        Composite innerContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 2;
        innerContainer.setLayout(innerLayout);
        innerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(innerContainer, SWT.NULL);
        label.setText("Authenticate Docker and Kubernetes CLI tools with Oracle Cloud Infrastructure. "
                + "If the session is valid, this step can be skipped.");

        label = new Label(innerContainer, SWT.NULL);

        label = new Label(innerContainer, SWT.NULL);
        label.setText("1) Get OCI Authentication Token and the cluster KubeConfig");

        Button loginOCIButton = new Button(innerContainer, SWT.PUSH);
        loginOCIButton.setText("Get OCI Auth. Token");
        loginOCIButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                downloadKubeConfig();

                getAuthToken();
            }
        });
        loginOCIButton.setEnabled(true);



        label = new Label(innerContainer, SWT.NULL);
        label.setText("2) Authenticate Docker and OKE tools with OCI");

        Button loginRegistryButton = new Button(innerContainer, SWT.PUSH);
        loginRegistryButton.setText("Authenticate");
        loginRegistryButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                loginToDocker();

                loginToKubectl();
            }
        });
        loginRegistryButton.setEnabled(true);


        Composite innerBottomContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerBottomLayout = new GridLayout();
        innerBottomLayout.numColumns = 1;
        innerBottomContainer.setLayout(innerBottomLayout);
        GridData innerGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        innerGridData.heightHint = 460;
        innerBottomContainer.setLayoutData(innerGridData);

        outputlabel = new Text(innerBottomContainer, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        outputlabel.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Lower container
        Composite innerBottomExpandContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerBottomExpand = new GridLayout();
        innerBottomExpand.numColumns = 2;
        innerBottomExpandContainer.setLayout(innerBottomExpand);
        innerBottomExpandContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(innerBottomExpandContainer, SWT.NULL);
        label.setText("Delete Cached OCI Authentication Token (Optional step. It can be used if the token in cloud is deleted)");

        Button deleteOCIAuthButton = new Button(innerBottomExpandContainer, SWT.PUSH);
        deleteOCIAuthButton.setText("Delete Token");
        deleteOCIAuthButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteAuthToken();
            }
        });
    }

    private String getOCIRURL() {
        return AuthProvider.getInstance().getRegion().getRegionCode() + ".ocir.io";
    }

    private String getUserInfo() {
        // <tenancy>/<user>
        return IdentClient.getInstance().getTenancy().getName() + "/" + IdentClient.getInstance().getUser().getName();
    }

    private void downloadKubeConfig() {
        outputlabel.append("Download default Config file for cluster: " + clusterName + "\n");
        new ContainerClustersAction(table, "", true).run();
        outputlabel.append( "File download is successful.\n");
        outputlabel.append( "--------------------------------------------------------------------------------------------\n");
    }

    String token = null;
    private void getAuthToken() {
        outputlabel.append("Getting OCI Authuntication Token ..." + "\n");
        token =  AuthProvider.getInstance().getAuthToken();
        if (token == null || token.isEmpty()) {
            outputlabel.append("Failed to get OCI Authuntication Token" + "\n");
        } else {
            outputlabel.append("OCI Authuntication Token is succesfully fetched." + "\n");
        }
        outputlabel.append( "--------------------------------------------------------------------------------------------\n");
    }

    private void deleteAuthToken() {
        outputlabel.append("Deleted cached OCI Authuntication Token" + "\n");
        AuthProvider.getInstance().deleteAuthToken();
        outputlabel.append( "--------------------------------------------------------------------------------------------\n");
    }

    private void loginToDocker() {
        if(token != null && !token.isEmpty()) {
            outputlabel.append("Login to OCI Registry ..." + "\n");
            // docker login  phx.ocir.io -p <auth-token> -u  <tenancy>/<user>
            String command = "docker login " + getOCIRURL() + " -u " + getUserInfo() + " -p " + token;
            CommandUtilsInstance.executeCommandBG(command,  outputlabel);
        } else {
            outputlabel.append( "Authentication Token is null.\n");
        }
    }

    private void loginToKubectl() {
        if(token != null && !token.isEmpty()) {
            outputlabel.append("Create a docker-registry secret in Kubernetes ..." + "\n");
            String command1 = "kubectl delete secret oci-registry";
            CommandUtilsInstance.executeCommandBG(command1,  outputlabel);
            // Wait for delete to complete
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //kubectl create secret docker-registry ocirsecret --docker-server=phx.ocir.io --docker-username=<tenancy>/<user> --docker-password=<auth-token> --docker-email='xxx@xxx.com'
            String command = "kubectl create secret docker-registry oci-registry --docker-server=" + getOCIRURL() + " --docker-username=" + getUserInfo() + " --docker-password=" + token;
            CommandUtilsInstance.executeCommandBG(command,  outputlabel);
            //}
        } else {
            outputlabel.append( "Authentication Token is null.\n");
        }
    }
}
