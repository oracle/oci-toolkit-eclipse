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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;

public class ScaleUpDownADBWizardPage extends WizardPage {

	private Spinner cpuCoreCountSpinner;
    private Spinner storageInTBSpinner;
    private Button autoScalingEnabledCheckBox;
    AutonomousDatabaseSummary instance;

    public ScaleUpDownADBWizardPage(ISelection selection, AutonomousDatabaseSummary instance) {
        super("wizardPage");
        setTitle("ADB Scale Up/Down");
        setDescription("");
        this.instance = instance;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
        
        Label label = new Label(container, SWT.NULL);
        label.setText("CPU CORE COUNT:");

        cpuCoreCountSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        cpuCoreCountSpinner.setLayoutData(gd);
        
        cpuCoreCountSpinner.setMinimum(ADBConstants.CPU_CORE_COUNT_MIN);
        cpuCoreCountSpinner.setMaximum(ADBConstants.CPU_CORE_COUNT_MAX);
        cpuCoreCountSpinner.setIncrement(ADBConstants.CPU_CORE_COUNT_INCREMENT);
        // default value
        cpuCoreCountSpinner.setSelection(instance.getCpuCoreCount());
        
        Label label1 = new Label(container, SWT.NULL);
        label1.setText("&STORAGE (TB):");
        
        storageInTBSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
        GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
        storageInTBSpinner.setLayoutData(gd1);
        storageInTBSpinner.setMinimum(ADBConstants.STORAGE_IN_TB_MIN);
        storageInTBSpinner.setMaximum(ADBConstants.STORAGE_IN_TB_MAX);
        storageInTBSpinner.setIncrement(ADBConstants.STORAGE_IN_TB_INCREMENT);
        // default value
        storageInTBSpinner.setSelection(instance.getDataStorageSizeInTBs());
        
        if(!(instance.getIsDedicated() != null && instance.getIsDedicated())) {
        	Label autoScalingLabel = new Label(container, SWT.NULL);
            autoScalingLabel.setText("&AUTO SCALING:");
            autoScalingEnabledCheckBox = new Button(container, SWT.CHECK);
            GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
            autoScalingEnabledCheckBox.setLayoutData(gd5);
            autoScalingEnabledCheckBox.setSelection(instance.getIsAutoScalingEnabled().booleanValue());
        }
        
        setControl(container);
    }

    public String getCPUCoreCount() {
        return cpuCoreCountSpinner.getText();
    }
    
    public String getDataStorageInTBText() {
    	return storageInTBSpinner.getText();
    }
    
	public Boolean isAutoScalingEnabled() {
		if (instance.getIsDedicated() != null && instance.getIsDedicated())
			return null;
		return autoScalingEnabledCheckBox.getSelection();
	}

}