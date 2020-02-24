/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class UpgradeADBInstanceToPaidWizardPage extends WizardPage {
    private ISelection selection;

    public UpgradeADBInstanceToPaidWizardPage(ISelection selection) {
        super("wizardPage");
        setTitle("Confirm Upgrade");
        setDescription("");
        this.selection = selection;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        final String msg ="If you elect to upgrade this Oracle Cloud Infrastructure Always Free service to paid, your use of \n"
        		          +"the paid service is subject to the updated Oracle IaaS and PaaS Universal Credits service \n"
                          +"descriptions found at www.oracle.com/contracts. Oracle will start billing as described in the \n"
        		          +"service description for the following:\n\n"
                          +"Oracle Autonomous Transaction Processing OCPU Per Hour (B90453)\n"
        		          +"Oracle Autonomous Database - Exadata Storage (ATP on shared Exadata infrastructure) (B90455)\n\n"
                          +"Are you sure you wish to Upgrade?";
        Label terminationMsgLabel = new Label(container, SWT.NULL);
        terminationMsgLabel.setText(msg);
        new Label(container, SWT.NULL);
        new Label(container, SWT.NULL);
        new Label(container, SWT.NULL);
        setControl(container);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

}
