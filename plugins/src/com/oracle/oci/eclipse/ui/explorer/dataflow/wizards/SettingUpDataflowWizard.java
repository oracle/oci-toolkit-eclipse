package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddSettingUpDataflowAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.ObjStorageContentProvider;

public class SettingUpDataflowWizard extends Wizard implements INewWizard{

	private ISelection selection;
	private SettingUpObjectStoragePage page1;
	private CreatingDataflowUserPoliciesPage page2;
	private CreatingDataflowUserPoliciesPageUsers page2n;
	private CreateDataflowServicePolicyPage page3;
	private CreatePrivateEndpointsPoliciesPage page4;
	private CreateHiveMetastorePoliciesPage page5;
	boolean canFinish = false;
	private String COMPARTMENT_ID;
	private ArrayList<String> policiesListDataflowAdmin = new ArrayList<String>();
	private ArrayList<String> policiesListDataflowUsers = new ArrayList<String>();
	private ArrayList<String> policiesListDataflowServices = new ArrayList<String>();
	private ArrayList<String> policiesListDataflowPrivateEndpoints = new ArrayList<String>();
	private ArrayList<String> policiesListDataflowHiveMetastore = new ArrayList<String>();
	
	public SettingUpDataflowWizard() {
		super();
		setWindowTitle("Dataflow Administration Setup");
		this.COMPARTMENT_ID= AuthProvider.getInstance().getCompartmentId();
		setNeedsProgressMonitor(true);
	}
	
	@Override
    public void addPages() {	   
	   try {
          	IRunnableWithProgress op = new AddSettingUpDataflowAction(this);
              new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
               String errorMessage=((AddSettingUpDataflowAction)op).getErrorMessage();
             	if(errorMessage!=null) 
             		throw new Exception(errorMessage);
          } catch (Exception e) {
          	MessageDialog.openError(getShell(), "Unable to add pages to Dataflow Setup wizard", e.getMessage());
          }   
    }
	
	public void addPagesWithProgress(IProgressMonitor monitor) {
		DataTransferObject.local=true;
		monitor.subTask("Adding Setting Up Object Storage Page");
		page1 = new SettingUpObjectStoragePage(selection);
		addPage(page1);
		monitor.subTask("Creating Data Flow User Policies");
		page2 = new CreatingDataflowUserPoliciesPage(selection,COMPARTMENT_ID);
		addPage(page2);
		monitor.subTask("Creating Data Flow User Policies");
		page2n = new CreatingDataflowUserPoliciesPageUsers(selection,COMPARTMENT_ID);
		addPage(page2n);
		monitor.subTask("Creating Data Flow Service Policies");
		page3 = new CreateDataflowServicePolicyPage(selection);
		addPage(page3);
		monitor.subTask("Creating Private Endpoints Policies");
		page4 = new CreatePrivateEndpointsPoliciesPage(selection, COMPARTMENT_ID);
		addPage(page4);
		monitor.subTask("Creating Private Endpoints Policies");
		page5 = new CreateHiveMetastorePoliciesPage(selection);
		addPage(page5);
	}
	
	@Override
    public IWizardPage getNextPage(IWizardPage page) {
		if(page.equals(page1)) {
    		return page2;
    	}
		if(page.equals(page2)) {
    		return page2n;
    	}
		if(page.equals(page2n)) {
    		return page3;
    	}
		if(page.equals(page3)) {
    		return page4;
    	}
		if(page.equals(page4)) {
    		return page5;
    	}
		return null;
	}
	
	@Override
	public boolean canFinish()
	{
		if(getContainer().getCurrentPage() == page4 || getContainer().getCurrentPage() == page5) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
	
	@Override
    public boolean performCancel() {
    	DataTransferObject.local=false;
        DataTransferObject.applicationId=null;
    	return true;
    }
	
	@Override
    public boolean performFinish() {
		//First Wizard Function - Create Object Storage
		final String dataflowLogBucket = "dataflow-logs";
		final String dataflowWarehouseBucket = "dataflow-warehouse";
		
		// Creating Dataflow Log Object Storage
		IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
            	System.out.println(ObjStorageClient.getInstance().createBucket(dataflowLogBucket));
            	monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to Create Dataflow Log Bucket ", realException.getMessage());
            return false;
        }
        
        // Creating Dataflow Warehouse Object Storage
        IRunnableWithProgress op2 = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
            	ObjStorageClient.getInstance().createBucket(dataflowWarehouseBucket);
                monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op2);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to Create Dataflow Warehouse Bucket ", realException.getMessage());
            return false;
        }

        //IAM Policies
        String compartmentName1 = "tenancy";
        
        //Data Flow User Policies
        String policy11 = "Allow group dataflow-admin to read buckets in " + compartmentName1;
        String policy21 = "Allow group dataflow-admin to manage dataflow-family in " + compartmentName1;
        String policy31 = "Allow group dataflow-admin to manage objects in " + compartmentName1 + " where ALL {target.bucket.name='dataflow-logs', any {request.permission='OBJECT_CREATE', request.permission='OBJECT_INSPECT'}}";
        
     
        String policy41 = "Allow group dataflow-users to read buckets in " + compartmentName1;
        String policy51 = "Allow group dataflow-users to use dataflow-family in " + compartmentName1;
        String policy61 = "Allow group dataflow-users to manage dataflow-family in " + compartmentName1 + " where any {request.user.id = target.user.id, request.permission = 'DATAFLOW_APPLICATION_CREATE', request.permission = 'DATAFLOW_RUN_CREATE'}";
        String policy71 = "Allow group dataflow-users to manage objects in " + compartmentName1 + " where ALL {target.bucket.name='dataflow-logs', any {request.permission='OBJECT_CREATE', request.permission='OBJECT_INSPECT'}}";
        
        //Data Flow Service Policy
        String policy12 = "Allow service dataflow to read objects in tenancy where target.bucket.name='dataflow-logs'";
        
        //Private Endpoints Policies
        String policy13 = "Allow group dataflow-admin to use virtual-network-family in " + compartmentName1;
        String policy23 = "Allow group dataflow-admin to manage vnics in " + compartmentName1;
        String policy33 = "Allow group dataflow-admin to use subnets in " + compartmentName1;
        String policy43 = "Allow group dataflow-admin to use network-security-groups in " + compartmentName1;
        String policy53 = "Allow group dataflow-admin to manage virtual-network-family in " + compartmentName1 + " where any {request.operation='CreatePrivateEndpoint', request.operation='UpdatePrivateEndpoint', request.operation='DeletePrivateEndpoint' }";
        String policy63 = "Allow group dataflow-admin to manage dataflow-private-endpoint in " + compartmentName1;
        
        //Hive MetaStore Policies
        String groupOcid = page5.getGroupOcid();
        String metastoreOcid = page5.getMetastoreOcid();
        String managedTableLocationBucket = page5.getManagedTableLocationBucket();
        String managedTableLocationBucketRegion = page5.getManagedTableLocationBucketRegion();
        String externalTableLocationBucket = page5.getExternalTableLocationBucket();
        String externalTableLocationBucketRegion = page5.getExternalTableLocationBucketRegion();
        
        String policy14 = "Allow group id " + groupOcid + " to {CATALOG_METASTORE_READ, CATALOG_METASTORE_EXECUTE} in tenancy where target.metastore.id='" + metastoreOcid + "'";
        String policy24 = "Allow group id " + groupOcid + " to {CATALOG_METASTORE_INSPECT} in tenancy";
        String policy34 = "Allow group id " + groupOcid + " to read buckets in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucket + "', request.region='" + managedTableLocationBucketRegion + "'}, all {target.bucket.name='" + externalTableLocationBucket + "', \n"
        		+ "request.region='" + externalTableLocationBucketRegion + "'}}";
        String policy44 = "Allow group id " + groupOcid + " to manage objects in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucket + "', request.region='" + managedTableLocationBucketRegion + "'}, all \n"
        		+ "{target.bucket.name='" + externalTableLocationBucket + "', request.region='" + externalTableLocationBucketRegion + "'}}";
       
        policiesListDataflowAdmin.add(policy11);
        policiesListDataflowAdmin.add(policy21);
        policiesListDataflowAdmin.add(policy31);
        
        policiesListDataflowUsers.add(policy41);
        policiesListDataflowUsers.add(policy51);
        policiesListDataflowUsers.add(policy61);
        policiesListDataflowUsers.add(policy71);

        policiesListDataflowServices.add(policy12);
        
        policiesListDataflowPrivateEndpoints.add(policy13);
        policiesListDataflowPrivateEndpoints.add(policy23);
        policiesListDataflowPrivateEndpoints.add(policy33);
        policiesListDataflowPrivateEndpoints.add(policy43);
        policiesListDataflowPrivateEndpoints.add(policy53);
        policiesListDataflowPrivateEndpoints.add(policy63);
        
        policiesListDataflowHiveMetastore.add(policy14);
        policiesListDataflowHiveMetastore.add(policy24);
        policiesListDataflowHiveMetastore.add(policy34);
        policiesListDataflowHiveMetastore.add(policy44);
        
        IRunnableWithProgress op3 = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
            	System.out.println(IdentClient.getInstance().createIAMPolicy(COMPARTMENT_ID, "Policies for dataflow-admin", "dataflow-admin", policiesListDataflowAdmin));
            	monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op3);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error in creating Policies for dataflow-admin", realException.getMessage());
            System.out.println("Error in creating Policies for dataflow-admin. Exception: \n " + e);
            return false;
        }
        
        IRunnableWithProgress op4 = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
            	System.out.println(IdentClient.getInstance().createIAMPolicy(COMPARTMENT_ID, "Policies for dataflow-users", "dataflow-users", policiesListDataflowUsers));
            	monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op4);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error in creating Policies for dataflow-users", realException.getMessage());
            System.out.println("Error in creating Policies for dataflow-users. Exception: \n " + e);
            return false;
        }
        
        IRunnableWithProgress op5 = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
            	System.out.println(IdentClient.getInstance().createIAMPolicy(COMPARTMENT_ID, "Policies for dataflow-service", "dataflow-service", policiesListDataflowServices));
            	monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op5);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error in creating Policies for dataflow-service", realException.getMessage());
            System.out.println("Error in creating Policies for dataflow-service. Exception: \n " + e);
            return false;
        }
        
        IRunnableWithProgress op6 = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
            	System.out.println(IdentClient.getInstance().createIAMPolicy(COMPARTMENT_ID, "Policies for dataflow-private-endpoints", "dataflow-private-endpoints", policiesListDataflowPrivateEndpoints));
            	monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op6);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error in creating Policies for dataflow-private-endpoints", realException.getMessage());
            System.out.println("Error in creating Policies for dataflow-private-endpoints. Exception: \n " + e);
            return false;
        }
        
        if(!groupOcid.equals("") || !metastoreOcid.equals("") || !managedTableLocationBucket.equals("") || !managedTableLocationBucketRegion.equals("") || !externalTableLocationBucket.equals("") || !externalTableLocationBucketRegion.equals("")) {
        	IRunnableWithProgress op7 = new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                	System.out.println(IdentClient.getInstance().createIAMPolicy(COMPARTMENT_ID, "Policies for dataflow-hive-metastore", "dataflow-hive-metastore", policiesListDataflowHiveMetastore));
                	monitor.done();
                }
            };
            try {
                getContainer().run(true, false, op7);
            } catch (InterruptedException e) {
                return false;
            } catch (InvocationTargetException e) {
                Throwable realException = e.getTargetException();
                MessageDialog.openError(getShell(), "Error in creating Policies for dataflow-hive-metastore", realException.getMessage());
                System.out.println("Error in creating Policies for dataflow-hive-metastore. Exception: \n " + e);
                return false;
            }
        }
        
        // Refresh TreeView to show new nodes
        ObjStorageContentProvider.getInstance().getBucketsAndRefresh();
        try {
			new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        MessageDialog.openConfirm(getShell(), "Dataflow Setup", "Dataflow has been setup Succesfully");
        return true;
	}
	
	public void open(String h,String m) {
    	MessageDialog.openInformation(getShell(), h, m);
    }
	
	@Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}
