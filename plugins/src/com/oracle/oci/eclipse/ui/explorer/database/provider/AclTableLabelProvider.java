package com.oracle.oci.eclipse.ui.explorer.database.provider;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlRowHolder;

public class AclTableLabelProvider extends BaseLabelProvider implements ITableLabelProvider, ITableColorProvider {
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof AccessControlRowHolder) {
            switch (columnIndex) {
            case 0:
                return ((AccessControlRowHolder) element).getAclType().getTypeLabel();
            case 1:
                return ((AccessControlRowHolder) element).getAclType().getValue();
            }
        }
        return "";
    }

    @Override
    public Color getForeground(Object element, int columnIndex) {
        return null;
    }

    @Override
    public Color getBackground(Object element, int columnIndex) {
        if (columnIndex == 1)
        {
            if (((AccessControlRowHolder)element).getAclType().isValueValid() != null)
            {
                return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
            }
        }
        return null;
    }

}