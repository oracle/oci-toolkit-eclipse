package com.oracle.oci.eclipse.ui.explorer.database.provider.dialog;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.sdkclients.NetworkClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizardPage;
import com.oracle.oci.eclipse.ui.explorer.database.model.AccessControlRowHolder;
import com.oracle.oci.eclipse.ui.explorer.database.model.OcidBasedAccessControlType;

public class VcnByNameSelectionWizard extends Wizard implements INewWizard {

    private final class CompartmentSelectWizardPageExtension extends CompartmentSelectWizardPage {
        private CompartmentSelectWizardPageExtension(ISelection selection, boolean showCurrentCompartment) {
            super(selection, showCurrentCompartment);
            setPageComplete(false);
        }

        @Override
        protected void addCompartmentDrivenUI(Composite container, Tree tree) {
            Composite vcnComp = new Composite(container, SWT.NONE);
            vcnComp.setLayout(new GridLayout(2, false));
            vcnComp.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

            Label vcn = new Label(vcnComp, SWT.NONE);
            vcn.setText("Virtual Cloud Network (VCN):");
            vcn.setLayoutData(new GridData());
            ComboViewer viewer = new ComboViewer(vcnComp);
            viewer.setContentProvider(new IStructuredContentProvider() {
                @Override
                public Object[] getElements(Object inputElement) {
                    if (inputElement instanceof List<?>) {
                        return ((List<?>) inputElement).toArray();
                    }
                    return new Object[0];
                }

            });

            viewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    if (element instanceof Vcn) {
                        return ((Vcn) element).getDisplayName();
                    }
                    return null;
                }
            });
            viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));
 
            viewer.addSelectionChangedListener(new ISelectionChangedListener() {
                
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection structuredSelection = event.getStructuredSelection();
                    Vcn vcn = (Vcn) structuredSelection.getFirstElement();
                    OcidBasedAccessControlType aclType = (OcidBasedAccessControlType) aclHolder.getAclType();
                    // is it a change?
                    String id = aclType.getVcn() != null ? aclType.getVcn().getId() : null;
                    if (vcn.getId() != null && !vcn.getId().equals(id))
                    {
                        newVcn = vcn;
                        setPageComplete(true);
                    }
                    else
                    {
                        setPageComplete(false);
                    }
                }
            });

            tree.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
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
                                    if (listVcns.size() > 0)
                                    {
                                        viewer.setSelection(new StructuredSelection(listVcns.get(0)));
                                    }
                                }
                            });
                        }
                    }).schedule();
                }

            });
        }
    }

    private Vcn newVcn;
    private AccessControlRowHolder aclHolder;
    @SuppressWarnings("unused")
    private Compartment rootCompartment;
    private IStructuredSelection selection;
    private CompartmentSelectWizardPage page;

    public VcnByNameSelectionWizard(AccessControlRowHolder aclHolder) {
        super();
        this.aclHolder = aclHolder;
        setNeedsProgressMonitor(true);
        // this.selectedCompartment = selectedCompartment;
        // this.showCurrentCompartment = showCurrentCompartment;
    }

    @Override
    public void addPages() {
        page = new CompartmentSelectWizardPageExtension(selection, true);
        addPage(page);
    }

//    @Override
//    protected Control createDialogArea(Composite parent) {
//        Composite dialogAreaComp = (Composite) super.createDialogArea(parent);
//        
//        Composite panel = new Composite(dialogAreaComp, SWT.NONE);
//        panel.setLayout(new GridLayout(2, false));
//        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//        
//        Label compartmentLbl = new Label(panel, SWT.NONE);
//        compartmentLbl.setText("Compartment:");
//        createCompartmentViewer(panel);
//        
//        Label vcn = new Label(panel, SWT.NONE);
//        vcn.setText("Virtual Cloud Network (VCN):");
//        createCompartmentViewer(panel);
//
//        return dialogAreaComp;
//    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    public Vcn getNewVcn()
    {
        return newVcn;
    }
}
