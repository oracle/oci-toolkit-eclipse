/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.model.Vnic;
import com.oracle.bmc.core.model.VnicAttachment;
import com.oracle.bmc.core.model.VolumeAttachment;

public class InstanceWrapper {


    private Instance instance;
    private List<VolumeAttachment> volumeAttachmentList = new ArrayList<VolumeAttachment>();
    private VnicAttachment vnicAttachment;
    private Vnic vnic;

    public InstanceWrapper(Instance instance) {
        this.instance = instance;
    }

    public VnicAttachment getVnicAttachment() {
        return vnicAttachment;
    }

    public void setVnicAttachment(VnicAttachment vnicAttachment) {
        this.vnicAttachment = vnicAttachment;
    }

    public Vnic getVnic() {
        return vnic;
    }

    public void setVnic(Vnic vnic) {
        this.vnic = vnic;
    }

    public Instance getInstance() {
        return this.instance;
    }

    public List<VolumeAttachment> getVolumeAttachments() {
        return volumeAttachmentList;
    }

    public void addVolumeAttachment(VolumeAttachment volumeAttachment) {
        this.volumeAttachmentList.add(volumeAttachment);
    }

}
