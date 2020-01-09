package com.oracle.oci.eclipse.ui.explorer.database;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.ErrorHandler;

public class CreateADBConnectionWizard  extends Wizard implements INewWizard {

    private CreateADBConnectionWizardPage page;
    private IStructuredSelection selection;
    private AutonomousDatabaseSummary adbInstance;

	public CreateADBConnectionWizard(AutonomousDatabaseSummary adbInstance) {
		super();
		this.adbInstance = adbInstance;
		setNeedsProgressMonitor(true);
	}

    @Override
    public void addPages() {
        page = new CreateADBConnectionWizardPage(adbInstance);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
    	final String user = page.getUserName();
    	final String password = page.getPassword();
    	final String walletLocation = page.getWalletDirectory();
    	final String aliasName = page.getSelectedAlias();
    	
    	if(!validateInput(user, password, walletLocation, aliasName))
    		return false;
    	
    	IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					ConfigureADBConnectionProfile.createConnectionProfile(monitor, adbInstance, user, password,walletLocation, aliasName);
				} catch (Exception e) {
					ErrorHandler.logErrorStack("Error occured while creating connection to database: " + adbInstance.getDbName(), e);
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
        };
        
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error occured while creating connection to database: "+adbInstance.getDbName(), realException.getMessage());
            return false;
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Error occured while creating connection to database: "+adbInstance.getDbName(), e.getMessage());
            return false;
        }

        return true;
    }
    
    private boolean validateInput(final String user, final String password, final String walletLocation, final String aliasName) {
    	if(user == null || user.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Database user required error",
					"Database user name cannot be empty");
			return false;
        }
    	
    	if(password == null || password.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Password required error",
					"User's password cannot be empty");
			return false;
        }
    	
    	if(walletLocation == null || walletLocation.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Wallet location required error",
					"Wallet location cannot be empty");
			return false;
        }
    	
    	if(aliasName == null || aliasName.trim().equals("")) {
        	MessageDialog.openError(getShell(), "Alias name required error",
					"Unable to find matching tnsnames alias for the database, make sure wallet location is correct");
			return false;
        }
    	
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
