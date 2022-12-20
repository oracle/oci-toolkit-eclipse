/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
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

    public synchronized void dispose() throws Throwable {
        close();
    }

}
