package com.oracle.oci.eclipse.ui.explorer.database.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CIDRBlockType extends IPBasedAccessControlType {
    public final static Pattern CIDR_BLOCK_PATTERN = Pattern.compile("(\\d+).(\\d+).(\\d+).(\\d+)/(\\d+)");

    @SuppressWarnings("unused")
    private String block; 
    private List<String> octalStrings;
    private String cidrStr;

    public CIDRBlockType(String cidrStr) {
        super(Types.CIDR);
        this.cidrStr = cidrStr;
        parseCidrBlock();
    }

    private void parseCidrBlock() {
        if (this.cidrStr != null && !this.cidrStr.trim().isEmpty()) {
            Matcher matcher = CIDR_BLOCK_PATTERN.matcher(this.cidrStr);
            if (matcher.matches()) {
                this.octalStrings = new ArrayList<>();
                this.octalStrings.add(matcher.group(1));
                this.octalStrings.add(matcher.group(2));
                this.octalStrings.add(matcher.group(3));
                this.octalStrings.add(matcher.group(4));
                this.block = matcher.group(5);
                return;
            }
        }
        this.octalStrings = Collections.emptyList();
        this.block = "";
    }

    @Override
    public String getValue() {
        return this.cidrStr;
//            if (this.octalStrings != null && !this.octalStrings.isEmpty() && this.block != null
//                    && !this.block.isEmpty()) {
//                String str = StringUtils.join(this.octalStrings, ".");
//                str += "/" + block;
//                return str;
//            }
//            return "";
    }

    @Override
    public void doSetValue(String value) {
        String oldStr = getValue();
        this.cidrStr = value;
        parseCidrBlock();
        if (!oldStr.equals(this.cidrStr)) {
            this.pcs.firePropertyChange("value", oldStr, this.cidrStr);
        }
    }

    @SuppressWarnings("unused")
    public String isValueValid() {
        return isValid(getValue());
    }

    public static String isValid(String cidrStr) {
        if (cidrStr != null && !cidrStr.trim().isEmpty()) {
            Matcher matcher = CIDR_BLOCK_PATTERN.matcher(cidrStr);
            if (matcher.matches()) {
                return null;
            }
        }
        return "CIDR block must be of the form 10.0.0.1/16";
    }
}