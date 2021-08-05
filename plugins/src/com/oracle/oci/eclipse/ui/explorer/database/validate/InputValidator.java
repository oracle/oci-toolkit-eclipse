package com.oracle.oci.eclipse.ui.explorer.database.validate;

import org.eclipse.core.runtime.IStatus;

public abstract class InputValidator<INPUTTYPE> {
    public abstract IStatus validate(INPUTTYPE inputValue);
}
