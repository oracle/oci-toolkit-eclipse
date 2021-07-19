/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
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
import com.oracle.oci.eclipse.ui.explorer.database.validate.InputValidator;
import com.oracle.oci.eclipse.ui.explorer.database.validate.PasswordInputValidator;

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
    private Text adminPasswordText;
    private Text confirmAdminPasswordText;
    private Button saveToSecureStoreBtn;

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

    private Map<String, String> containertMap = new TreeMap<String, String>();
    private DbWorkload workloadType;

    private final Object lock = new Object();
    private boolean isDedicatedSelected = false;

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
        layout.verticalSpacing = 9;

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
		Composite compPasswordPanel = new Composite(container, SWT.NONE);
		GridLayout passwordLayout = new GridLayout(3, false);
		compPasswordPanel.setLayout(passwordLayout);
		GridDataFactory.defaultsFor(compPasswordPanel).span(1,1).grab(true, true)
			.align(SWT.FILL, SWT.FILL).applyTo(compPasswordPanel);
		adminPasswordText = new Text(compPasswordPanel, SWT.BORDER | SWT.PASSWORD);
		GridData gd7 = new GridData(GridData.FILL_HORIZONTAL );
		adminPasswordText.setLayoutData(gd7);
		ModifyListener passwordListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateStatus(validate());
			}
		};
		adminPasswordText.addModifyListener(passwordListener);
		Button generateBtn = new Button(compPasswordPanel, SWT.PUSH);
		generateBtn.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
		generateBtn.setToolTipText("Generate");

		Button copyToClipboard = new Button(compPasswordPanel, SWT.PUSH);
		copyToClipboard.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
		copyToClipboard.setToolTipText("Copy to Clipboard");
		copyToClipboard.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DBUtils.copyPasswordToClipboard(e.display, adminPasswordText.getText());
            }
        });

		Label confirmAdminPasswordLabel = new Label(container, SWT.NULL);
		confirmAdminPasswordLabel.setText("&Confirm password:");
		confirmAdminPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
		confirmAdminPasswordText.setLayoutData(gd8);
		confirmAdminPasswordText.addModifyListener(passwordListener);
		generateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final String newPass = generateAdminPassword();
                adminPasswordText.setText(newPass);
                confirmAdminPasswordText.setText(newPass);
            }
        });

        new Label(container, SWT.NONE);
        this.saveToSecureStoreBtn = new Button(container, SWT.CHECK);
        saveToSecureStoreBtn.setText("Save password to Eclipse Secure Store");
        saveToSecureStoreBtn.setSelection(true);
        GridDataFactory.defaultsFor(saveToSecureStoreBtn).applyTo(saveToSecureStoreBtn);
        
        new Label(container, SWT.NULL);
        Label passwordRule1 = new Label(container, SWT.NULL);
        passwordRule1.setText("Password must be 12 to 30 characters and contain at least one uppercase letter,\n"
                + " one lowercase letter, and one number. The password cannot contain the double \n"
                + "quote (\") character or the username \"admin\".");
        passwordRule1.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        // GridDataFactory.defaultsFor(passwordRule1).span(2, 1).align(SWT.END,
        // SWT.BEGINNING).applyTo(passwordRule1);

        if (workloadType == DbWorkload.Oltp) {
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

        if (workloadType != DbWorkload.Ajd) {
            alwaysFreeCheckButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    alwaysFreeButtonSelectionAction(event, container);
                }
            });
        }

        setControl(container);
        isInitialized = true;
    }

    private void serverlessButtonSelection(Composite container) {
        synchronized (lock) {
            if (!isDedicatedSelected)
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
            if (isDedicatedSelected)
                return;
            isDedicatedSelected = true;
        }

        // dispose ATP-S specific widgets
        alwaysFreeLabel.dispose();
        alwaysFreeCheckButton.dispose();
        if (alwaysFreeStorageInTBText != null)
            alwaysFreeStorageInTBText.dispose();

        autoScalingLabel.dispose();
        autoScalingEnabledCheckBox.dispose();
        licenseTypeLabel.dispose();
        licenseTypeGroup.dispose();
        cpuCoreCountSpinner.setEnabled(true);
        if (storageInTBSpinner.isDisposed()) {
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
		if(workloadType == DbWorkload.Ajd)
	      return;
		
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

    public String getAdminPassword() {
        return adminPasswordText.getText();
    }

    public String getConfirmAdminPassword() {
        return confirmAdminPasswordText.getText();
    }

    public boolean isStoreAdminPassword() {
        return this.saveToSecureStoreBtn.getSelection();
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
                for (IStatus child : multiStatus.getChildren()) {
                    if (!child.isOK()) {
                        fullStatus = new Status(child.getSeverity(), getClass(), child.getMessage());
                    }
                }
            }
        }
        if (isInitialized) {
            if (!fullStatus.isOK()) {
                // don't update status if we're not initialized yet
                setErrorMessage(fullStatus.getMessage());
                setPageComplete(false);
            } else {
                setErrorMessage(null);
                setPageComplete(true);
            }
        } else {
            setMessage("You must enter an admin password");
        }
    }

    public MultiStatus validate() {
        MultiStatus multiStatus = new MultiStatus(getClass(), 0, "");
        IStatus status = validatePasswords();
        if (!status.isOK()) {
            multiStatus.add(status);
        }
        return multiStatus;
    }

    private static final InputValidator<String> passwordInputValidator = new InputValidator<String>() {
        @Override
        public IStatus validate(String inputValue) {
            if (inputValue == null || inputValue.isBlank()) {
                return error("Passwords can't be empty");
            }
            if (inputValue.length() < 12 || inputValue.length() > 30) {
                return error("Passwords must be between 12 and 30 characters long");
            }
            boolean containsAtLeastOneUpper = false;
            boolean containsAtLeastOneLower = false;
            boolean containsAtLeastOneNumber = false;
            for (char character : inputValue.toCharArray()) {
                if (Character.isUpperCase(character)) {
                    containsAtLeastOneUpper = true;
                } else if (Character.isLowerCase(character)) {
                    containsAtLeastOneLower = true;
                } else if (Character.isDigit(character)) {
                    containsAtLeastOneNumber = true;
                } else {
                    if (character == '"') {
                        return error("Passwords must be between 12 and 30 characters long");
                    }
                }
            }
            if (!containsAtLeastOneLower) {
                return error("Password must contain at least one lower case character");
            }
            if (!containsAtLeastOneUpper) {
                return error("Password must contain at least one upper case character");
            }
            if (!containsAtLeastOneNumber) {
                return error("Password must contain at least one number");
            }
            if (inputValue.toLowerCase().contains("admin")) {
                return error("Passwords cannot contain the word 'admin' in any case");
            }
            return Status.OK_STATUS;
        }

    };

    private final PasswordInputValidator passwordValidator = new PasswordInputValidator(passwordInputValidator);

    private IStatus validatePasswords() {
        IStatus status = validatePassword(this.adminPasswordText);
        if (!status.isOK()) {
            return status;
        }
        String password = this.adminPasswordText.getText();
        String confirmPassword = this.confirmAdminPasswordText.getText();
        if (!password.equals(confirmPassword)) {
            return error("Confirmed password must match password");
        }
        return Status.OK_STATUS;
    }

    private IStatus validatePassword(Text source) {
        return passwordValidator.validate(source);
    }

    private final static List<Character> PASSWORD_CHARS;
    private final static List<Character> LOWER_CASE = new ArrayList<>(26);
    private final static List<Character> UPPER_CASE = new ArrayList<>(26);
    private final static List<Character> DIGITS = new ArrayList<>(10);
    private final static List<?> AT_LEAST_ONE[] = {LOWER_CASE, UPPER_CASE, DIGITS};

    static {
        List<Character> characters = new ArrayList<>();
        for (int i = (int) 'a'; i <= (int) 'z'; i++) {
            LOWER_CASE.add((char) i);
        }

        for (int i = (int) 'A'; i <= (int) 'Z'; i++) {
            UPPER_CASE.add((char) i);
        }

        for (int i = (int) '0'; i <= (int) '9'; i++) {
            DIGITS.add((char) i);
        }

        characters.addAll(LOWER_CASE);
        characters.addAll(UPPER_CASE);
        characters.addAll(DIGITS);
        characters.addAll(Arrays.asList('!', '@', '#','$', '%', '^', '&', '*', '(', ')', '+', '=', '-', '?'));

        PASSWORD_CHARS = Collections.unmodifiableList(characters);
    }

    private String generateAdminPassword() {
        final int minSize = 12;
        final int maxSize = 30;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int length = random.nextInt(minSize, maxSize + 1);
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++)
        {
            int nextInt = random.nextInt(0, PASSWORD_CHARS.size());
            password.append(PASSWORD_CHARS.get(nextInt));
        }
        
        // get a list of three unique random indices that are within the
        // password length
        List<Integer>  checkOffsets = new ArrayList<>();
        while(checkOffsets.size() < 3)
        {
            int nextInt = random.nextInt(0, length);
            if (!checkOffsets.contains(nextInt)) {
                checkOffsets.add(nextInt);
            }
        }
        
        // for each offset, check the character at that offset.  If it's
        // not one of the mandatory ones, replace it.
        for (int i = 0; i < 3; i++)
        {
            Integer index = checkOffsets.get(i);
            char charAt = password.charAt(index);
            List<?> allowed = AT_LEAST_ONE[i];
            if (!allowed.contains(charAt))
            {
                int nextInt = random.nextInt(0, allowed.size());
                @SuppressWarnings("unchecked")
                Character allowedChar = ((List<Character>)allowed).get(nextInt);
                password.setCharAt(index, allowedChar);
            }
        }
        return password.toString();
    }

    private static IStatus error(String message) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
    }
}
