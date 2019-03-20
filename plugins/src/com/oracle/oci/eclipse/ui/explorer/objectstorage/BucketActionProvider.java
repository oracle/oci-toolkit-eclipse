/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.CommonActionProvider;

import com.oracle.bmc.objectstorage.model.BucketSummary;

import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.CreateBucketAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.OpenBucketAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.RefreshBucketAction;

public class BucketActionProvider extends CommonActionProvider{

    @Override
    public void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

        BucketSummary bucket = null;
        if (selection.size() == 1 && selection.toList().get(0) instanceof BucketSummary) {
            bucket = (BucketSummary)selection.toList().get(0);
        }
        menu.add(new RefreshBucketAction());
        menu.add(new Separator());
        if (bucket != null ) {
            menu.add(new OpenBucketAction(bucket));
            menu.add(new Separator());
        }
        menu.add(new CreateBucketAction());
    }
}
