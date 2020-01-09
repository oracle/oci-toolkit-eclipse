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
