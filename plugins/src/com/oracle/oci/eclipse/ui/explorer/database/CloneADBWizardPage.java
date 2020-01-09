package com.oracle.oci.eclipse.ui.explorer.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.LicenseModel;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseCloneDetails.CloneType;

public class CloneADBWizardPage  extends WizardPage {
	private Group cloneTypeGroup;
    private Button fullCloneRadioButton;
    private Button metaDataCloneRadioButton;
	private Text originDBNameText;
    private Text displayNameText;
    private Text databaseNameText;
    private Combo compartmentList;
    private Label alwaysFreeLabel;
	private Button alwaysFreeCheckButton;
    private Spinner cpuCoreCountSpinner;
    private Label storageInTBLabel;
    private Spinner storageInTBSpinner;
    private Text alwaysFreeStorageInTBText;
    private Button autoScalingEnabledCheckBox;
    private Text adminUserNameText;
    private Text adminPasswordText;
    private Text confirmAdminPasswordText;
    private Group licenseTypeGroup;
    private Button licenseTypeOwnRadioButton;
    private Button licenseTypeIncludedRadioButton;
    private ISelection selection;
    private Map<String, String> compartmentMap;
    AutonomousDatabaseSummary sourceInstance;

    public CloneADBWizardPage(ISelection selection, Map<String, String> compartmentMap, AutonomousDatabaseSummary sourceInstance) {
        super("wizardPage");
        setTitle("Create Autonomous Database Clone");
        setDescription("This wizard clones an ADB instance. Please enter the required details.");
        this.selection = selection;
        this.compartmentMap = compartmentMap;
        this.sourceInstance = sourceInstance;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
        
        Label cloneTypeLabel = new Label(container, SWT.NULL);
        cloneTypeLabel.setText("&Choose Clone Type:");
        cloneTypeGroup = new Group(container, SWT.NONE);
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.spacing = 100;
        cloneTypeGroup.setLayout(rowLayout);
        GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
        cloneTypeGroup.setLayoutData(gd10);
        fullCloneRadioButton = new Button(cloneTypeGroup, SWT.RADIO);
        fullCloneRadioButton.setText("Full clone");
        metaDataCloneRadioButton= new Button(cloneTypeGroup, SWT.RADIO);
        metaDataCloneRadioButton.setText("Metadata clone");
        fullCloneRadioButton.setSelection(true);
        
        Label compartmentLabel = new Label(container, SWT.NULL);
        compartmentLabel.setText("&Create in compartment:");
        compartmentList = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData gd0 = new GridData(GridData.FILL_HORIZONTAL);
        compartmentList.setLayoutData(gd0);
        for (String compartmentName: compartmentMap.keySet()) {
        	compartmentList.add(compartmentName);
		}
        compartmentList.select(0);
        
        final String originDBName = sourceInstance.getDisplayName();
        Label originDBNameLabel = new Label(container, SWT.NULL);
        originDBNameLabel.setText("&Origin Database Name:");
        originDBNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd_1 = new GridData(GridData.FILL_HORIZONTAL);
        originDBNameText.setLayoutData(gd_1);
        originDBNameText.setText(originDBName);
        originDBNameText.setEditable(false);
        final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
        final String defaultDBName = DATE_TIME_FORMAT.format(new Date());
        Label displayNameLabel = new Label(container, SWT.NULL);
        displayNameLabel.setText("&Display Name:");
        displayNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        displayNameText.setLayoutData(gd);
        displayNameText.setText("Clone of "+originDBName);
        Label databaseNamelabel = new Label(container, SWT.NULL);
        databaseNamelabel.setText("&Database Name:");
        databaseNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
        databaseNameText.setLayoutData(gd1);
        databaseNameText.setText("DB"+defaultDBName);
        Label dbNameRule = new Label(container, SWT.NULL);
        Label dbNameRule1 = new Label(container, SWT.NULL);
        dbNameRule1.setText(
				"The name must contain only letters and numbers, starting with a letter. Maximum of 14 characters.");
        
        if(sourceInstance.getIsFreeTier() !=null && sourceInstance.getIsFreeTier()) {
	        alwaysFreeLabel = new Label(container, SWT.NULL);
			alwaysFreeLabel.setText("Always Free");
			alwaysFreeCheckButton = new Button(container, SWT.CHECK);
			alwaysFreeCheckButton.setText("Show only Always Free configuration options");
			GridData alwaysFreeGD = new GridData(GridData.FILL_HORIZONTAL);
			alwaysFreeCheckButton.setLayoutData(alwaysFreeGD);
			alwaysFreeCheckButton.addSelectionListener(new SelectionAdapter() {
		        @Override
		        public void widgetSelected(SelectionEvent event) {
		        	alwaysFreeButtonSelectionAction(event, container);
		        }
		    });
        }
        
        Label cpuCoreCountLabel = new Label(container, SWT.NULL);
        cpuCoreCountLabel.setText("&CPU Core Count:");
        cpuCoreCountSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
        GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
        cpuCoreCountSpinner.setLayoutData(gd3);
        
        cpuCoreCountSpinner.setMinimum(ADBConstants.CPU_CORE_COUNT_MIN);
        cpuCoreCountSpinner.setMaximum(ADBConstants.CPU_CORE_COUNT_MAX);
        cpuCoreCountSpinner.setIncrement(ADBConstants.CPU_CORE_COUNT_INCREMENT);
        // default value
        cpuCoreCountSpinner.setSelection(ADBConstants.CPU_CORE_COUNT_DEFAULT);
        
        storageInTBLabel = new Label(container, SWT.NULL);
        storageInTBLabel.setText("&Storage (TB):");
        createStorageInTBSpinner(container);
        
        Label autoScalingLabel = new Label(container, SWT.NULL);
        autoScalingLabel.setText("&Auto Scaling:");
        autoScalingEnabledCheckBox = new Button(container, SWT.CHECK);
        autoScalingEnabledCheckBox.setText("Enable auto scaling");
        GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
        autoScalingEnabledCheckBox.setLayoutData(gd5);
        
        Label adminUserNameLabel = new Label(container, SWT.NULL);
        adminUserNameLabel.setText("&Username:");
        adminUserNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd6 = new GridData(GridData.FILL_HORIZONTAL);
        adminUserNameText.setLayoutData(gd6);
        adminUserNameText.setText("ADMIN");
        adminUserNameText.setEditable(false);
        
        Label adminPasswordLabel = new Label(container, SWT.NULL);
        adminPasswordLabel.setText("&Password:");
        adminPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
        GridData gd7 = new GridData(GridData.FILL_HORIZONTAL);
        adminPasswordText.setLayoutData(gd7);
        Label confirmAdminPasswordLabel = new Label(container, SWT.NULL);
        confirmAdminPasswordLabel.setText("&Confirm password:");
        confirmAdminPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
        GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
        confirmAdminPasswordText.setLayoutData(gd8);
        Label passwordRule = new Label(container, SWT.NULL);
        Label passwordRule1 = new Label(container, SWT.NULL);
        passwordRule1.setText(
				"Password must be 12 to 30 characters and contain at least one uppercase letter,\n"
				+ " one lowercase letter, and one number. The password cannot contain the double \n"
				+ "quote (\") character or the username \"admin\".");
        passwordRule1.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        
        Label licenseTypeLabel = new Label(container, SWT.NULL);
        licenseTypeLabel.setText("&Choose a license type:");
        licenseTypeGroup = new Group(container, SWT.NONE);
        RowLayout rowLayout1 = new RowLayout(SWT.HORIZONTAL);
        rowLayout1.spacing = 100;
        licenseTypeGroup.setLayout(rowLayout1);
        GridData gd9 = new GridData(GridData.FILL_HORIZONTAL);
        licenseTypeGroup.setLayoutData(gd9);
        licenseTypeOwnRadioButton = new Button(licenseTypeGroup, SWT.RADIO);
        licenseTypeOwnRadioButton.setText("Bring Your Own License");
        licenseTypeIncludedRadioButton= new Button(licenseTypeGroup, SWT.RADIO);
        licenseTypeIncludedRadioButton.setText("License Included");
        licenseTypeOwnRadioButton.setSelection(true);
        
        setControl(container);
    }
    
    private void alwaysFreeButtonSelectionAction(SelectionEvent event, Composite container) {
        Button btn = (Button) event.getSource();
        if(btn.getSelection()) {
        	storageInTBSpinner.dispose();
        	alwaysFreeStorageInTBText = new Text(container, SWT.BORDER | SWT.SINGLE);
    		GridData gdFreeStorage = new GridData(GridData.FILL_HORIZONTAL);
    		alwaysFreeStorageInTBText.setLayoutData(gdFreeStorage);
    		alwaysFreeStorageInTBText.setText(ADBConstants.ALWAYS_FREE_STORAGE_TB);
    		alwaysFreeStorageInTBText.setEditable(false);
    		alwaysFreeStorageInTBText.moveBelow(storageInTBLabel);
    		setControl(container);
			container.layout();
			
			cpuCoreCountSpinner.setSelection(ADBConstants.ALWAYS_FREE_CPU_CORE_COUNT);
        	cpuCoreCountSpinner.setEnabled(false);
        	autoScalingEnabledCheckBox.setSelection(false);
        	autoScalingEnabledCheckBox.setEnabled(false);
        	licenseTypeOwnRadioButton.setSelection(false);
        	licenseTypeIncludedRadioButton.setSelection(true);
        	licenseTypeOwnRadioButton.setEnabled(false);
        	licenseTypeIncludedRadioButton.setEnabled(false);
        	licenseTypeGroup.setEnabled(false);
        } else {
        	alwaysFreeStorageInTBText.dispose();
        	createStorageInTBSpinner(container);
    		storageInTBSpinner.moveBelow(storageInTBLabel);
    		setControl(container);
			container.layout();
        	
			cpuCoreCountSpinner.setSelection(ADBConstants.CPU_CORE_COUNT_DEFAULT);
        	cpuCoreCountSpinner.setEnabled(true);
        	autoScalingEnabledCheckBox.setSelection(false);
        	autoScalingEnabledCheckBox.setEnabled(true);
        	licenseTypeIncludedRadioButton.setSelection(false);
        	licenseTypeOwnRadioButton.setSelection(true);
        	licenseTypeOwnRadioButton.setEnabled(true);
        	licenseTypeIncludedRadioButton.setEnabled(true);
        	licenseTypeGroup.setEnabled(true);
        }
	}
    
    private void createStorageInTBSpinner(Composite container) {
    	storageInTBSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
        GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
        storageInTBSpinner.setLayoutData(gd4);
        
        storageInTBSpinner.setMinimum(ADBConstants.STORAGE_IN_TB_MIN);
        storageInTBSpinner.setMaximum(ADBConstants.STORAGE_IN_TB_MAX);
        storageInTBSpinner.setIncrement(ADBConstants.STORAGE_IN_TB_INCREMENT);
        // default value
        storageInTBSpinner.setSelection(ADBConstants.STORAGE_IN_TB_DEFAULT);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getDisplayName() {
        return displayNameText.getText();
    }
    
    public String getDatabaseName() {
    	return databaseNameText.getText();
    }
    
    public String getAdminPassword() {
    	return adminPasswordText.getText();
    }
    
    public String getConfirmAdminPassword() {
    	return confirmAdminPasswordText.getText();
    }
    
    public boolean isAutoScalingEnabled() {
    	return autoScalingEnabledCheckBox.getSelection();
    }
    
	public LicenseModel getLicenseModel() {
		if (licenseTypeIncludedRadioButton.getSelection()) {
			return LicenseModel.LicenseIncluded;
		} else {
			return LicenseModel.BringYourOwnLicense;
		}
	}
	
	public String getCPUCoreCount() {
		return cpuCoreCountSpinner.getText();
	}

	public String getStorageInTB() {
		if(isAlwaysFreeInstance())
			return ADBConstants.ALWAYS_FREE_STORAGE_TB_DUMMY;
		return storageInTBSpinner.getText();
	}
	
	public String getSelectedCompartment() {
		return compartmentList.getText();
	}
	
	public CloneType getCloneType() {
		if (fullCloneRadioButton.getSelection()) {
			return CloneType.Full;
		} else {
			return CloneType.Metadata;
		}
	}
	
	public boolean isAlwaysFreeInstance() {
		return alwaysFreeCheckButton != null && alwaysFreeCheckButton.getSelection();
	}

}