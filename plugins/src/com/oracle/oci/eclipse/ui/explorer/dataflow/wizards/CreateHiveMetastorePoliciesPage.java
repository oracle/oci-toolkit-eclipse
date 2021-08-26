package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CreateHiveMetastorePoliciesPage extends WizardPage{

	private Text groupOcid;
	private Text metastoreOcid;
	private Text managedTableLocationBucket;
	private Text managedTableLocationBucketRegion;
	private Text externalTableLocationBucket;
	private Text externalTableLocationBucketRegion;
	private String groupOcidStr = "<group-ocid>";
	private String metastoreOcidStr = "<metastore-ocid>";
	private String managedTableLocationBucketStr = "<managed-table-location-bucket>";
	private String managedTableLocationBucketRegionStr = "<managed-table-location-bucket-region>";
	private String externalTableLocationBucketStr = "<external-table-location-bucket>";
	private String externalTableLocationBucketRegionStr = "<external-table-location-bucket-region>";
	private Label policy1;
	private Label policy2;
	private Label policy3;
	private Label policy4;
	
	public CreateHiveMetastorePoliciesPage(ISelection selection) {
		super("wizardPage");
		setTitle("Identity: Hive Metastore policies setup");
		setDescription("This page creates the required IAM policies to allow Data Flow integration with Hive Metastore");
	}
	
	@Override
    public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        
		Composite groupOcidContainer = new Composite(container, SWT.NONE);
        GridLayout groupOcidLayout = new GridLayout();
        groupOcidLayout.numColumns = 2;
        groupOcidLayout.makeColumnsEqualWidth = true;
        groupOcidContainer.setLayout(groupOcidLayout);
        groupOcidContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label groupOcidLabel = new Label(groupOcidContainer, SWT.NULL);
        groupOcidLabel.setText("&group-ocid");
        groupOcid = new Text(groupOcidContainer, SWT.BORDER | SWT.SINGLE);
        groupOcid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupOcid.setText("");
        
        
        
        Composite metastoreOcidContainer = new Composite(container, SWT.NONE);
        GridLayout metastoreOcidLayout = new GridLayout();
        metastoreOcidLayout.numColumns = 2;
        metastoreOcidLayout.makeColumnsEqualWidth = true;
        metastoreOcidContainer.setLayout(metastoreOcidLayout);
        metastoreOcidContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label metastoreOcidLabel = new Label(metastoreOcidContainer, SWT.NULL);
        metastoreOcidLabel.setText("&metastore-ocid");
        metastoreOcid = new Text(metastoreOcidContainer, SWT.BORDER | SWT.SINGLE);
        metastoreOcid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        metastoreOcid.setText("");
        
        
        
        
        Composite managedTableLocationContainer = new Composite(container, SWT.NONE);
        GridLayout managedTableLocationLayout = new GridLayout();
        managedTableLocationLayout.numColumns = 2;
        managedTableLocationLayout.makeColumnsEqualWidth = true;
        managedTableLocationContainer.setLayout(managedTableLocationLayout);
        managedTableLocationContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label managedTableLocationLabel = new Label(managedTableLocationContainer, SWT.NULL);
        managedTableLocationLabel.setText("&managed-table-location-bucket");
        managedTableLocationBucket = new Text(managedTableLocationContainer, SWT.BORDER | SWT.SINGLE);
        managedTableLocationBucket.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        managedTableLocationBucket.setText("");
        
        
        
        Composite managedTableLocationBucketRegionContainer = new Composite(container, SWT.NONE);
        GridLayout managedTableLocationBucketRegionLayout = new GridLayout();
        managedTableLocationBucketRegionLayout.numColumns = 2;
        managedTableLocationBucketRegionLayout.makeColumnsEqualWidth = true;
        managedTableLocationBucketRegionContainer.setLayout(managedTableLocationBucketRegionLayout);
        managedTableLocationBucketRegionContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label managedTableLocationBucketRegionLabel = new Label(managedTableLocationBucketRegionContainer, SWT.NULL);
        managedTableLocationBucketRegionLabel.setText("&managed-table-location-bucket-region");
        managedTableLocationBucketRegion = new Text(managedTableLocationBucketRegionContainer, SWT.BORDER | SWT.SINGLE);
        managedTableLocationBucketRegion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        managedTableLocationBucketRegion.setText("");
        
        
        
        Composite externalTableLocationBucketContainer = new Composite(container, SWT.NONE);
        GridLayout externalTableLocationBucketLayout = new GridLayout();
        externalTableLocationBucketLayout.numColumns = 2;
        externalTableLocationBucketLayout.makeColumnsEqualWidth = true;
        externalTableLocationBucketContainer.setLayout(externalTableLocationBucketLayout);
        externalTableLocationBucketContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label externalTableLocationBucketLabel = new Label(externalTableLocationBucketContainer, SWT.NULL);
        externalTableLocationBucketLabel.setText("&external-table-location-bucket");
        externalTableLocationBucket = new Text(externalTableLocationBucketContainer, SWT.BORDER | SWT.SINGLE);
        externalTableLocationBucket.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        externalTableLocationBucket.setText("");
        
        
        
        
        Composite externalTableLocationBucketRegionContainer = new Composite(container, SWT.NONE);
        GridLayout externalTableLocationBucketRegionLayout = new GridLayout();
        externalTableLocationBucketRegionLayout.numColumns = 2;
        externalTableLocationBucketRegionLayout.makeColumnsEqualWidth = true;
        externalTableLocationBucketRegionContainer.setLayout(externalTableLocationBucketRegionLayout);
        externalTableLocationBucketRegionContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label externalTableLocationBucketRegionLabel = new Label(externalTableLocationBucketRegionContainer, SWT.NULL);
        externalTableLocationBucketRegionLabel.setText("&external-table-location-bucket-region");
        externalTableLocationBucketRegion = new Text(externalTableLocationBucketRegionContainer, SWT.BORDER | SWT.SINGLE);
        externalTableLocationBucketRegion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        externalTableLocationBucketRegion.setText("");
        
        groupOcid.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	groupOcidStr = groupOcid.getText();
            	policy1.setText("ALLOW group id " + groupOcidStr + " to \n"
            			+ "{CATALOG_METASTORE_READ, CATALOG_METASTORE_EXECUTE} in tenancy where \n"
        				+ "target.metastore.id='" + metastoreOcidStr + "'");
        		
        		policy4.setText("ALLOW group id " + groupOcidStr + " to \n"
        				+ "{CATALOG_METASTORE_INSPECT} in tenancy");
        		
        		policy2.setText("ALLOW group id " + groupOcidStr + " to read buckets \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
        		
        		policy3.setText("ALLOW group id " + groupOcidStr + " to manage objects \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
            	System.out.println("Modifying groupOcid");
              }
            });
        
        metastoreOcid.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	metastoreOcidStr = metastoreOcid.getText();
            	policy1.setText("ALLOW group id " + groupOcidStr + " to \n"
            			+ "{CATALOG_METASTORE_READ, CATALOG_METASTORE_EXECUTE} in tenancy where \n"
        				+ "target.metastore.id='" + metastoreOcidStr + "'");
        		
        		policy4.setText("ALLOW group id " + groupOcidStr + " to \n"
        				+ "{CATALOG_METASTORE_INSPECT} in tenancy");
        		
        		policy2.setText("ALLOW group id " + groupOcidStr + " to read buckets \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
        		
        		policy3.setText("ALLOW group id " + groupOcidStr + " to manage objects \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
            	System.out.println("Modifying metastoreOcid");
              }
            });
        
        managedTableLocationBucket.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	managedTableLocationBucketStr = managedTableLocationBucket.getText();
            	policy1.setText("ALLOW group id " + groupOcidStr + " to \n"
            			+ "{CATALOG_METASTORE_READ, CATALOG_METASTORE_EXECUTE} in tenancy where \n"
        				+ "target.metastore.id='" + metastoreOcidStr + "'");
        		
        		policy4.setText("ALLOW group id " + groupOcidStr + " to \n"
        				+ "{CATALOG_METASTORE_INSPECT} in tenancy");
        		
        		policy2.setText("ALLOW group id " + groupOcidStr + " to read buckets \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
        		
        		policy3.setText("ALLOW group id " + groupOcidStr + " to manage objects \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
            	System.out.println("Modifying managedTableLocationBucket");
              }
            });
        
        managedTableLocationBucketRegion.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	managedTableLocationBucketRegionStr = managedTableLocationBucketRegion.getText();
            	policy1.setText("ALLOW group id " + groupOcidStr + " to \n"
            			+ "{CATALOG_METASTORE_READ, CATALOG_METASTORE_EXECUTE} in tenancy where \n"
        				+ "target.metastore.id='" + metastoreOcidStr + "'");
        		
        		policy4.setText("ALLOW group id " + groupOcidStr + " to \n"
        				+ "{CATALOG_METASTORE_INSPECT} in tenancy");
        		
        		policy2.setText("ALLOW group id " + groupOcidStr + " to read buckets \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
        		
        		policy3.setText("ALLOW group id " + groupOcidStr + " to manage objects \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
            	System.out.println("Modifying managedTableLocationBucketRegion");
              }
            });
        
        externalTableLocationBucket.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	externalTableLocationBucketStr = externalTableLocationBucket.getText();
            	policy1.setText("ALLOW group id " + groupOcidStr + " to \n"
            			+ "{CATALOG_METASTORE_READ, CATALOG_METASTORE_EXECUTE} in tenancy where \n"
        				+ "target.metastore.id='" + metastoreOcidStr + "'");
        		
        		policy4.setText("ALLOW group id " + groupOcidStr + " to \n"
        				+ "{CATALOG_METASTORE_INSPECT} in tenancy");
        		
        		policy2.setText("ALLOW group id " + groupOcidStr + " to read buckets \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
        		
        		policy3.setText("ALLOW group id " + groupOcidStr + " to manage objects \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
            	System.out.println("Modifying externalTableLocationBucket");
              }
            });
        
        externalTableLocationBucketRegion.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	externalTableLocationBucketRegionStr = externalTableLocationBucketRegion.getText();
            	policy1.setText("ALLOW group id " + groupOcidStr + " to \n"
            			+ "{CATALOG_METASTORE_READ, CATALOG_METASTORE_EXECUTE} in tenancy where \n"
        				+ "target.metastore.id='" + metastoreOcidStr + "'");
        		
        		policy4.setText("ALLOW group id " + groupOcidStr + " to \n"
        				+ "{CATALOG_METASTORE_INSPECT} in tenancy");
        		
        		policy2.setText("ALLOW group id " + groupOcidStr + " to read buckets \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
        		
        		policy3.setText("ALLOW group id " + groupOcidStr + " to manage objects \n"
        				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
        				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
        				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
            	System.out.println("Modifying externalTableLocationBucketRegion");
              }
            });
        
        Composite messageContainer = new Composite(container, SWT.NONE);
        GridLayout messageLayout = new GridLayout();
        messageLayout.numColumns = 1;
        messageContainer.setLayout(messageLayout);
        messageContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label messageLabel = new Label(messageContainer, SWT.NULL);
		messageLabel.setText("&Below IAM policies will be created:"); 
		messageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		messageLabel.setLocation(100, 100);
		
		
		Composite policyContainer = new Composite(container, SWT.NONE);
        GridLayout policyLayout = new GridLayout();
        policyLayout.numColumns = 1;
        policyContainer.setLayout(policyLayout);
        policyContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		policy1 = new Label(policyContainer, SWT.NULL);
		policy1.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy1.setText("ALLOW group id " + groupOcidStr + " to \n"
				+ "{CATALOG_METASTORE_READ, CATALOG_METASTORE_EXECUTE} in tenancy where \n"
				+ "target.metastore.id='" + metastoreOcidStr + "'");
		policy1.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		
		Composite policyContainer3 = new Composite(container, SWT.NONE);
        GridLayout policyLayout3 = new GridLayout();
        policyLayout3.numColumns = 1;
        policyContainer3.setLayout(policyLayout3);
        policyContainer3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		policy4 = new Label(policyContainer3, SWT.NULL);
		policy4.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy4.setText("ALLOW group id " + groupOcidStr + " to \n"
				+ "{CATALOG_METASTORE_INSPECT} in tenancy");
		policy4.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		
		Composite policyContainer2 = new Composite(container, SWT.NONE);
        GridLayout policyLayout2 = new GridLayout();
        policyLayout2.numColumns = 1;
        policyContainer2.setLayout(policyLayout2);
        policyContainer2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		policy2 = new Label(policyContainer2, SWT.NULL);
		policy2.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy2.setText("ALLOW group id " + groupOcidStr + " to read buckets \n"
				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
		policy2.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		policy3 = new Label(policyContainer2, SWT.NULL);
		policy3.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		policy3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		policy3.setText("ALLOW group id " + groupOcidStr + " to manage objects \n"
				+ "in tenancy where any{ all {target.bucket.name='" + managedTableLocationBucketStr + "', \n"
				+ "request.region='" + managedTableLocationBucketRegionStr + "'}, all {target.bucket.name='" + externalTableLocationBucketStr + "', \n"
				+ "request.region='" + externalTableLocationBucketRegionStr + "'}}");
		policy3.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		
        setControl(container);
        setPageComplete(true);
	}
	
	public String getGroupOcid() {
		return groupOcid.getText();
	}
	
	public String getMetastoreOcid() {
		return metastoreOcid.getText();
	}
	
	public String getManagedTableLocationBucket() {
		return managedTableLocationBucket.getText();
	}
	
	public String getManagedTableLocationBucketRegion() {
		return managedTableLocationBucketRegion.getText();
	}
	
	public String getExternalTableLocationBucket() {
		return externalTableLocationBucket.getText();
	}
	
	public String getExternalTableLocationBucketRegion() {
		return externalTableLocationBucketRegion.getText();
	}
}
