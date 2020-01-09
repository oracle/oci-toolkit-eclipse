package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StopADBWizardPage extends WizardPage {
    private ISelection selection;

    public StopADBWizardPage(ISelection selection) {
        super("wizardPage");
        setTitle("Stop Autonomous Database");
        setDescription("");
        this.selection = selection;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        final String msg = "Are you sure you want to stop the Autonomous Database?";
        Label terminationMsgLabel = new Label(container, SWT.NULL);
        terminationMsgLabel.setText(msg);
        setControl(container);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

}
