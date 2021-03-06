/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.compute;
import com.oracle.oci.eclipse.ui.explorer.RootElement;
import com.oracle.oci.eclipse.ui.explorer.common.BaseContentProvider;

public class ComputeContentProvider extends BaseContentProvider
{
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
    public boolean hasChildren(Object element)
    {
        return (element instanceof RootElement || element instanceof ComputeRootElement);
    }
}