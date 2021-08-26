package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import com.oracle.bmc.identity.requests.ListTagsRequest;
import com.oracle.bmc.identity.responses.GetTagResponse;
import com.oracle.bmc.identity.responses.ListTagsResponse;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.BaseTagDefinitionValidator;
import com.oracle.bmc.identity.model.TagNamespace;
import com.oracle.bmc.identity.model.TagNamespaceSummary;
import com.oracle.bmc.identity.model.TagSummary;
import com.oracle.bmc.identity.requests.GetTagRequest;
import com.oracle.bmc.identity.requests.ListTagNamespacesRequest;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;

public class TagsPage extends WizardPage {
    private ISelection selection;
    private Composite container;
    private ScrolledComposite sc;
    private Set<Tags> tagsSet=new HashSet<Tags>();
    private String[] namespacesList;
    private Map<String,String> namespaceMap=new HashMap<String,String>();
    private IdentityClient client = new IdentityClient(AuthProvider.getInstance().getProvider());
    private Map<String,Map<String,String[]>> defTagMap= new HashMap<String,Map<String,String[]>>();
    private Map<String,Map<String,Object>> defMap;
    private Map<String,String> freeMap;
    
    public TagsPage(ISelection selection,String compid,Map<String,Map<String,Object>> defMap,Map<String,String> freeMap) {
        super("wizardPage");
        setTitle("Tags");
        setDescription("Tagging is a metadata system that allows you to organize and track resources within your tenancy. Tags are composed of keys and values that can be attached to resources.\r\n");
        this.selection = selection;
        this.namespacesList=getNamespaces();
        this.defMap=defMap;
        this.freeMap=freeMap;
        getTags(defMap,freeMap);
    }

    @Override
    public void createControl(Composite parent) {
    	
    	sc=new ScrolledComposite(parent,SWT.V_SCROLL);
    	sc.setExpandHorizontal( true );
    	sc.setExpandVertical( true );
    	sc.setLayoutData(new GridData());
    	
        container = new Composite(sc,SWT.NULL);
        sc.setContent(container);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;
        
        Link link = new Link(container, SWT.NONE);
        link.setText("<a href=\"https://docs.oracle.com/en-us/iaas/Content/Tagging/Concepts/taggingoverview.htm\">Learn more about tagging</a>");
         
        // Event handling when users click on links.
        link.addSelectionListener(new SelectionAdapter()  {
         
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://docs.oracle.com/en-us/iaas/Content/Tagging/Concepts/taggingoverview.htm");
            }
             
        });

        Button addNsg=new Button(container,SWT.PUSH);
        addNsg.setText("Additional Tags");
        
        setTags(defMap,freeMap);
        
        addNsg.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	
            	tagsSet.add(new Tags());
            }
          });
        
        setControl(sc);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
	 
	 class Tags{
		 
		 Composite comp;
		 Combo namespaceCombo,keyCombo,valueCombo;
		 Text keyText,valueText;
		 Button removeButton;
		 
		 Tags(){
			 
			 comp=new Composite(container,SWT.NONE);
			 comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 GridLayout gl=new GridLayout();
			 gl.numColumns=4;
			 comp.setLayout(gl);
			 
			 removeButton=new Button(comp,SWT.PUSH);
			 removeButton.setText("Remove");
			 
			 namespaceCombo=new Combo(comp,SWT.READ_ONLY);
			 namespaceCombo.setItems(namespacesList);
			 namespaceCombo.setText("Free Form Tags");
			 
			 keyText=new Text(comp,SWT.BORDER);
			 keyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 keyText.setMessage("key");
			 
			 valueText=new Text(comp,SWT.BORDER);
			 valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 valueText.setMessage("value");
			 
			 addNamespaceComboListener();
			 addRemoveButtonListener();
			 refresh();
		 }
		 
		 void addRemoveButtonListener() {
			 removeButton.addSelectionListener(new SelectionAdapter() {
				 public void widgetSelected(SelectionEvent e) {
					 if(namespaceCombo!=null) 
						 namespaceCombo.dispose();
					 if(keyCombo!=null) 
						 keyCombo.dispose();
					 if(valueCombo!=null) 
						 valueCombo.dispose();
			    	 if(keyText!=null) 
			    		 keyText.dispose();
			    	 if(valueText!=null) 
			    		 valueText.dispose();
					 
			    	 comp.dispose();
					 tagsSet.remove(Tags.this);
					 refresh();
				 }
			 });
		 }
		
		 void addNamespaceComboListener() {
			 
			 namespaceCombo.addSelectionListener(new SelectionAdapter() {
			      public void widgetSelected(SelectionEvent e) {
			    	  if(keyCombo!=null) 
			    		  keyCombo.dispose();
			    	  if(valueCombo!=null) 
			    		  valueCombo.dispose();
			    	  if(keyText!=null) 
			    		  keyText.dispose();
			    	  if(valueText!=null) 
			    		  valueText.dispose();
			    	  
			    	  refresh();
			    	  
			    	  if(!namespaceCombo.getText().equals("Free Form Tags")) {
			    		  keyCombo=new Combo(comp,SWT.READ_ONLY);
			    		  keyCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  
			    		  valueCombo=new Combo(comp,SWT.READ_ONLY);
			    		  valueCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  
			    		  refresh();
			    		  
			    		  String[] tagKeys=getTagKeys(namespaceMap.get(namespaceCombo.getText()));
			    		  if(tagKeys==null||tagKeys.length==0) {
			    			  MessageDialog.openError(getShell(), "Tag namespace selected does not have any keys", "It will be removed");
			    			  comp.dispose();
			    			  tagsSet.remove(Tags.this);
			    			  return;
			    		  }
			    		  keyCombo.setItems(tagKeys);
			    		  addKeyComboListener();
			    		  //
			    		  keyCombo.select(0);
			    		  keyComboListener();
			    		  //
			    	  }
			    	  else {
			    		  keyText=new Text(comp,SWT.BORDER);
			    		  keyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  keyText.setMessage("key");
			    		  
			    		  valueText=new Text(comp,SWT.BORDER);
			    		  valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  valueText.setMessage("value");
			    	  }
			    	  refresh();
				   }
			});
		 }
		 
		 void addKeyComboListener() {
			 keyCombo.addSelectionListener(new SelectionAdapter() {
			      public void widgetSelected(SelectionEvent e) {
			    	  keyComboListener();
				   }
			});
		 }
		 
		 void keyComboListener() {
			 if(valueText!=null)
	    		  valueText.dispose();
	    	  if(valueCombo==null||valueCombo.isDisposed()) {
	    		  valueCombo=new Combo(comp,SWT.READ_ONLY);
	    		  valueCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    		  refresh();
	    	  }
	    	  String[] items=getTagValues(keyCombo.getText(),namespaceMap.get(namespaceCombo.getText()));
	    	  if(items==null) {
	    		  if(valueCombo!=null)
	    			  valueCombo.dispose();
	    		  valueText=new Text(comp,SWT.BORDER);
	    		  valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    		  valueText.setMessage("value");
	    	  }
	    	  else {
	    		  if(valueCombo==null||valueCombo.isDisposed()) 
	    			  valueCombo=new Combo(comp,SWT.READ_ONLY);
	    		  valueCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    		  valueCombo.setItems(items);
	    	  }
	    	  refresh();
		 }
		 
	 }
	 
	 void refresh() {
		 container.layout(true,true);
     	 sc.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	 }
	 
	 String[] getNamespaces() {
		 IdentityClient client = new IdentityClient(AuthProvider.getInstance().getProvider());
		 
		String compid=IdentClient.getInstance().getRootCompartment().getId();
		ListTagNamespacesRequest listTagNamespacesRequest = ListTagNamespacesRequest.builder().compartmentId(compid)
			//.limit(570)
			.includeSubcompartments(true).lifecycleState(TagNamespace.LifecycleState.Active)
			.build();

	        /* Send request to the Client */
	        List<TagNamespaceSummary> l = client.listTagNamespaces(listTagNamespacesRequest).getItems();
	        System.out.print(l.get(0).getName()+l.get(0).getId());
	        String[] rl=new String[l.size()+1];
	        for(int i=0;i<l.size();i++) {
	        	rl[i]=l.get(i).getName();
	        	namespaceMap.put(rl[i], l.get(i).getId());
	        }
	        
	        rl[l.size()]="Free Form Tags";
	        return rl;
	 }
	 
	 String[] getTagKeys(String namespaceId) {
		 if(defTagMap.containsKey(namespaceId))
			 return defTagMap.get(namespaceId).keySet().toArray(new String[0]);
		 IdentityClient client = new IdentityClient(AuthProvider.getInstance().getProvider());
		 ListTagsRequest listTagsRequest = ListTagsRequest.builder().tagNamespaceId(namespaceId).build();
		 ListTagsResponse response = client.listTags(listTagsRequest);
		 List<String> l=new ArrayList<String>();
		 Map<String,String[]> tm=new HashMap<String,String[]>();
		 for(TagSummary ts:response.getItems()) {
			 l.add(ts.getName());
			 tm.put(ts.getName(),new String[] {""});
		 }		 
		 defTagMap.put(namespaceId,tm);
		 return l.toArray(new String[0]);
	 }
	 
	 String[] getTagValues(String key,String namespaceId) {
		 String[] tagValues=defTagMap.get(namespaceId).get(key);
		 if(tagValues==null||!tagValues[0].isEmpty())
			 return tagValues;
			 
		 GetTagRequest getTagRequest = GetTagRequest.builder()
					.tagNamespaceId(namespaceId).tagName(key).build();
		 GetTagResponse res = client.getTag(getTagRequest);
		 BaseTagDefinitionValidator validator=res.getTag().getValidator();
		 if(validator==null) {
			 defTagMap.get(namespaceId).put(key, null);
			 return null;
		 }
		 String v=validator.toString();
		 tagValues=v.split("values=\\[")[1].split("\\]")[0].split(",");
		 for(int i=0;i<tagValues.length;i++)
			 tagValues[i]=tagValues[i].trim();
		 defTagMap.get(namespaceId).put(key, tagValues);
		 return tagValues;
	 }
	 
	 public Map<String,Map<String,Object>> getOT(){
		 
		 Map<String,Map<String,Object>> ots=new HashMap<String,Map<String,Object>>();
		 
		 for(Tags t:tagsSet) {
			 if(!t.namespaceCombo.getText().equals("Free Form Tags")) {
				 Map<String,Object> tm=ots.get(t.namespaceCombo.getText());
				 if(tm==null) tm=new  HashMap<String,Object>();
				 if(t.valueCombo!=null&&!t.valueCombo.isDisposed()) tm.put(t.keyCombo.getText(), t.valueCombo.getText());
				 else tm.put(t.keyCombo.getText(), t.valueText.getText());
				 ots.put(t.namespaceCombo.getText(), tm);
			 }
		 }
		 return ots;
	 }
	 
	 public Map<String,String> getFT(){
		 Map<String,String> m=new HashMap<String,String>();
		 
		 for(Tags t:tagsSet) {
			 if(t.namespaceCombo.getText().equals("Free Form Tags")) {
				 m.put(t.keyText.getText(), t.valueText.getText());
			 }
		 }
		 
		 return m;
	 }
	 
	 public void getTags(Map<String,Map<String,Object>> defMap,Map<String,String> freeMap) {
		 if(defMap==null||freeMap==null) return;
		 for(Map.Entry<String,Map<String,Object>> me:defMap.entrySet()) {
			 for(Map.Entry<String,Object> mee:me.getValue().entrySet()) {
				 getTagKeys(namespaceMap.get(me.getKey()));
				 getTagValues(mee.getKey(),namespaceMap.get(me.getKey()));
			 }
		 }
	 }
		 
	 public void setTags(Map<String,Map<String,Object>> defMap,Map<String,String> freeMap) {
		 if(defMap==null||freeMap==null) return;
		 for(Map.Entry<String,Map<String,Object>> me:defMap.entrySet()) {
			 for(Map.Entry<String,Object> mee:me.getValue().entrySet()) {
				 Tags t=new Tags();
				 t.namespaceCombo.setText(me.getKey());
				 
				 t.keyText.dispose();
				 
				 t.valueText.dispose();
				 
				 t.keyCombo=new Combo(t.comp,SWT.READ_ONLY);
				 t.keyCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				 t.keyCombo.setItems(getTagKeys(namespaceMap.get(t.namespaceCombo.getText())));
				 t.keyCombo.setText(mee.getKey());
				 
				 String[] val=getTagValues(mee.getKey(),namespaceMap.get(me.getKey()));
				 
				 if(val!=null) {
		    		  if(t.valueCombo==null||t.valueCombo.isDisposed()) 
		    			  t.valueCombo=new Combo(t.comp,SWT.READ_ONLY);
		    		  t.valueCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    		  t.valueCombo.setItems(val);
		    		 if(mee.getValue()!=null) 
		    			 t.valueCombo.setText((String)mee.getValue());
		    	 }
				 else {
					 t.valueText=new Text(t.comp,SWT.BORDER);
					 t.valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					 if(mee.getValue()!=null) 
						 t.valueText.setText((String)mee.getValue());
				 }
				 tagsSet.add(t);
			 }
			 refresh();
		 }
		 
		 for(Map.Entry<String, String> me:freeMap.entrySet()) {
			 Tags t=new Tags();
			 t.keyText.setText(me.getKey());
			 if(me.getValue()!=null) 
				 t.valueText.setText(me.getValue());
			 tagsSet.add(t);
		 }
	 }
	 
	 public void clearTags() {
		 for(Tags tag:tagsSet) {
			 tag.comp.dispose();
		 }
		 tagsSet.clear();
	 }
}