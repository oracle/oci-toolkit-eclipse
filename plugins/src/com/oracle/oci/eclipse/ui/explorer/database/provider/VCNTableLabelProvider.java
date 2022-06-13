package com.oracle.oci.eclipse.ui.explorer.database.provider;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.core.model.Vcn;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlRowHolder;
import com.oracle.oci.eclipse.ui.explorer.database.model.OcidBasedAccessControlType;

public class VCNTableLabelProvider extends BaseLabelProvider implements ITableLabelProvider, ITableColorProvider {

    public VCNTableLabelProvider(/*PropertyListeningArrayList<VcnWrapper> vcns*/)
    {
        
    }
    
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof AccessControlRowHolder) {
            OcidBasedAccessControlType aclType = 
                    (OcidBasedAccessControlType) ((AccessControlRowHolder) element).getAclType();
            switch (columnIndex) {
            case 0:
                
                if (((AccessControlRowHolder)element).isFullyLoaded())
                {
                    Vcn vcn = aclType.getVcn();
                    if (vcn != null)
                    {
                        return vcn.getDisplayName();
                    }
                }
                else if (((AccessControlRowHolder)element).isNew()) {
                    return "New ACL";
                }
                return "Loading..";
            case 1:
                return aclType.getIPListAsString();
            case 2:
                return aclType.getOcid();
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
        if (((AccessControlRowHolder) element).getAclType().isValueValid() != null) {
            return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        }
        return null;
    }

}
