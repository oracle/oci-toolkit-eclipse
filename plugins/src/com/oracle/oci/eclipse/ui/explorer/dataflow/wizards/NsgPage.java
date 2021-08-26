package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.core.model.NetworkSecurityGroup;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.sdkclients.VirtualnetworkClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;


public class NsgPage extends WizardPage {
    private ISelection selection;
    private Composite container;
    private ScrolledComposite sc;
    private ArrayList<String> nsgList;
    private Set<Nsg> set=new HashSet<Nsg>();

    public NsgPage(ISelection selection,String compid) {
        super("wizardPage");
        setTitle("Network Security Group Wizard");
        setDescription("This wizard lets you add Network Security Group(NSGs).");
        this.selection = selection;
    }

    @Override
    public void createControl(Composite parent) {
    	
    	sc=new ScrolledComposite(parent,SWT.V_SCROLL);
    	sc.setExpandHorizontal( true );
    	sc.setExpandVertical( true );
    	sc.setLayoutData(new GridData());
    	
        container = new Composite(sc,SWT.NULL);sc.setContent(container);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        
        sc.addListener( SWT.Resize, event -> {
  		  int width = sc.getClientArea().width;
  		  sc.setMinSize( container.computeSize( width, SWT.DEFAULT ));
  		});
		
        Button addNsg=new Button(container,SWT.PUSH);
        addNsg.setText("Another Network Security Group");
        GridData gd=new GridData();gd.horizontalSpan=3;addNsg.setLayoutData(gd);
        
        addNsg.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	set.add(new Nsg());
            }
        });
        
        setControl(sc);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
	 
	 class Nsg{

		 private Button close;
		 private Button selComp;
		 private Combo combo;
		 private String nsgid;
		 private Map<String,String> nsgMap;
		 
		 Nsg(){
			 close=new Button(container,SWT.PUSH);
			 close.setText("X");
			 selComp=new Button(container,SWT.PUSH);
			 selComp.setText("Select Compartment");
			 selComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 
			 combo=new Combo(container,SWT.READ_ONLY);
			 combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 
			 refresh();
			 addClose();
			 addSelectComp();
			 addComboListener();
		 }
		 
		 void refresh() {
			 container.layout(true,true);
         	 sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		 }
		 
		 void addComboListener() {
			 combo.addSelectionListener(new SelectionAdapter() {
			      public void widgetSelected(SelectionEvent e) {
			          nsgid=nsgMap.get(combo.getText());
			       }
			 });
		 }
		 
		 void addClose() {
			 
			 close.addSelectionListener(new SelectionAdapter() {
				 public void widgetSelected(SelectionEvent e) {
					 selComp.dispose();
					 combo.dispose();
					 close.dispose();
					 set.remove(Nsg.this);
					 refresh();
				 }
			 });
		 }
		 
		 void addSelectComp() {
			 
			 selComp.addSelectionListener(new SelectionAdapter() {
				    @Override
		            public void widgetSelected(SelectionEvent e) {
		                
						Consumer<Compartment> consumer=new Consumer<Compartment>() {

						@Override
						public void accept(Compartment comp) {
							
							selComp.setText(comp.getName());
					        nsgList=new ArrayList<String>();
					        List<NetworkSecurityGroup> nsgl;
							try {
								nsgl = VirtualnetworkClient.getInstance().getNetworkSecurityGroupList(comp.getId());
							} catch (Exception e1) {
								MessageDialog.openError(getShell(), "Unable to get Network Security Groups", e1.getMessage());
								return;
							}
					        nsgMap=new HashMap<String,String>();
					        
					        String[] sl=new String[0];
					        if(nsgl!=null&&nsgl.size()>0) {
					        	sl=new String[nsgl.size()];int i=0;
					        	for(NetworkSecurityGroup e:nsgl) {
					        		sl[i]=e.getDisplayName();nsgMap.put(e.getDisplayName(),e.getId());i++;
					        	}
					        }
					        combo.setItems(sl);
						}
						};
						CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),new CompartmentSelectWizard(consumer, false));
						dialog.setFinishButtonText("Select");
						if (Window.OK == dialog.open()) {}
		            }
			 });
		 }
	 }
	 
	 ArrayList<String> getnsgs(){
		 nsgList=new ArrayList<String>();
		 for(Nsg e:set) {
			 if(e.nsgid==null) return null;
			 nsgList.add(e.nsgid);
		 }
		 return nsgList;
	 }

}