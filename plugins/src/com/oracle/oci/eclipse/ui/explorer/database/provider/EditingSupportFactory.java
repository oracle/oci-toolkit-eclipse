package com.oracle.oci.eclipse.ui.explorer.database.provider;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Control;

import com.oracle.bmc.core.model.Vcn;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlRowHolder;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlType;
import com.oracle.oci.eclipse.ui.explorer.database.model.CIDRBlockType;
import com.oracle.oci.eclipse.ui.explorer.database.model.IPAddressType;
import com.oracle.oci.eclipse.ui.explorer.database.model.IPBasedAccessControlType;
import com.oracle.oci.eclipse.ui.explorer.database.model.OcidBasedAccessControlType;
import com.oracle.oci.eclipse.ui.explorer.database.provider.dialog.VcnByNameSelectionWizard;
import com.oracle.oci.eclipse.ui.explorer.database.provider.dialog.VcnIPRestrictionSelectionWizard;

public class EditingSupportFactory {
    public static class IPTypeColumnEditingSupport extends EditingSupport {
        private final static List<AccessControlType.Types> supportedTypes = Stream
                .of(IPBasedAccessControlType.Types.values())
                .filter(e -> e != IPBasedAccessControlType.Types.Unknown && 
                             e != IPBasedAccessControlType.Types.VCN_BY_OCID)
                .collect(Collectors.toList());
        private final static List<String> ipNotationValues = supportedTypes.stream()
                .map(IPBasedAccessControlType.Types::getLabel)
                .collect(Collectors.toList());

        TableViewer ipAddressAclTableViewer;

        public IPTypeColumnEditingSupport(ColumnViewer viewer) {
            super(viewer);
            ipAddressAclTableViewer = (TableViewer) viewer;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof AccessControlRowHolder) {
                if (value instanceof Integer) {
                    Integer index = (Integer) value;
                    if (index < ipNotationValues.size()) {
                        AccessControlType.Types type = supportedTypes.get(index);
                        if (((AccessControlRowHolder) element).getAclType().getType() != type) {
                            IPBasedAccessControlType newType = createAclType(type);
                            ((AccessControlRowHolder) element).setAclType(newType);
                        }
                    }
                }
            }
            ipAddressAclTableViewer.update(element, null);
        }

        private IPBasedAccessControlType createAclType(IPBasedAccessControlType.Types type) {
            switch (type) {
            case CIDR:
                return new CIDRBlockType("");
            case IP:
                return new IPAddressType("");
            default:
                throw new AssertionError("Unknown ACL type");
            }
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof AccessControlRowHolder) {
                return ((AccessControlRowHolder) element).getAclType().getType().ordinal();
            }
            return null;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new ComboBoxCellEditor(ipAddressAclTableViewer.getTable(), ipNotationValues.toArray(new String[0]));
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    public static class IPValueColumnEditingSupport extends EditingSupport {
        TableViewer ipAddressAclTableViewer;

        public IPValueColumnEditingSupport(ColumnViewer viewer) {
            super(viewer);
            ipAddressAclTableViewer = (TableViewer) viewer;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof AccessControlRowHolder) {
                if (value instanceof String) {
                    ((AccessControlRowHolder) element).getAclType().setValue((String) value);
                }
            }
            ipAddressAclTableViewer.update(element, null);
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof AccessControlRowHolder) {
                return ((AccessControlRowHolder) element).getAclType().getValue();
            }
            return null;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            TextCellEditor textCellEditor = new TextCellEditor(this.ipAddressAclTableViewer.getTable());
            return textCellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    public static class VCNTypeColumnEditingSupport extends EditingSupport {
        private final static List<IPBasedAccessControlType.Types> supportedTypes = Stream
                .of(IPBasedAccessControlType.Types.values()).filter(e -> e != IPBasedAccessControlType.Types.Unknown)
                .collect(Collectors.toList());
        private final static List<String> ipNotationValues = supportedTypes.stream().map(Enum::name)
                .collect(Collectors.toList());

        TableViewer ipAddressAclTableViewer;

        public VCNTypeColumnEditingSupport(ColumnViewer viewer) {
            super(viewer);
            ipAddressAclTableViewer = (TableViewer) viewer;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof AccessControlRowHolder) {
                if (value instanceof Integer) {
                    Integer index = (Integer) value;
                    if (index < ipNotationValues.size()) {
                        IPBasedAccessControlType.Types type = supportedTypes.get(index);
                        if (((AccessControlRowHolder) element).getAclType().getType() != type) {
                            IPBasedAccessControlType newType = createAclType(type);
                            ((AccessControlRowHolder) element).setAclType(newType);
                        }
                    }
                }
            }
            ipAddressAclTableViewer.update(element, null);
        }

        private IPBasedAccessControlType createAclType(IPBasedAccessControlType.Types type) {
            switch (type) {
            case CIDR:
                return new CIDRBlockType("");
            case IP:
                return new IPAddressType("");
            default:
                throw new AssertionError("Unknown ACL type");
            }
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof AccessControlRowHolder) {
                return ((AccessControlRowHolder) element).getAclType().getType().ordinal();
            }
            return null;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new ComboBoxCellEditor(ipAddressAclTableViewer.getTable(), ipNotationValues.toArray(new String[0]));
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    public static class VcnDisplayNameColumnEditingSupport extends EditingSupport {

        private TableViewer tableViewer;

        public VcnDisplayNameColumnEditingSupport(TableViewer viewer) {
            super(viewer);
            this.tableViewer = viewer;
            
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new DialogCellEditor(this.tableViewer.getTable()) {
                @Override
                protected Object openDialogBox(Control cellEditorWindow) {
                    final AccessControlRowHolder aclHolder = (AccessControlRowHolder) element;
                    VcnByNameSelectionWizard wizard = 
                        new VcnByNameSelectionWizard(aclHolder);
                    WizardDialog dialog = new WizardDialog(tableViewer.getTable().getShell(), wizard);
                    int open = dialog.open();
                    if (open == Window.OK)
                    {
                        Vcn newVcn = wizard.getNewVcn();
                        if (aclHolder.isNew())
                        {
                            aclHolder.setNew(true);
                            aclHolder.setFullyLoaded(true);
                        }
                        ((OcidBasedAccessControlType)aclHolder.getAclType()).setVcn(newVcn);
                    }
                    return null;
                }
            };
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            AccessControlRowHolder holder = (AccessControlRowHolder) element;
            AccessControlType aclType = holder.getAclType();
            if (aclType instanceof OcidBasedAccessControlType)
            {
                Vcn vcn = ((OcidBasedAccessControlType)aclType).getVcn();
                if (vcn != null)
                {
                     return vcn.getDisplayName();
                }
                else if (holder.isNew())
                {
                    return "New ACL";
                }
            }
            return "Not loaded";
        }

        @Override
        protected void setValue(Object element, Object value) {
            // Do nothing; the set for this is done directly by the cell editor when it returns.
        }

    }
    
    public static class VcnOcidColumnEditingSupport extends EditingSupport
    {
        private TableViewer tableViewer;

        public VcnOcidColumnEditingSupport(TableViewer viewer) {
            super(viewer);
            this.tableViewer = viewer;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new TextCellEditor(this.tableViewer.getTable());
        }

        @Override
        protected boolean canEdit(Object element) {
            return false;
        }

        @Override
        protected Object getValue(Object element) {
            AccessControlRowHolder holder = (AccessControlRowHolder) element;
            AccessControlType aclType = holder.getAclType();
            if (aclType instanceof OcidBasedAccessControlType)
            {
                return ((OcidBasedAccessControlType) aclType).getOcid();
            }
            return "Not loaded";
        }

        @Override
        protected void setValue(Object element, Object value) {
            // TODO Auto-generated method stub
            AccessControlRowHolder holder = (AccessControlRowHolder) element;
            AccessControlType aclType = holder.getAclType();
            if (aclType instanceof OcidBasedAccessControlType)
            {
                ((OcidBasedAccessControlType) aclType).setVcn(null);
                ((OcidBasedAccessControlType) aclType).setOcid((String)value); 
            }
        }
        
    }
    
    public static class VcnIPRestrictionColumnEditingSupport extends EditingSupport {

        private TableViewer tableViewer;

        public VcnIPRestrictionColumnEditingSupport(TableViewer viewer) {
            super(viewer);
            this.tableViewer = viewer;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return new DialogCellEditor(this.tableViewer.getTable()) {
                @Override
                protected Object openDialogBox(Control cellEditorWindow) {
                    final AccessControlRowHolder aclHolder = (AccessControlRowHolder) element; 
                    VcnIPRestrictionSelectionWizard wizard = 
                        new VcnIPRestrictionSelectionWizard(aclHolder); 
                    WizardDialog dialog = new WizardDialog(tableViewer.getTable().getShell(), wizard);
                    int open = dialog.open();
                    if (open == Window.OK)
                    {
                        List<String> newIPList = wizard.getNewIPList();
                        if (aclHolder.isNew())
                        {
                            aclHolder.setNew(false);
                            aclHolder.setFullyLoaded(true);
                        }
                        ((OcidBasedAccessControlType)aclHolder.getAclType()).setIPList(newIPList);
                        tableViewer.refresh(true);
                    }
                    return null;
                }
            };
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            AccessControlRowHolder holder = (AccessControlRowHolder) element;
            AccessControlType aclType = holder.getAclType();
            if (aclType instanceof OcidBasedAccessControlType)
            {
                String ipListAsString = ((OcidBasedAccessControlType)aclType).getIPListAsString();
                if (((OcidBasedAccessControlType) aclType).getVcn() != null)
                {
                     return ipListAsString;
                }
                else if (holder.isNew())
                {
                    return "";
                }
            }
            return "Not loaded";
        }

        @Override
        protected void setValue(Object element, Object value) {
            // Do nothing; the set for this is done directly by the cell editor when it returns.
        }

    }
}
