package com.oracle.oci.eclipse.ui.explorer.database.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

//    private static class CellEditor extends 
public abstract class IPBasedAccessControlType extends AccessControlType {


    public IPBasedAccessControlType(IPBasedAccessControlType.Types type) {
        super(AccessControlType.Category.IP_BASED, type);
    }

    public IPBasedAccessControlType.Types getType() {
        return super.getType();
    }

    public String getTypeLabel() {
        return this.getType().getLabel();
    }

    protected List<String> parseOctals(String value) {
        if (value != null && !value.trim().isEmpty()) {
            Matcher matcher = IPAddressType.IP_ADDR_PATTERN.matcher(value);
            if (matcher.matches()) {
                List<String> octals = new ArrayList<>();
                octals.add(matcher.group(1));
                octals.add(matcher.group(2));
                octals.add(matcher.group(3));
                octals.add(matcher.group(4));
                return octals;
            }
        }
        return Collections.emptyList();
    }
}