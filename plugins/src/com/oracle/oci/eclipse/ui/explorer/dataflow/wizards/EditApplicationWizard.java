package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddEditApplicationPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddRunApplicationPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;


public class EditApplicationWizard extends Wizard implements INewWizard {	
    private EditApplicationWizardPage1 firstPage;
    private EditApplicationWizardPage2 secondPage;
    private TagsPage tagPage;
    private ISelection selection;
    private Application application;

	public EditApplicationWizard(String applicationId) {
		super();
		setNeedsProgressMonitor(true);
		application = DataflowClient.getInstance().getApplicationDetails(applicationId);
	}
	
    @Override
    public void addPages() {
    	 try {
           	IRunnableWithProgress op = new AddEditApplicationPagesAction(this);
               new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
               String errorMessage=((AddEditApplicationPagesAction)op).getErrorMessage();
             	if(errorMessage!=null) 
             		throw new Exception(errorMessage);
           } catch (Exception e) {
           	MessageDialog.openError(getShell(), "Unable to add pages to Edit Application wizard", e.getMessage());
           }    
    	 getShell().setSize(1000, 800);
    }
    
    public void addPagesWithProgress(IProgressMonitor monitor) {
    	monitor.subTask("Adding Main page");
    	firstPage = new EditApplicationWizardPage1(selection, application.getId());
        addPage(firstPage);     
    	monitor.subTask("Adding Tags Page");
        tagPage = new TagsPage(selection,application.getId(),application.getDefinedTags(),application.getFreeformTags());
        addPage(tagPage);
        monitor.subTask("Adding Advanced Options page");
        secondPage = new EditApplicationWizardPage2(selection, application.getId());
        addPage(secondPage);
    }
    
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */   
    @Override
    public boolean performFinish() { 
    	
       	List<Object> validObjects = new ArrayList<Object>();
    	List<String> objectType = new ArrayList<String>();
    	
    	performValidations(validObjects,objectType);       	
    	String message=Validations.check(validObjects.toArray(),objectType.toArray(new String[1]));
    	
    	if(!message.isEmpty()) {
    		open("Validation errors",message);
    		return false;
    	}
    	
	    if(application.getExecute() != null && !application.getExecute().equals("")) {	    		
	    	 	Builder editApplicationRequestBuilder = 
	    	 	        UpdateApplicationDetails.builder()
	    	 			.displayName(firstPage.getDisplayName())
	    	 			.description(firstPage.getApplicationDescription())
	    	 			.sparkVersion(firstPage.getSparkVersion())
	    	 			.driverShape(firstPage.getDriverShape())
	    	 			.executorShape(firstPage.getExecutorShape())
	    	 			.numExecutors(Integer.valueOf(firstPage.getNumofExecutors()))
	    	 			.execute(firstPage.getSparkSubmit())
	    	 			.definedTags(tagPage.getOT())
	    	 			.freeformTags(tagPage.getFT());
	    	 	
	    	 	 		final UpdateApplicationDetails editApplicationRequest;	    	 	    	
	    	 			final boolean usesAdvancedOptions = secondPage.usesAdvancedOptions();
	    	 			if (usesAdvancedOptions) {
	    	 				editApplicationRequestBuilder = editApplicationRequestBuilder
	    	 						.logsBucketUri(secondPage.getApplicationLogLocation())
	    	 						.warehouseBucketUri(secondPage.getWarehouseUri());	    	 				
	    	 				if(secondPage.usesPrivateSubnet()) {
	    	 					editApplicationRequest  = editApplicationRequestBuilder.privateEndpointId(secondPage.getPrivateEndPointId())
	    	 							.build();
	    	 				}
	    	 				else {	    	 					
	    	 					if(application.getPrivateEndpointId() != null) {					
	    	 						editApplicationRequestBuilder
	    	 						.privateEndpointId("");
	    	 					}
	    	 					editApplicationRequest = editApplicationRequestBuilder.build();
	    	 				}	    	 						
	    	 			} else {
	    	 				editApplicationRequest = editApplicationRequestBuilder.build();	    	 						
	    	 			}	        			
	    	 			 IRunnableWithProgress op = new IRunnableWithProgress() {
	     	 	            @Override
	     	 	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
	     	 	            	DataflowClient.getInstance().editApplication(application.getId(),editApplicationRequest);
	     	 	                monitor.done();
	     	 	            }
	     	 	        };
	     	 	        try {
	     	 	            getContainer().run(true, false, op);
	     	 	        } catch (InterruptedException e) {
	     	 	            return false;
	     	 	        } catch (InvocationTargetException e) {
	     	 	            Throwable realException = e.getTargetException();
	     	 	            MessageDialog.openError(getShell(), "Failed to Edit Application ", realException.getMessage());
	     	 	            return false;
	     	 	        }

	     	 	        return true;
    	}
    	else {
    	 	Builder editApplicationRequestBuilder = 
    	 	        UpdateApplicationDetails.builder()
    	 			.displayName(firstPage.getDisplayName())
    	 			.description(firstPage.getApplicationDescription())
    	 			.sparkVersion(firstPage.getSparkVersion())
    	 			.driverShape(firstPage.getDriverShape())
    	 			.executorShape(firstPage.getExecutorShape())
    	 			.numExecutors(Integer.valueOf(firstPage.getNumofExecutors()))
    	 			.language(firstPage.getLanguage())
    	 			.fileUri(firstPage.getFileUri())
    	 			.archiveUri(firstPage.getArchiveUri())
    	 			.parameters(firstPage.getParameters())
    	 			.definedTags(tagPage.getOT())
    	 			.freeformTags(tagPage.getFT());    	 	    	
    	 	        
    	 		   	if(firstPage.getLanguage() == ApplicationLanguage.Java || firstPage.getLanguage()== ApplicationLanguage.Scala){
    	 		   		editApplicationRequestBuilder = editApplicationRequestBuilder.className(firstPage.getMainClassName())
    	 	    				.arguments(firstPage.getArguments());					
    	 	    	}
    	 	    	else if (firstPage.getLanguage() == ApplicationLanguage.Python) {
    	 	    		editApplicationRequestBuilder = editApplicationRequestBuilder.arguments(firstPage.getArguments());			
    	 	    	}    	 	    	
    	 		   	editApplicationRequestBuilder = editApplicationRequestBuilder.parameters(firstPage.getParameters());				    	 			
    	 	 		final UpdateApplicationDetails editApplicationRequest;	 	    	
    	 			final boolean usesAdvancedOptions = secondPage.usesAdvancedOptions();
    	 			if (usesAdvancedOptions) {
    	 				editApplicationRequestBuilder = editApplicationRequestBuilder.configuration(secondPage.getSparkProperties())
    	 						.logsBucketUri(secondPage.getApplicationLogLocation()).warehouseBucketUri(secondPage.getWarehouseUri());    	 				
    	 				if(secondPage.usesPrivateSubnet()) {
    	 					editApplicationRequest  = editApplicationRequestBuilder.privateEndpointId(secondPage.getPrivateEndPointId())
    	 							.build();
    	 				}
    	 				else {    	 					
    	 					if(application.getPrivateEndpointId() != null) {					
    	 						editApplicationRequestBuilder
    	 						.privateEndpointId("");
    	 					}
    	 					editApplicationRequest = editApplicationRequestBuilder.build();
    	 				}    	 						
    	 			} else {
    	 				editApplicationRequest = editApplicationRequestBuilder.build();    	 						
    	 			}
    	 	        IRunnableWithProgress op = new IRunnableWithProgress() {
    	 	            @Override
    	 	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
    	 	            	DataflowClient.getInstance().editApplication(application.getId(),editApplicationRequest);
    	 	                monitor.done();
    	 	            }
    	 	        };
    	 	        try {
    	 	            getContainer().run(true, false, op);
    	 	        } catch (InterruptedException e) {
    	 	            return false;
    	 	        } catch (InvocationTargetException e) {
    	 	            Throwable realException = e.getTargetException();
    	 	            MessageDialog.openError(getShell(), "Failed to Edit Application ", realException.getMessage());
    	 	            return false;
    	 	        }
    	 	        return true;
    	}   
    }
    
   public void performValidations(List<Object> objectArray,List<String>nameArray) {
    	
    	objectArray.add(firstPage.getDisplayName());
    	nameArray.add("name");
    	
    	objectArray.add(firstPage.getApplicationDescription());
    	nameArray.add("description");

    	if(application.getExecute() == null) {    		
        	objectArray.add(firstPage.getFileUri());
        	nameArray.add("fileuri");   		
    	}
    	if(application.getExecute() == null  && (firstPage.getLanguage() == ApplicationLanguage.Java )) {
        	objectArray.add(firstPage.getMainClassName());
        	nameArray.add("mainclassname"); 
    	}
    	
       objectArray.add(secondPage.getApplicationLogLocation());
       nameArray.add("loguri"); 
       
       if(secondPage.getWarehouseUri() != null && !secondPage.getWarehouseUri().isEmpty()) {
           objectArray.add(secondPage.getWarehouseUri());
           nameArray.add("warehouseuri"); 
       }     
    	if (secondPage.usesPrivateSubnet()){
    	       objectArray.add(secondPage.privateEndpointsCombo.getText());
    	       nameArray.add("subnetid"); 
    	}
    	
    	if(application.getExecute() == null && firstPage.getArchiveUri() != null && !firstPage.getArchiveUri().isEmpty()) {
    	       objectArray.add(firstPage.getArchiveUri());
    	       nameArray.add("archiveuri"); 
    	}
    	
    	if(application.getExecute() == null && secondPage.getSparkProperties() != null) {
 	       objectArray.add(secondPage.getSparkProperties().keySet());
 	       nameArray.add("sparkprop" + firstPage.getSparkVersion().charAt(0));         
    	}

    }
    public void open(String h,String m) {
    	MessageDialog.openInformation(getShell(), h, m);
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
