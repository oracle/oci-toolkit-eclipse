/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ui.explorer.objectstorage.ObjStorageContentProvider;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.StorageRootElement;

public class NavigatorTreeViewerListener implements ITreeViewerListener{

    boolean firstTimeExpand = false;

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        Action treeExpandedAction = new Action() {
            @Override
            public void run() {
                Object obj = event.getElement();
                if (obj != null &&  obj instanceof StorageRootElement) {
                    if(firstTimeExpand) {
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                ObjStorageContentProvider.getInstance().getBucketsAndRefresh();
                            }
                        });
                    } else {
                        firstTimeExpand = true;
                    }
                }

            }
        };
        treeExpandedAction.run();
    }
}
