package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


public class Parameters {
	 Composite composite;
	 Text tagValue;
	 Text tagKey;
	 Button closeButton;
	 
	 Parameters(Composite current,Composite container, ScrolledComposite scrolledcomposite,Set<Parameters> CreatedParametersSet){
		 
		 composite=new Composite(current,SWT.NONE);
		 composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 GridLayout GridLayout1 = new GridLayout();
		 GridLayout1.numColumns = 8 ;
		 GridLayout1.makeColumnsEqualWidth=true;
		 composite.setLayout(GridLayout1);
		 
		 tagKey = new Text(composite,SWT.BORDER);
		 GridData gridData1=new GridData(GridData.FILL_HORIZONTAL);
		 gridData1.horizontalSpan=2;
		 tagKey.setLayoutData(gridData1);
		 tagKey.setMessage("Key");
		 
		 tagValue = new Text(composite,SWT.BORDER);
		 GridData gridData2=new GridData(GridData.FILL_HORIZONTAL);
		 gridData2.horizontalSpan=5;
		 tagValue.setLayoutData(gridData2);
		 tagValue.setMessage("Value");
		 
		 closeButton=new Button(composite,SWT.PUSH);
		 closeButton.setText("Remove");
		 
		 refresh(container, scrolledcomposite);
		 addClose(composite,container, scrolledcomposite,CreatedParametersSet);
	 }
	
	 void refresh(Composite container, ScrolledComposite scrolledcomposite ) {
		 container.layout(true,true);
     	 scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	 }
	 
	 void addClose(Composite current,Composite container, ScrolledComposite scrolledcomposite,Set<Parameters> CreatedPropertiesSet) {
		 closeButton.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
					 composite.dispose();
					 CreatedPropertiesSet.remove(Parameters.this);
					 refresh(container, scrolledcomposite);
	            }
	        });
	 }
}
