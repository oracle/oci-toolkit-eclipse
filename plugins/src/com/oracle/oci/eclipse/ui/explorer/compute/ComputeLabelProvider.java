/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.compute;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;

public class ComputeLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider
{
    @Override
    public String getText(Object element)
    {
        if ( element instanceof ComputeRootElement ) {
            return ComputeRootElement.getName();
        } else if ( element instanceof ComputeInstanceElement ){
            return ComputeInstanceElement.getName();
        }  else if (element instanceof BlockStorageElement) {
            return BlockStorageElement.getName();
        }
        return null;
    }

    @Override
    public String getDescription(Object element)
    {
        String text = getText(element);
        return "Double click to open " + text;
    }

    @Override
    public Image getImage(Object element)
    {
        if (element instanceof ComputeRootElement)
        {
            return Activator.getImage(Icons.COMPUTE.getPath());
        }
        else if (element instanceof ComputeInstanceElement)
        {
            return Activator.getImage(Icons.COMPUTE_INSTANCE.getPath());
        }
        else if (element instanceof BlockStorageElement)
        {
            return Activator.getImage(Icons.BLOCK_STORAGE.getPath());
        }
        return null;
    }
}
