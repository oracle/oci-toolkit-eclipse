    package com.oracle.oci.eclipse.ui.explorer.database.editor;

import java.util.function.Consumer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.ui.explorer.database.DBUtils;
import com.oracle.oci.eclipse.ui.explorer.database.PasswordUtils;
import com.oracle.oci.eclipse.ui.explorer.database.validate.InputValidator;
import com.oracle.oci.eclipse.ui.explorer.database.validate.PasswordInputValidator;

public class PasswordPanel {

    private Text adminPasswordText;
    private Text confirmAdminPasswordText;
    private Button saveToSecureStoreBtn;
    private ModifyListener passwordModifyListener;

    /**
     * @param parent Expected to have a two column gridlayout.
     */
    public void createControls(Composite container) {
        Label adminPasswordLabel = new Label(container, SWT.NULL);
        adminPasswordLabel.setText("&Password:");
        Composite compPasswordPanel = new Composite(container, SWT.NULL);
        GridLayout passwordLayout = new GridLayout(3, false);
        compPasswordPanel.setLayout(passwordLayout);

        GridDataFactory.defaultsFor(compPasswordPanel).span(1, 1).grab(true, true).align(SWT.FILL, SWT.CENTER)
                .indent(0, 0).applyTo(compPasswordPanel);
        adminPasswordText = new Text(compPasswordPanel, SWT.BORDER | SWT.PASSWORD);
        GridData gd7 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        adminPasswordText.setLayoutData(gd7);

        Button generateBtn = new Button(compPasswordPanel, SWT.PUSH);
        generateBtn.setImage(Activator.getImage(Icons.GENERATE_RANDOM_PASSWORD.getPath()));
        generateBtn.setToolTipText("Generate Random Password");

        Button copyToClipboard = new Button(compPasswordPanel, SWT.PUSH);
        copyToClipboard.setImage(Activator.getImage(Icons.COPY_TO_CLIPBOARD_CUSTOM.getPath()));
        copyToClipboard.setToolTipText("Copy Password to Clipboard");
        copyToClipboard.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DBUtils.copyPasswordToClipboard(e.display, adminPasswordText.getText());
            }
        });
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(copyToClipboard);

        new Label(container, SWT.NULL);
        Label passwordGeneratorInfo = new Label(container, SWT.NULL);
        passwordGeneratorInfo.setText("You can use the 'Generate Random Password' button (with the dice icon)\n"
                + " to generate a password that fits the minimum requirements for the ADMIN password.");
        GridData gData = GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).create();
        passwordGeneratorInfo.setLayoutData(gData);

        Label confirmAdminPasswordLabel = new Label(container, SWT.NULL);
        confirmAdminPasswordLabel.setText("&Confirm password:");
        confirmAdminPasswordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
        GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
        confirmAdminPasswordText.setLayoutData(gd8);
        generateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final String newPass = PasswordUtils.generateAdminPassword();
                adminPasswordText.setText(newPass);
                confirmAdminPasswordText.setText(newPass);
            }
        });

        new Label(container, SWT.NONE);
        this.saveToSecureStoreBtn = new Button(container, SWT.CHECK);
        saveToSecureStoreBtn.setText("Save password to Eclipse Secure Store");
        saveToSecureStoreBtn.setSelection(true);
        GridDataFactory.defaultsFor(saveToSecureStoreBtn).applyTo(saveToSecureStoreBtn);
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

    public void addPasswordModifyListener(Consumer<IStatus> passwordListener) {
        this.passwordModifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                IStatus status = validatePasswords();
                passwordListener.accept(status);
            }
        };
        adminPasswordText.addModifyListener(passwordModifyListener);
        confirmAdminPasswordText.addModifyListener(this.passwordModifyListener);
    }

    public void removePasswordModifyListener() {
        adminPasswordText.removeModifyListener(this.passwordModifyListener);
        confirmAdminPasswordText.removeModifyListener(passwordModifyListener);
    }

    private final PasswordInputValidator passwordValidator = new PasswordInputValidator(passwordInputValidator);

    public IStatus validatePasswords() {
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

    private static final InputValidator<String> passwordInputValidator = new InputValidator<String>() {
        @Override
        public IStatus validate(String inputValue) {
            if (inputValue == null || inputValue.trim().isEmpty()) {
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

    private static IStatus error(String message) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
    }

    public void setPasswords(String newPassword) {
        adminPasswordText.setText(newPassword);
        confirmAdminPasswordText.setText(newPassword);
    }
}
