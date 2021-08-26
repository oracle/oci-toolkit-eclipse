package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.PrivateEndpoint;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class EditApplicationWizardPage2  extends WizardPage {
	
    private Composite container;
    private ScrolledComposite scrolledComposite;
	private ISelection selection;
	private Application application;
	private Text logLocationText;
	private Text warehouseLocationText;
	private Group networkGroup;
	private Button networkGroupInternetAccessRadioButton;
	private Button networkGroupPrivateSubnetRadioButton;
	private Composite networkSection;
	private Composite privateEndpointSection;
	private Composite innerTopContainer;
	private Button compartmentButton;
	private Text compartmentText;
	Combo privateEndpointsCombo;
	private Label privateEndpointsLabel;
	private Label compartmentLabel;
	private List<PrivateEndpointSummary> privateEndpoints;			
    private Set<SparkProperty> createdPropertiesSet=new HashSet<SparkProperty>();   
	private Composite propertiesSection;
	private Composite buttonComposite;
	private Composite advancedOptionsComposite;
	private boolean networkSectionSelected=false;
	private int intial = -1; 
	private boolean usesAdvancedOptions=false;
	private String selectedApplicationCompartmentId;
	private String selectedApplicationCompartmentName;
	
	public EditApplicationWizardPage2(ISelection selection,String applicationId) {
		super("Page 2");
		setTitle("Edit DataFlow application");
		setDescription("Change Advanced Options for application if required.");
		this.selection = selection;		
		application = DataflowClient.getInstance().getApplicationDetails(applicationId);
		String compartmentId = application.getCompartmentId();	
		Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
		this.selectedApplicationCompartmentId= rootCompartment.getId();
		this.selectedApplicationCompartmentId= rootCompartment.getName();
		List<Compartment> allCompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
		for(Compartment compartment : allCompartments) {
			if(compartment.getId().equals(compartmentId)) {
				this.selectedApplicationCompartmentId= compartment.getId();
				this.selectedApplicationCompartmentName = compartment.getName();
				break;
			}
		}
	}
	
	@Override
	public void createControl(Composite parent) {   	
    	scrolledComposite=new ScrolledComposite(parent,SWT.V_SCROLL| SWT.H_SCROLL);
    	scrolledComposite.setExpandHorizontal( true );
    	scrolledComposite.setExpandVertical( true );
    	scrolledComposite.setLayoutData(new GridData());    	
     	
        container = new Composite(scrolledComposite,SWT.NULL);
        scrolledComposite.setContent(container);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;					

        buttonComposite = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout1 = new GridLayout();
        innerTopLayout1.numColumns = 1;
        buttonComposite.setLayout(innerTopLayout1);
        buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        advancedOptionsComposite = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout2 = new GridLayout();
        innerTopLayout2.numColumns = 1;
        advancedOptionsComposite.setLayout(innerTopLayout2);
        advancedOptionsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         
        Button useadvancedoptionsButton =  new Button(buttonComposite,SWT.CHECK);
     	useadvancedoptionsButton.setText("Show Advanced Options");
     	useadvancedoptionsButton.addSelectionListener(new SelectionAdapter() {
     	    @Override
     	    public void widgetSelected(SelectionEvent event) {
     	    	 Button btn = (Button) event.getSource();
     	    	 if(btn.getSelection()) 
     	    		usesAdvancedOptions=true;
     	    	 else
     	    		 usesAdvancedOptions=false;
     	    	advancedOptionsComposite.setVisible(usesAdvancedOptions);  
     	    }
     	});
		if(application.getExecute() == null) {
	        propertiesSection = new Composite(advancedOptionsComposite, SWT.NONE);
	        GridLayout innerTopLayout3 = new GridLayout();
	        innerTopLayout3.numColumns = 1;
	        propertiesSection.setLayout(innerTopLayout3);
	        propertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        
	        Button addProperty = new Button(propertiesSection,SWT.PUSH);
	        addProperty.setText("Add a Spark Property");
	        
	        if(application.getConfiguration() != null) {        	
	        	 for (Map.Entry<String,String> property : application.getConfiguration().entrySet()) {
	        		 SparkProperty propertyPresent = new SparkProperty(propertiesSection,advancedOptionsComposite,scrolledComposite,createdPropertiesSet);
	        		 createdPropertiesSet.add(propertyPresent);
	        		 propertyPresent.tagKey.setText(property.getKey());
					 propertyPresent.tagValue.setText(property.getValue());
	        	 }         	
	    		 container.layout(true,true);
	         	 scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	        	
	        }       
	        	addProperty.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	             createdPropertiesSet.add(new SparkProperty(propertiesSection,advancedOptionsComposite,scrolledComposite,createdPropertiesSet));
	    		 container.layout(true,true);
	         	 scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	            }
	          });
		}


		Label logLocationlabel = new Label(advancedOptionsComposite, SWT.NULL);
		logLocationlabel.setText("&Application Log Location:");
		logLocationText = new Text(advancedOptionsComposite, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		logLocationText.setLayoutData(gd1);		
		logLocationText.setText(application.getLogsBucketUri());
		
		
		Label warehouseLocationlabel = new Label(advancedOptionsComposite, SWT.NULL);
		warehouseLocationlabel.setText("&Warehouse Bucket Uri:");
		warehouseLocationText = new Text(advancedOptionsComposite, SWT.BORDER | SWT.SINGLE);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		warehouseLocationText.setLayoutData(gd2);
		if(application.getWarehouseBucketUri() != null) {
			warehouseLocationText.setText(application.getWarehouseBucketUri());
		}		
		Label NetworkAccesslabel = new Label(advancedOptionsComposite, SWT.NULL);
		NetworkAccesslabel.setText("&Choose Network Access:");
	    		
		networkSection = new Composite(advancedOptionsComposite, SWT.NONE);
        GridLayout innerTopLayout4 = new GridLayout();
        innerTopLayout4.numColumns = 1;
        networkSection.setLayout(innerTopLayout4);
        networkSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createNetworkSection(networkSection);
		       
		advancedOptionsComposite.setVisible(usesAdvancedOptions);
		container.layout(true,true);
		
	    scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		setControl(scrolledComposite);
	}
	
	private void createNetworkSection(Composite currentcontainer) {
		
		networkGroup = new Group(currentcontainer, SWT.NULL);
		RowLayout rowLayout1 = new RowLayout(SWT.HORIZONTAL);
        rowLayout1.spacing = 100;
        networkGroup.setLayout(rowLayout1);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		networkGroup.setLayoutData(gd3);
	
		networkGroupInternetAccessRadioButton = new Button(networkGroup, SWT.RADIO);
		networkGroupInternetAccessRadioButton.setText("Internet Access(No Subnet)");
		
		networkGroupPrivateSubnetRadioButton = new Button(networkGroup, SWT.RADIO);
		networkGroupPrivateSubnetRadioButton.setText("Secure Access to Private Subnet");
		if(application.getPrivateEndpointId() != null && !application.getPrivateEndpointId().equals("")) {
			networkGroupPrivateSubnetRadioButton.setSelection(true);
			networkSectionSelected= true;					
			privateEndpointSection= new Composite(currentcontainer, SWT.NONE);
            GridLayout innerTopLayout = new GridLayout();
            innerTopLayout.numColumns = 1;
            privateEndpointSection.setLayout(innerTopLayout);
            privateEndpointSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            DataflowClient dataflowClient = DataflowClient.getInstance();
            PrivateEndpoint current = dataflowClient.getPrivateEndpointDetails(application.getPrivateEndpointId());
    		Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
    		List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
    		for(Compartment compartment : Allcompartments) {
    			if(compartment.getId().equals(current.getCompartmentId())) {
    				 selectedApplicationCompartmentId = compartment.getId();
    				 selectedApplicationCompartmentName = compartment.getName();
    				 break;
    			}
    		}    		
    		privateEndpoints = dataflowClient.getPrivateEndPoints(selectedApplicationCompartmentId);	    		
    		int sizeoflist= privateEndpoints.size();
    		String[] PrivateEndpointsList = new String[sizeoflist];
    		for(int i = 0; i < privateEndpoints.size(); i++){  
    			PrivateEndpointsList[i]= privateEndpoints.get(i).getDisplayName();
    			if(privateEndpoints.get(i).getId().equals(application.getPrivateEndpointId())) {
    				intial = i;
    				break;
    			}
    		}
            choosePrivateSubnet(privateEndpointSection);     			
		}
		else {
			networkGroupInternetAccessRadioButton.setSelection(true);
			networkSectionSelected= false;
		}
		
		
		networkGroupPrivateSubnetRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(!networkSectionSelected)
            	{
            		networkSectionSelected= true;
            		privateEndpointSection= new Composite(currentcontainer, SWT.NONE);
                    GridLayout innerTopLayout = new GridLayout();
                    innerTopLayout.numColumns = 1;
                    privateEndpointSection.setLayout(innerTopLayout);
                    privateEndpointSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    choosePrivateSubnet(privateEndpointSection);     
            	}
            	advancedOptionsComposite.layout(true,true);
        		container.layout(true,true);
        		scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
                container.pack();
            }
        });
		
		networkGroupInternetAccessRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(networkSectionSelected)
            	{	
            		networkSectionSelected= false;	
            		compartmentLabel.dispose();
            		compartmentText.dispose();
            		compartmentButton.dispose();
            		privateEndpointsCombo.dispose();
            		innerTopContainer.dispose();            		
            		privateEndpointSection.dispose();	
            	} 
            	advancedOptionsComposite.layout(true,true);
        		container.layout(true,true);
                container.pack();
            }
        });
  
        container.layout(true,true);
        scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        container.pack();
	}
	
	private void choosePrivateSubnet(Composite currentcontainer) {
		
		compartmentLabel = new Label(currentcontainer, SWT.NULL);
		compartmentLabel.setText("&Choose a compartment:");
		innerTopContainer = new Composite(currentcontainer, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 2;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        compartmentText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        compartmentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        compartmentText.setEditable(false);
        compartmentText.setText(selectedApplicationCompartmentName);

              
        compartmentButton = new Button(innerTopContainer, SWT.PUSH);
        compartmentButton.setText("Choose...");
        compartmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleSelectApplicationCompartmentEvent(currentcontainer);           	
            }
        });	
        chooseSubnet(currentcontainer,selectedApplicationCompartmentId);
        currentcontainer.layout(true,true);
        advancedOptionsComposite.layout(true,true);
        container.layout(true,true);
        scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        container.pack();                
	}
	
	private void chooseSubnet(Composite currentcontainer, String compartmentId) {		
        
		DataflowClient dataflowClient = DataflowClient.getInstance();
		privateEndpoints = dataflowClient.getPrivateEndPoints(compartmentId);		

		int sizeoflist= privateEndpoints.size();
		String[] PrivateEndpointsList = new String[sizeoflist];
		for(int i = 0; i < privateEndpoints.size(); i++)
		{  
			PrivateEndpointsList[i]= privateEndpoints.get(i).getDisplayName();
		}

		privateEndpointsLabel = new Label(currentcontainer, SWT.NULL);
		privateEndpointsLabel.setText("&Choose Private Endpoint:");
		privateEndpointsCombo = new Combo(currentcontainer, SWT.DROP_DOWN | SWT.READ_ONLY);
		privateEndpointsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 
		privateEndpointsCombo.setItems(PrivateEndpointsList);
		if(intial != -1)
			privateEndpointsCombo.select(intial);
        currentcontainer.layout(true,true);
        advancedOptionsComposite.layout(true,true);
        container.layout(true,true);
        scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        container.pack();
	}
	
	private void handleSelectApplicationCompartmentEvent(Composite currentcontainer) {
    	Consumer<Compartment> consumer=new Consumer<Compartment>() {
			@Override
			public void accept(Compartment compartment) {
				if (compartment != null) {
					selectedApplicationCompartmentId = compartment.getCompartmentId();
					selectedApplicationCompartmentName = compartment.getName();
					compartmentText.setText(selectedApplicationCompartmentName);
					if(privateEndpointsCombo != null)
						privateEndpointsCombo.dispose();
					if(privateEndpointsLabel != null)
						privateEndpointsLabel.dispose();					
					chooseSubnet(currentcontainer,selectedApplicationCompartmentId);
				}
			}
		};
    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
				new CompartmentSelectWizard(consumer, false));
		dialog.setFinishButtonText("Select");
		if (Window.OK == dialog.open()) {
		}
    }
		
	public String getWarehouseUri() {
		
		return warehouseLocationText.getText();
	}
	
	public String getApplicationLogLocation() {
		
		return logLocationText.getText();
	}
	
	public String getPrivateEndPointId() {
		return privateEndpoints.get(privateEndpointsCombo.getSelectionIndex()).getId()   ;		
	}

	 public Map<String,String> getSparkProperties(){
		 Map<String,String> SparkProperties=new HashMap<String,String>();
		 
		 for(SparkProperty Property : createdPropertiesSet) {			
			 SparkProperties.put(Property.tagKey.getText(), Property.tagValue.getText());	
		 }
		 
		 return SparkProperties;
	 }
	 
	 public boolean usesAdvancedOptions(){
		 return usesAdvancedOptions;		 
	 }
	 
	 public boolean usesPrivateSubnet(){
		 return networkSectionSelected;		 
	 }

}
