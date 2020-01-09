package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.oracle.oci.eclipse.ui.account.ClientUpdateManager;

public abstract class BaseClient implements PropertyChangeListener {

    public BaseClient() {
        ClientUpdateManager.getInstance().addPropertyChangeListener(this);
        ClientUpdateManager.getInstance().addClient(this);
    }

    public abstract void updateClient();
    public abstract void close();

    @Override
    public abstract void propertyChange(PropertyChangeEvent evt);

    @Override
    public void finalize() throws Throwable {
        close();
    }

}
