package com.oracle.oci.eclipse.ui.explorer.database.model;

import java.util.List;
import java.util.regex.Pattern;

public class IPAddressType extends IPBasedAccessControlType {
    public final static Pattern IP_ADDR_PATTERN = Pattern.compile("(\\d+).(\\d+).(\\d+).(\\d+)");

    private List<String> octalStrings;
    private String strValue;

    public IPAddressType(String asStr) {
        super(Types.IP);
        this.strValue = asStr;
        this.octalStrings = parseOctals(this.strValue);
    }

    @Override
    public String getValue() {
        return strValue;
    }

    @Override
    public void doSetValue(String value) {
        this.strValue = value;
        this.octalStrings = parseOctals(value);
    }


    public String isValueValid() {
        return isValid(this.octalStrings);
    }

    public static String isValid(List<String> octals) {
        if (octals != null && octals.size() == 4)
        {
            OCTAL_LOOP: for (String octalStr : octals)
            {
                try
                {
                    Integer octal = Integer.valueOf(octalStr);
                    if (octal < 0 || octal > 255)
                    {
                        break OCTAL_LOOP;
                    }
                }
                catch (NumberFormatException e)
                {
                    break OCTAL_LOOP;
                }
            }
            return null;
        }
        return "IP Address must be of the form 192.168.0.5.  Each number must be between 0 and 255.";
    }
}