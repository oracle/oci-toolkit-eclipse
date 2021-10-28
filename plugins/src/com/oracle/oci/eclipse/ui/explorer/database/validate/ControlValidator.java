/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.validate;

import org.eclipse.core.runtime.IStatus;

public abstract class ControlValidator<CONTROLTYPE, INPUTTYPE> {

    private InputValidator<INPUTTYPE> inputValidator;
    public ControlValidator(InputValidator<INPUTTYPE> inputValidator)
    {
        this.inputValidator = inputValidator;
    }
    public IStatus validate(CONTROLTYPE control)
    {
        INPUTTYPE value = getInput(control);
        return inputValidator.validate(value);
    }

    protected abstract INPUTTYPE getInput(CONTROLTYPE control);
}
