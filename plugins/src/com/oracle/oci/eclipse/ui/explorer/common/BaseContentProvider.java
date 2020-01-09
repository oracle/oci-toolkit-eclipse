/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.common;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.oracle.oci.eclipse.ui.explorer.RootElement;

public class BaseContentProvider implements ITreeContentProvider, IResourceChangeListener
{
    private static BaseContentProvider instance;
    private TreeViewer treeViewer;

    public BaseContentProvider() {
        instance = this;
    }
    public static BaseContentProvider getInstance() {
        return instance;
    }

    @Override
    public Object getParent(Object element)
    {
        return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        return (element instanceof RootElement);
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        return getChildren(inputElement);
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        this.treeViewer = (TreeViewer) viewer;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        // TODO Auto-generated method stub

    }
    @Override
    public Object[] getChildren(Object parentElement) {
        // TODO Auto-generated method stub
        return null;
    }
}