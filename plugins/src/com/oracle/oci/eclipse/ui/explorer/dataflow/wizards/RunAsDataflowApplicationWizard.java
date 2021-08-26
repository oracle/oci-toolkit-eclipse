package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails.Builder;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddRunAsDataflowApplicationPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.ScheduleUploadObjectAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;

public class RunAsDataflowApplicationWizard extends Wizard implements INewWizard  {	
	private ProjectSelectWizardPage page1;
	private AppAndArchiveCreationPage page2;	 
    protected LocalFileSelectWizardPage1 firstbpage;
    protected LocalFileSelectWizardPage2 secondbpage;
    protected LocalFileSelectWizardPage3 thirdbpage;
    private ISelection selection;
	private String COMPARTMENT_ID;
	protected CreateApplicationWizardPage firstpage;
	protected CreateApplicationWizardPage3 thirdpage;
    protected TagsPage tagpage;
    private Application application;
    boolean canFinish = false;
    
	public RunAsDataflowApplicationWizard() {
		super();
		this.COMPARTMENT_ID= AuthProvider.getInstance().getCompartmentId();
		setNeedsProgressMonitor(true);
	}	
	   @Override
	    public void addPages() {	   
		   try {
	          	IRunnableWithProgress op = new AddRunAsDataflowApplicationPagesAction(this);
	              new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
	               String errorMessage=((AddRunAsDataflowApplicationPagesAction)op).getErrorMessage();
	             	if(errorMessage!=null) 
	             		throw new Exception(errorMessage);
	          } catch (Exception e) {
	          	MessageDialog.openError(getShell(), "Unable to add pages to Run as Dataflow Application wizard", e.getMessage());
	          }   
	    }
	   
	    public void addPagesWithProgress(IProgressMonitor monitor) {
	    	DataTransferObject.local=true;	    	
	    	monitor.subTask("Adding Project Select page");  
	        page1 = new ProjectSelectWizardPage(selection);
	        addPage(page1);	        
	    	monitor.subTask("Adding Dependencies Select Page page");  
	        page2= new AppAndArchiveCreationPage(selection,page1);
	        addPage(page2);	        
	    	monitor.subTask("Adding Application Jar Bucket Select page");  
	        firstbpage = new LocalFileSelectWizardPage1(selection,COMPARTMENT_ID);
	        addPage(firstbpage);	        
	    	monitor.subTask("Adding Archive Zip Bucket Select page");
	        secondbpage = new LocalFileSelectWizardPage2(selection,COMPARTMENT_ID);
		    addPage(secondbpage);		    
	    	monitor.subTask("Adding Previous Application Select page");  
	        thirdbpage = new LocalFileSelectWizardPage3(selection,COMPARTMENT_ID);
		    addPage(thirdbpage);		    
	    	monitor.subTask("Adding Main page");    	
	        firstpage = new CreateApplicationWizardPage(selection,COMPARTMENT_ID);
	        addPage(firstpage);  	       	
	    	monitor.subTask("Adding Tags Page");
	        tagpage= new TagsPage(selection,COMPARTMENT_ID,null,null);
	        addPage(tagpage);  	        
	        monitor.subTask("Adding Advanced Options page");
	        thirdpage = new CreateApplicationWizardPage3(selection);
	        addPage(thirdpage);
	    }
	   
	    @Override
	    public IWizardPage getNextPage(IWizardPage page) {
	    	
	    	if(page.equals(page1)) {
	    		page2.job.schedule();
	    		return page2;
	    	}
	    	if(page.equals(page2)) {
	    		return firstbpage;
	    	}
	    	
	    	if(DataTransferObject.archivedir == null && page.equals(firstbpage)) {
	    		return thirdbpage;
	    	}
	    	
	    	if (DataTransferObject.archivedir != null && page.equals(firstbpage)) {
       		return secondbpage;
	    	}     
	    	
	    	if (page.equals(secondbpage)) {
	       		return thirdbpage;
	    	}    
	    	
	    	if( page.equals(thirdbpage)) {
	    		return firstpage;
	    	}
	    	
	        if (page.equals(firstpage)) {
	        		return tagpage;
	        }       
	        if (page.equals(tagpage)) {
	            return thirdpage;
	        }    
	        return null;       
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
	    public boolean canFinish() {
	    	return canFinish;
	    }
	    
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
	    	
	    	String bucketName = firstbpage.getBucketSelected();
	    	File applicationFile = new File(DataTransferObject.filedir);
	    	String newfileName = DataTransferObject.filedir.substring(0,DataTransferObject.filedir.lastIndexOf('\\')+1);
	    	File applicationFileNew = new File(newfileName+firstbpage.getnewName());
	    	applicationFile.renameTo(applicationFileNew);
	    	
	    	IRunnableWithProgress op1 = new ScheduleUploadObjectAction(bucketName,applicationFileNew);
            try {
				new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op1);
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				MessageDialog.openError(getShell(), "Failed to Upload Application Jar", e1.getMessage());
				return false;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				MessageDialog.openError(getShell(), "Failed to Upload Application Jar", e1.getMessage());
				return false;
			}
	    	
			
			if(DataTransferObject.archivedir != null && secondbpage.getBucketSelected() != null)
			{
				String archivebucketName = secondbpage.getBucketSelected();
		    	File archiveFile = new File(DataTransferObject.archivedir);
		    	String newfileName2 = DataTransferObject.archivedir.substring(0,DataTransferObject.archivedir.lastIndexOf('\\')+1);
		    	File archiveFileNew = new File(newfileName2+secondbpage.getnewName());
		    	archiveFile.renameTo(archiveFileNew);
		    	IRunnableWithProgress op2 = new ScheduleUploadObjectAction(archivebucketName, archiveFileNew);
	            try {
					new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op2);
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
					MessageDialog.openError(getShell(), "Failed to Upload Archive Zip ", e1.getMessage());
					return false;
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					MessageDialog.openError(getShell(), "Failed to Upload Archive Zip ", e1.getMessage());
					return false;
				}
			}

			if(DataTransferObject.applicationId != null) {				
				Application applicationOld = DataflowClient.getInstance().getApplicationDetails(DataTransferObject.applicationId);				
		    	final String compartmentId = firstpage.getApplicationCompartmentId();
		    	
		    	if(firstpage.usesSparkSubmit()) {		    		
		    		CreateRunDetails.Builder createApplicationRequestBuilder =  
		    				CreateRunDetails.builder()
		        	        .compartmentId(compartmentId)
		        	        .applicationId(DataTransferObject.applicationId)
		        			.displayName(firstpage.getDisplayName())
		        			.sparkVersion(firstpage.getSparkVersion())
		        			.driverShape(firstpage.getDriverShape())
		        			.executorShape(firstpage.getExecutorShape())
		        			.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
		        			.definedTags(tagpage.getOT())
		        			.freeformTags(tagpage.getFT())   
		        			.execute(firstpage.getSparkSubmit())
		        	        .logsBucketUri(thirdpage.getApplicationLogLocation())
		        	        .warehouseBucketUri(thirdpage.getWarehouseUri());
		        			
		    				final CreateRunDetails createApplicationRequest;	
		        			createApplicationRequest = createApplicationRequestBuilder.build();		
		        	        IRunnableWithProgress op = new IRunnableWithProgress() {
		        	            @Override
		        	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
		        	            	DataflowClient.getInstance().runApplication(createApplicationRequest);
		        	                monitor.done();
		        	            }
		        	        };
		        	        try {
		        	            getContainer().run(true, false, op);
		        	        } catch (Exception e) {
		        	            MessageDialog.openError(getShell(), "Failed to Run Previously Created Application ", e.getMessage());
		        	            DataTransferObject.applicationId=null;
		        	            return false;
		        	        }
		                    DataTransferObject.applicationId=null;
		                    DataTransferObject.local=false;
		        	        return true;
		    	}
		    	
		    	else {
		    		UpdateApplicationDetails.Builder editApplicationRequestBuilder = 
		        	        UpdateApplicationDetails.builder()
		        			.displayName(firstpage.getDisplayName())
		        			.description(firstpage.getApplicationDescription())
		        			.sparkVersion(firstpage.getSparkVersion())
		        			.driverShape(firstpage.getDriverShape())
		        			.executorShape(firstpage.getExecutorShape())
		        			.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
		        			.definedTags(tagpage.getOT())
		        			.freeformTags(tagpage.getFT())
    						.language(firstpage.getLanguage())
    						.fileUri(firstpage.getFileUri())
    						.parameters(firstpage.getParameters())
    						.archiveUri(firstpage.getArchiveUri());
		    		
		        	if(firstpage.getLanguage()== ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
		        					editApplicationRequestBuilder = editApplicationRequestBuilder.className(firstpage.getMainClassName())
		        		    				.arguments(firstpage.getArguments());					
		        	}
		        	else if (firstpage.getLanguage()== ApplicationLanguage.Python) {
		        		    		editApplicationRequestBuilder = editApplicationRequestBuilder.arguments(firstpage.getArguments());					        		    	
		        	}		        		    	

		        	final UpdateApplicationDetails editApplicationRequest;		        	    	
		        	final boolean usesAdvancedOptions = thirdpage.usesAdvancedOptions();
		        	if (usesAdvancedOptions) {
		        		editApplicationRequestBuilder = editApplicationRequestBuilder.configuration(thirdpage.getSparkProperties())
		        				.logsBucketUri(thirdpage.getApplicationLogLocation()).warehouseBucketUri(thirdpage.getWarehouseUri());
		        				
		        		final boolean usesPrivateSubnet = thirdpage.usesPrivateSubnet();
		        		if(usesPrivateSubnet) {
		        					editApplicationRequest = editApplicationRequestBuilder.privateEndpointId(thirdpage.getPrivateEndPointId())
		        							.build();
		        		}
		        		else {
		        			if(applicationOld.getPrivateEndpointId() != null) {					
		        						editApplicationRequestBuilder
		        						.privateEndpointId("");
		        			}
		        			editApplicationRequest = editApplicationRequestBuilder.build();
		        			}		        						
		        		} 
		        	else {
		        				editApplicationRequest = editApplicationRequestBuilder.build();		        						
		        	}		        	        		        	        
		        	try {
		        	      application= DataflowClient.getInstance().editApplication(DataTransferObject.applicationId,editApplicationRequest);     
		        	} catch (Exception e) {
		        	MessageDialog.openError(getShell(), "Failed to Edit Application ", e.getMessage());
		        	DataTransferObject.applicationId=null;
		        	return false;
		        	}    
		        	      
	     	         final CreateRunDetails runApplicationRequest =CreateRunDetails.builder()
	     		        	        .compartmentId(application.getCompartmentId())
	     		        			.displayName(application.getDisplayName())
	     		    				.applicationId(application.getId())
	     		    				.archiveUri(application.getArchiveUri())
	     		    				.driverShape(application.getDriverShape())
	     		    		        .executorShape(application.getExecutorShape())
	     		    		        .numExecutors(application.getNumExecutors())
	     		    		        .configuration(application.getConfiguration())
	     		    		        .logsBucketUri(application.getLogsBucketUri())
	     		    		        .warehouseBucketUri(application.getWarehouseBucketUri())
	     		    		        .arguments(application.getArguments())
	     		    		        .parameters(application.getParameters())
	     		    		        .build();

	     	             try {
	     	            	DataflowClient.getInstance().runApplication(runApplicationRequest);
	     	             } catch (Exception e) {
	     	                 MessageDialog.openError(getShell(), "Failed to Run Previously Created Application ", e.getMessage());
	     	                 DataTransferObject.applicationId=null;
	     	                 return false;
	     	             }
	     	            DataTransferObject.applicationId=null;
	     	            DataTransferObject.local=false;
		        	    return true;
		    	}				
			}
			else {
		    	final String compartmentId = firstpage.getApplicationCompartmentId();
		    	
		    	if(firstpage.usesSparkSubmit()) {		    		
		    		CreateRunDetails.Builder createApplicationRequestBuilder =  
		    				CreateRunDetails.builder()
		        	        .compartmentId(compartmentId)
		        			.displayName(firstpage.getDisplayName())
		        			.sparkVersion(firstpage.getSparkVersion())
		        			.driverShape(firstpage.getDriverShape())
		        			.executorShape(firstpage.getExecutorShape())
		        			.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
		        			.definedTags(tagpage.getOT())
		        			.freeformTags(tagpage.getFT())   
		        			.execute(firstpage.getSparkSubmit())
		        	        .logsBucketUri(thirdpage.getApplicationLogLocation())
		        	        .warehouseBucketUri(thirdpage.getWarehouseUri());
		        			
		    				final CreateRunDetails createApplicationRequest;	
		        			createApplicationRequest = createApplicationRequestBuilder.build();		
		        	        try {
		        	        	DataflowClient.getInstance().runApplication(createApplicationRequest);
		        	        } catch (Exception e) {
		        	            MessageDialog.openError(getShell(), "Failed to Run Application ", e.getMessage());
		        	            return false;
		        	        }
		        	        DataTransferObject.local=false;
		        	        return true;
		    	}
		    	
		    	else {
		        	Builder createApplicationRequestBuilder = 
		        	        CreateApplicationDetails.builder()
		        	        .compartmentId(compartmentId)
		        			.displayName(firstpage.getDisplayName())
		        			.description(firstpage.getApplicationDescription())
		        			.sparkVersion(firstpage.getSparkVersion())
		        			.driverShape(firstpage.getDriverShape())
		        			.executorShape(firstpage.getExecutorShape())
		        			.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
		        			.definedTags(tagpage.getOT())
		        			.freeformTags(tagpage.getFT())
		        			.language(firstpage.getLanguage())
		        			.fileUri(firstpage.getFileUri())
		        			.archiveUri(firstpage.getArchiveUri());
		        				
		        				if(firstpage.getLanguage()== ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
		        		    		createApplicationRequestBuilder = createApplicationRequestBuilder.className(firstpage.getMainClassName())
		        		    				.arguments(firstpage.getArguments());					
		        		    	}
		        		    	else if (firstpage.getLanguage()== ApplicationLanguage.Python) {
		        		    		createApplicationRequestBuilder = createApplicationRequestBuilder.arguments(firstpage.getArguments());			
		        		    	}
		        		    	
		        		    createApplicationRequestBuilder = createApplicationRequestBuilder.parameters(firstpage.getParameters());			
		        		    	

		        	    	final CreateApplicationDetails createApplicationRequest;
		        	    	
		        			final boolean usesAdvancedOptions = thirdpage.usesAdvancedOptions();
		        			if (usesAdvancedOptions) {
		        				createApplicationRequestBuilder = createApplicationRequestBuilder.configuration(thirdpage.getSparkProperties())
		        						.logsBucketUri(thirdpage.getApplicationLogLocation()).warehouseBucketUri(thirdpage.getWarehouseUri());
		        				
		        				final boolean usesPrivateSubnet = thirdpage.usesPrivateSubnet();
		        				if(usesPrivateSubnet) {
		        					createApplicationRequest = createApplicationRequestBuilder.privateEndpointId(thirdpage.getPrivateEndPointId())
		        							.build();
		        				}
		        				else {
		        					createApplicationRequest = createApplicationRequestBuilder.build();
		        				}
		        						
		        			} else {
		        				createApplicationRequest = createApplicationRequestBuilder.build();
		        						
		        			}
		        	    		
		        	        
		        	        try {
		        	        	application= DataflowClient.getInstance().createApplication(createApplicationRequest);       	        
		        	        } catch (Exception e) {
		        	            MessageDialog.openError(getShell(), "Failed to Create Application ", e.getMessage());
		        	            return false;
		        	        }    
		        	      
	     	               final CreateRunDetails runApplicationRequest =CreateRunDetails.builder()
	     		        	        .compartmentId(application.getCompartmentId())
	     		        			.displayName(application.getDisplayName())
	     		    				.applicationId(application.getId())
	     		    				.archiveUri(application.getArchiveUri())
	     		    				.driverShape(application.getDriverShape())
	     		    		        .executorShape(application.getExecutorShape())
	     		    		        .numExecutors(application.getNumExecutors())
	     		    		        .configuration(application.getConfiguration())
	     		    		        .logsBucketUri(application.getLogsBucketUri())
	     		    		        .warehouseBucketUri(application.getWarehouseBucketUri())
	     		    		        .arguments(application.getArguments())
	     		    		        .parameters(application.getParameters())
	     		    		        .build();
	     	               
	     	             try {
	     	            	DataflowClient.getInstance().runApplication(runApplicationRequest);
	     	             } 
	     	             catch (Exception e) {
	     	                 MessageDialog.openError(getShell(), "Failed to Run Application ", e.getMessage());
	     	                 return false;
	     	             }
	     	            DataTransferObject.local=false;
		        	    return true;
		    	}
			}
	    }
	    public void performValidations(List<Object> objectArray,List<String>nameArray) {
	    	
	    	objectArray.add(firstpage.getDisplayName());
	    	nameArray.add("name");
	    	
	    	objectArray.add(firstpage.getApplicationDescription());
	    	nameArray.add("description");

	    	if(!firstpage.usesSparkSubmit()) {    		
	        	objectArray.add(firstbpage.getFileUri());
	        	nameArray.add("fileuri");   		
	    	}
	    	if(!firstpage.usesSparkSubmit() && (firstpage.getLanguage() == ApplicationLanguage.Java )) {
	        	objectArray.add(firstpage.getMainClassName());
	        	nameArray.add("mainclassname"); 
	    	}
	    	
	       objectArray.add(thirdpage.getApplicationLogLocation());
	       nameArray.add("loguri"); 
	       
	       if(thirdpage.getWarehouseUri() != null && !thirdpage.getWarehouseUri().isEmpty()) {
	           objectArray.add(thirdpage.getWarehouseUri());
	           nameArray.add("warehouseuri"); 
	       }     
	    	if (thirdpage.usesPrivateSubnet()){
	    	       objectArray.add(thirdpage.privateEndpointsCombo.getText());
	    	       nameArray.add("subnetid"); 
	    	}
	    	
	    	if(!firstpage.usesSparkSubmit() && secondbpage.getArchiveUri() != null && !secondbpage.getArchiveUri().isEmpty()) {
	    	       objectArray.add(secondbpage.getArchiveUri());
	    	       nameArray.add("archiveuri"); 
	    	}
	    	
	    	if(!firstpage.usesSparkSubmit() && thirdpage.getSparkProperties() != null) {
	 	       objectArray.add(thirdpage.getSparkProperties().keySet());
	 	       nameArray.add("sparkprop" + firstpage.getSparkVersion().charAt(0));         
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
