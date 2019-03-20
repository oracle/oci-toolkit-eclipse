/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import static com.oracle.bmc.util.internal.FileUtils.expandUserHome;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.Region;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.ConfigFileOperations;
import com.oracle.oci.eclipse.account.ConfigFileOperations.ConfigFile;
import com.oracle.oci.eclipse.account.PreferencesWrapper;

public class ConfigurationElementsUI {

    private Combo profileCombo;
    private Combo regionCombo;

    private Text userOcidText;
    private Text keyFileText;
    private Text tenancyOcidText;
    private Text fingerprintText;
    private Text passphraseText;

    private Text configFileText;

    private Label newProfileLabel;
    private Text newProfileText;

    private Button keyFileButton;
    private Button saveButton;

    private Text connectionTimeoutText;
    private Text readTimeoutText;

    private ConfigFile config = null;
    private int phx_idx = 0;

    private final String ADD_PROFILE = "Add Profile";

    static Pattern pv=Pattern.compile("\\$\\{(\\w+)\\}");

    /*
     * os.path.expanduser
     */
    public static String expanduser(String path) {
        String user=System.getProperty("user.home");

        return path.replaceFirst("~", user);
    }//expanduser

    /*
     * os.path.expandvars
     */
    public static String expandvars(String path) {
        String result=new String(path);

        Matcher m=pv.matcher(path);
        while(m.find()) {
            String var=m.group(1);
            String value=System.getenv(var);
            if (value!=null)
                result=result.replace("${"+var+"}", value);
        }
        return result;
    }//expandvars


    public String getProfileName() {
        return profileCombo.getText();
    }

    public String getRegionName() {
        return regionCombo.getText();
    }

    public String getUserOcid() {
        return userOcidText.getText();
    }

    public String getKeyFileName() {
        return keyFileText.getText();
    }

    public String getTenancyOcid() {
        return tenancyOcidText.getText();
    }

    public String getFingerPrint() {
        return fingerprintText.getText();
    }

    public String getPassphrase() {
        return passphraseText.getText();
    }

    public String getConfigFileName() {
        return configFileText.getText();
    }

    public String getConnectionTimeout() {
        return connectionTimeoutText.getText();
    }

    public String getReadTimeout() {
        return readTimeoutText.getText();
    }

    public boolean saveProfile(Shell shell) {
        try {
            if (!saveButton.isEnabled())
                return true;

            if (! validateValues(shell,
                    newProfileText.getText(),
                    userOcidText.getText(),
                    keyFileText.getText(),
                    fingerprintText.getText(),
                    passphraseText.getText(),
                    tenancyOcidText.getText()))
                return false;

            config = ConfigFileOperations.parse(configFileText.getText());

            // 4. Profile Name does not already exist
            if (config.getProfileNames().contains(newProfileText)) {
                MessageDialog.openError(shell, "Form Error ", "Profile already exists " + newProfileText);
                return false;
            }

            Map<String, String> newProfile = new HashMap<String, String>();
            newProfile.put("user", userOcidText.getText());
            newProfile.put("key_file", keyFileText.getText());
            newProfile.put("fingerprint", fingerprintText.getText());
            if (!passphraseText.getText().isEmpty())
                newProfile.put("pass_phrase", passphraseText.getText());
            newProfile.put("tenancy", tenancyOcidText.getText());

            for (Region region: Region.values()) {
                if (region.toString().equals(regionCombo.getText().toUpperCase().replace('-', '_'))) {
                    newProfile.put("region", regionCombo.getText());;
                    break;
                }
            }

            File f = new File(expandUserHome(configFileText.getText()));
            if(!f.exists()) {
                config.update("DEFAULT", newProfile);
                ConfigFileOperations.save(configFileText.getText(), config, "DEFAULT");
            }

            config.update(newProfileText.getText(), newProfile);
            ConfigFileOperations.save(configFileText.getText(), config, newProfileText.getText());

            saveButton.setEnabled(false);
            profileComboChanged(shell, newProfileText.getText().toUpperCase());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }
        return true;
    }

    public Control drawElements(Composite parent) {
        Composite topLevelContainer = new Composite(parent, SWT.NULL);
        GridLayout topLevelLayout = new GridLayout();
        topLevelContainer.setLayout(topLevelLayout);

        /* Top Section */
        Group group = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        group.setText("Configuration File");
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setLayout(new GridLayout(1, true));

        Composite innerTopContainer = new Composite(group, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 3;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(innerTopContainer, SWT.NULL);
        label.setText("&FileName: ");

        String expandedFilename = expanduser(expandvars(PreferencesWrapper.getConfigFileName()));

        configFileText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        configFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        configFileText.setText(expandedFilename);
        configFileText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                configFileChanged(topLevelContainer.getShell());
            }
        });

        Button button = new Button(innerTopContainer, SWT.PUSH);
        button.setText("Browse...");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                configFileText.setText(handleBrowse(innerTopContainer.getShell(), configFileText.getText(), null));
            }
        });

        /* Middle Section */
        group = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        group.setText("Parameters");
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setLayout(new GridLayout(1, true));

        Composite innerContainer = new Composite(group, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 3;
        innerContainer.setLayout(innerLayout);
        innerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Profile:");

        profileCombo = new Combo(innerContainer, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        profileCombo.add(ADD_PROFILE);
        profileCombo.select(0);
        profileCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                profileComboChanged(topLevelContainer.getShell(), profileCombo.getText());
            }
        });

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        newProfileLabel = new Label(innerContainer, SWT.NULL);
        newProfileLabel.setText("&Profile Name:");

        newProfileText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        newProfileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Region:");

        regionCombo = new Combo(innerContainer, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        int index = 0;
        for (Region region: Region.values()) {
            String regionId = region.getRegionId();
            regionCombo.add(regionId);
            if (regionId == RegionOptions.DEFAULT_REGION)
                phx_idx = index;
            index++;
        }
        regionCombo.select(phx_idx);
        regionCombo.setEnabled(false);

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&User OCID:");

        userOcidText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        userOcidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userOcidText.setEditable(false);

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Tenancy OCID:");

        tenancyOcidText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        tenancyOcidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tenancyOcidText.setEditable(false);

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Private Key File:");

        keyFileText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        keyFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        keyFileText.setEditable(false);

        keyFileButton = new Button(innerContainer, SWT.PUSH);
        keyFileButton.setText("Browse...");
        keyFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                keyFileText.setText(handleBrowse(innerContainer.getShell(), keyFileText.getText(), "*.pem"));
            }
        });
        keyFileButton.setEnabled(false);


        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Fingerprint:");

        fingerprintText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        fingerprintText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fingerprintText.setEditable(false);

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("&Passphrase:");

        passphraseText = new Text(innerContainer, SWT.BORDER | SWT.SINGLE);
        passphraseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        passphraseText.setEditable(false);

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        label = new Label(innerContainer, SWT.NULL);
        label.setText("");

        saveButton = new Button(innerContainer, SWT.PUSH);
        saveButton.setText("Save Profile");
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveProfile(innerContainer.getShell());
            }
        });
        saveButton.setEnabled(false);

        /* Bottom Section */
        group = new Group(topLevelContainer, SWT.SHADOW_ETCHED_IN);
        group.setText("Client Side Options");
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setLayout(new GridLayout(1, true));

        Composite innerBottomContainer = new Composite(group, SWT.NONE);
        GridLayout innerBottomLayout = new GridLayout();
        innerBottomLayout.numColumns = 2;
        innerBottomContainer.setLayout(innerBottomLayout);
        innerBottomContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(innerBottomContainer, SWT.NULL);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText("&Connection Timeout (in milliseconds):");

        connectionTimeoutText = new Text(innerBottomContainer, SWT.BORDER | SWT.SINGLE);
        connectionTimeoutText.setText("3000");
        connectionTimeoutText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                //dialogChanged(connectionTimeoutText);
            }
        });

        label = new Label(innerBottomContainer, SWT.NULL);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText("&Read Timeout (in milliseconds):");

        readTimeoutText = new Text(innerBottomContainer, SWT.BORDER | SWT.SINGLE );
        readTimeoutText.setText("60000");
        readTimeoutText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                //dialogChanged(readTimeoutText);
            }
        });

        /* Footer */
        Composite footerContainer = new Composite(topLevelContainer, SWT.NONE);
        GridLayout footerLayout = new GridLayout();
        footerLayout.numColumns = 4;
        footerContainer.setLayout(footerLayout);
        footerContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(footerContainer, SWT.NULL);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText("");

        label = new Label(footerContainer, SWT.NULL);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText("");

        configFileChanged(topLevelContainer.getShell());
        return innerContainer;
    }

    private boolean validateValues(Shell shell, String profileName, String userOcidText, String keyFileText, String fingerprintText, String passphraseText, String tenancyOcidText) {

        // Validate the following:
        // 2. Valid Ocids (userOcid, tenancyOcid)
        if (!(userOcidText.startsWith("ocid") && (userOcidText.contains("user")))) {
            MessageDialog.openError(shell, "Form Error ", "Enter valid User OCID. Not a valid User OCID: " + userOcidText);
            return false;
        }

        if (!(tenancyOcidText.startsWith("ocid") && (tenancyOcidText.contains("tenancy")))) {
            MessageDialog.openError(shell, "Form Error ", "Enter valid Tenancy OCID. Not a valid Tenancy OCID: " + tenancyOcidText);
            return false;
        }

        // 5. Fingerprint is in valid format
        String fingerprintRegex = "([0-9a-f]{2}:){15}[0-9a-f]{2}";
        if (!fingerprintText.matches(fingerprintRegex)) {
            MessageDialog.openError(shell, "Form Error ", "Enter valid fingerprint. Not a valid fingerprint: " + fingerprintRegex);
            return false;
        }

        // 3. Key file name is a valid file
        File keyFile = new File(keyFileText);
        if (!keyFile.exists()) {
            MessageDialog.openError(shell, "Form Error ", "Enter valid key file. The provided file does not exist: " + keyFileText);
            return false;
        }

        return true;
    }

    private void configFileChanged(Shell shell) {
        File f = new File(configFileText.getText());
        if (f.length() > 50000) {
            MessageDialog.openError(shell, "File Error", "File is too large");
            return;
        }
        profileComboChanged(shell, PreferencesWrapper.getProfile());
    }

    private void resetProfileFields(boolean enabled) {
        if (enabled) {
            userOcidText.setText("");
            tenancyOcidText.setText("");
            fingerprintText.setText("");
            passphraseText.setText("");
            keyFileText.setText("");
            regionCombo.select(phx_idx);
        }

        userOcidText.setEditable(enabled);
        tenancyOcidText.setEditable(enabled);
        fingerprintText.setEditable(enabled);
        passphraseText.setEditable(enabled);
        keyFileText.setEditable(enabled);
        keyFileButton.setEnabled(enabled);
        regionCombo.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }

    private void populateProfileFieldsFromConfig(Shell shell, String profileName) {
        boolean add_profile = false, config_file_absent = false;
        try {
            config = ConfigFileOperations.parse(configFileText.getText(), profileName);
        } catch (IOException e1) {
            ErrorHandler.logError("Config File not found");
            config_file_absent = true;
            add_profile = true;
        } catch (IllegalStateException e) {
            MessageDialog.openError(shell, "File Read Error", "Unable to parse config within file");
            add_profile = true;
        } catch (IllegalArgumentException e) {
            profileName = "DEFAULT";
            PreferencesWrapper.setProfile(profileName);
            try {
                config = ConfigFileOperations.parse(configFileText.getText(), profileName);
            } catch (IOException e1) {
                ErrorHandler.logError("Config File not found");
                config_file_absent = true;
                add_profile = true;
            } catch (IllegalStateException e2) {
                MessageDialog.openError(shell, "File Read Error", "Unable to parse config within file");
                add_profile = true;
            }
        }

        profileCombo.removeAll();
        profileCombo.add(ADD_PROFILE);
        profileCombo.select(0);

        if (! config_file_absent) {
            int defaultSelection = 0;
            for (String profile: config.getProfileNames()) {
                profileCombo.add(profile);
                defaultSelection++;
                if (profile.equals(profileName)) {
                    profileCombo.select(defaultSelection);
                }
            }
        }

        if (add_profile) {
            resetProfileFields(true);
            newProfileText.setText("");
            newProfileText.setVisible(true);
            newProfileLabel.setVisible(true);
        } else {
            userOcidText.setText(config.get("user"));
            tenancyOcidText.setText(config.get("tenancy"));
            fingerprintText.setText(config.get("fingerprint"));
            String passphrase = config.get_if_present("pass_phrase");
            if (passphrase == null) {
                passphraseText.setText("");
            } else {
                passphraseText.setText(passphrase);
            }

            keyFileText.setText(expanduser(expandvars(config.get("key_file"))));
            int count = 0;
            for (Region region: Region.values()) {
                if (region.getRegionId().equals(config.get("region"))) {
                    regionCombo.select(count);
                    break;
                }
                count++;
            }
            saveButton.setEnabled(false);
        }

    }

    private void profileComboChanged(Shell shell, String profileName) {
        if (profileName.equals(ADD_PROFILE)) {
            resetProfileFields(true);
            newProfileText.setText("");
            newProfileText.setVisible(true);
            newProfileLabel.setVisible(true);
        } else {
            resetProfileFields(false);
            newProfileText.setVisible(false);
            newProfileLabel.setVisible(false);
            populateProfileFieldsFromConfig(shell, profileName);
        }
    }

    private String handleBrowse(Shell shell, String path, String filter) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        if (filter != null)
            dialog.setFilterExtensions(new String [] {filter});
        dialog.setFilterPath(path);
        return dialog.open();
    }

}
