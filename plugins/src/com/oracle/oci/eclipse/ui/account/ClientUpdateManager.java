package com.oracle.oci.eclipse.ui.account;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.BaseClient;
import com.oracle.oci.eclipse.ui.explorer.NavigatorDoubleClick;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.ObjStorageContentProvider;

public class ClientUpdateManager {

    private static ClientUpdateManager single_instance = null;
    List<BaseClient> clientsList = Collections.synchronizedList(new ArrayList<BaseClient>());
    private PropertyChangeSupport supportRegion;
    private PropertyChangeSupport supportCompartment;
    private PropertyChangeSupport supportViews;

    private ClientUpdateManager() {
        supportRegion = new PropertyChangeSupport(this);
        supportViews = new PropertyChangeSupport(this);
        supportCompartment = new PropertyChangeSupport(this);
    }

    public static ClientUpdateManager getInstance()
    {
        if (single_instance == null) {
            single_instance = new ClientUpdateManager();
        }
        return single_instance;
    }

    // Update Table views when region or compartment changes
    public void addViewChangeListener(PropertyChangeListener pcl) {
        supportViews.addPropertyChangeListener(pcl);
    }
    public void removeViewChangeListener(PropertyChangeListener pcl) {
        supportViews.removePropertyChangeListener(pcl);
    }

    // Update region or compartment values in SDK clients
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        supportRegion.addPropertyChangeListener(pcl);
        supportCompartment.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        supportRegion.removePropertyChangeListener(pcl);
        supportCompartment.addPropertyChangeListener(pcl);
    }

    public PropertyChangeSupport getSupportRegion() {
        return supportRegion;
    }

    public PropertyChangeSupport getSupportViews() {
        return supportViews;
    }

    public void refreshClients() {
        // Update Clients only when switching connection profiles
        new Job("Update clients") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    // Update clients after region or compartment change
                    clientsList.forEach( client -> client.updateClient());

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            RegionOptions.refreshRegions();
                            CompartmentOptions.refreshCompartments();
                            NavigatorDoubleClick.closeAllWindows();
                        }
                    });

                    ObjStorageContentProvider.getInstance().getBucketsAndRefresh();

                } catch(Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    public void addClient(BaseClient baseClient) {
        clientsList.add(baseClient);
    }

    public void removeClient(BaseClient baseClient) {
        clientsList.remove(baseClient);
    }
}
