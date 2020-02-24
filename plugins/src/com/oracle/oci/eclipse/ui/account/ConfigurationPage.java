/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ConfigurationPage extends WizardPage {

    private final String TITLE_TEXT = "Oracle Cloud Infrastructure Connection Setup";
    private final String DESCRIPTION_TEXT = "Connect to Oracle Cloud Infrastructure";
    private ConfigurationElementsUI elements = null;

    /**
     * Constructor for ConfigurationPage.
     */
    public ConfigurationPage(ISelection selection) {
        super("configurationPage");
        setTitle(TITLE_TEXT);
        setDescription(DESCRIPTION_TEXT);
    }

    @Override
    public void createControl(Composite parent) {
        elements = new ConfigurationElementsUI();
        Control innerContainer = elements.drawElements(parent);
        setControl(innerContainer);
    }

    public ConfigurationElementsUI getElements() {
        return elements;
    }

}