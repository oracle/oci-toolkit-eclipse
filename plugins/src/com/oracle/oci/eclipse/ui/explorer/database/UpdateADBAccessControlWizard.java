package com.oracle.oci.eclipse.ui.explorer.database;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

import com.oracle.bmc.database.model.AutonomousDatabase;
import com.oracle.bmc.database.model.AutonomousDatabase.LifecycleState;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.sdkclients.ADBInstanceClient;
import com.oracle.oci.eclipse.ui.explorer.database.UpdateADBAccessControlWizardPage.UpdateState;

public class UpdateADBAccessControlWizard extends Wizard {

    private AutonomousDatabaseSummary instance;
    private UpdateADBAccessControlWizardPage updateADBAccessControlPage;

    public UpdateADBAccessControlWizard(final AutonomousDatabaseSummary instance) {
        super();
        this.instance = instance;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        this.updateADBAccessControlPage = new UpdateADBAccessControlWizardPage(instance);
        addPage(updateADBAccessControlPage);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        final UpdateState curState = new UpdateState(instance.getWhitelistedIps(), instance.getIsMtlsConnectionRequired());
        boolean configureAccess = updateADBAccessControlPage.getConfigureAccess();
        List<String> whitelistIps = null;
        boolean requireMTLS;
        if (configureAccess)
        {
            whitelistIps = updateADBAccessControlPage.getWhitelistedIps();
            requireMTLS = updateADBAccessControlPage.isMTLSRequired();
        }
        else
        {
            whitelistIps = Collections.emptyList();
            requireMTLS = true;
        }
        final UpdateState newState = new UpdateState(whitelistIps, requireMTLS);
        final Runnable updateMTLS = new Runnable() {
            @Override
            public void run() {
                ADBInstanceClient.getInstance().updateRequiresMTLS(instance, requireMTLS);
            }
        };
        final List<String> whitelistIpsFinal = whitelistIps;
        final Runnable updateWhitelistIps = new Runnable() {
            @Override
            public void run() {
                ADBInstanceClient.getInstance().updateAcl(instance, whitelistIpsFinal);
            }
        };
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                // if changing from mTLS to single, apply the acl first because the former will
                // fail without first having an ACL
                List<Runnable> todo = new ArrayList<Runnable>();
                if (curState.isMtlsConnectionRequiredChanged(newState))
                {
                    if (curState.isAclChanged(newState))
                    {
                        if (!requireMTLS)
                        {
                            // if going to one-way, ensure ACLs are updated so support
                            todo.add(updateWhitelistIps);
                            todo.add(updateMTLS);
                        }
                        else
                        {
                            // if already in one-way, switch to mTLS first which supports no ACLs.
                            todo.add(updateMTLS);
                            todo.add(updateWhitelistIps);
                        }
                    }
                    else
                    {
                        todo.add(updateMTLS);
                    }
                }
                else if (curState.isAclChanged(newState))
                {
                    // if mTLS is not changing but acl is.
                    todo.add(updateWhitelistIps);
                }
                
                for (Runnable r : todo)
                {
                    r.run();
                    waitForAvailable(30000, 5000);
                }
                monitor.done();
            }

            private void waitForAvailable(int maxWaitMs, int sleepLengthMs) {
                boolean updating = true;
                POLLING: while (updating)
                {
                    AutonomousDatabase autonomousDatabase = 
                            ADBInstanceClient.getInstance().getAutonomousDatabase(instance);
                        LifecycleState lifecycleState = autonomousDatabase.getLifecycleState();
                        if (lifecycleState == LifecycleState.Available)
                        {
                            break POLLING;
                        }

                    // not there yet.  sleep
                    maxWaitMs -= sleepLengthMs;
                    if (maxWaitMs <= 0)
                    {
                        throw new IllegalStateException();
                    }
                    try {
                        Thread.sleep(sleepLengthMs);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        throw new IllegalStateException();
                    }
                }
            }
        };
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to update License model for ADB instance : "+instance.getDbName(), realException.getMessage());
            return false;
        }

        return true;

    }

}
