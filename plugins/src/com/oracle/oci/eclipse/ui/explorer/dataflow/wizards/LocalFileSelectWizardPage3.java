package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.dataflow.responses.ListApplicationsResponse;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetApplications;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetBuckets;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetRuns;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.SetTagsAction;

public class LocalFileSelectWizardPage3  extends WizardPage{
	
    private static final String APPLICATION_KEY = "application";
    private ISelection selection;
    private Tree tree;
    private Text compartmentText;
    private Image IMAGE;
    private List<ApplicationSummary> applications;
	private Compartment selectedApplicationCompartment;
	private Map<ApplicationSummary, TreeItem> applicationTreeMap;
	private String applicationIdSelected = null;
    private ListApplicationsRequest.SortBy sortBy=ListApplicationsRequest.SortBy.TimeCreated;
	private ListApplicationsRequest.SortOrder sortOrder=ListApplicationsRequest.SortOrder.Desc;
	private ListApplicationsResponse listApplicationsResponse;
	private String pageToShow= null;
	private Button previousPage,nextPage;
	private String nextPageStr;
	
	   public LocalFileSelectWizardPage3(ISelection selection, String COMPARTMENT_ID) {
	        super("wizardPage");
	        setTitle("Choose an Application to edit or Create new.");     
	        setDescription("To edit a previously created application, select an application. Ignore this page"
	        		+ " if you want to create a new application.");
	        this.selection = selection;
	        IMAGE = Activator.getImage(Icons.BUCKET.getPath());
	        
	        this.selectedApplicationCompartment = IdentClient.getInstance().getRootCompartment();
	        
			Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
			List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
			for(Compartment compartment : Allcompartments) {
				if(compartment.getId().equals(COMPARTMENT_ID)) {
					this.selectedApplicationCompartment= compartment;
					break;
				}
			}
	    }
	   
	    @Override
	    public void createControl(Composite parent) {
	    	
	        Composite container = new Composite(parent, SWT.NULL);
	        GridLayout layout = new GridLayout();
	        container.setLayout(layout);
			Composite innerTopContainer = new Composite(container, SWT.NONE);
	        GridLayout innerTopLayout = new GridLayout();
	        innerTopLayout.numColumns = 3;
	        innerTopContainer.setLayout(innerTopLayout);
	        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label compartmentLabel = new Label(innerTopContainer, SWT.NULL);
			compartmentLabel.setText("&Choose a compartment:");
	        compartmentText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
	        compartmentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        compartmentText.setEditable(false);
	        compartmentText.setText(selectedApplicationCompartment.getName());

	        Button compartmentButton = new Button(innerTopContainer, SWT.PUSH);
	        compartmentButton.setText("Choose...");
	        compartmentButton.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	handleSelectApplicationCompartmentEvent();
	            }
	        });

	        
	        tree = new Tree(container, SWT.RADIO | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));	               
	        
	        Composite page=new Composite(container,SWT.NONE);GridLayout gl=new GridLayout();gl.numColumns=2;
	        page.setLayout(gl);
	        previousPage=new Button(page,SWT.TRAVERSE_PAGE_PREVIOUS);
	        nextPage=new Button(page,SWT.TRAVERSE_PAGE_NEXT);
	        previousPage.setText("Refresh");
	        nextPage.setText(">");
	        previousPage.setLayoutData(new GridData());
	        nextPage.setLayoutData(new GridData());
	        
	        nextPage.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {	                
					pageToShow=listApplicationsResponse.getOpcNextPage();
					createApplicationSection();
	            }

	            @Override
	            public void widgetDefaultSelected(SelectionEvent e) {}
	        });
	        
	        previousPage.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	                
					pageToShow=listApplicationsResponse.getOpcPrevPage();
					createApplicationSection();
	            }

	            @Override
	            public void widgetDefaultSelected(SelectionEvent e) {}
	        });
	        
	        createApplicationSection();  
	        
	        setControl(container);
	    }
	    
	    private void createApplicationSection() {
	    	
	    	if(applicationTreeMap != null)
	    	{
	        	for(Entry<ApplicationSummary,TreeItem> item: applicationTreeMap.entrySet() ) {        		
	        		item.getValue().removeAll();
	        		item.getValue().dispose();      		
	        	}
	    	}
	    	try {	    		
	    		IRunnableWithProgress op = new GetApplications(selectedApplicationCompartment.getId(),sortBy,sortOrder,pageToShow);
                new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
                String errorMessage=((GetApplications)op).getErrorMessage();
            	if(errorMessage!=null) 
            		throw new Exception(errorMessage);
                listApplicationsResponse=((GetApplications)op).listApplicationsResponse;
                applications=((GetApplications)op).applicationSummaryList;
                
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to get list applications ", e1.getMessage());	     
			}
	    	
	        nextPageStr=listApplicationsResponse.getOpcNextPage();
			if(nextPageStr!=null&&!nextPageStr.isEmpty()) {
				nextPage.setEnabled(true);
			}
			else {
				nextPage.setEnabled(false);
			}

	   	 Job job = new Job("Get Applications in User Compartment") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {
	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                        	applicationTreeMap= new HashMap<ApplicationSummary,TreeItem>();
	                        	for(ApplicationSummary application : applications) {                       		
	                        		applicationTreeMap.put(application, new TreeItem(tree,0));
	                        		applicationTreeMap.get(application).setText(application.getDisplayName());
	                        		applicationTreeMap.get(application).setImage(IMAGE);
	                        		applicationTreeMap.get(application).setData(APPLICATION_KEY,application);
	                        	}	                         
	                        } catch(Exception ex) {}
	                    }
	                });
	                return Status.OK_STATUS;
	            }
	        };
	        job.schedule();  
	       
	        tree.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	 TreeItem[] items = tree.getSelection();
		       	        if(items !=null && items.length>0) {
		       	            TreeItem selectedItem = items[0];
		       	            ApplicationSummary application = (ApplicationSummary)selectedItem.getData(APPLICATION_KEY);	      
		       	            applicationIdSelected= application.getId();		       	            
		       	        }
	            }
	        });	        
	   }
	   private void handleSelectApplicationCompartmentEvent() {
	    	Consumer<Compartment> consumer=new Consumer<Compartment>() {
				@Override
				public void accept(Compartment compartment) {
					if (compartment != null) {
						selectedApplicationCompartment = compartment;
						compartmentText.setText(selectedApplicationCompartment.getName());
						pageToShow= null;
						createApplicationSection();
					}
				}
			};
	    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
	    	new CompartmentSelectWizard(consumer, false));
			dialog.setFinishButtonText("Select");
			if (Window.OK == dialog.open()) {
			}
	    }
		
	    private void updateStatus(String message) {
	        setErrorMessage(message);
	        setPageComplete(message == null);
	    }
	    
	    public String getApplicationSelected() {
	        TreeItem[] items = tree.getSelection();
	        if(items !=null && items.length>0) {
	            TreeItem selectedItem = items[0];
	            ApplicationSummary Application = (ApplicationSummary)selectedItem.getData(APPLICATION_KEY);	      	            
	            return Application.getId();
	        }
	        return null;
	    }
	    
		@Override
		public boolean canFlipToNextPage() {
			return true;
		}	    
		
		
		 @Override
		    public IWizardPage getNextPage() {
			 RunAsDataflowApplicationWizard wizard = (RunAsDataflowApplicationWizard)getWizard();
			 wizard.canFinish= true;
			 wizard.canFinish();
			 getWizard().getContainer().updateButtons();
			 String applicationId=getApplicationSelected();
			 if(applicationId==null) {
				 DataTransferObject.applicationId=null;
				 CreateApplicationWizardPage page = ((RunAsDataflowApplicationWizard)getWizard()).firstpage;
				 page.clearSelection();
				 TagsPage tagsPage = ((RunAsDataflowApplicationWizard)getWizard()).tagpage;
				 tagsPage.clearTags();
				 CreateApplicationWizardPage3 advpage = ((RunAsDataflowApplicationWizard)getWizard()).thirdpage;
				 advpage.clearSelection();
			 }

			 if(applicationId!=null&&DataTransferObject.applicationId != applicationIdSelected) {
				 
				 DataTransferObject.applicationId=applicationIdSelected; 		
				    CreateApplicationWizardPage page = ((RunAsDataflowApplicationWizard)getWizard()).firstpage;
				    page.onEnterPage();
				    TagsPage tagsPage = ((RunAsDataflowApplicationWizard)getWizard()).tagpage;
				    tagsPage.clearTags();
				    Application application = DataflowClient.getInstance().getApplicationDetails(applicationIdSelected);
				    
				    try {
				    IRunnableWithProgress op = new SetTagsAction(tagsPage,application.getDefinedTags(),application.getFreeformTags());
                    new ProgressMonitorDialog(getShell()).run(true, true, op);
                    String errorMessage=((SetTagsAction)op).getErrorMessage();
                	if(errorMessage!=null) 
                		throw new Exception(errorMessage);
				    }
				    catch (Exception e) {
				    	MessageDialog.openError(getShell(), "Unable to get already set tags", e.getMessage());
				    }
				    tagsPage.setTags(application.getDefinedTags(),application.getFreeformTags());
				    
				    CreateApplicationWizardPage3 advpage = ((RunAsDataflowApplicationWizard)getWizard()).thirdpage;
				    advpage.onEnterPage();	
				    return page;				     
			 }
			 CreateApplicationWizardPage page = ((RunAsDataflowApplicationWizard)getWizard()).firstpage;
			 page.setJarAndArchiveUri();
			 return super.getNextPage();
		   }

}
