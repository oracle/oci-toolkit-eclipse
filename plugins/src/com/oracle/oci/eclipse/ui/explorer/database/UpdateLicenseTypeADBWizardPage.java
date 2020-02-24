/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LicenseModel;
import com.oracle.bmc.database.model.UpdateAutonomousDatabaseDetails;

public class UpdateLicenseTypeADBWizardPage extends WizardPage {

	private Group licenseTypeGroup;
	private Button bringYourOwnLicenseRadioButton;
    private Button licenseIncludedRadioButton;
    
    private ISelection selection;
    AutonomousDatabaseSummary instance;

    public UpdateLicenseTypeADBWizardPage(ISelection selection, AutonomousDatabaseSummary instance) {
        super("wizardPage");
        setTitle("ADB Update License Type Wizard");
        setDescription("This wizard updates license model for ADB instance");
        this.selection = selection;
        this.instance = instance;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
        
        Label licenseTypeLabel = new Label(container, SWT.NULL);
        licenseTypeLabel.setText("Update License Type:");
        
        licenseTypeGroup = new Group(container, SWT.NONE);
        licenseTypeGroup.setLayout(new RowLayout(SWT.VERTICAL));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        licenseTypeGroup.setLayoutData(gd);
        
        bringYourOwnLicenseRadioButton = new Button(licenseTypeGroup, SWT.RADIO);
        bringYourOwnLicenseRadioButton.setText("MY ORGANIZATION ALREADY OWNS ORACLE DATABASE SOFTWARE LICENSES.");
        
        licenseIncludedRadioButton= new Button(licenseTypeGroup, SWT.RADIO);
        licenseIncludedRadioButton.setText("SUBSCRIBE TO NEW DATABASE SOFTWARE LICENSES AND THE DATABASE CLOUD SERVICE.");
        
		LicenseModel licenseModel = instance.getLicenseModel();
		if (licenseModel.equals(LicenseModel.LicenseIncluded)) {
			licenseIncludedRadioButton.setSelection(true);
		} else {
			bringYourOwnLicenseRadioButton.setSelection(true);
		}

        setControl(container);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
    
	public UpdateAutonomousDatabaseDetails.LicenseModel getLicenseType() {
		final UpdateAutonomousDatabaseDetails.LicenseModel licenseModel;
		if (true == bringYourOwnLicenseRadioButton.getSelection()) {
			licenseModel = UpdateAutonomousDatabaseDetails.LicenseModel.BringYourOwnLicense;
		} else {
			licenseModel = UpdateAutonomousDatabaseDetails.LicenseModel.LicenseIncluded;
		}
		return licenseModel;
	}

}
