package com.oracle.oci.eclipse.ui.explorer.database.model;

import java.util.regex.Matcher;

public abstract class AccessControlType extends EventSource {
    public enum Category {
        IP_BASED, VCN_BASED;
    }
    public enum Types {
        IP("IP Address"), CIDR("CIDR Block"),  VCN_BY_OCID("VCNByOCID"), Unknown("Unknown ACL Type");

        private String label;

        private Types(String label) {
            this.label = label;
        }
        public String getLabel()
        {
            return this.label;
        }
    }
    
    private final Category category;
    private final Types type;

    public static AccessControlType parseAcl(String aclStr) {
        if (aclStr.startsWith("ocid"))
        {
            return OcidBasedAccessControlType.parseOcidAcl(aclStr);
        }
        Matcher matcher = IPAddressType.IP_ADDR_PATTERN.matcher(aclStr);
        if (matcher.matches()) {
            return new IPAddressType(aclStr);
        } else {
            matcher = CIDRBlockType.CIDR_BLOCK_PATTERN.matcher(aclStr);
            if (matcher.matches()) {
                return new CIDRBlockType(aclStr);
            } else {
                return new UnknownAccessControlType(aclStr);
            }
        }
    }

    protected AccessControlType(Category category, Types type)
    {
        this.category = category;
        this.type = type;
    }

    public Category getCategory() {
        return category;
    }
    
    public Types getType() {
        return type;
    }

    public final void setValue(String value) {
        String oldValue = this.getValue();
        doSetValue(value);
        String newValue = getValue();
        if (!oldValue.equals(newValue)) {
            this.pcs.firePropertyChange("value", oldValue, newValue);
        }
    }

    public abstract String isValueValid();

    public abstract void doSetValue(String value);

    public abstract String getValue();

    public abstract String getTypeLabel();
}
