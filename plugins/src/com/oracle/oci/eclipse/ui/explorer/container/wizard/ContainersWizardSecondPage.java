/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersTable;
import com.oracle.oci.eclipse.ui.explorer.container.wizard.CommandUtils.CommandUtilsInstance;

public class ContainersWizardSecondPage extends WizardPage {
    private final String TITLE_TEXT = "Oracle Cloud Infrastructure OKE Setup";
    private final String DESCRIPTION_TEXT = "Push Docker Image to Oracle Cloud Registry";
    Text outputlabel;
    Combo dockerImageCombo;
    Text imageTagText;
    Label uploadLabel;
    private ContainerClustersTable table;

    protected ContainersWizardSecondPage(ContainerClustersTable table) {
        super("secondPage");
        setTitle(TITLE_TEXT);
        setDescription(DESCRIPTION_TEXT);
        this.table = table;
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
        parametersGroup.setText("Docker");
        parametersGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersGroup.setLayout(new GridLayout(1, true));

        Composite innerContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 2;
        innerContainer.setLayout(innerLayout);
        innerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(innerContainer, SWT.NULL);
        label.setText("Follow the steps below to upload Docker images to OCIR: ");

        Label errorLabel = new Label(innerContainer, SWT.NULL);
        errorLabel.setText("Cannot connect to the Docker daemon. Is the docker daemon running?");
        Color redColor = new Color(null, 255, 0, 0);
        errorLabel.setForeground(redColor);
        errorLabel.setVisible(false);

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&1) Select Docker Image:");

        dockerImageCombo = new Combo(innerContainer, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        dockerImageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (String s : CommandUtilsInstance.getDockerImages()) {
            if (s.startsWith("Cannot")) {
                dockerImageCombo.setEnabled(false);
                break;
            }
            dockerImageCombo.add(s);
        }
        dockerImageCombo.select(0);
        dockerImageCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                imageTagText.setText(createImageTag(dockerImageCombo.getText()));
            }
        });

        label = new Label(innerContainer, SWT.NULL);
        label.setText("Image new Tag:");

        imageTagText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        imageTagText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(innerContainer, SWT.NULL);
        label.setText("2) Tag Docker Image and upload to OCI Registry:");

        Button pushDockerButton = new Button(innerContainer, SWT.PUSH);
        pushDockerButton.setText("Upload Image to OCI");
        pushDockerButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                pushDocker();
                uploadLabel.setVisible(true);
            }
        });
        // If docker daemon is disconnected, disable the upload
        if (!dockerImageCombo.isEnabled()) {
            pushDockerButton.setEnabled(false);
            // Show error msg
            errorLabel.setVisible(true);
        }

        Color greenColor = new Color(null, 3, 125, 80);
        label = new Label(innerContainer, SWT.NULL);
        uploadLabel = new Label(innerContainer, SWT.NULL);
        //uploadLabel.setText("Upload is successful.");
        uploadLabel.setForeground(greenColor);
        uploadLabel.setVisible(false);

        Composite innerBottomContainer = new Composite(parametersGroup, SWT.NONE);
        GridLayout innerBottomLayout = new GridLayout();
        innerBottomLayout.numColumns = 1;
        innerBottomContainer.setLayout(innerBottomLayout);
        GridData innerGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        innerGridData.heightHint = 460;
        innerBottomContainer.setLayoutData(innerGridData);

        outputlabel = new Text(innerBottomContainer, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        outputlabel.setLayoutData(new GridData(GridData.FILL_BOTH));
        //outputlabel.setVisible(false);
    }

    private String createImageTag(String imageName) {
        String tagName = imageName;
        if(!imageName.startsWith(getOCIRURL())) {
            tagName = getOCIRURL() + "/" + IdentClient.getInstance().getTenancy().getName() + "/" + imageName;
        }
        return tagName;
    }


    private String getOCIRURL() {
        return AuthProvider.getInstance().getRegion().getRegionCode() + ".ocir.io";
    }

    private void pushDocker() {
        String command = "docker tag " + dockerImageCombo.getText() + " " + imageTagText.getText();
        StringBuilder commandOutput = new StringBuilder();
        CommandUtilsInstance.executeCommandBG(command,  outputlabel);

        command = "docker push " + imageTagText.getText();
        CommandUtilsInstance.executeCommandBG(command,  outputlabel);
    }
}
