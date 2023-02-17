package com.oracle.oci.eclipse.ui.explorer.database.provider.dialog;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.sdkclients.NetworkClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizardPage;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlRowHolder;
import com.oracle.oci.eclipse.ui.explorer.database.model.OcidBasedAccessControlType;

public class VcnByNameSelectionWizard extends Wizard implements INewWizard {

    Vcn newVcn;
    AccessControlRowHolder aclHolder;
    @SuppressWarnings("unused")
    private Compartment rootCompartment;
    private IStructuredSelection selection;
    private CompartmentSelectWizardPage page;

    public VcnByNameSelectionWizard(AccessControlRowHolder aclHolder) {
        super();
        this.aclHolder = aclHolder;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        page = new CompartmentSelectWizardPageExtension(selection, true, "Virtual Cloud Network (VCN):") {
            @Override
            protected void handleViewerSelection(IStructuredSelection structuredSelection) {
                Vcn vcn = (Vcn) structuredSelection.getFirstElement();
                OcidBasedAccessControlType aclType = 
                    (OcidBasedAccessControlType) aclHolder.getAclType();
                // is it a change?
                String id = aclType.getVcn() != null ? aclType.getVcn().getId() : null;
                if (vcn.getId() != null && !vcn.getId().equals(id)) {
                    newVcn = vcn;
                    setPageComplete(true);
                } else {
                    setPageComplete(false);
                }
            }

            @Override
            protected void handleTreeSelection(ComboViewer viewer, SelectionEvent e) {
                Compartment selectedCompartment = getSelectedCompartment();
                Job.create("Get VCNs", new ICoreRunnable() {

                    @Override
                    public void run(IProgressMonitor monitor) throws CoreException {
                        NetworkClient networkClient = new NetworkClient();
                        List<Vcn> listVcns = networkClient.listVcns(selectedCompartment.getId());
                        e.display.asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                viewer.setInput(listVcns);
                                if (listVcns.size() > 0) {
                                    viewer.setSelection(new StructuredSelection(listVcns.get(0)));
                                }
                            }
                        });
                    }
                }).schedule();
            }

        };
        addPage(page);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    public Vcn getNewVcn() {
        return newVcn;
    }
}
