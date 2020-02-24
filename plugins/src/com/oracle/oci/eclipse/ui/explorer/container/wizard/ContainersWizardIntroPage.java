/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.account.SystemPropertiesUtils;
import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersTable;
import com.oracle.oci.eclipse.ui.explorer.container.wizard.CommandUtils.CommandUtilsInstance;

public class ContainersWizardIntroPage extends WizardPage {
    private final String TITLE_TEXT = "Oracle Cloud Infrastructure OKE Setup";
    private final String DESCRIPTION_TEXT = "Prerequsites to deploy Docker Images to Oracle Cloud Kubernetes Service";
    Text outputlabel;
    Combo dockerImageCombo;
    Text imageTagText;

    protected ContainersWizardIntroPage(ContainerClustersTable table) {
        super("firstPage");
        setTitle(TITLE_TEXT);
        setDescription(DESCRIPTION_TEXT);
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
        parametersGroup.setText("Pre-requsites");
        parametersGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersGroup.setLayout(new GridLayout(1, true));

        Composite innerContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 2;
        innerContainer.setLayout(innerLayout);
        innerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Color greenColor = new Color(null, 3, 125, 80);

        Label label = new Label(innerContainer, SWT.NULL);
        label.setText("Install Docker");
        label = new Label(innerContainer, SWT.NULL);
        label.setForeground(greenColor);
        //label.setText("Installed");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("Start Docker Daemon");
        label = new Label(innerContainer, SWT.NULL);
        label.setForeground(greenColor);
        //label.setText("Started");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("Install OCI CLI");
        label = new Label(innerContainer, SWT.NULL);
        label.setForeground(greenColor);
        //label.setText("Installed");
        //Text cliPathField = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        //cliPathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        //cliPathField.setText();
        //label = new Label(innerContainer, SWT.NULL);

        label = new Label(innerContainer, SWT.NULL);
        label.setText("Install Kubectl");
        label = new Label(innerContainer, SWT.NULL);
        label.setForeground(greenColor);
        //label.setText("Installed");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("Build your application as a Docker image.");

        Composite innerBottomContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerBottomLayout = new GridLayout();
        innerBottomLayout.numColumns = 1;
        innerBottomContainer.setLayout(innerBottomLayout);
        GridData innerGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        innerGridData.heightHint = 460;
        innerBottomContainer.setLayoutData(innerGridData);

        outputlabel = new Text(innerBottomContainer, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        outputlabel.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (SystemPropertiesUtils.isMac() || SystemPropertiesUtils.isLinux()) {
            label = new Label(innerBottomContainer, SWT.NULL);
            label.setText("Note: The deployment wizard searches for installed applications under this path:'/usr/local/bin/'.");
        }

        outputlabel.append("Checking installed tools .... \n\n");

        String command = "docker --version";
        CommandUtilsInstance.executeCommandBG(command,  outputlabel);

        command = "oci --version";
        CommandUtilsInstance.executeCommandBG(command,  outputlabel);

        command = "kubectl version";
        CommandUtilsInstance.executeCommandBG(command,  outputlabel);

    }

}
