package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class CreateRunWizardPage3 extends WizardPage  {

	private ISelection selection;
	private Application application;
    private Composite container;
    private ScrolledComposite scrolledComposite;
   	private Text logLocationText;
	private Text warehouseLocationText;	
    private Set<SparkProperty> createdPropertiesSet=new HashSet<SparkProperty>();
	private Composite propertiesSection;
	private  Button addProperty;
	
	public CreateRunWizardPage3(ISelection selection,DataTransferObject dto,String applicationId) {
		super("Page 3");
		setTitle("Create run for Dataflow application");
		setDescription("Set advanced options for run if required.");
		application = DataflowClient.getInstance().getApplicationDetails(applicationId);
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
		
        propertiesSection = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout4 = new GridLayout();
        innerTopLayout4.numColumns = 1;
        propertiesSection.setLayout(innerTopLayout4);
        propertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        addProperty = new Button(propertiesSection,SWT.PUSH);
        addProperty.setText("Add a Spark Property");        
        addProperty.addSelectionListener(new SelectionAdapter() {       	
            public void widgetSelected(SelectionEvent e) {           	
            	createdPropertiesSet.add(new SparkProperty(propertiesSection,container,scrolledComposite,createdPropertiesSet));          	
            }
          });
        
        if(application.getConfiguration() != null) {        	
       	 for (Map.Entry<String,String> property : application.getConfiguration().entrySet()) {
       		 SparkProperty propertyPresent = new SparkProperty(propertiesSection,container,scrolledComposite,createdPropertiesSet);
       		 createdPropertiesSet.add(propertyPresent);
       		 propertyPresent.tagKey.setText(property.getKey());
				 propertyPresent.tagValue.setText(property.getValue());
       	 }         	
   		 container.layout(true,true);
   		 scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );       	
       }       
        
        Label logLocationlabel = new Label(container, SWT.NULL);
		logLocationlabel.setText("&Application Log Location:");
		logLocationText = new Text(container, SWT.BORDER | SWT.SINGLE);
		logLocationText.setText(application.getLogsBucketUri());
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		logLocationText.setLayoutData(gd1);
		logLocationText.setText(application.getLogsBucketUri());
				
		Label warehouseLocationlabel = new Label(container, SWT.NULL);
		warehouseLocationlabel.setText("&Warehouse Bucket Uri:");
		warehouseLocationText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		warehouseLocationText.setLayoutData(gd2);
		if(application.getWarehouseBucketUri()!=null){
			warehouseLocationText.setText(application.getWarehouseBucketUri());
		}		
		container.layout(true,true);
	    scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		setControl(container);		
	}

	public String getWarehouseUri() {		
		return warehouseLocationText.getText();
	}
	
	public String getApplicationLogLocation() {		
		return logLocationText.getText();
	}
	
	 public Map<String,String> getSparkProperties(){
		 Map<String,String> SparkProperties=new HashMap<String,String>();		 
		 for(SparkProperty Property : createdPropertiesSet) {			
			 SparkProperties.put(Property.tagKey.getText(), Property.tagValue.getText());	
		 }		 
		 return SparkProperties;
	 }
	
	 public void usesSparkSubmit() {
		 
		 addProperty.setEnabled(false);
			if(!createdPropertiesSet.isEmpty()) {
				for(SparkProperty item : createdPropertiesSet) {
					item.composite.dispose();
				}
			}			
	 }

}
