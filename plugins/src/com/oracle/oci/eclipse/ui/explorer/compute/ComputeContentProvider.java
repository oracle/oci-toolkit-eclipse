/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.compute;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.oracle.oci.eclipse.ui.explorer.RootElement;

public class ComputeContentProvider implements ITreeContentProvider, IResourceChangeListener
{
    private static ComputeContentProvider instance;
    private TreeViewer treeViewer;

    public ComputeContentProvider() {
        instance = this;
    }
    public static ComputeContentProvider getInstance() {
        return instance;
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
        if (parentElement instanceof RootElement) {
            return new Object[] { new ComputeRootElement() };
        } else if (parentElement instanceof ComputeRootElement) {
            return new Object[] {
                    new BlockStorageElement(),
                    new ComputeInstanceElement()
            };
        } else {
            return new Object[0];
        }
    }

    @Override
    public Object getParent(Object element)
    {
        return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        return (element instanceof RootElement || element instanceof ComputeRootElement);
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
}