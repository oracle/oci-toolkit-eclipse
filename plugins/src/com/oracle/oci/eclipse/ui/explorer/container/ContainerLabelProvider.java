/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;

public class ContainerLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider
{
    @Override
    public String getText(Object element)
    {
        if ( element instanceof ContainerRootElement ) {
            return ContainerRootElement.getName();
        } else if ( element instanceof ContainerClustersElement ){
            return ContainerClustersElement.getName();
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
        if (element instanceof ContainerRootElement)
        {
            return Activator.getImage(Icons.CONTAINER.getPath());
        }
        else if (element instanceof ContainerClustersElement)
        {
            return Activator.getImage(Icons.CONTAINER.getPath());
        }
        return null;
    }
}
