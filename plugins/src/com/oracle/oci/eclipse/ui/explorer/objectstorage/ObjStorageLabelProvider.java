/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;

import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;

public class ObjStorageLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider
{
    @Override
    public String getText(Object element)
    {
        if ( element instanceof BucketSummary ) {
            return ((BucketSummary)element).getName();
        }

        if ( element instanceof StorageRootElement ) {
            return StorageRootElement.getName();
        }
        return null;
    }

    @Override
    public String getDescription(Object element)
    {
        String text = getText(element);
        if (element instanceof BucketSummary)
        {
            return "Object Storage Bucket: " + text;
        }
        return text;
    }

    @Override
    public Image getImage(Object element)
    {
        if (element instanceof StorageRootElement)
        {
            return Activator.getImage(Icons.OBJECT_STORAGE.getPath());
        } else if (element instanceof BucketSummary)
        {
            return Activator.getImage(Icons.BUCKET.getPath());
        }
        return null;
    }
}
