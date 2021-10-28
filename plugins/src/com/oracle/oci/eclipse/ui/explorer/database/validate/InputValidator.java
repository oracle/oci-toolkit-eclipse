/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.validate;

import org.eclipse.core.runtime.IStatus;

public abstract class InputValidator<INPUTTYPE> {
    public abstract IStatus validate(INPUTTYPE inputValue);
}
