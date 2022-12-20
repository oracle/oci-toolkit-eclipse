package com.oracle.oci.eclipse.ui.explorer.common;

import org.eclipse.jface.viewers.LabelProvider;

import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.keymanagement.model.Vault;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.vault.model.SecretSummary;

public class BmcCoreModelLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof Vcn) {
            return ((Vcn) element).getDisplayName();
        }
        else if (element instanceof Vault) {
            String displayName = ((Vault)element).getDisplayName();
            if (displayName == null) {
                displayName = ((Vault)element).getId();
            }
            return displayName;
        }
        else if (element instanceof VaultSummary) {
           return ((VaultSummary)element).getDisplayName();
        }
        else if (element instanceof SecretSummary) {
            return ((SecretSummary)element).getSecretName();
        }
        return null;
    }

}
