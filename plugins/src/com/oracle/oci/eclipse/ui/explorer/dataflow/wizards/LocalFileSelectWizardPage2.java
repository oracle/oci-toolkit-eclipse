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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.bmc.objectstorage.responses.ListBucketsResponse;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetBuckets;

public class LocalFileSelectWizardPage2 extends WizardPage {
    private static final String BUCKET_KEY = "bucket";
    private ISelection selection;
    private Tree tree;
    private Text compartmentText;
    private Image IMAGE;
    private String fileName;
    private List<BucketSummary> buckets;
	private Compartment selectedApplicationCompartment;
	private Map<BucketSummary, TreeItem> bucketTreeMap;
	private String fileUriSelected;
	private Text fileUriText = null;
	boolean fileSelected = false;
	private ListBucketsResponse listBucketsResponse;
	private String pageToShow= null;
	private Button previousPage,nextPage;
	private String nextPageStr;
	
	   public LocalFileSelectWizardPage2(ISelection selection, String COMPARTMENT_ID) {
	        super("wizardPage");
	        setTitle("Bucket Selection Wizard for Archive Zip.");     
	        setDescription("Choose a Bucket for uploading Archive Zip file for adding external dependencies.");
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
					pageToShow=listBucketsResponse.getOpcNextPage();
			        createBucketSection();	
	            }

	            @Override
	            public void widgetDefaultSelected(SelectionEvent e) {}
	        });
	        
	        previousPage.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	                
					pageToShow = null;
			        createBucketSection();	
	            }

	            @Override
	            public void widgetDefaultSelected(SelectionEvent e) {}
	        });
	        
	        createBucketSection();	
	        
			Composite fileUriContainer = new Composite(container, SWT.NONE);
	        GridLayout fileUriLayout = new GridLayout();
	        fileUriLayout.numColumns = 2;
	        fileUriContainer.setLayout(fileUriLayout);
	        fileUriContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
	        Label fileUriLabel = new Label(fileUriContainer, SWT.NULL);
			fileUriLabel.setText("& Archive Uri :"); 
			fileUriText = new Text(fileUriContainer, SWT.BORDER | SWT.SINGLE);
			fileUriText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fileUriText.setText("");
			
			Label messageLabel = new Label(fileUriContainer, SWT.NULL);
			messageLabel.setText("&Format :"); 
			messageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
			Label fileUriMessage = new Label(fileUriContainer, SWT.NULL);
			fileUriMessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fileUriMessage.setText("oci://<bucket_name>@<namespace_name>/<file_name>");
			fileUriMessage.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
	        
	        setControl(container);
	    }
	    
	    private void createBucketSection() {

	    	if(bucketTreeMap != null)
	    	{
	        	for(Entry<BucketSummary,TreeItem> item: bucketTreeMap.entrySet() ) {        		
	        		item.getValue().removeAll();
	        		item.getValue().dispose();     		
	        	}
	    	}
	    	try {	    		
	    		IRunnableWithProgress op = new GetBuckets(selectedApplicationCompartment.getId(),pageToShow);
                new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
                String errorMessage=((GetBuckets)op).getErrorMessage();
            	if(errorMessage!=null) 
            		throw new Exception(errorMessage);
                listBucketsResponse=((GetBuckets)op).listBucketsResponse;
                buckets=((GetBuckets)op).bucketSummaryList;
                
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to get list buckets", e1.getMessage());	     
			}
	    	
	        nextPageStr=listBucketsResponse.getOpcNextPage();
			if(nextPageStr!=null&&!nextPageStr.isEmpty()) {
				nextPage.setEnabled(true);
			}
			else {
				nextPage.setEnabled(false);
			} 
	    	

	   	 Job job = new Job("Get Objects inside Bucket in User Compartment") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {	            	
	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                        	bucketTreeMap= new HashMap<BucketSummary,TreeItem>();
	                        	for(BucketSummary bucket : buckets) {                       		
	                        		bucketTreeMap.put(bucket, new TreeItem(tree,0));
	                        		bucketTreeMap.get(bucket).setText(bucket.getName());
	                        		bucketTreeMap.get(bucket).setImage(IMAGE);
	                        		bucketTreeMap.get(bucket).setData(BUCKET_KEY,bucket);
	                        	}	                         
	                        } catch(Exception ex) {
	                        	ErrorHandler.logError("Unable to create tree Item: " + ex.getMessage());
	                        }
	                    }
	                });
	                return Status.OK_STATUS;
	            }
	        };
	        job.schedule();  
	        
	        tree.addListener (SWT.MeasureItem, new Listener() {
	            @Override
	            public void handleEvent(Event event) {}
	        });
	        tree.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	 TreeItem[] items = tree.getSelection();
		       	        if(items !=null && items.length>0) {
		       	            TreeItem selectedItem = items[0];
		       	            BucketSummary bucket = (BucketSummary)selectedItem.getData(BUCKET_KEY);	   
		       	            fileName = getPreviousName();
		       	            fileUriSelected = "oci://"+ bucket.getName() +"@" + ObjStorageClient.getInstance().getNamespace()+"/"+fileName;
		       	            fileUriText.setText(fileUriSelected);
		       	            fileSelected = true;
		       	            canFlipToNextPage();
		       	            getWizard().getContainer().updateButtons();
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
						 createBucketSection();
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
	    
	    public String getBucketSelected() {
	        TreeItem[] items = tree.getSelection();
	        if(items !=null && items.length>0) {
	            TreeItem selectedItem = items[0];
	            BucketSummary bucket = (BucketSummary)selectedItem.getData(BUCKET_KEY);	      	            
	            return bucket.getName();
	        }
	        return null;
	    }
	    
	    public String getPreviousName() {	
	    	if(fileUriText.getText() != null && !fileUriText.getText().equals(""))
	    		return fileUriText.getText().substring(fileUriText.getText().lastIndexOf('/')+1);
	    	else
	    		return fileName;
	    }
	    
	    public String getArchiveUri() {
	       return fileUriText.getText();
	    }	    
	    
		void onEnterPage()
		{
		    if(DataTransferObject.archivedir != null) {
		    	String fileDirectory = DataTransferObject.archivedir;
		    	this.fileName = fileDirectory.substring(fileDirectory.lastIndexOf('\\')+1);
		    }
		}
		
		@Override
		public boolean canFlipToNextPage() {
			
	    if(DataTransferObject.archivedir == null || (DataTransferObject.archivedir != null && fileSelected)) {
	    	return true;
	    }
		return false;
		}
		
		public String getnewName() {			
			return fileUriText.getText().substring(fileUriText.getText().lastIndexOf('/')+1);
		}
		
		 @Override
		    public IWizardPage getNextPage() { 
	        return super.getNextPage();
		    }
}
