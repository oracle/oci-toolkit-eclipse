package com.oracle.oci.eclipse.ui.account;

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
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.responses.ListBucketsResponse;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetBuckets;

public class BucketSelectWizardPage extends WizardPage {
	
    private static final String BUCKET_KEY = "bucket";
    private static final String OBJECT_KEY = "object";
    private ISelection selection;
    private Tree tree;
    private Text compartmentText;
    private Image IMAGE;
    private ApplicationLanguage language;
    private List<BucketSummary> buckets;
	private Compartment selectedApplicationCompartment;
	Map<BucketSummary, TreeItem> bucketTreeMap;
	private ListBucketsResponse listbucketsresponse;
	private String pagetoshow= null;
	private Button previouspage,nextpage;
    
    public BucketSelectWizardPage(ISelection selection,String CompartmentId,ApplicationLanguage language) {
        super("wizardPage");
        setTitle("Select application file");     
        setDescription("Select application file from Object Storage");
        this.selection = selection;
        this.language= language;
        IMAGE = Activator.getImage(Icons.BUCKET.getPath());
        Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
        this.selectedApplicationCompartment = rootCompartment;
		List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
		for(Compartment compartment : Allcompartments) {
			if(compartment.getId().equals(CompartmentId)) {
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
                
        createBucketSection();
        
        Composite page=new Composite(container,SWT.NONE);GridLayout gl=new GridLayout();gl.numColumns=2;
        page.setLayout(gl);
        previouspage=new Button(page,SWT.TRAVERSE_PAGE_PREVIOUS);
        nextpage=new Button(page,SWT.TRAVERSE_PAGE_NEXT);
        previouspage.setText("<");
        nextpage.setText(">");
        previouspage.setLayoutData(new GridData());
        nextpage.setLayoutData(new GridData());
        
        nextpage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {	                
				pagetoshow=listbucketsresponse.getOpcNextPage();
		        createBucketSection();	
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        previouspage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				pagetoshow = null;
		        createBucketSection();	
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
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
    		IRunnableWithProgress op = new GetBuckets(selectedApplicationCompartment.getId(),pagetoshow);
            new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
            listbucketsresponse=((GetBuckets)op).listBucketsResponse;
            buckets=((GetBuckets)op).bucketSummaryList;
            
		} catch (Exception e1) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to get list buckets", e1.getMessage());	     
		}
    	
    	
   	 Job job = new Job("Get Objects inside Bucket in User Compartment") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // update tree node using UI thread
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
                           
                        	for(BucketSummary bucket : buckets) {                        		
                        		for(ObjectSummary objects :ObjStorageClient.getInstance().getBucketObjects(bucket.getName())) {
                        			Job job = new Job("Get contents of buckets") {
                                        @Override
                                        protected IStatus run(IProgressMonitor monitor) {                                            
                                            Display.getDefault().asyncExec(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                    	if(correctformat(objects.getName())) {
                                                    		TreeItem treeItem = new TreeItem(bucketTreeMap.get(bucket), 0);
                                                            treeItem.setText(objects.getName());
                                                            treeItem.setImage(IMAGE);
                                                            treeItem.setData(OBJECT_KEY,objects);
                                                    	}                                                     
                                                    } catch(Exception e) {
                                                    	ErrorHandler.logError("Unable to create Tree Item :" + e.getMessage());
                                                    }
                                                }
                                            });
                                            return Status.OK_STATUS;
                                        }
                                    };
                                    job.schedule();
                        			}
                        	}
                        } catch(Exception ex) {
                        	ErrorHandler.logError("Unable to list buckets: " + ex.getMessage());
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

    
   }
    
	private void handleSelectApplicationCompartmentEvent() {
    	Consumer<Compartment> consumer=new Consumer<Compartment>() {
			@Override
			public void accept(Compartment compartment) {
				if (compartment != null) {
					selectedApplicationCompartment = compartment;
					compartmentText.setText(selectedApplicationCompartment.getName());
					pagetoshow= null;
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
    private boolean correctformat (String Name) {
    	if(language == ApplicationLanguage.Java) {
    		String ending = Name.substring(Name.length()-4);
    		if(!ending.equals(".jar")) {
    			return false;
    		}
    	}
    	else if(language == ApplicationLanguage.Python) {
    		String ending = Name.substring(Name.length()-3);
    		if(!ending.equals(".py")) {
    			return false;
    		}
    	}
    	else if(language == ApplicationLanguage.Sql) {
    		String ending = Name.substring(Name.length()-4);
    		if(!ending.equals(".sql")) {
    			return false;
    		}
    	}
    	else if(language == ApplicationLanguage.Scala) {
    		String ending = Name.substring(Name.length()-4);
    		if(!ending.equals(".jar")) {
    			return false;
    		}
    	}
    	return true;
    }
    
    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getObjectSelected() {
        TreeItem[] items = tree.getSelection();
        if(items !=null && items.length>0) {
            TreeItem selectedItem = items[0];
            TreeItem parent = selectedItem.getParentItem();
            BucketSummary bucket = (BucketSummary)parent.getData(BUCKET_KEY);
            ObjectSummary object = (ObjectSummary)selectedItem.getData(OBJECT_KEY);
            
            String fileUri = "oci://"+bucket.getName()+"@" + ObjStorageClient.getInstance().getNamespace()+"/"+object.getName();
            return fileUri;
        }

        return null;
    }
    

}
