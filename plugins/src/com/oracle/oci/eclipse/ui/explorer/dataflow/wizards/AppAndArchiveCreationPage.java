package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.CreateFiles;

public class AppAndArchiveCreationPage extends WizardPage{
	
	    private ISelection selection;
	    private Tree tree;
	    private Image IMAGE;
	    private Composite pc,container;
	    private ProjectSelectWizardPage page;
	    boolean fileCreated = false;
	    protected Job job;
	    private IJavaProject project=null;
	    private Button createJar,createArchive,selDesel;

	    public AppAndArchiveCreationPage(ISelection selection,ProjectSelectWizardPage page) {
	        super("wizardPage");
	        setTitle("Application dependencies selection");
	        setDescription("Select the external dependencies required to run this project as Dataflow application");
	        this.page=page;
	        this.selection=selection;
	    }

	    @Override
	    public void createControl(Composite parent) {
	    	
	        container = new Composite(parent, SWT.NULL);
	        GridLayout layout = new GridLayout();
	        container.setLayout(layout);
	        
	        job = new Job("Get External Jars") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {

	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                        	
	                        	IJavaProject newProj=page.getSelectedProject();
	                        	if(project!=null&&project.equals(newProj)) {
	                        		return;
	                        	}
	                        	project=newProj;
	                        	createJar.setEnabled(true);
	                        	createArchive.setEnabled(true);
	                        	selDesel.setText("Deselect All");
	                        	DataTransferObject.filedir=null;
	                        	DataTransferObject.archivedir=null;
                        		fileCreated=false;
                        		getWizard().getContainer().updateButtons();
	                        	tree.removeAll();
	                            for (IClasspathEntry p : project.getRawClasspath()) {
	                                                try {
	                                                	String path=p.getPath().toString();
	                                                    if(!path.endsWith(".jar")) continue;
	                                                    TreeItem treeItem = new TreeItem(tree, 0);
	                                                    treeItem.setText(path.substring(path.lastIndexOf(File.separator)+1));
	                                                    treeItem.setImage(IMAGE);
	                                                    treeItem.setData("path", path);
	                                                } 
	                                                catch(Exception e) {
	                                                	MessageDialog.openError(getShell(), "Error", "Failed to load external jar files");
	                                                }
	                            }
	                            for(TreeItem e:tree.getItems()) {
	                            	e.setChecked(true);
	                            }
	                        } catch(Exception e) {
	                        	MessageDialog.openError(getShell(), "Error creating Jar tree items", e.getMessage());
	                        }
	                    }
	                });
	                return Status.OK_STATUS;
	            }
	        };
	        
	        pc=new Composite(container,SWT.NONE);
	        GridLayout gl=new GridLayout();
	        gl.numColumns=2;
	        pc.setLayout(gl);
	        pc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	        selDesel=new Button(pc,SWT.PUSH);
	        selDesel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        selDesel.setText("Deselect All");
	        selDesel.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		if(selDesel.getText().equals("Select All")) {
	            			for(TreeItem te:tree.getItems()) {
                            	te.setChecked(true);
                            }
	            			selDesel.setText("Deselect All");
	            		}
	            		else {
	            			for(TreeItem te:tree.getItems()) {
                            	te.setChecked(false);
                            }
	            			selDesel.setText("Select All");
	            		}
						
					} catch (Exception e1) {
						MessageDialog.openError(getShell(), "Error perfroming select/deselect operation", e1.getMessage());
					}
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	        tree = new Tree(container, SWT.CHECK | SWT.BORDER);
	        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	        
	        Composite dComp=new Composite(container,SWT.NONE);
	        GridLayout gl2=new GridLayout();
	        gl2.numColumns=2;
	        dComp.setLayout(gl2);
	        dComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        
	        createJar=new Button(dComp,SWT.PUSH);
	        createJar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        
	        createArchive=new Button(dComp,SWT.PUSH);
	        createJar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        createJar.setText("Create Application Jar File *");
	        createArchive.setText("Create Dependency archive file");
	        createArchive.setEnabled(false);
	        
	        createJar.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		IJavaProject p=page.getSelectedProject();
	            		if(p==null) 
	            			throw new Exception("Improper Selection of Project");
	            		page.start(p);
	            		createJar.setEnabled(false);
	            		createArchive.setEnabled(true);
	            		fileCreated = true;
	            		canFlipToNextPage();
	            		getWizard().getContainer().updateButtons();
						
					} catch (Exception e1) {
						MessageDialog.openError(getShell(), "Error creating Jar", e1.getMessage());
					}
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	        createArchive.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		setJars();
	            		IRunnableWithProgress op = new CreateFiles(DataTransferObject.jarList.size()+1);
	                    new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
						
					} 
	            	catch (Exception e1) {
	            		MessageDialog.openError(getShell(), "Error", e1.getMessage());
	            	}
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	        setControl(container);
	    }

		public List<String> setJars() throws Exception{
			DataTransferObject.jarList=new ArrayList<String>();
			for(TreeItem e:tree.getItems()) {
				if(e.getChecked()) DataTransferObject.jarList.add(e.getData("path").toString());
			}
			return DataTransferObject.jarList;
		}
		 
		@Override
		public boolean canFlipToNextPage() {
			return fileCreated;
		}
		   
		@Override
		public IWizardPage getNextPage() { 		 				   			   
			LocalFileSelectWizardPage1 firstpage = ((RunAsDataflowApplicationWizard)getWizard()).firstbpage;
			firstpage.onEnterPage();
			LocalFileSelectWizardPage2 secondpage = ((RunAsDataflowApplicationWizard)getWizard()).secondbpage;
			secondpage.onEnterPage();
			return firstpage;       
		}
}
