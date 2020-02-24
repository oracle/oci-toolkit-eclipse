/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.compute.actions;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.compute.editor.InstanceTable;

public class RefreshInstanceAction extends BaseAction {

    private final InstanceTable objectTable;

    public RefreshInstanceAction (InstanceTable table){
        objectTable = table;
    }

    @Override
    public String getText() {
        return "Refresh List";
    }

    @Override
    protected void runAction() {
        objectTable.refresh(true);
    }
}
