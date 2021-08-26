package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails.Builder;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddCreateApplicationPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetBuckets;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;
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
import org.eclipse.ui.IWorkbenchWizard;

public class CreateApplicationWizard extends Wizard implements INewWizard {	
	private String COMPARTMENT_ID;
    private CreateApplicationWizardPage firstPage;
    protected CreateApplicationWizardPage3 thirdPage;
    private TagsPage tagsPage;
    private ISelection selection;
    
	public CreateApplicationWizard(String COMPARTMENT_ID) {
		super();
		this.COMPARTMENT_ID= COMPARTMENT_ID;
		setNeedsProgressMonitor(true);
	}
	
    @Override
    public void addPages() {
    	 try {
          	IRunnableWithProgress op = new AddCreateApplicationPagesAction(this);
              new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
              String errorMessage=((AddCreateApplicationPagesAction)op).getErrorMessage();
          	if(errorMessage!=null) 
          		throw new Exception(errorMessage);
          } catch (Exception e) {
          	MessageDialog.openError(getShell(), "Unable to add pages to Create Application wizard", e.getMessage());
          }   
    	 getShell().setSize(1000, 800);
    }
    
    public void addPagesWithProgress(IProgressMonitor monitor) {
    	monitor.subTask("Adding Main page");    	
    	firstPage = new CreateApplicationWizardPage(selection,COMPARTMENT_ID);
       	addPage(firstPage);
    	monitor.subTask("Adding Tags Page");
        tagsPage= new TagsPage(selection,COMPARTMENT_ID,null,null);
        addPage(tagsPage);  
        monitor.subTask("Adding Advanced Options page");
        thirdPage = new CreateApplicationWizardPage3(selection);
        addPage(thirdPage);
    }
    
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page.equals(firstPage)) {
        		return tagsPage;
        }       
        if (page.equals(tagsPage)) {
            return thirdPage;
        }    
        return null;       
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
    	
    	final String compartmentId = firstPage.getApplicationCompartmentId();    	
    	if(firstPage.usesSparkSubmit()) {   		
    		CreateRunDetails.Builder createApplicationRequestBuilder =  
    				CreateRunDetails.builder()
        	        .compartmentId(compartmentId)
        			.displayName(firstPage.getDisplayName())
        			.sparkVersion(firstPage.getSparkVersion())
        			.driverShape(firstPage.getDriverShape())
        			.executorShape(firstPage.getExecutorShape())
        			.numExecutors(Integer.valueOf(firstPage.getNumofExecutors()))
        			.definedTags(tagsPage.getOT())
        			.freeformTags(tagsPage.getFT())   
        			.execute(firstPage.getSparkSubmit())
        	        .logsBucketUri(thirdPage.getApplicationLogLocation())
        	        .warehouseBucketUri(thirdPage.getWarehouseUri());
        			
    				final CreateRunDetails createRunRequest;	
        			createRunRequest = createApplicationRequestBuilder.build();		
        	        IRunnableWithProgress op = new IRunnableWithProgress() {
        	            @Override
        	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
        	            	DataflowClient.getInstance().runApplication(createRunRequest);
        	                monitor.done();
        	            }
        	        };
        	        try {
        	            getContainer().run(true, false, op);
        	        } 
        	        catch (Exception e) {
        	        	ErrorHandler.logError("Unable to create applications: " + e.getMessage());
        	            MessageDialog.openError(getShell(), "Failed to Create Application ", e.getMessage());
        	            return false;
        	        }
        	        return true;
    	}
    	
    	else {
        	Builder createApplicationRequestBuilder = 
        	        CreateApplicationDetails.builder()
        	        .compartmentId(compartmentId)
        			.displayName(firstPage.getDisplayName())
        			.description(firstPage.getApplicationDescription())
        			.sparkVersion(firstPage.getSparkVersion())
        			.driverShape(firstPage.getDriverShape())
        			.executorShape(firstPage.getExecutorShape())
        			.numExecutors(Integer.valueOf(firstPage.getNumofExecutors()))
        			.definedTags(tagsPage.getOT())
        			.freeformTags(tagsPage.getFT());
        	        			
        			if(firstPage.usesSparkSubmit() == false) {
        				createApplicationRequestBuilder = createApplicationRequestBuilder
        						.language(firstPage.getLanguage())
        						.fileUri(firstPage.getFileUri())
        						.archiveUri(firstPage.getArchiveUri());
        				
        				if(firstPage.getLanguage()== ApplicationLanguage.Java || firstPage.getLanguage()== ApplicationLanguage.Scala){
        		    		createApplicationRequestBuilder = createApplicationRequestBuilder.className(firstPage.getMainClassName())
        		    				.arguments(firstPage.getArguments());					
        		    	}
        		    	else if (firstPage.getLanguage()== ApplicationLanguage.Python) {
        		    		createApplicationRequestBuilder = createApplicationRequestBuilder.arguments(firstPage.getArguments());			
        		    	}        		    	
        		    createApplicationRequestBuilder = createApplicationRequestBuilder.parameters(firstPage.getParameters());			       		    	
        			}
        			else{
        				createApplicationRequestBuilder = createApplicationRequestBuilder.execute(firstPage.getSparkSubmit());
        			}
        			final CreateApplicationDetails createApplicationRequest;       	    	
        			final boolean usesAdvancedOptions = thirdPage.usesAdvancedOptions();
        			if (usesAdvancedOptions) {
        				createApplicationRequestBuilder = createApplicationRequestBuilder.configuration(thirdPage.getSparkProperties())
        						.logsBucketUri(thirdPage.getApplicationLogLocation()).warehouseBucketUri(thirdPage.getWarehouseUri());
        				
        				final boolean usesPrivateSubnet = thirdPage.usesPrivateSubnet();
        				if(usesPrivateSubnet) {
        					createApplicationRequest = createApplicationRequestBuilder.privateEndpointId(thirdPage.getPrivateEndPointId())
        							.build();
        				}
        				else {
        					createApplicationRequest = createApplicationRequestBuilder.build();
        				}        						
        			} else {
        				createApplicationRequest = createApplicationRequestBuilder.build();       						
        			}
        	    		
        	        IRunnableWithProgress op = new IRunnableWithProgress() {
        	            @Override
        	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
        	            	DataflowClient.getInstance().createApplication(createApplicationRequest);
        	                monitor.done();
        	            }
        	        };
        	        try {
        	            getContainer().run(true, false, op);
        	        }catch (Exception e) {        	        
        	            MessageDialog.openError(getShell(), "Failed to Create Application ", e.getMessage());
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

    	if(!firstPage.usesSparkSubmit()) {    		
        	objectArray.add(firstPage.getFileUri());
        	nameArray.add("fileuri");   		
    	}
    	if(!firstPage.usesSparkSubmit() && (firstPage.getLanguage() == ApplicationLanguage.Java )) {
        	objectArray.add(firstPage.getMainClassName());
        	nameArray.add("mainclassname"); 
    	}
    	
       objectArray.add(thirdPage.getApplicationLogLocation());
       nameArray.add("loguri"); 
       
       if(thirdPage.getWarehouseUri() != null && !thirdPage.getWarehouseUri().isEmpty()) {
           objectArray.add(thirdPage.getWarehouseUri());
           nameArray.add("warehouseuri"); 
       }     
    	if (thirdPage.usesPrivateSubnet()){
    	       objectArray.add(thirdPage.privateEndpointsCombo.getText());
    	       nameArray.add("subnetid"); 
    	}
    	
    	if(!firstPage.usesSparkSubmit() && firstPage.getArchiveUri() != null && !firstPage.getArchiveUri().isEmpty()) {
    	       objectArray.add(firstPage.getArchiveUri());
    	       nameArray.add("archiveuri"); 
    	}
    	
    	if(!firstPage.usesSparkSubmit() && thirdPage.getSparkProperties() != null) {
 	       objectArray.add(thirdPage.getSparkProperties().keySet());
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
