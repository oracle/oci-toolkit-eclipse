/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.validate;

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
