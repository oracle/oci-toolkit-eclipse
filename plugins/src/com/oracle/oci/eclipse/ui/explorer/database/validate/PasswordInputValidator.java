package com.oracle.oci.eclipse.ui.explorer.database.validate;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Text;

public class PasswordInputValidator extends ControlValidator<Text, String> {

    public PasswordInputValidator(InputValidator<String> inputValidator) {
        super(inputValidator);
    }

    @Override
    protected String getInput(Text control) {
        return control.getText();
    }

}
