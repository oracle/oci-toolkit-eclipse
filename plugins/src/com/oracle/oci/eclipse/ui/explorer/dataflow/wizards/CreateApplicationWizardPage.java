package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.ApplicationParameter;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.account.BucketSelectWizard;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class CreateApplicationWizardPage extends WizardPage {
	
	private ISelection selection;
	private Composite container;	
	private Text displayNameText;
	private Text applicationDescriptionText;
	private Text compartmentText;	
    private ScrolledComposite scrolledComposite;   	
	private String selectedApplicationCompartmentId= AuthProvider.getInstance().getCompartmentId();
	private String selectedApplicationCompartmentName= AuthProvider.getInstance().getCompartmentName();	
	private Combo sparkVersionCombo;
	private Combo driverShapeCombo;
	private Combo executorShapeCombo;
	private Spinner numofExecutorsSpinner;
	private Button useSparkSubmitButton;
	private Group languageGroup;
	private Button languageGroupJavaRadioButton;
	private Button languageGroupPythonRadioButton;
	private Button languageGroupSQLRadioButton;
	private Button languageGroupScalaRadioButton;	
	private ApplicationLanguage languageUsed;
    private Composite parameterContainer;
    private Set<Parameters> parameterSet=new HashSet<Parameters>();		
	private Label mainClassNamelabel;
	private Label argumentsLabel;
	private Text mainClassNameText;
	private Text argumentsText;
	private Text archiveUriText;
	private Text fileUriText;
	private boolean usesSparkSubmit=false;
	private Button fileSelectButton;
	private Label fileUriLabel;
	private Label languageLabel;
	private Label archiveUriLabel;
	private Composite fileUriContainer ;	
	private Label sparkSubmitLabel;
	private Text sparkSubmitText;
	boolean allow = false;
	
	public CreateApplicationWizardPage(ISelection selection,String COMPARTMENT_ID) {
		super("page");
		setTitle("Create DataFlow Application");
		setDescription("This wizard creates a new DataFlow Application. Please enter the required details.");
		this.selection = selection;

		Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
		if(DataTransferObject.applicationId!=null)
		{
			Application app= DataflowClient.getInstance().getApplicationDetails(DataTransferObject.applicationId);
			COMPARTMENT_ID = app.getId();
		}
		List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
		for(Compartment compartment : Allcompartments) {
			if(compartment.getId().equals(COMPARTMENT_ID)) {
				this.selectedApplicationCompartmentId= compartment.getId();
				this.selectedApplicationCompartmentName= compartment.getName();
				break;
			}
		}
	}
	
	@Override
	public void createControl(Composite parent) {	
		
		scrolledComposite=new ScrolledComposite(parent,SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setExpandHorizontal( true );
		scrolledComposite.setExpandVertical( true );       
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	
		container = new Composite(scrolledComposite, SWT.NULL);
		scrolledComposite.setContent(container);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		
		Label compartmentLabel = new Label(container, SWT.NULL);
		compartmentLabel.setText("&Choose a compartment: *");
		Composite innerTopContainer = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 2;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        compartmentText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        compartmentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        compartmentText.setEditable(false);
        compartmentText.setText(selectedApplicationCompartmentName);

        Button compartmentButton = new Button(innerTopContainer, SWT.PUSH);
        compartmentButton.setText("Choose...");
        compartmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleSelectApplicationCompartmentEvent();
            }
        });
        
		final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
		final String defaultApplicationName = DATE_TIME_FORMAT.format(new Date());
		
		Label displayNameLabel = new Label(container, SWT.NULL);
		displayNameLabel.setText("&Display name: *");
		displayNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		displayNameText.setLayoutData(gd);
		displayNameText.setText("App " + defaultApplicationName);
		
		Label Applicationdescriptionlabel = new Label(container, SWT.NULL);
		Applicationdescriptionlabel.setText("&Application Description:");
		applicationDescriptionText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		applicationDescriptionText.setLayoutData(gd1);
		
		Label SparkVersionLabel = new Label(container, SWT.NULL);
		SparkVersionLabel.setText("&Spark Version: *");
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		sparkVersionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		sparkVersionCombo.setLayoutData(gd2);		 
		sparkVersionCombo.setItems(DataflowConstants.versions);
		sparkVersionCombo.select(0);		      
		
		Label DriverShapeLabel = new Label(container, SWT.NULL);
		DriverShapeLabel.setText("&Driver Shape: *");
		createDriverShapeCombo(container);
		
		Label ExecutorShapeLabel = new Label(container, SWT.NULL);
		ExecutorShapeLabel.setText("&Executor Shape: *");
		createExecutorShapeCombo(container);
		
		Label NumofExecutorslabel = new Label(container, SWT.NULL);
		NumofExecutorslabel.setText("&Number of Executors: *");
		createNumofExecutorsSpinner(container); 
				
		useSparkSubmitButton =  new Button(container,SWT.CHECK);
		useSparkSubmitButton.setText("Use Spark Submit");
		useSparkSubmitButton.addSelectionListener(new SelectionAdapter() {
	     	    @Override
	     	    public void widgetSelected(SelectionEvent event) {
	     	    	 Button btn = (Button) event.getSource();
	     	    	 if(btn.getSelection()) {
	     	    		usesSparkSubmit=true; 
	     	    		disposeLanguagesection();
	     	    		withSparkSubmit(container);
	     	    	 }
	     	    	 else
	     	    	 {
	     	    		usesSparkSubmit=false;
	     	    		disposeSparkSubmit();
	     	    		withoutSparkSubmit(container);
	     	    	 }
	     	    	 container.layout(true,true);
	     	    }
	     	});
	        	        
	     Label dummy = new Label(container, SWT.NULL);	   				
	     withoutSparkSubmit(container);	 
	     scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		 setControl(scrolledComposite);		
	}
	
	private void withSparkSubmit(Composite container) {		
		 sparkSubmitLabel = new Label(container, SWT.NULL);
		 sparkSubmitLabel.setText("&Spark Submit Command: *");
		 sparkSubmitText = new Text(container, SWT.BORDER | SWT.MULTI);
		 GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		 gridData.heightHint = 5 * sparkSubmitText.getLineHeight();
		 sparkSubmitText.setLayoutData(gridData);      		        
	}
	
	private void withoutSparkSubmit(Composite container){
		languageLabel = new Label(container, SWT.NULL);
		languageLabel.setText("&Language: *");
		createLanguageCombo(container);
		
		fileUriLabel = new Label(container, SWT.NULL);
		fileUriLabel.setText("&Choose a File: *");
		fileUriContainer = new Composite(container, SWT.NONE);
        GridLayout FileUriLayout = new GridLayout();
        FileUriLayout.numColumns = 2;
        fileUriContainer.setLayout(FileUriLayout);
        fileUriContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fileUriText = new Text(fileUriContainer, SWT.BORDER | SWT.SINGLE);
        fileUriText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fileUriText.setEditable(false);
        fileSelectButton = new Button(fileUriContainer, SWT.PUSH);
        fileSelectButton.setText("Choose");
        
        fileSelectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleSelectObjectEvent();
            }
        });        
		 archiveUriLabel = new Label(container, SWT.NULL);
		 archiveUriLabel.setText("&Archive URL:");
		 archiveUriText = new Text(container, SWT.BORDER | SWT.SINGLE);
		 GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
		 archiveUriText.setLayoutData(gd10);   		 
		 languageUsed  = ApplicationLanguage.Java;            		
		 JavaLanguageSelected(container);
		 SQLLanguageSelected(container);
		 if(DataTransferObject.local) {
			 LocalFileSelectWizardPage1 page1 = ((RunAsDataflowApplicationWizard)getWizard()).firstbpage;
				fileUriText.setText(page1.getFileUri());
				fileUriText.setEditable(false);
				fileSelectButton.setEnabled(false);
				
				LocalFileSelectWizardPage2 page2 = ((RunAsDataflowApplicationWizard)getWizard()).secondbpage;
				archiveUriText.setText(page2.getArchiveUri());
				archiveUriText.setEditable(true);
		 }
	}
	
	private void disposeSparkSubmit() {
		if(sparkSubmitLabel != null) {
			sparkSubmitLabel.dispose();
			sparkSubmitText.dispose();
		}
	}
	
	private void disposeLanguagesection() {

		if(languageLabel != null) 
			languageLabel.dispose();
			
		if(languageGroup != null) {			
			languageGroupJavaRadioButton.dispose();
			languageGroupPythonRadioButton.dispose();
			languageGroupSQLRadioButton.dispose();
			languageGroupScalaRadioButton.dispose();
			languageGroup.dispose();
		}
			
		if(fileUriLabel != null) {
			fileUriLabel.dispose();
			fileUriText.dispose();
			fileSelectButton.dispose();
			fileUriContainer.dispose();
		}
		
		disposePrevious();
		
		if(archiveUriLabel != null) {
			archiveUriLabel.dispose();
			archiveUriText.dispose();
		}
		
			
	}
	private void createDriverShapeCombo(Composite container) {	
		driverShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		driverShapeCombo.setLayoutData(gd3);
		driverShapeCombo.setItems(DataflowConstants.shapesDetails);		
		driverShapeCombo.select(0);
		
	}
	
	private void createExecutorShapeCombo(Composite container) {
		executorShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		executorShapeCombo.setLayoutData(gd4);	 
		executorShapeCombo.setItems(DataflowConstants.shapesDetails);		
		executorShapeCombo.select(0);
		
	}
	
	private void createNumofExecutorsSpinner(Composite container) {
		numofExecutorsSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		numofExecutorsSpinner.setLayoutData(gd5);
		numofExecutorsSpinner.setMinimum(DataflowConstants.numOfExecutorsMin);
		numofExecutorsSpinner.setMaximum(DataflowConstants.numOfExecutorsMax);
		numofExecutorsSpinner.setIncrement(DataflowConstants.numOfExecutorsIncrement);
		// default value
		numofExecutorsSpinner.setSelection(DataflowConstants.numOfExecutorsDefault);
	}
	
	private void createLanguageCombo(Composite currentcontainer) {
				
		
		languageGroup = new Group(currentcontainer, SWT.NONE);
		RowLayout rowLayout1 = new RowLayout(SWT.HORIZONTAL);
        rowLayout1.spacing = 100;
		languageGroup.setLayout(rowLayout1);
		GridData gd6 = new GridData(GridData.FILL_HORIZONTAL);
		languageGroup.setLayoutData(gd6);
		
		languageGroupJavaRadioButton = new Button(languageGroup, SWT.RADIO);
		languageGroupJavaRadioButton.setText("Java");		
		languageGroupJavaRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(languageUsed != ApplicationLanguage.Java )
            	{
            		if(!DataTransferObject.local)
            			fileUriText.setText("");
            		disposePrevious();
            		languageUsed  = ApplicationLanguage.Java;            		
            		JavaLanguageSelected(container);
            		SQLLanguageSelected(container);             		
            		
            	}
            	currentcontainer.layout(true,true);
            	container.layout(true,true);
            	currentcontainer.pack();
            }
        });
      
		languageGroupJavaRadioButton.setSelection(true); 
		
		languageGroupPythonRadioButton = new Button(languageGroup, SWT.RADIO);
		languageGroupPythonRadioButton.setText("Python");		
		languageGroupPythonRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(languageUsed != ApplicationLanguage.Python )
            	{
            		disposePrevious();
            		if(!DataTransferObject.local)
            			fileUriText.setText("");
            		languageUsed  = ApplicationLanguage.Python;            		
            		PythonLanguageSelected(container);  
            		SQLLanguageSelected(container);  
            		
            		
            	}
            	currentcontainer.layout(true,true);
            	currentcontainer.pack();
            	container.layout(true,true);

            }
        });	
		
		languageGroupSQLRadioButton = new Button(languageGroup, SWT.RADIO);
		languageGroupSQLRadioButton.setText("SQL");		
		languageGroupSQLRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(languageUsed != ApplicationLanguage.Sql )
            	{
            		disposePrevious();
            		if(!DataTransferObject.local)
            			fileUriText.setText("");
            		languageUsed  = ApplicationLanguage.Sql;            		
            		SQLLanguageSelected(container);   
            		
            	}
            	currentcontainer.layout(true,true);
            	currentcontainer.pack();
            	container.layout(true,true);

            }
        });	
		
		languageGroupScalaRadioButton = new Button(languageGroup, SWT.RADIO);
		languageGroupScalaRadioButton.setText("Scala");
		languageGroupScalaRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(languageUsed != ApplicationLanguage.Scala )
            	{
            		disposePrevious();
            		if(!DataTransferObject.local)
            			fileUriText.setText("");
            		languageUsed  = ApplicationLanguage.Scala;            		
            		JavaLanguageSelected(container); 
            		SQLLanguageSelected(container);  
            		
            	}
            	
            	currentcontainer.layout(true,true);
            	container.layout(true,true);
            	currentcontainer.pack();
            }
        });

	}
	private void disposePrevious() {
		if(mainClassNameText != null) {
			mainClassNameText.dispose();
		}
		if(argumentsText != null) {
			argumentsText.dispose();
		}
		if(argumentsLabel != null) {
			argumentsLabel.dispose();
		}
		if(mainClassNamelabel != null) {
			mainClassNamelabel.dispose();
		}
		
		for(Parameters item : parameterSet) {
			item.composite.dispose();
		}
		parameterSet.clear();
		
		if(parameterContainer != null) {
			parameterContainer.dispose();
		}
	
	}
	private void JavaLanguageSelected(Composite container) {	
		mainClassNamelabel = new Label(container, SWT.NULL);
		mainClassNamelabel.setText("&Main Class Name: *");
		mainClassNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd7 = new GridData(GridData.FILL_HORIZONTAL);
		mainClassNameText.setLayoutData(gd7);		
		argumentsLabel = new Label(container, SWT.NULL);
		argumentsLabel.setText("&Arguments:");
		argumentsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
		argumentsText.setLayoutData(gd8);
		
		
	}
	
	private void PythonLanguageSelected(Composite container) {		
		argumentsLabel = new Label(container, SWT.NULL);
		argumentsLabel.setText("&Arguments:");
		argumentsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
		argumentsText.setLayoutData(gd8);		
	}
	
	private void SQLLanguageSelected(Composite parent) {
		
		parameterContainer = new Composite(parent, SWT.NULL);
		GridData grid1 = new GridData(GridData.FILL_HORIZONTAL);
		grid1.horizontalSpan = 2;
		parameterContainer.setLayoutData(grid1);
        GridLayout layout1 = new GridLayout();
        parameterContainer.setLayout(layout1);
        layout1.numColumns = 1;
		

        
        Button addParameter = new Button(parameterContainer,SWT.PUSH);
        addParameter.setLayoutData(new GridData());
        addParameter.setText("Add a Parameter");        
        addParameter.addSelectionListener(new SelectionAdapter() {        	
            public void widgetSelected(SelectionEvent e) {          	
            	Parameters newtag= new Parameters(parameterContainer,container,scrolledComposite, parameterSet);
            	parameterSet.add(newtag);
            }
          });        

	}
	
	private void handleSelectApplicationCompartmentEvent() {
    	Consumer<Compartment> consumer=new Consumer<Compartment>() {
			@Override
			public void accept(Compartment compartment) {
				if (compartment != null) {
					selectedApplicationCompartmentId = compartment.getId();
					selectedApplicationCompartmentName = compartment.getName();
					compartmentText.setText(selectedApplicationCompartmentName);
				}
			}
		};
    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
    	new CompartmentSelectWizard(consumer, false));
		dialog.setFinishButtonText("Select");
		if (Window.OK == dialog.open()) {
		}
    }

	private void handleSelectObjectEvent() {
    	Consumer<String> consumer=new Consumer<String>() {
			@Override
			public void accept(String object) {
				if (object != null) {
					fileUriText.setText(object);
				}
			}
		};
    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
				new BucketSelectWizard(consumer,selectedApplicationCompartmentId,languageUsed));
		dialog.setFinishButtonText("Select");
		if (Window.OK == dialog.open()) {
		}
    }
	
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
	
	public String getApplicationCompartmentId() {
		return selectedApplicationCompartmentId;
	}
	
	public String getDisplayName() {
		return displayNameText.getText();
	}
	
	public String getApplicationDescription() {
		return applicationDescriptionText.getText();
	}
	
	public String getSparkVersion() {		
		return sparkVersionCombo.getText();
	}
	
	public String getDriverShape() {			
		return driverShapeCombo.getText().split(" ")[0];
	}
	
	public String getExecutorShape() {	
		return executorShapeCombo.getText().split(" ")[0];
	}
	
	public String getNumofExecutors() {		
		return numofExecutorsSpinner.getText();
	}
	
	public ApplicationLanguage getLanguage() {		
		if(languageGroupJavaRadioButton.getSelection()) {
			return ApplicationLanguage.Java;
		}
		else if(languageGroupPythonRadioButton.getSelection()) {
			return ApplicationLanguage.Python;
		}
		else if(languageGroupSQLRadioButton.getSelection()) {
			return ApplicationLanguage.Sql;
		}
		else{
			return ApplicationLanguage.Scala;
		}
	}
	
	public String getFileUri() {		
		return fileUriText.getText();
	}
	
	public String getArchiveUri() {		
		return archiveUriText.getText().trim();
	}
	
	public String getMainClassName() {		
		return mainClassNameText.getText();
	}	
	
	public boolean usesSparkSubmit() {
		return usesSparkSubmit;
	}
	
	public String getSparkSubmit() {
		return sparkSubmitText.getText();
	}
	
	public  List<ApplicationParameter> getParameters(){
		List<ApplicationParameter> Parameters = new ArrayList<ApplicationParameter>();	 
		 for(Parameters parameter : parameterSet) {	
			 Parameters.add(ApplicationParameter.builder()
					 .name(parameter.tagKey.getText())
					 .value(parameter.tagValue.getText())
					 .build());
		 }		 
		 return Parameters;
	 }
	
	public List<String> getArguments(){		
	    List<String> arguments = new ArrayList<String>();
	    String argumentsunseperated = argumentsText.getText();
	   boolean invertedcomma = false;
	   String currentword = "";
	   for(int i= 0 ; i < argumentsunseperated.length() ; i++) {
		   if(Character.isWhitespace(argumentsunseperated.charAt(i)))
		   {
			   if(invertedcomma == true){
				   currentword+= argumentsunseperated.charAt(i);
			   }
			   else{
				   if(currentword != ""){
					   arguments.add(currentword);
					   currentword="";
				   }
			   }
		   }
		   else if (argumentsunseperated.charAt(i) == '"') {
			   if(invertedcomma == false) {				   
				   if(currentword == "") {
					   invertedcomma=true;
					   currentword+=argumentsunseperated.charAt(i);
				   }
				   else {
					   currentword+=argumentsunseperated.charAt(i);
				   }
			   }
			   else {
				   currentword += argumentsunseperated.charAt(i);
				   invertedcomma=false;
			   }  
		   }
		   else{
			   currentword += argumentsunseperated.charAt(i);
		   }   
	   }
	  if(currentword!="")
		   arguments.add(currentword);
	  
	  if(!parameterSet.isEmpty()) {
		  for(Parameters parameter : parameterSet) {	
			  	if(!arguments.contains("${"+parameter.tagKey.getText()+"}"))
			  		arguments.add("${"+parameter.tagKey.getText()+"}");
			 }		 
	  }
		   
	    return arguments;		
	}

	void onEnterPage()
	{
		setPageComplete(true);
		getWizard().getContainer().updateButtons();
	    String applicationId = DataTransferObject.applicationId;
	    if(applicationId != null) {
	    	Application application = DataflowClient.getInstance().getApplicationDetails(applicationId);
	    	
	    	displayNameText.setText(application.getDisplayName());
	    	
	    	if(application.getDescription()!= null)
	    		applicationDescriptionText.setText(application.getDescription());
	    	
	    	if(application.getSparkVersion().equals(DataflowConstants.versions[0])) {
	    		sparkVersionCombo.select(0);
			}
			else {
				sparkVersionCombo.select(1);
			}		
	    	
	    	for(int i=0; i<DataflowConstants.shapesDetails.length ; i++) {
				if(application.getDriverShape().equals(DataflowConstants.shapesDetails[i])) {
					driverShapeCombo.select(i);
				}
			}	
	    	
	    	for(int i=0; i<DataflowConstants.shapesDetails.length ; i++) {
				if(application.getExecutorShape().equals(DataflowConstants.shapesDetails[i])) {
					executorShapeCombo.select(i);
				}
			}		    	
	    	numofExecutorsSpinner.setSelection(application.getNumExecutors());	    	
	    	if(usesSparkSubmit){
	    		disposeSparkSubmit();
	    	}
	    	else{
	    		disposeLanguagesection();
	    	}	    	
	    	 if(application.getExecute() != null && !application.getExecute().equals("")){
	    		usesSparkSubmit=true; 
	    		withSparkSubmit(container);
	    		useSparkSubmitButton.setSelection(true);
	    		useSparkSubmitButton.setVisible(false);
	    		sparkSubmitText.setText(application.getExecute());    		
	    	 }
	    	 else {
	    		 usesSparkSubmit=false; 	    		 
	    		 useSparkSubmitButton.setSelection(false);
	    		 useSparkSubmitButton.setVisible(false);
	    		 withoutSparkSubmit(container);	 
	 	        if(DataTransferObject.local) {
	 	        	LocalFileSelectWizardPage1 page1 = ((RunAsDataflowApplicationWizard)getWizard()).firstbpage;
    				fileUriText.setText(page1.getFileUri());
    				fileUriText.setEditable(false);
    				fileSelectButton.setEnabled(false);
    				
    				LocalFileSelectWizardPage2 page2 = ((RunAsDataflowApplicationWizard)getWizard()).secondbpage;
    				
    				if(DataTransferObject.archivedir==null||DataTransferObject.archivedir.isEmpty())
    					archiveUriText.setText(application.getArchiveUri());
    				else
    					archiveUriText.setText(page2.getArchiveUri());
    				
    				archiveUriText.setEditable(true);
		        }
	    		 languageUsed  = application.getLanguage();    		
	    		 languageGroupJavaRadioButton.setSelection(false);
	    		 if(languageUsed == ApplicationLanguage.Java) {
	    				languageGroupJavaRadioButton.setSelection(true);
	    		 }
	    		 else if(languageUsed == ApplicationLanguage.Python) {
	    			 languageGroupPythonRadioButton.setSelection(true);
	    		 }
	    		 else if(languageUsed == ApplicationLanguage.Scala) {
	    			 languageGroupScalaRadioButton.setSelection(true);
	    		 }
	    		 else  {
	    			 languageGroupSQLRadioButton.setSelection(true);
	    		 }
	    		 
	    		 disposePrevious();
	    		 if(languageUsed == ApplicationLanguage.Java ||languageUsed == ApplicationLanguage.Scala ) {
	    			 JavaLanguageSelected(container);
	    			 SQLLanguageSelected(container);  
	    			 mainClassNameText.setText(application.getClassName());
	    			 if(application.getArguments() != null) {
	    					String arguments= "";
	    					for(String i : application.getArguments()) {
	    						arguments +=  i  +" ";
	    					}
	    					argumentsText.setText(arguments);
	    				}
	    		 }
	    		 else if(languageUsed == ApplicationLanguage.Python ) {
	    			 PythonLanguageSelected(container);
	    			 SQLLanguageSelected(container);  
	    			 if(application.getArguments() != null) {
	    					String arguments= "";
	    					for(String i : application.getArguments()) {
	    						arguments +=   i  +" ";
	    					}
	    					argumentsText.setText(arguments);
	    				}
	    		 } 
	    		 else {
	    			 SQLLanguageSelected(container);	    			
	    		 }
	    		 
	    		 if(application.getParameters() != null)
 		        {
 		        	 for (ApplicationParameter parameter : application.getParameters()) {
 		             	Parameters newparameter = new Parameters(parameterContainer,container,scrolledComposite, parameterSet);
 		             	parameterSet.add(newparameter);
 		        		newparameter.tagKey.setText(parameter.getName());
 		     			newparameter.tagValue.setText(parameter.getValue());
 		        	 }         	
 		        }       
	    	 }    	
	    }	   
	    container.layout(true,true);
	    scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		}
	
		public void clearSelection() {
			final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
			final String defaultApplicationName = DATE_TIME_FORMAT.format(new Date());
			displayNameText.setText("App " + defaultApplicationName);
			applicationDescriptionText.setText("");
			sparkVersionCombo.select(0);
			driverShapeCombo.select(0);
			executorShapeCombo.select(0);
			usesSparkSubmit = false;
			useSparkSubmitButton.setSelection(false);
			useSparkSubmitButton.setVisible(true);
			disposeSparkSubmit();
			disposeLanguagesection();
			withoutSparkSubmit(container);
			
		    container.layout(true,true);
		    scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		}
	    	
		@Override
		public boolean canFlipToNextPage() {
			return true;
		}		
    	
   	 	@Override
	    public IWizardPage getPreviousPage() {
   		 	if(DataTransferObject.local) {
   		 	RunAsDataflowApplicationWizard wizard = (RunAsDataflowApplicationWizard)getWizard();
			    wizard.canFinish= false;
			    wizard.canFinish();
		 	}	             
	        return super.getPreviousPage();
	    }
   	 	   	 	
   	 	@Override
	    public IWizardPage getNextPage() {	
   		 	if(DataTransferObject.local) {
   		 	RunAsDataflowApplicationWizard wizard = (RunAsDataflowApplicationWizard)getWizard();
   			    wizard.canFinish= true;
   			    wizard.canFinish();
   			 CreateApplicationWizardPage3 advpage = wizard.thirdpage;
   	   		 	if(usesSparkSubmit)    			    
   	   			    advpage.usesSparkSubmit(true);
   	   		 	else
   	   		 		advpage.usesSparkSubmit(false);
		 	}
   		 	else {
   	   		 CreateApplicationWizardPage3 advpage =((CreateApplicationWizard)getWizard()).thirdPage;
	   		 	if(usesSparkSubmit)    	   			    
	   			    advpage.usesSparkSubmit(true);
	   		 	else 
	   		 		advpage.usesSparkSubmit(false);
   		 	}          
	        return super.getNextPage();
	    }
	 	 
   	 	
   	 	
   	 	public void setJarAndArchiveUri() {
   	 		LocalFileSelectWizardPage2 page2 = ((RunAsDataflowApplicationWizard)getWizard()).secondbpage;
   	 		archiveUriText.setText(page2.getArchiveUri());
   	 	    LocalFileSelectWizardPage1 page1 = ((RunAsDataflowApplicationWizard)getWizard()).firstbpage;
	 		fileUriText.setText(page1.getFileUri());
   	 	}
   	 	
}
