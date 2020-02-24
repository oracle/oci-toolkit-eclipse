/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;


public class ExplorerNavigator extends CommonNavigator
{
    @Override
    protected IAdaptable getInitialInput()
    {
        return new RootElement();
    }

    @Override
    protected CommonViewer createCommonViewerObject(Composite aParent) {
        CommonViewer viewer = super.createCommonViewerObject(aParent);
        viewer.setUseHashlookup(true);
        return viewer;
    }

    @Override
    protected void initListeners(TreeViewer viewer) {
        super.initListeners(viewer);
        viewer.addTreeListener(new NavigatorTreeViewerListener());
        viewer.addDoubleClickListener(new NavigatorDoubleClick(viewer));
    }

}
