package com.oracle.oci.eclipse.ui.explorer.database.model;

public class UnknownAccessControlType extends IPBasedAccessControlType {
    private String rawValue;

    public UnknownAccessControlType(String aclStr) {
        super(Types.Unknown);
        this.rawValue = aclStr;
    }

    @Override
    public String getValue() {
        return rawValue;
    }

    @Override
    public void doSetValue(String value) {
        throw new AssertionError();
    }

    @Override
    public String isValueValid() {
        return "Unknown ACL value: "+rawValue;
    }
}