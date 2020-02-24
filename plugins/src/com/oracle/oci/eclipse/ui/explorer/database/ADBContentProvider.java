/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import com.oracle.oci.eclipse.ui.explorer.RootElement;
import com.oracle.oci.eclipse.ui.explorer.common.BaseContentProvider;

public class ADBContentProvider extends BaseContentProvider
{
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof RootElement) {
            return new Object[] { new ADBRootElement() };
        } else {
            return new Object[0];
        }
    }

}
