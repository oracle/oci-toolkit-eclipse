package com.oracle.oci.eclipse.ui.explorer.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.DbWorkload;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.LicenseModel;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;

public class CreateADBWizardPage extends WizardPage {

	private Text displayNameText;
	private Text databaseNameText;
	private Label dbNameRule;
	private Label dbNameRule1;
	private Combo compartmentList;

	private Group deploymentTypeGroup;
	private Button serverlessDeploymentRadioButton;
	private Button dedicatedDeploymentRadioButton;

	private Label alwaysFreeLabel;
	private Button alwaysFreeCheckButton;
	private Spinner cpuCoreCountSpinner;
	private Label storageInTBLabel;
	private Spinner storageInTBSpinner;
	private Text alwaysFreeStorageInTBText;
	private Label autoScalingLabel;
	private Button autoScalingEnabledCheckBox;

	private Label adminUserNameLabel;
	private Text adminUserNameText;
	private Text adminPasswordText;
	private Text confirmAdminPasswordText;

	private Label licenseTypeLabel;
	private Group licenseTypeGroup;
	private Button licenseTypeOwnRadioButton;
	private Button licenseTypeIncludedRadioButton;

	private Label containerDBCompartmentLabel;
	private Combo containerDBCompartmentList;
	private Label containerDBLabel;
	private Combo containerDBList;
	

	private ISelection selection;

	private Map<String, String> compartmentMap;
	private Map<String, String> containertMap = new TreeMap<String, String>();
	private DbWorkload workloadType;
	
	private final Object lock = new Object();
	private boolean isDedicatedSelected = false;

	public CreateADBWizardPage(ISelection selection, Map<String, String> compartmentMap, DbWorkload workloadType) {
		super("wizardPage");
		setTitle("Create Autonomous Database");
		setDescription("This wizard creates a new Autonomous Database. Please enter the required details.");
		this.selection = selection;
		this.compartmentMap = compartmentMap;
		this.workloadType = workloadType;
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		
		Label compartmentLabel = new Label(container, SWT.NULL);
		compartmentLabel.setText("&Choose a compartment:");
		compartmentList = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd0 = new GridData(GridData.FILL_HORIZONTAL);
		compartmentList.setLayoutData(gd0);

		for (String compartmentName : compartmentMap.keySet()) {
			compartmentList.add(compartmentName);
		}
		compartmentList.select(0);

		final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
		final String defaultDBName = DATE_TIME_FORMAT.format(new Date());

		Label displayNameLabel = new Label(container, SWT.NULL);
		displayNameLabel.setText("&Display name:");
		displayNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		displayNameText.setLayoutData(gd);
		displayNameText.setText("DB " + defaultDBName);

		Label databaseNamelabel = new Label(container, SWT.NULL);
		databaseNamelabel.setText("&Database name:");
		databaseNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		databaseNameText.setLayoutData(gd1);
		databaseNameText.setText("DB" + defaultDBName);

		dbNameRule = new Label(container, SWT.NULL);
		dbNameRule1 = new Label(container, SWT.NULL);
		dbNameRule1.setText(
				"The name must contain only letters and numbers, starting with a letter. Maximum of 14 characters.");
		
		createAlwaysFreeControl(container);
		
		
		Label cpuCoreCountLabel = new Label(container, SWT.NULL);
		cpuCoreCountLabel.setText("&CPU core count:");
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
		
		createAutoScalingControl(container);
		
		adminUserNameLabel = new Label(container, SWT.NULL);
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
		passwordRule1.setText("Password must be 12 to 30 characters and contain at least one uppercase letter,\n"
				+ " one lowercase letter, and one number. The password cannot contain the double \n"
				+ "quote (\") character or the username \"admin\".");
		passwordRule1.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		
		if(workloadType == DbWorkload.Oltp) {
			Label deploymentTypeLabel = new Label(container, SWT.NULL);
			deploymentTypeLabel.setText("&Choose a deployment type:");
			deploymentTypeGroup = new Group(container, SWT.NONE);
			RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
	        rowLayout.spacing = 205;
			deploymentTypeGroup.setLayout(rowLayout);
			GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
			deploymentTypeGroup.setLayoutData(gd2);
			serverlessDeploymentRadioButton = new Button(deploymentTypeGroup, SWT.RADIO);
			serverlessDeploymentRadioButton.setText("Shared Infrastructure");
			dedicatedDeploymentRadioButton = new Button(deploymentTypeGroup, SWT.RADIO);
			dedicatedDeploymentRadioButton.setText("Dedicated Infrastructure");
			serverlessDeploymentRadioButton.setSelection(true);
		}
		
		createLicenseTypeControl(container);

		if (workloadType == DbWorkload.Oltp) {
			SelectionListener serverlessListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					serverlessButtonSelection(container);
				};
			};
			SelectionListener dedicatedListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					dedicatedButtonSelection(container);
				};
			};
			serverlessDeploymentRadioButton.addSelectionListener(serverlessListener);
			dedicatedDeploymentRadioButton.addSelectionListener(dedicatedListener);
		}
		
		alwaysFreeCheckButton.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent event) {
	        	alwaysFreeButtonSelectionAction(event, container);
	        }
	    });

		setControl(container);
	}
	
	private void serverlessButtonSelection(Composite container) {
		synchronized (lock) {
			if(!isDedicatedSelected)
				return;
			isDedicatedSelected = false;
		}
		
		// dispose ATP-D specific widgets
		containerDBCompartmentLabel.dispose();
		containerDBCompartmentList.dispose();
		containerDBLabel.dispose();
		containerDBList.dispose();
		containertMap.clear();
		
		createAlwaysFreeControl(container);
		alwaysFreeLabel.moveBelow(dbNameRule1);
		alwaysFreeCheckButton.moveBelow(alwaysFreeLabel);
		
		createAutoScalingControl(container);
		autoScalingEnabledCheckBox.moveAbove(adminUserNameLabel);
		autoScalingLabel.moveAbove(autoScalingEnabledCheckBox);
		
		createLicenseTypeControl(container);
		
		alwaysFreeCheckButton.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent event) {
	        	alwaysFreeButtonSelectionAction(event, container);
	        }
	    });
		
		setControl(container);
		container.layout();
	}
	
	private void dedicatedButtonSelection(Composite container) {
		synchronized (lock) {
			if(isDedicatedSelected)
				return;
			isDedicatedSelected = true;
		}
		
		// dispose ATP-S specific widgets
		alwaysFreeLabel.dispose();
		alwaysFreeCheckButton.dispose();
		if(alwaysFreeStorageInTBText != null)
			alwaysFreeStorageInTBText.dispose();
		
		autoScalingLabel.dispose();
		autoScalingEnabledCheckBox.dispose();
		licenseTypeLabel.dispose();
		licenseTypeGroup.dispose();
		cpuCoreCountSpinner.setEnabled(true);
		if(storageInTBSpinner.isDisposed()) {
			// re-create the storage spinner
			createStorageInTBSpinner(container);
			storageInTBSpinner.moveBelow(storageInTBLabel);
		} else {
			storageInTBSpinner.setEnabled(true);
		}
		
		containerDBCompartmentLabel = new Label(container, SWT.NULL);
		containerDBCompartmentLabel.setText("&ADC Compartment:");
		containerDBCompartmentList = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd11 = new GridData(GridData.FILL_HORIZONTAL);
		containerDBCompartmentList.setLayoutData(gd11);
		
		for (String compartmentName : compartmentMap.keySet()) {
			containerDBCompartmentList.add(compartmentName);
		}
		containerDBCompartmentList.select(0);
		
		containerDBLabel = new Label(container, SWT.NULL);
		containerDBLabel.setText("&Database Container:");
		containerDBList = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
		containerDBList.setLayoutData(gd10);
		
		containerDBList.clearSelection();
		containerDBList.removeAll();
		containertMap.clear();
		
		containertMap.putAll(ADBInstanceClient.getInstance()
				.getContainerDatabaseMap(compartmentMap.get(getContainerDBCompartment())));
		
		if (containertMap.size() > 0) {
			for (String containerName : containertMap.keySet()) {
				containerDBList.add(containerName);
			}
			containerDBList.select(0);
		}
		
		setControl(container);
		container.layout();

		SelectionListener compartmentListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (isDedicatedInfra()) {
					containerDBList.clearSelection();
					containerDBList.removeAll();
					containertMap.clear();
					containertMap.putAll(ADBInstanceClient.getInstance()
							.getContainerDatabaseMap(compartmentMap.get(getContainerDBCompartment())));
					if (containertMap.size() > 0) {
						for (String containerName : containertMap.keySet()) {
							containerDBList.add(containerName);
						}
						containerDBList.select(0);
					}
				}
			};
		};
		
		containerDBCompartmentList.addSelectionListener(compartmentListener);
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
	
	private void createAlwaysFreeControl(Composite container) {
		alwaysFreeLabel = new Label(container, SWT.NULL);
		alwaysFreeLabel.setText("Always Free");
		alwaysFreeCheckButton = new Button(container, SWT.CHECK);
		alwaysFreeCheckButton.setText("Show only Always Free configuration options");
		GridData alwaysFreeGD = new GridData(GridData.FILL_HORIZONTAL);
		alwaysFreeCheckButton.setLayoutData(alwaysFreeGD);
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
	
	private void createAutoScalingControl(Composite container) {
		autoScalingLabel = new Label(container, SWT.NULL);
		autoScalingLabel.setText("&Auto Scaling:");
		autoScalingEnabledCheckBox = new Button(container, SWT.CHECK);
		autoScalingEnabledCheckBox.setText("Enable auto scaling");
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		autoScalingEnabledCheckBox.setLayoutData(gd5);
	}
	
	private void createLicenseTypeControl(Composite container) {
		licenseTypeLabel = new Label(container, SWT.NULL);
		licenseTypeLabel.setText("&Choose a license type:");
		licenseTypeGroup = new Group(container, SWT.NONE);
		RowLayout rowLayout1 = new RowLayout(SWT.HORIZONTAL);
        rowLayout1.spacing = 100;
		licenseTypeGroup.setLayout(rowLayout1);
		GridData gd9 = new GridData(GridData.FILL_HORIZONTAL);
		licenseTypeGroup.setLayoutData(gd9);
		licenseTypeOwnRadioButton = new Button(licenseTypeGroup, SWT.RADIO);
		licenseTypeOwnRadioButton.setText("Bring Your Own License");
		licenseTypeIncludedRadioButton = new Button(licenseTypeGroup, SWT.RADIO);
		licenseTypeIncludedRadioButton.setText("License Included");
		licenseTypeOwnRadioButton.setSelection(true);
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

	public Boolean isAutoScalingEnabled() {
		if(isDedicatedInfra())
			return null;
		
		return autoScalingEnabledCheckBox.getSelection();
	}

	public LicenseModel getLicenseModel() {
		if(isDedicatedInfra())
			return null;
		
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

	public String getADBCompartment() {
		return compartmentList.getText();
	}

	public boolean isDedicatedInfra() {
		if (workloadType == DbWorkload.Oltp) {
			return dedicatedDeploymentRadioButton.getSelection();
		}
		return false;
	}
	
	public String getContainerDBCompartment() {
		if (workloadType == DbWorkload.Oltp && dedicatedDeploymentRadioButton.getSelection()) {
			return containerDBCompartmentList.getText();
		}
		return null;
	}

	public String getSelectedContainerDbId() {
		if (workloadType == DbWorkload.Oltp && dedicatedDeploymentRadioButton.getSelection()) {
			return containertMap.get(containerDBList.getText());
		}
		return null;
	}
	
	public boolean isAlwaysFreeInstance() {
		if (workloadType == DbWorkload.Oltp && dedicatedDeploymentRadioButton.getSelection()) {
			return false;
		}
		return alwaysFreeCheckButton.getSelection();
	}

}
