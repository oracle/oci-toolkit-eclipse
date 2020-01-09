package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;

public class RotateWalletWizardPage extends WizardPage {
    private ISelection selection;
    AutonomousDatabaseSummary instance;
    String walletType;
    private Text databaseNameText;

    public RotateWalletWizardPage(ISelection selection, AutonomousDatabaseSummary instance, String walletType) {
        super("wizardPage");
        setTitle("Rotate Wallet");
        setDescription("");
        this.selection = selection;
        this.instance = instance;
        this.walletType = walletType;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;
        layout.verticalSpacing = 9;
        
        final String rotationMsgInstanceWallet =
        		"Rotating the instance wallet invalidates the certificate keys associated with the existing instance wallet"+"\n"
        		+"and generates a new wallet. The existing regional wallet will continue to work. Existing connections"+"\n"
                +"to the Autonomous Database that use the old instance wallet will be terminated over a period of"+"\n"
                +"time, and will need to be reestablished using the new wallet. If you need to terminate all existing"+"\n"
                +"connections to a database immediately, stop and restart the database."+"\n\n";
        
        final String rotationMsgRegionalWallet = 
        		"Are you sure you want to rotate the regional wallet?"+"\n\n"
        		+"Rotating the regional wallet will invalidate all existing regional and instance wallets in the region."+"\n"
        		+"Certificate keys associated with the existing wallets in the region will be invalidated. All connections"+"\n"
        		+"to databases in the region that use the existing regional wallet will be terminated over a period of time."+"\n"
        		+"If you need to terminate all existing connections to a database immediately, stop and restart the database."+"\n\n";
        
        final String rotationMsg;
        if(walletType.equalsIgnoreCase(ADBConstants.REGIONAL_WALLET)) {
        	rotationMsg = rotationMsgRegionalWallet;
        } else {
        	rotationMsg = rotationMsgInstanceWallet;
        }
        Label rotationMsgLabel = new Label(container, SWT.NULL);
        rotationMsgLabel.setText(rotationMsg);
        
        final String rotationMsg1 = "ENTER THE DATABASE NAME ("+instance.getDbName()+") TO CONFIRM THE "+walletType.toUpperCase()+" ROTATION";
        Label rotationMsgLabel1 = new Label(container, SWT.NULL);
        rotationMsgLabel1.setText(rotationMsg1);
        
        databaseNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        databaseNameText.setLayoutData(gd);
        
        new Label(container, SWT.NULL);
        new Label(container, SWT.NULL);
        new Label(container, SWT.NULL);
        
        
        setControl(container);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
    
	public String getDbName() {
		return databaseNameText.getText();
	}

}
