/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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
		elements = new ConfigurationElementsUI();
		elements.drawElements(parent);
		return null;
	}

}
