/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.common;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class BaseTableLabelProvider implements ITableLabelProvider {
    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        return "";
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }
}
