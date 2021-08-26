package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;
import com.oracle.bmc.dataflow.model.ApplicationParameter;
import com.oracle.bmc.dataflow.model.Run;
import com.oracle.bmc.dataflow.model.RunSummary;


public class RunWizardPage extends WizardPage {
    private Text nameText,sparkSubmitText;
	private Combo dshapeCombo;
	private Combo eshapeCombo;
	private Spinner numExecSpinner;
    private ISelection selection;
	private Run run;
	private ScrolledComposite scrolledComposite;
	private Set<Parameters> parameterset;

    public RunWizardPage(ISelection selection,RunSummary runSum) {
        super("wizardPage");
        setTitle("Re Run Wizard");
        setDescription("This wizard creates a re-run request. Please enter the following details.");
        this.selection = selection;
		try {
			this.run=DataflowClient.getInstance().getRunDetails(runSum.getId());
		} 
		catch (Exception e) {
			MessageDialog.openError(getShell(), "Error fetching run details", e.getMessage());
		}
		this.parameterset = new HashSet<Parameters>();
    }

    @Override
    public void createControl(Composite parent) {
    	
    	scrolledComposite=new ScrolledComposite(parent,SWT.V_SCROLL| SWT.H_SCROLL);
		scrolledComposite.setExpandHorizontal( true );
		scrolledComposite.setExpandVertical( true );       
    	
        Composite container = new Composite(scrolledComposite, SWT.NULL);
        scrolledComposite.setContent(container);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
		
        Label nameLabel = new Label(container, SWT.NULL);
        nameLabel.setText("&Display Name: *");
        nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        nameText.setText(run.getDisplayName());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        nameText.setLayoutData(gd);
		
		Label dshapeLabel = new Label(container, SWT.NULL);
        dshapeLabel.setText("&Driver Shape: *");
		dshapeCombo = new Combo(container, SWT.READ_ONLY);
		dshapeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			dshapeCombo.setItems(DataflowConstants.shapesDetails);
		} 
		catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		}
		
		for(int i=0; i<DataflowConstants.shapesDetails.length ; i++) {
			if(run.getDriverShape().equals(DataflowConstants.shapesDetails[i].split(" ")[0])) {
				dshapeCombo.select(i);
			}
		}	
		
		Label eshapeLabel = new Label(container, SWT.NULL);
        eshapeLabel.setText("&Executor Shape: *");
		eshapeCombo = new Combo(container, SWT.READ_ONLY);
		eshapeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			eshapeCombo.setItems(DataflowConstants.shapesDetails);
		} 
		catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		}
		
		for(int i=0; i<DataflowConstants.shapesDetails.length ; i++) {
			if(run.getDriverShape().equals(DataflowConstants.shapesDetails[i].split(" ")[0])) {
				eshapeCombo.select(i);
			}
		}	
		
		Label numExecLabel = new Label(container, SWT.NULL);
        numExecLabel.setText("&Number of Executors:");
		numExecSpinner = new Spinner(container, SWT.BORDER);
		numExecSpinner.setMinimum(1);
		numExecSpinner.setMaximum(128);
		
		numExecSpinner.setSelection(run.getNumExecutors());
		
		numExecSpinner.setIncrement(1);
		
		if(run.getExecute()!=null&!run.getExecute().isEmpty()) {
			//adding spark submit
			Label sparkSubmit=new Label(container, SWT.NULL);
			sparkSubmit.setText("&Spark Submit Command: *");
			sparkSubmitText=new Text(container, SWT.BORDER);
			sparkSubmitText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			sparkSubmitText.setText(run.getExecute());
			//
		}
		
		Label argLabel = new Label(container, SWT.NULL);
        argLabel.setText("&Arguments:");
        Text argText = new Text(container, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        
        argText.setText(run.getArguments().toString());
        
        argText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		Composite parametercontainer = new Composite(container, SWT.NULL);
		GridData grid1 = new GridData(GridData.FILL_HORIZONTAL);
		grid1.horizontalSpan=2;
		parametercontainer.setLayoutData(grid1);
        parametercontainer.setLayout(new GridLayout());
        
        if(run.getParameters() != null)
        {
        	 for (ApplicationParameter parameter : run.getParameters()) {
             	Parameters newparameter = new Parameters(parametercontainer,container,scrolledComposite, parameterset);
             	parameterset.add(newparameter);
        		newparameter.tagKey.setText(parameter.getName());
        		newparameter.tagKey.setEditable(false);
     			newparameter.tagValue.setText(parameter.getValue());
     			newparameter.closeButton.setEnabled(false);
        	 	}         	
        } 
        
        scrolledComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
      	container.layout(true,true);
		setControl(scrolledComposite);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public Object[] getDetails() {
        
		return (new Object[]{run.getApplicationId(),run.getArchiveUri(),null,run.getCompartmentId(),null,null,
				nameText.getText().trim(), dshapeCombo.getText().split(" ")[0],run.getExecute(),eshapeCombo.getText().split(" ")[0],null,
				run.getLogsBucketUri(),numExecSpinner.getSelection(),getParameters(),run.getSparkVersion(),
				run.getWarehouseBucketUri(),run.getOpcRequestId(),(sparkSubmitText==null?null:sparkSubmitText.getText())
				});
    }
	
	public  List<ApplicationParameter> getParameters(){
		List<ApplicationParameter> Parameters = new ArrayList<ApplicationParameter>();	 
		 for(Parameters parameter : parameterset) {	
			 Parameters.add(ApplicationParameter.builder()
					 .name(parameter.tagKey.getText())
					 .value(parameter.tagValue.getText())
					 .build());
		 }		 
		 return Parameters;
	 }
}