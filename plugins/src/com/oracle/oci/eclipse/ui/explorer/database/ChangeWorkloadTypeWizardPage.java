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

public class ChangeWorkloadTypeWizardPage extends WizardPage {

    public ChangeWorkloadTypeWizardPage(ISelection selection) {
        super("wizardPage");
        setTitle("Change Workload Type to Transaction Processing");
        setDescription("");
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        final String msg ="You can convert Oracle Autonomous JSON Databases to Oracle Autonomous Transaction Processing databases. \n"
        		          +"This action is not reversible. After conversion, Oracle will bill you for the following: \n\n"
                          +"Oracle Autonomous Transaction Processing OCPU Per Hour (B90453)\n"
        		          +"Oracle Autonomous Transaction Processing Exadata Storage (B90455)\n\n"
                          +"Are you sure you want to convert this database to Autonomous Transaction Processing?";
        Label terminationMsgLabel = new Label(container, SWT.NULL);
        terminationMsgLabel.setText(msg);
        new Label(container, SWT.NULL);
        new Label(container, SWT.NULL);
        new Label(container, SWT.NULL);
        setControl(container);
    }

}
