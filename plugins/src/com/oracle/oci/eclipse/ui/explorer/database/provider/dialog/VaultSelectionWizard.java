package com.oracle.oci.eclipse.ui.explorer.database.provider.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.oci.eclipse.sdkclients.VaultClient;
import com.oracle.oci.eclipse.ui.explorer.common.BmcCoreModelLabelProvider;

public class VaultSelectionWizard extends Wizard implements INewWizard {

    private final class CompartmentSelectWizardPageExtensionExtension extends CompartmentSelectWizardPageExtension {
        private boolean initialized = false;
        
        private CompartmentSelectWizardPageExtensionExtension(ISelection selection, boolean showCurrentCompartment,
                String labelText) {
            super(selection, showCurrentCompartment, labelText);
        }

        @Override
        protected Composite addCompartmentDrivenUI(Composite container, Tree tree) {
            Composite superComp = super.addCompartmentDrivenUI(container, tree);

            Label dialogControl = new Label(superComp, SWT.NONE);
            dialogControl.setText("Secret Name:");
            dialogControl.setLayoutData(new GridData());
            secretViewer = new ComboViewer(superComp);
            secretViewer.setContentProvider(new IStructuredContentProvider() {
                @Override
                public Object[] getElements(Object inputElement) {
                    if (inputElement instanceof Collection<?>) {
                        return ((Collection<?>) inputElement).toArray();
                    }
                    return new Object[0];
                }

            });

            secretViewer.setLabelProvider(new BmcCoreModelLabelProvider());
            secretViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

            secretViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    newSecretSummary = (SecretSummary) event.getStructuredSelection().getFirstElement();
                }
            });

            return superComp;
        }

        @Override
        protected void handleViewerSelection(IStructuredSelection structuredSelection) {
            Optional<VaultSummary> vault = 
                Optional.ofNullable((VaultSummary)structuredSelection.getFirstElement());
            if (vault.isPresent()) {
                if (!vault.get().equals(newVault))
                {
                    newVault = vault.get();
                    VaultClient client = new VaultClient();
                    Map<String, SecretSummary> listSecretsInVault = client.listSecretsInVault(vault.get(),
                            vault.get().getCompartmentId());
                    Collection<SecretSummary> values = listSecretsInVault.values();
                    secretViewer.setInput(values);
                    if (values.size() > 0) {
                        secretViewer.setSelection(new StructuredSelection(values.toArray()[0]));
                    }
                }
            } else {
                secretViewer.setInput(null);
                secretViewer.setSelection(NO_SELECTION);
                newVault = null;
            }
            
            updateStatus();
            if (!initialized)
            {
                initialized = true;
            }
        }

        @Override
        protected void handleTreeSelection(ComboViewer viewer, SelectionEvent e) {
            Compartment selectedCompartment = getSelectedCompartment();
            Job.create("Get Vaults", new ICoreRunnable() {

                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    VaultClient vaultClient = new VaultClient();
                    Map<String, VaultSummary> vaults = vaultClient.listVaults(selectedCompartment.getId());
                    e.display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            viewer.setInput(vaults.values());
                            ISelection viewerSelection = NO_SELECTION;
                            if (vaults.values().size() > 0) {
                                List<VaultSummary> values = new ArrayList<>(vaults.values());
                                viewerSelection = new StructuredSelection(values.get(0));
                                //newVault = values.get(0);
                            } 
                            viewer.setSelection(viewerSelection);
                            updateStatus();
                        }
                    });
                }
            }).schedule();
        }

        private void updateStatus() {
            if (initialized)
            {
                if (newSecretSummary ==  null) {
                    setErrorMessage("Please select a secret by first selecting a compartment and vault");
                    setPageComplete(false);
                }
                else
                {
                    setErrorMessage(null); 
                    setPageComplete(true);
                }
            }
        }
        
        
    }

    private final static ISelection NO_SELECTION = null;

    private IStructuredSelection selection;
    private CompartmentSelectWizardPageExtension page;
    private VaultSummary newVault;
    private ComboViewer secretViewer;
    private SecretSummary newSecretSummary;
    private SecretSummary curSecretSummary;

    private String secretContent;

    @Override
    public void addPages() {
        page = new CompartmentSelectWizardPageExtensionExtension(selection, true, "Vault:");
        addPage(page);
    }
    
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        SecretSummary secret = (SecretSummary) this.selection.getFirstElement();
        if (secret != null) {
            this.curSecretSummary = secret;
        }
        this.newVault = null;
    }

    @Override
    public boolean performFinish() {
         VaultClient client = new VaultClient();
         this.secretContent = client.getSecretContent(newSecretSummary);
         return true;
    }

    public VaultSummary getNewVault() {
        return newVault;
    }

    public SecretSummary getNewSecretSummary() {
        return newSecretSummary;
    }

    public String getSecretContent() {
        return secretContent;
    }
}
