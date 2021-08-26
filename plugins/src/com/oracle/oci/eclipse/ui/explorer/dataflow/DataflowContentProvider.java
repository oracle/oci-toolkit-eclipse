package com.oracle.oci.eclipse.ui.explorer.dataflow;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.oci.eclipse.ui.explorer.RootElement;
import com.oracle.oci.eclipse.ui.explorer.common.BaseContentProvider;

public class DataflowContentProvider extends BaseContentProvider
{
    private static DataflowContentProvider instance;
	private TreeViewer treeViewer;
	Object dataflowRootElement;
	Object dataflowApplicationElement;
	Object dataflowSettingUpElement;
	List<ApplicationSummary> applicationList = new ArrayList<ApplicationSummary>();
	List<PrivateEndpointSummary> privateendpointsList = new ArrayList<PrivateEndpointSummary>();
    private boolean foundApplications = true;
    private boolean foundPrivateEndpoints = true;
        
    public DataflowContentProvider() {
        instance = this;
    }
    
    public static DataflowContentProvider getInstance() {
        if (instance == null) {
            instance = new DataflowContentProvider();
        }
        return instance;
    }
    
	 @Override
	    public Object[] getChildren(Object parentElement)
	    {
	        if (parentElement instanceof RootElement) {
	            return new Object[] { new DataflowRootElement() };
	        } else if (parentElement instanceof DataflowRootElement) {	        	
	            return new Object[] {	            			            		
	                    new DataflowApplicationElement(),
	                    new DataflowRunElement(),
	                    new DataflowPrivateEndPointsElement(),
	                    new DataflowSettingUpElement()
	            };
	        } 
	        else {
	            return new Object[0];
	        }
	    }

	    @Override
	    public boolean hasChildren(Object element)
	    {
	        return (element instanceof RootElement || element instanceof DataflowRootElement);
	    }


	    @Override
	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	    {
	        this.treeViewer = (TreeViewer) viewer;
	    }

	    public synchronized void refresh() {
	        Display.getDefault().asyncExec(new Runnable() {
	            @Override
	            public void run() {
	                if (treeViewer != null) {
	                    if (treeViewer.getTree().getSelectionCount() > 0)
	                        treeViewer.getTree().deselectAll();
	                    treeViewer.refresh(dataflowRootElement);
	                    treeViewer.expandToLevel(1);
	                }
	            }
	        });
	    }

}
