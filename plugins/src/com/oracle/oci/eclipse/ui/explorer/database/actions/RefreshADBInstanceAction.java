/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.actions;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.database.editor.ADBInstanceTable;

public class RefreshADBInstanceAction extends BaseAction {

    private final ADBInstanceTable objectTable;

    public RefreshADBInstanceAction (ADBInstanceTable table){
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
