/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.oracle.oci.eclipse.ui.account.ClientUpdateManager;
import com.oracle.oci.eclipse.ui.explorer.common.BaseEditor;

public class ADBInstanceEditor extends BaseEditor implements PropertyChangeListener {


    public final static String ID = ADBInstanceEditor.class.getName();
    public final static String TITLE = "Autonomous Databases";

    ADBInstanceEditor classReference = this;
    ADBInstanceTable table;

    @Override
    public void createPartControl(Composite parent) {
        ScrolledForm form = super.initForm(parent);
        table = new ADBInstanceTable(form.getBody(), SWT.None);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ClientUpdateManager.getInstance().addViewChangeListener(this);
        DisposeListener disposeListener = new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                ClientUpdateManager.getInstance().removeViewChangeListener(classReference);
            }
        };
        table.addDisposeListener(disposeListener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        table.refresh(true);
    }
}
