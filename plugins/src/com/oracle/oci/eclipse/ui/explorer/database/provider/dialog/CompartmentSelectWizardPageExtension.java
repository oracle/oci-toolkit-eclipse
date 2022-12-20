package com.oracle.oci.eclipse.ui.explorer.database.provider.dialog;

import java.util.Collection;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizardPage;
import com.oracle.oci.eclipse.ui.explorer.common.BmcCoreModelLabelProvider;

public abstract class CompartmentSelectWizardPageExtension extends CompartmentSelectWizardPage {
    private String labelText;

    CompartmentSelectWizardPageExtension(ISelection selection, boolean showCurrentCompartment, String labelText) {
        super(selection, showCurrentCompartment);
        setPageComplete(false);
        this.labelText = labelText;
    }

    @Override
    protected Composite addCompartmentDrivenUI(Composite container, Tree tree) {
        Composite vcnComp = new Composite(container, SWT.BORDER);
        vcnComp.setLayout(new GridLayout(2, false));
        vcnComp.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

        Label dialogControl = new Label(vcnComp, SWT.NONE);
        dialogControl.setText(labelText);
        dialogControl.setLayoutData(new GridData());
        ComboViewer viewer = new ComboViewer(vcnComp);
        viewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof Collection<?>) {
                    return ((Collection<?>) inputElement).toArray();
                }
                return new Object[0];
            }

        });

        viewer.setLabelProvider(new BmcCoreModelLabelProvider());
        viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection structuredSelection = event.getStructuredSelection();
                handleViewerSelection(structuredSelection);
            }
        });

        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleTreeSelection(viewer, e);
            }
        });
        
        return vcnComp;
    }

    protected abstract void handleTreeSelection(ComboViewer viewer, SelectionEvent e);

    protected abstract void handleViewerSelection(IStructuredSelection selection);
}