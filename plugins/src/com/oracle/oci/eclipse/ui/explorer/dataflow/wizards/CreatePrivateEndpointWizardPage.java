package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.core.requests.ListSubnetsRequest;
import com.oracle.bmc.core.requests.ListVcnsRequest;
import com.oracle.bmc.core.responses.ListSubnetsResponse;
import com.oracle.bmc.core.responses.ListVcnsResponse;


public class CreatePrivateEndpointWizardPage extends WizardPage {
    private Text nameText;
	private Combo vcnCombo;
	private Combo subnetCombo;
	private Text dnszText;
    private ISelection selection;
    private String compid;
    private Spinner numExecSpinner;
    private Map<String,String> mapVcn=new HashMap<String,String>();
    private Map<String,String> mapSubnet=new HashMap<String,String>();

    public CreatePrivateEndpointWizardPage(ISelection selection,String compid) {
        super("wizardPage");
        setTitle("Create Private Endpoint");
        setDescription("This wizard creates a Private Endpoint. Please enter the following details.");
        this.selection = selection;
        this.compid=compid;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent,SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
		
        Label nameLabel = new Label(container, SWT.NULL);
        nameLabel.setText("&Name:");
        nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        nameText.setLayoutData(gd);
		
		Label vcnLabel = new Label(container, SWT.NULL);
        vcnLabel.setText("&Select a VCN:");
		vcnCombo = new Combo(container, SWT.READ_ONLY);
		vcnCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		vcnCombo.setItems(getVcns());
		vcnCombo.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		  		subnetCombo.setItems(getSubnets(mapVcn.get(vcnCombo.getText())));
		        }
		      });
		
		Label subnetLabel = new Label(container, SWT.NULL);
        subnetLabel.setText("&Select Subnet:");
		subnetCombo = new Combo(container, SWT.READ_ONLY);
		subnetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label dnszLabel = new Label(container, SWT.NULL);
        dnszLabel.setText("&Enter DNS Zones to resolve:");
        dnszText = new Text(container, SWT.BORDER | SWT.SINGLE);
        dnszText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label numExecLabel = new Label(container, SWT.NULL);
        numExecLabel.setText("&Number of Hosts to Resolve:");
		numExecSpinner = new Spinner(container, SWT.BORDER);
		numExecSpinner.setMinimum(1);
		numExecSpinner.setMaximum(1000000000);
		numExecSpinner.setSelection(256);
		numExecSpinner.setIncrement(1);
        
        setControl(container);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public Object[] getDetails() {
        
		return (new Object[]{AuthProvider.getInstance().getCompartmentId(),null,null,nameText.getText(),
				dnszText.getText().split(","),null,null,null,mapSubnet.get(subnetCombo.getText()),numExecSpinner.getSelection()
				});
    }
	
	String[] getVcns(){
		if(compid==null) 
			compid=AuthProvider.getInstance().getCompartmentId();
		VirtualNetworkClient client = new VirtualNetworkClient(AuthProvider.getInstance().getProvider());
		ListVcnsRequest listVcnsRequest = ListVcnsRequest.builder().compartmentId(compid).build();
		ListVcnsResponse response = client.listVcns(listVcnsRequest);
		List<Vcn> l=response.getItems();
		String[] s=new String[l.size()];
		int i=0;
		mapVcn.clear();
		for(Vcn v:l) {
			s[i]=v.getDisplayName();
			mapVcn.put(v.getDisplayName(), v.getId());
			i++;
		}
		return s;
	}
	
	String[] getSubnets(String id){
		
		if(compid==null) 
			compid=AuthProvider.getInstance().getCompartmentId();
		
		VirtualNetworkClient client = new VirtualNetworkClient(AuthProvider.getInstance().getProvider());

		ListSubnetsRequest listSubnetsRequest = ListSubnetsRequest.builder().compartmentId(compid).vcnId(id).build();

        ListSubnetsResponse response = client.listSubnets(listSubnetsRequest);
        List<Subnet> l=response.getItems();
		String[] s=new String[l.size()];
		int i=0;
		mapSubnet.clear();
		
		for(Subnet v:l) {
			s[i]=v.getDisplayName();
			mapSubnet.put(v.getDisplayName(), v.getId());
			i++;
		}
		return s;
	}
}