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
