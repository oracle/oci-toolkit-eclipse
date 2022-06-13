package com.oracle.oci.eclipse.ui.explorer.database.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class EventSource {
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

}