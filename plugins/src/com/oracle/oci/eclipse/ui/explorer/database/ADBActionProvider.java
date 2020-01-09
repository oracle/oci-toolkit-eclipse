package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.CommonActionProvider;

import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase.DbWorkload;
import com.oracle.oci.eclipse.ui.explorer.database.actions.CreateADBInstanceAction;

public class ADBActionProvider extends CommonActionProvider{

    @Override
    public void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
        menu.add(new CreateADBInstanceAction(ADBConstants.CREATE_ADW_INSTANCE, DbWorkload.Dw));
        menu.add(new CreateADBInstanceAction(ADBConstants.CREATE_ATP_INSTANCE, DbWorkload.Oltp));
    }

}
