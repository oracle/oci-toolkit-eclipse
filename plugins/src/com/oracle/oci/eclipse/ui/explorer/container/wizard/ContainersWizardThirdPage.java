/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersTable;

public class ContainersWizardThirdPage extends WizardPage {

    private final String TITLE_TEXT = "Oracle Cloud Infrastructure OKE Setup";
    private final String DESCRIPTION_TEXT = "Choose Docker image to deploy to Oracle Cloud Kubernetes Engine";
    private ContainersWizardThirdPageUI elements = null;
    private ContainerClustersTable table;

    /**
     * Constructor for ConfigurationPage.
     */
    public ContainersWizardThirdPage(ContainerClustersTable table) {
        super("ContainersConfigurationPage");
        setTitle(TITLE_TEXT);
        setDescription(DESCRIPTION_TEXT);
        this.table = table;
    }

    @Override
    public void createControl(Composite parent) {
        elements = new ContainersWizardThirdPageUI(table);
        Control innerContainer = elements.drawElements(parent);
        setControl(innerContainer);
    }

    public ContainersWizardThirdPageUI getElements() {
        return elements;
    }

}