/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.DbWorkload;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.LicenseModel;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.database.editor.PasswordPanel;

public class CreateADBWizardPage extends WizardPage {

    private Text displayNameText;
    private Text databaseNameText;
    private Label dbNameRule1;
    private Text compartmentText;

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

    private Label licenseTypeLabel;
    private Group licenseTypeGroup;
    private Button licenseTypeOwnRadioButton;
    private Button licenseTypeIncludedRadioButton;

    private Label containerDBCompartmentLabel;
    private Composite adcCompartmentContainer;
    private Text containerDBCompartmentText;
    private Button selectContainerDBCompartmentButton;
    private Label containerDBLabel;
    private Combo containerDBList;
    private Compartment selectedADBCompartment;
    private Compartment selectedContainerDBCompartment;
    private Compartment defaultContainerDBCompartment;

    // private ISelection selection;
    private boolean isInitialized; // = false

    private Combo databaseVersionCbo;
//    private Label version21cWarning;
//    private Label warningIcon;
    private Map<String, String> containertMap = new TreeMap<String, String>();
    private DbWorkload workloadType;

    private final Object lock = new Object();
    private boolean isDedicatedSelected = false;
    private PasswordPanel passwordPanel;

    public CreateADBWizardPage(ISelection selection, DbWorkload workloadType) {
        super("wizardPage");
        setTitle("Create Autonomous Database");
        setDescription("This wizard creates a new Autonomous Database. Please enter the required details.");
        // this.selection = selection;
        this.workloadType = workloadType;
        Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
        this.selectedADBCompartment = rootCompartment;
        this.defaultContainerDBCompartment = rootCompartment;
        setPageComplete(false); // password starts out blank and must be entered to complete
    }

    @Override
    public void createControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        //layout.verticalSpacing = 9;

        Label compartmentLabel = new Label(container, SWT.NULL);
        compartmentLabel.setText("&Choose a compartment:");
        Composite innerTopContainer = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 2;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        compartmentText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        compartmentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        compartmentText.setEditable(false);
        compartmentText.setText(selectedADBCompartment.getName());

        Button compartmentButton = new Button(innerTopContainer, SWT.PUSH);
        compartmentButton.setText("Choose...");
        compartmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleSelectADBCompartmentEvent();
            }
        });

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

        new Label(container, SWT.NULL);
        dbNameRule1 = new Label(container, SWT.NULL);
        dbNameRule1.setText(
                "The name must contain only letters and numbers, starting with a letter. Maximum of 14 characters.");

        createAlwaysFreeControl(container);

        Label databaseVersion = new Label(container, SWT.NULL);
        databaseVersion.setText("Database &Version:");

        Composite versionPanel = new Composite(container, SWT.NONE);
        versionPanel.setLayout(new GridLayout(1, false));
        GridDataFactory.defaultsFor(versionPanel).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(versionPanel);
        databaseVersionCbo = new Combo(versionPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
        databaseVersionCbo.add("19c");
        databaseVersionCbo.select(0);
        GridDataFactory.defaultsFor(databaseVersionCbo).align(SWT.FILL, SWT.CENTER).indent(0,5).grab(true,true)
            .applyTo(databaseVersionCbo);
        databaseVersionCbo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus(validate());
			}
        });

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

		this.passwordPanel = new PasswordPanel();
		passwordPanel.createControls(container);

        Consumer<IStatus> f = new Consumer<IStatus> () {

            @Override
            public void accept(IStatus status) {
                if (status.isOK()) {
                    status = validate();
                }
                updateStatus(status);
            }
        };

		new Label(container, SWT.NULL);
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

		// TODO: should be controlled based on whether account is free tier.
		alwaysFreeCheckButton.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent event) {
	        	alwaysFreeButtonSelectionAction(event, container);
	        }
	    });

        passwordPanel.addPasswordModifyListener(f);

        setControl(container);
        isInitialized = true;
    }

	
	private void serverlessButtonSelection(Composite container) {
		synchronized (lock) {
			if(!isDedicatedSelected)
				return;
			isDedicatedSelected = false;
		}
		
		// dispose ATP-D specific widgets
		containerDBCompartmentLabel.dispose();
		containerDBCompartmentText.dispose();
		selectContainerDBCompartmentButton.dispose();
		adcCompartmentContainer.dispose();
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
		
		adcCompartmentContainer = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 2;
        adcCompartmentContainer.setLayout(innerTopLayout);
        adcCompartmentContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        containerDBCompartmentText = new Text(adcCompartmentContainer, SWT.BORDER | SWT.SINGLE);
        containerDBCompartmentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        containerDBCompartmentText.setEditable(false);
        selectedContainerDBCompartment = defaultContainerDBCompartment;
        containerDBCompartmentText.setText(defaultContainerDBCompartment.getName());

        selectContainerDBCompartmentButton = new Button(adcCompartmentContainer, SWT.PUSH);
        selectContainerDBCompartmentButton.setText("Choose...");
        selectContainerDBCompartmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleSelectContainerDBCompartmentEvent();
            	if(selectedContainerDBCompartment !=null) {
            		containerDBCompartmentText.setText(selectedContainerDBCompartment.getName());
            	}
            	
            }
        });
		
		containerDBLabel = new Label(container, SWT.NULL);
		containerDBLabel.setText("&Database Container:");
		containerDBList = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
		containerDBList.setLayoutData(gd10);
		
		containerDBList.clearSelection();
		containerDBList.removeAll();
		containertMap.clear();
		
		containertMap.putAll(ADBInstanceClient.getInstance()
				.getContainerDatabaseMap(getContainerDBCompartmentId()));
		
		if (containertMap.size() > 0) {
			for (String containerName : containertMap.keySet()) {
				containerDBList.add(containerName);
			}
			containerDBList.select(0);
		}
		
		setControl(container);
		container.layout();

		containerDBCompartmentText.addModifyListener(new ModifyListener(){
		      public void modifyText(ModifyEvent event) {
				if (isDedicatedInfra()) {
					containerDBList.clearSelection();
					containerDBList.removeAll();
					containertMap.clear();
					containertMap.putAll(ADBInstanceClient.getInstance()
							.getContainerDatabaseMap(getContainerDBCompartmentId()));
					if (containertMap.size() > 0) {
						for (String containerName : containertMap.keySet()) {
							containerDBList.add(containerName);
						}
						containerDBList.select(0);
					}
				}
			
		      }
		    });
	}
	
	private void alwaysFreeButtonSelectionAction(SelectionEvent event, Composite container) {
        Button btn = (Button) event.getSource();
        if(btn.getSelection()) {
            // Always Free Selected
            databaseVersionCbo.add("21c");
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
            databaseVersionCbo.remove("21c");
            databaseVersionCbo.select(0);
            //version21cWarning.setText("");
            //warningIcon.setImage(null);

        	alwaysFreeStorageInTBText.dispose();
        	createStorageInTBSpinner(container);
    		storageInTBSpinner.moveBelow(storageInTBLabel);
    		setControl(container);
			container.layout();
        	
			cpuCoreCountSpinner.setSelection(ADBConstants.CPU_CORE_COUNT_DEFAULT);
        	cpuCoreCountSpinner.setEnabled(true);
        	autoScalingEnabledCheckBox.setSelection(true);
        	autoScalingEnabledCheckBox.setEnabled(true);
        	licenseTypeIncludedRadioButton.setSelection(false);
        	licenseTypeOwnRadioButton.setSelection(true);
        	licenseTypeOwnRadioButton.setEnabled(true);
        	licenseTypeIncludedRadioButton.setEnabled(true);
        	licenseTypeGroup.setEnabled(true);
        }
	}
	
	private void createAlwaysFreeControl(Composite container) {
		// TODO: this should be based on the tier of the account

		alwaysFreeLabel = new Label(container, SWT.NULL);
		alwaysFreeLabel.setText("Always Free:");
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
		autoScalingEnabledCheckBox.setSelection(true);  // default is no always free, so default auto-scaling
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
		
		if(workloadType == DbWorkload.Ajd) {
			licenseTypeOwnRadioButton.setEnabled(false);
			licenseTypeIncludedRadioButton.setSelection(true);
		} else {
			licenseTypeOwnRadioButton.setSelection(true);
		}
	}
	
	private void handleSelectADBCompartmentEvent() {
    	Consumer<Compartment> consumer=new Consumer<Compartment>() {
			@Override
			public void accept(Compartment compartment) {
				if (compartment != null) {
					selectedADBCompartment = compartment;
					compartmentText.setText(selectedADBCompartment.getName());
				}
			}
		};
    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
				new CompartmentSelectWizard(consumer, false));
		dialog.setFinishButtonText("Select");
		if (Window.OK == dialog.open()) {
		}
    }
	

    private void handleSelectContainerDBCompartmentEvent() {
        Consumer<Compartment> consumer = new Consumer<Compartment>() {

            @Override
            public void accept(Compartment compartment) {
                selectedContainerDBCompartment = compartment;
            }
        };
        CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                new CompartmentSelectWizard(consumer, false));
        dialog.setFinishButtonText("Select");
        if (Window.OK == dialog.open()) {
        }
    }

    public String getDisplayName() {
        return displayNameText.getText();
    }

    public String getDatabaseName() {
        return databaseNameText.getText();
    }

    public Boolean isAutoScalingEnabled() {
        if (isDedicatedInfra())
            return null;

        return autoScalingEnabledCheckBox.getSelection();
    }

    public LicenseModel getLicenseModel() {
        if (isDedicatedInfra())
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
        if (isAlwaysFreeInstance())
            return ADBConstants.ALWAYS_FREE_STORAGE_TB_DUMMY;
        return storageInTBSpinner.getText();
    }

    public String getADBCompartmentId() {
        return selectedADBCompartment.getId();
    }

    public boolean isDedicatedInfra() {
        if (workloadType == DbWorkload.Oltp) {
            return dedicatedDeploymentRadioButton.getSelection();
        }
        return false;
    }

    public String getContainerDBCompartmentId() {
        if (workloadType == DbWorkload.Oltp && dedicatedDeploymentRadioButton.getSelection()) {
            return selectedContainerDBCompartment.getId();
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
        if ((workloadType == DbWorkload.Ajd)
                || (workloadType == DbWorkload.Oltp && dedicatedDeploymentRadioButton.getSelection())) {
            return false;
        }
        return alwaysFreeCheckButton.getSelection();
    }

    private void updateStatus(IStatus status) {
        IStatus fullStatus = status;
        if (status.isMultiStatus()) {
            if (!status.isOK()) {
                MultiStatus multiStatus = (MultiStatus) status;
                int highestSeverity = IStatus.OK;
                for (IStatus child : multiStatus.getChildren()) {
                    int severity = child.getSeverity();
					if (severity > highestSeverity) {
                        fullStatus = new Status(severity, Activator.PLUGIN_ID, child.getMessage());
                        highestSeverity = severity;
                    }
                }
            }
        }
        if (isInitialized) {
        	setImageDescriptor(null);
            if (!fullStatus.isOK()) {
            	if (fullStatus.getSeverity() == IStatus.ERROR) {
	                // don't update status if we're not initialized yet
	                setErrorMessage(fullStatus.getMessage());
	                setPageComplete(false);
            	}
            	else {
            		setErrorMessage(null);
            		setMessage(fullStatus.getMessage(), fullStatus.getSeverity());
            		setPageComplete(true);
            		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
            	}
            } else {
                setErrorMessage(null);
                setMessage("Create a New ADB Instance", IStatus.OK);
                setPageComplete(true);
            }
        } else {
            setMessage("You must enter an admin password");
        }
    }

    public MultiStatus validate() {
        MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, "", null);
        IStatus status = this.passwordPanel.validatePasswords();
        if (!status.isOK()) {
            multiStatus.add(status);
        }
        if ("21c".equals(this.databaseVersionCbo.getText())) {
        	multiStatus.add(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 
        			"Free Tier Autonomous Databases using Oracle Database 21c cannot currently be upgraded to paid instances."));
        }
        return multiStatus;
    }

    public String getAdminPassword() {
        return passwordPanel.getAdminPassword();
    }

    public boolean isStoreAdminPassword() {
        return passwordPanel.isStoreAdminPassword();
    }

    public String getConfirmAdminPassword() {
        return passwordPanel.getConfirmAdminPassword();
    }

	public String getDatabaseVersion() {
		return databaseVersionCbo.getText();
	}
}
