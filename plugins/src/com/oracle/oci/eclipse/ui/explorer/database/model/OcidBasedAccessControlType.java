package com.oracle.oci.eclipse.ui.explorer.database.model;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oracle.bmc.OCID;
import com.oracle.bmc.core.model.Vcn;

public class OcidBasedAccessControlType extends VCNBasedAccessControlType {

    @SuppressWarnings("unused")
    private String value;
    private String ocid;
    private List<String> ipList;
    private Vcn vcn;

    public OcidBasedAccessControlType(String ocid, List<String> ipList) {
        super(AccessControlType.Types.VCN_BY_OCID);
        this.ocid = ocid;
        this.ipList = ipList;
    }

    @Override
    public String isValueValid() {
        if (OCID.isValid(getOcid()))
        {
            return null;
        }
        return "OCID must be of the form: : ocid1.<RESOURCE TYPE>.<REALM>.[REGION].<UNIQUE ID>";
    }

    @Override
    public void doSetValue(String value) {
        this.value = value;
        OcidBasedAccessControlType newType = parseOcidAcl(value);
        if (newType != null)
        {
            this.ocid = newType.ocid;
            this.ipList = newType.ipList;
        }
    }

    @Override
    public String getValue() {
        StringBuilder builder = new StringBuilder(ocid);
        if (!ipList.isEmpty())
        {
            builder.append(";");
            buildIPListIfPresent(builder);
        }
        return builder.toString();
    }

    public void buildIPListIfPresent(StringBuilder builder) {
        if (!ipList.isEmpty())
        {
            for (String ip : this.ipList)
            {
                builder.append(ip);
                builder.append(";");
            }
            // remove the last trailing comma
            builder.deleteCharAt(builder.length()-1);
        }
    }

    public String getOcid() {
        return ocid;
    }

    public Vcn getVcn() {
        return vcn;
    }

    public void setVcn(Vcn vcn) {
        Vcn oldVcn = this.vcn;
        String oldOid = this.ocid;
        this.vcn = vcn;
        if (vcn != null)
        {
            this.ocid = vcn.getId();
        }
        else
        {
            this.ocid = null;
        }
        //this.ipList = Collections.emptyList();
        if (oldVcn != this.vcn)
        {
            this.pcs.firePropertyChange(new PropertyChangeEvent(this, "vcn", oldVcn, this.vcn));
        }
        this.pcs.firePropertyChange(new PropertyChangeEvent(this, "ocid", oldOid, this.ocid));
    }

    public List<String> getIPList() {
        if (this.ipList != null && !this.ipList.isEmpty())
        {
            return Collections.unmodifiableList(this.ipList);
        }
        return Collections.emptyList();
    }

    public void setIPList(List<String> newIPList) {
        if (ipList == null || ipList.isEmpty())
        {
            ipList = new ArrayList<>();
        }
        else
        {
            this.ipList.clear();
        }
        this.ipList.addAll(newIPList);
    }

    public String getIPListAsString()
    {
        if (this.ipList != null && !this.ipList.isEmpty())
        {
            return String.join(";", this.ipList.toArray(new String[0]));
        }
        return "";
    }
    public static OcidBasedAccessControlType parseOcidAcl(String aclStr) {
        if (aclStr.startsWith("ocid"))
        {
            String ocid = aclStr;
            List<String> ipList = new ArrayList<>();
            int firstSemi = aclStr.indexOf(';');
            if (firstSemi > -1)
            {
                ocid = aclStr.substring(0, firstSemi);
                String ipListStr = aclStr.substring(firstSemi+1);
                String[] splitOnSemi = ipListStr.split(";");
                for (String ip : splitOnSemi)
                {
                    ipList.add(ip);
                }
            }
            return new OcidBasedAccessControlType(ocid, ipList);
        }
        return null;
    }

    public void setOcid(String newOcid) {
        String oldOcid = newOcid;
        this.ocid = newOcid;
        if ((oldOcid == null && this.ocid != null) ||
            !oldOcid.equals(this.ocid))
        {
            this.pcs.firePropertyChange(new PropertyChangeEvent(this, "ocid", oldOcid, this.ocid));
        }
    }

}
