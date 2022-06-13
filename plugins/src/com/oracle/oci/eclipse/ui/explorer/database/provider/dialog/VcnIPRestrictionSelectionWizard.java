package com.oracle.oci.eclipse.ui.explorer.database.provider.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import com.oracle.oci.eclipse.ui.explorer.database.IPAddressPanel;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlRowHolder;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlType;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlType.Category;
import com.oracle.oci.eclipse.ui.explorer.database.model.OcidBasedAccessControlType;
import com.oracle.oci.eclipse.ui.explorer.database.provider.PropertyListeningArrayList;

public class VcnIPRestrictionSelectionWizard extends Wizard implements IWizard {

    private static class VcnIPRestrictionSelectionWizardPage extends WizardPage {

        private IPAddressPanel ipPanel;
        private PropertyListeningArrayList<AccessControlRowHolder> ipConfigs;
        private PropertyChangeListener changeListener;
        private OcidBasedAccessControlType ocidBasedACLType;

        protected VcnIPRestrictionSelectionWizardPage(OcidBasedAccessControlType ocidBasedACLType) {
            super("VCN IP Restriction");
            super.setPageComplete(false);
            this.ocidBasedACLType = ocidBasedACLType;
        }

        @Override
        public void createControl(Composite parent) {
            this.ipPanel = new IPAddressPanel();
            this.ipPanel.createControls(parent);
            this.ipConfigs = AccessControlRowHolder.parseAclsFromText(ocidBasedACLType.getIPList(), Category.IP_BASED);
            this.ipPanel.setInput(ipConfigs);
            this.ipPanel.refresh(true);

            this.changeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateStatus();
                }
            };
            this.ipPanel.getIpConfigs().addPropertyChangeListener(changeListener);
            setControl(ipPanel.getControl());
        }

        protected void updateStatus() {
            getShell().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MultiStatus result = ipPanel.validate();
                    if (!result.isOK()) {
                        setErrorMessage(result.getChildren()[0].getMessage());
                        setPageComplete(false);
                    } else {
                        setErrorMessage(null);
                        setPageComplete(true);
                    }
                    ipPanel.refresh(true);
                }
            });
        }

        @Override
        public void dispose() {
            super.dispose();
            this.ipPanel.getIpConfigs().removePropertyChangeListener(changeListener);
        }
    }

    private VcnIPRestrictionSelectionWizardPage page;
    private AccessControlRowHolder aclHolder;
    private List<String> ipList;

    public VcnIPRestrictionSelectionWizard(AccessControlRowHolder aclHolder) {
        assert aclHolder.getAclType().getType() == AccessControlType.Types.VCN_BY_OCID;
        this.aclHolder = aclHolder;
    }

    @Override
    public void addPages() {
        this.page = new VcnIPRestrictionSelectionWizardPage((OcidBasedAccessControlType)this.aclHolder.getAclType());
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        this.ipList = new ArrayList<>();
        for (AccessControlRowHolder holder : page.ipPanel.getIpConfigs()) {
            ipList.add(holder.getAclType().getValue());
        }
        return true;
    }

    public List<String> getNewIPList() {
        return ipList;
    }

}
