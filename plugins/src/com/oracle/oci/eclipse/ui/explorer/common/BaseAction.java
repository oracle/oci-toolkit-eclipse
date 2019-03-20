/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.common;

import org.eclipse.jface.action.Action;

public abstract class BaseAction extends Action {

    @Override
    public void run() {
    	runAction();
    }
    
    protected abstract void runAction();
}
