package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

public class SettingUpObjectStoragePage extends WizardPage{
	
    private Text objectStorageFirstText;
    private Text objectStorageSecondText;
    boolean pageFlip = false;
    
    private String namespace = ObjStorageClient.getInstance().getNamespace();
    
	public SettingUpObjectStoragePage(ISelection selection) {
		super("wizardPage");
		setTitle("Object Store: Setting Up Storage");
		setDescription("This page creates the required buckets in Object Storage for Dataflow");
	}
	
	@Override
    public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        
        Composite messageContainer = new Composite(container, SWT.NONE);
        GridLayout messageLayout = new GridLayout();
        messageLayout.numColumns = 1;
        messageContainer.setLayout(messageLayout);
        messageContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label messageLabel = new Label(messageContainer, SWT.NULL);
		messageLabel.setText("&Below buckets will be created in Object Storage:"); 
		messageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		messageLabel.setLocation(100, 100);
		
		Composite fileUriContainer = new Composite(container, SWT.NONE);
        GridLayout fileUriLayout = new GridLayout();
        fileUriLayout.numColumns = 1;
        fileUriContainer.setLayout(fileUriLayout);
        fileUriContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		Label fileUriMessage1 = new Label(fileUriContainer, SWT.NULL);
		fileUriMessage1.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		fileUriMessage1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileUriMessage1.setText("oci://dataflow-logs@" + namespace);
		fileUriMessage1.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		Label fileUriMessage2 = new Label(fileUriContainer, SWT.NULL);
		fileUriMessage2.setFont(
			    JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT)
			);
		fileUriMessage2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileUriMessage2.setText("oci://dataflow-warehouse@" + namespace);
		fileUriMessage2.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
        setControl(container);
        setPageComplete(true);
	}
	
	public String getDataflowLogsNamespace() {
		return objectStorageFirstText.getText();
	}
	
	public String getDataflowWarehouseNamespace() {
		return objectStorageSecondText.getText();
	}
    
}
