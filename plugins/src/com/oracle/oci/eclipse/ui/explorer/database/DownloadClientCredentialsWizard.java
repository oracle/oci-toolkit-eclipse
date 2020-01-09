package com.oracle.oci.eclipse.ui.explorer.database;

import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;

public class DownloadClientCredentialsWizard  extends Wizard implements INewWizard {

    private DownloadClientCredentialsWizardPage page;
    private ISelection selection;
    private AutonomousDatabaseSummary instance;
    Map<String, AutonomousDatabaseWallet> walletTypeMap;

	public DownloadClientCredentialsWizard(final AutonomousDatabaseSummary instance,
			Map<String, AutonomousDatabaseWallet> walletTypeMap) {
		super();
		setNeedsProgressMonitor(true);
		this.instance = instance;
		this.walletTypeMap = walletTypeMap;
	}

    @Override
    public void addPages() {
        page = new DownloadClientCredentialsWizardPage(selection, instance, walletTypeMap);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
	public boolean performFinish() {
		// No work required here, it delegates the work to DownloadWalletWizard or
		// RotateWalletWizard.
		return true;
	}

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

}