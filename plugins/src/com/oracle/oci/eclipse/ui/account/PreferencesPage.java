/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.ConfigFileReader.ConfigFile;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.account.PreferencesWrapper;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

public class PreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private ConfigurationElementsUI elements = null;

	public PreferencesPage() {
		// TODO Auto-generated constructor stub
	}

	public PreferencesPage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public PreferencesPage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		elements = new ConfigurationElementsUI();
		elements.drawElements(parent);
		return null;
	}


	@Override
	protected void performApply() { 
		performOk();
	}

	@Override
	public boolean performOk() {
		updateApplyButton();
		elements.saveProfile(getShell());
		final String profileName = elements.getProfileName();
		final String configFileName = elements.getConfigFileName();

		try {
			doFinish(profileName, configFileName);
		} catch (Exception e) {
           ErrorHandler.reportAndShowException(e.getMessage(), e);
           closeExplorerView(); 
		}
		// If connection is successful, open the Explorer
		openExplorerView();
		return true;
	}
	
	@Override
	protected void contributeButtons(final Composite parent)
	{
		final Button applyButton = new Button(parent, SWT.PUSH);
		applyButton.setText("Apply");

		final int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		final Point minButtonSize = applyButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minButtonSize.x);
		applyButton.setLayoutData(data);

		applyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				performApply();
			}
		});

		final GridLayout layout = (GridLayout)parent.getLayout();
		layout.numColumns++;
	}
 
	public void openExplorerView() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			activeWindow.getActivePage().showView(Activator.PLUGIN_EXPLORER_ID);
		} catch (PartInitException e) {
			ErrorHandler.logErrorStack(e.getMessage(), e);
		}
	}

	public void closeExplorerView() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			IViewPart explorerViewPart = activeWindow.getActivePage().findView(Activator.PLUGIN_EXPLORER_ID);
			activeWindow.getActivePage().hideView(explorerViewPart);
		} catch (Exception e) {
			ErrorHandler.logErrorStack(e.getMessage(), e);
		}
	}
	
    private void doFinish(
            String profileName,
            String configFileName)
                    throws CoreException {

        PreferencesWrapper.setConfigFileName(configFileName);
        PreferencesWrapper.setProfile(profileName);
        ConfigFile config = null;
        try {
            config = ConfigFileReader.parse(configFileName, profileName);
        } catch (IOException e1) {
            ErrorHandler.logError("ConfigFileReader parse error:" + e1.getMessage());
        }

        PreferencesWrapper.setRegion(config.get("region"));
        AuthProvider.getInstance().setCompartmentId(config.get("tenancy"));
        AuthProvider.getInstance().setCompartmentName(AuthProvider.ROOT_COMPARTMENT_NAME);

        ObjStorageClient.getInstance().getNamespace();
        ClientUpdateManager.getInstance().refreshClients();
   
    }
    
    @Override
    protected void performDefaults() {
    	// No need to override defaults. 
    }
}
