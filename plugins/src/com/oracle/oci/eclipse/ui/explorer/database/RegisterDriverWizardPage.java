package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RegisterDriverWizardPage extends WizardPage {
    
    private ISelection selection;
    private Text jarFileText;

    public RegisterDriverWizardPage(ISelection selection) {
        super("wizardPage");
        setTitle("Register JDBC Driver for Autonomous Database");
		String description = "Register a JDBC Driver in order to create connections to the Autonomous Database.\n"
               				+ "This is a one time effort after you install the plugin.";
        setDescription(description);
        this.selection = selection;
    }

    @Override
    public void createControl(Composite parent) {
    	
    	Composite topLevelContainer = new Composite(parent, SWT.NULL);
        GridLayout topLevelLayout = new GridLayout();
        topLevelContainer.setLayout(topLevelLayout);
        
        final String linkURI = "https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html";
        Link link = new Link(topLevelContainer, SWT.NONE);
        link.setText("Download the ojdbcN.jar along with oraclepki.jar, osdt_core.jar and osdt_cert.jar from the \n"
        +"<a href=\""+linkURI+"\">Oracle JDBC download page</a> "
        +"or from Maven Central. Recommendation is to use the jars from the \n latest release version. "
        +"Copy all the jars to the same directory and browse for the JDBC driver to register.\n");
        
        // Event handling when users click on links.
        link.addSelectionListener(new SelectionAdapter()  {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch(linkURI);
            }
        });

        Composite innerTopContainer = new Composite(topLevelContainer, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 3;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label label = new Label(innerTopContainer, SWT.NULL);
        label.setText("&JDBC Jar File:");

        jarFileText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        jarFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        jarFileText.setEditable(false);

        Button jarFileButton = new Button(innerTopContainer, SWT.PUSH);
        jarFileButton.setText("Browse...");
        jarFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	jarFileText.setText(handleBrowse(innerTopContainer.getShell(), jarFileText.getText(), "*.jar"));
            }
        });
        
        setControl(innerTopContainer);
    	
    }
    
    private String handleBrowse(Shell shell, String path, String filter) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        if (filter != null)
            dialog.setFilterExtensions(new String [] {filter});
        dialog.setFilterPath(path);
        return dialog.open();
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
    
	public String getDriverJarName() {
		return jarFileText.getText();
	}

}
