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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.ui.explorer.container.wizard.CommandUtils.CommandUtilsInstance;

public class ContainersWizardFourthPage extends WizardPage {
    private final String TITLE_TEXT = "Oracle Cloud Infrastructure OKE Setup";
    private final String DESCRIPTION_TEXT = "Deploy Docker image to Oracle Cloud Kubernetes Engine";
    private Text outputlabel;
    private ContainersWizardThirdPage page;
    private String applicationName = "";

    public ContainersWizardFourthPage(ContainersWizardThirdPage page) {
        super("ContainersConfigurationPageFour");
        setTitle(TITLE_TEXT);
        setDescription(DESCRIPTION_TEXT);
        this.page = page;
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
        setPageComplete(true);
    }

    public void createMiddleSection(Composite topLevelContainer) {
        Group parametersGroup = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        parametersGroup.setText("Kubernetes Deploy Results");
        parametersGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersGroup.setLayout(new GridLayout(1, true));

        Composite innerContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 1;
        innerContainer.setLayout(innerLayout);

        GridData innerGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        innerGridData.heightHint = 500;
        innerContainer.setLayoutData(innerGridData);

        outputlabel = new Text(innerContainer, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        outputlabel.setLayoutData(new GridData(GridData.FILL_BOTH));
        outputlabel.setText("");
        outputlabel.setEditable(false);

        Button getServiceButton = new Button(innerContainer, SWT.PUSH);
        getServiceButton.setText("Get Service and Rollout Info");
        getServiceButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!applicationName.isEmpty()) {
                    CommandUtilsInstance.executeCommandBG("kubectl get service " + applicationName, outputlabel);
                    CommandUtilsInstance.executeCommandBG("kubectl rollout status deployment.apps/" + applicationName, outputlabel);
                } else {
                    outputlabel.append("Application Name is Empty. Please check previous wizard page. \n");
                    CommandUtilsInstance.executeCommandBG("kubectl get services", outputlabel);
                }
            }
        });
        Button restartRolloutButton = new Button(innerContainer, SWT.PUSH);
        restartRolloutButton.setText("Restart Deployment Rollout");
        restartRolloutButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!applicationName.isEmpty()) {
                    CommandUtilsInstance.executeCommandBG("kubectl rollout restart deployment " + applicationName, outputlabel);
                } else {
                    outputlabel.append("Application Name is Empty. Please check previous wizard page. \n");
                }
            }
        });
    }
    public Text getOutputLabel() {
        return outputlabel;
    }

    public void executeKubeDeployCommand() {
        applicationName = page.getElements().getApplicationName();
        String kubeFilePath = page.getElements().getKubeFileName();
        if (!kubeFilePath.isEmpty()) {
            CommandUtilsInstance.executeCommandBG("kubectl apply -f " + kubeFilePath, outputlabel);
        }
    }
}
