package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
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
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class EditApplicationWizardPage1  extends WizardPage {
	
	private ISelection selection;
	private Composite container;	
	private Text displayNameText;
	private Text applicationDescriptionText;	
    private ScrolledComposite scrolledComposite;   	
	private String selectedApplicationCompartmentId;		
	private Combo sparkVersionCombo;
	private Combo driverShapeCombo;
	private Combo executorShapeCombo;
	private Spinner numofExecutorsSpinner;	
	private Group languageGroup;
	private Button languageGroupJavaRadioButton;
	private Button languageGroupPythonRadioButton;
	private Button languageGroupSQLRadioButton;
	private Button languageGroupScalaRadioButton;
	private boolean usesSparkSubmit=false;
	private Button fileSelectButton;
	private Label fileUrilabel;
	private Label languagelabel;
	private Label archiveUrilabel;
	private Composite fileUriContainer;
	private Label sparkSubmitlabel;
	private Text sparkSubmitText;	
	private ApplicationLanguage languageUsed;
    private Composite basesqlcontainer;
    private Set<Parameters> sqlset=new HashSet<Parameters>();		
	private Label mainClassNamelabel;
	private Label argumentslabel;
	private Text mainClassNameText;
	private Text argumentsText;
	private Text archiveUriText;
	private Text fileUriText;
	private Application application;

	public EditApplicationWizardPage1(ISelection selection,String applicationId) {
		super("Page 1");
		setTitle("Edit DataFlow Application");
		setDescription("This wizard edits an existing DataFlow application. Please enter the required details.");
		this.selection = selection;
		this.selectedApplicationCompartmentId= AuthProvider.getInstance().getCompartmentId();	
		application = DataflowClient.getInstance().getApplicationDetails(applicationId);
		String compartmentId = application.getCompartmentId();	
		Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
		List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
		for(Compartment compartment : Allcompartments) {
			if(compartment.getId().equals(compartmentId)) {
				this.selectedApplicationCompartmentId= compartment.getId();
				break;
			}
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		
        parent.setSize(1000,800);
		scrolledComposite=new ScrolledComposite(parent,SWT.V_SCROLL| SWT.H_SCROLL);
		scrolledComposite.setExpandHorizontal( true );
		scrolledComposite.setExpandVertical( true );       
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		container = new Composite(scrolledComposite, SWT.NULL);
		scrolledComposite.setContent(container);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		
		Label displayNameLabel = new Label(container, SWT.NULL);
		displayNameLabel.setText("&Display name: *");
		displayNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		displayNameText.setLayoutData(gd);
		displayNameText.setText(application.getDisplayName());
		
		Label Applicationdescriptionlabel = new Label(container, SWT.NULL);
		Applicationdescriptionlabel.setText("&Application Description:");
		applicationDescriptionText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		applicationDescriptionText.setLayoutData(gd1);
		if(application.getDescription()!=null)
			applicationDescriptionText.setText(application.getDescription());
		
		Label SparkVersionLabel = new Label(container, SWT.NULL);
		SparkVersionLabel.setText("&Spark Version: *");
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		sparkVersionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		sparkVersionCombo.setLayoutData(gd2);		 
		sparkVersionCombo.setItems(DataflowConstants.versions);
		if(application.getSparkVersion().equals(DataflowConstants.versions[0])) {
			sparkVersionCombo.select(0);
		}
		else {
			sparkVersionCombo.select(1);
		}				      
		
		Label DriverShapeLabel = new Label(container, SWT.NULL);
		DriverShapeLabel.setText("&Driver Shape: *");
		createDriverShapeCombo(container);
		
		Label ExecutorShapeLabel = new Label(container, SWT.NULL);
		ExecutorShapeLabel.setText("&Executor Shape: *");
		createExecutorShapeCombo(container);
		
		Label NumofExecutorslabel = new Label(container, SWT.NULL);
		NumofExecutorslabel.setText("&Number of Executors: *");
		createNumofExecutorsSpinner(container);
			
		if(application.getExecute() != null && !application.getExecute().equals("")) {
			withSparkSubmit(container);	 
		}
		else
		{
			withoutSparkSubmit(container);
		}

	    scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		setControl(scrolledComposite);
	}
	
	private void withSparkSubmit(Composite container) {		
		 sparkSubmitlabel = new Label(container, SWT.NULL);
		 sparkSubmitlabel.setText("&Spark Submit Command: *");
		 sparkSubmitText = new Text(container, SWT.BORDER | SWT.MULTI);
		 GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		 gridData.heightHint = 5 * sparkSubmitText.getLineHeight();
		 sparkSubmitText.setLayoutData(gridData);      	 
		 sparkSubmitText.setText(application.getExecute());
	}
	
	private void withoutSparkSubmit(Composite container){
		languagelabel = new Label(container, SWT.NULL);
		languagelabel.setText("&Language:");
		createLanguageCombo(container);
		
		fileUrilabel = new Label(container, SWT.NULL);
		fileUrilabel.setText("&Choose a File: *");
		fileUriContainer = new Composite(container, SWT.NONE);
		GridLayout fileUriLayout = new GridLayout();
		fileUriLayout.numColumns = 2;
		fileUriContainer.setLayout(fileUriLayout);
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

		 archiveUrilabel = new Label(container, SWT.NULL);
		 archiveUrilabel.setText("&Archive URL:");
		 archiveUriText = new Text(container, SWT.BORDER | SWT.SINGLE);
		 GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
		 archiveUriText.setLayoutData(gd10);   
		 
		 fileUriText.setText(application.getFileUri());
		 if(application.getArchiveUri() != null) {
			 archiveUriText.setText(application.getArchiveUri());
		 }		 
		 languageUsed  = application.getLanguage();    				 
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
		 if(languageUsed == ApplicationLanguage.Java || languageUsed == ApplicationLanguage.Scala ) {
			 JavaLanguageSelected(container);
			 SQLLanguageSelected(container);
		 }
		 else if(languageUsed == ApplicationLanguage.Python ) {
			 PythonLanguageSelected(container);
			 SQLLanguageSelected(container);
		 } 
		 else {
			 SQLLanguageSelected(container);
		 }		 
	}
	
	private void createDriverShapeCombo(Composite container) {		
		driverShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		driverShapeCombo.setLayoutData(gd3);		 
		driverShapeCombo.setItems(DataflowConstants.shapesDetails);		
		for(int i=0; i<DataflowConstants.shapesDetails.length ; i++) {
			if(application.getDriverShape().equals(DataflowConstants.shapesDetails[i].split(" ")[0])) {
				driverShapeCombo.select(i);
			}
		}	
	}
	
	private void createExecutorShapeCombo(Composite container) {		
		executorShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		executorShapeCombo.setLayoutData(gd4);	 
		executorShapeCombo.setItems(DataflowConstants.shapesDetails);		
		for(int i=0; i<DataflowConstants.shapesDetails.length ; i++) {
			if(application.getExecutorShape().equals(DataflowConstants.shapesDetails[i].split(" ")[0])) {
				executorShapeCombo.select(i);
			}
		}		
	}
	
	private void createNumofExecutorsSpinner(Composite container) {
		numofExecutorsSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		numofExecutorsSpinner.setLayoutData(gd5);
		numofExecutorsSpinner.setMinimum(DataflowConstants.numOfExecutorsMin);
		numofExecutorsSpinner.setMaximum(DataflowConstants.numOfExecutorsMax);
		numofExecutorsSpinner.setIncrement(DataflowConstants.numOfExecutorsIncrement);
		// default value
		numofExecutorsSpinner.setSelection(application.getNumExecutors());
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
		
		languageGroupPythonRadioButton = new Button(languageGroup, SWT.RADIO);
		languageGroupPythonRadioButton.setText("Python");		
		languageGroupPythonRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(languageUsed != ApplicationLanguage.Python ){
            		disposePrevious();
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
		if(argumentslabel != null) {
			argumentslabel.dispose();
		}
		if(mainClassNamelabel != null) {
			mainClassNamelabel.dispose();
		}
		
		for(Parameters item : sqlset) {
			item.composite.dispose();
		}
		
		if(basesqlcontainer != null) {
			basesqlcontainer.dispose();
		}
	
	}
	private void JavaLanguageSelected(Composite container) {	
		mainClassNamelabel = new Label(container, SWT.NULL);
		mainClassNamelabel.setText("&Main Class Name: *");
		mainClassNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		mainClassNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		mainClassNameText.setText(application.getClassName());
			
		argumentslabel = new Label(container, SWT.NULL);
		argumentslabel.setText("&Arguments:");
		argumentsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		argumentsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		if(application.getArguments() != null) {
			String arguments= "";
			for(String i : application.getArguments()) {
				arguments +=i  +" ";
			}
			argumentsText.setText(arguments);			
		}
	}
	
	private void PythonLanguageSelected(Composite container) {		
		argumentslabel = new Label(container, SWT.NULL);
		argumentslabel.setText("&Arguments:");
		argumentsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
		argumentsText.setLayoutData(gd8);
		if(application.getArguments() != null) {
			String arguments= "";
			for(String i : application.getArguments()) {
				arguments +=   i  +" ";
			}
			argumentsText.setText(arguments);			
		}
	}
	
	private void SQLLanguageSelected(Composite parent) {		
		basesqlcontainer = new Composite(parent, SWT.NULL);
		GridData grid1 = new GridData(GridData.FILL_HORIZONTAL);
		grid1.horizontalSpan = 2;
		basesqlcontainer.setLayoutData(grid1);
        GridLayout layout1 = new GridLayout();
        basesqlcontainer.setLayout(layout1);
        layout1.numColumns = 1;        
        Button addParameter = new Button(basesqlcontainer,SWT.PUSH);
        addParameter.setLayoutData(new GridData());
        addParameter.setText("Add a Parameter");        
        addParameter.addSelectionListener(new SelectionAdapter() {        	
            public void widgetSelected(SelectionEvent e) {          	
            	Parameters newtag= new Parameters(basesqlcontainer,container,scrolledComposite, sqlset);
            	sqlset.add(newtag);
            }
          });   
        if(application.getParameters() != null)
        {
        	 for (ApplicationParameter parameter : application.getParameters()) {
             	Parameters newparameter = new Parameters(basesqlcontainer,container,scrolledComposite, sqlset);
             	sqlset.add(newparameter);
        		newparameter.tagKey.setText(parameter.getName());
     			newparameter.tagValue.setText(parameter.getValue());
        	 	}         	
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
		return archiveUriText.getText();
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
		 for(Parameters parameter : sqlset) {	
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
		   if(Character.isWhitespace(argumentsunseperated.charAt(i))){
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
	  
	  if(!sqlset.isEmpty()) {
		  for(Parameters parameter : sqlset) {	
			  	if(!arguments.contains("${"+parameter.tagKey.getText()+"}"))
			  		arguments.add("${"+parameter.tagKey.getText()+"}");
			 }		 
	  }
		   
	    return arguments;		
	}
	
	 @Override
	    public IWizardPage getNextPage() {        
	        return super.getNextPage();
	    }
}
