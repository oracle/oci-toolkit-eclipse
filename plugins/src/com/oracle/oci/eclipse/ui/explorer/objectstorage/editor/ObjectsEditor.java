/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.oracle.oci.eclipse.ui.explorer.common.BaseEditor;
import com.oracle.oci.eclipse.ui.explorer.common.EditorInput;

public class ObjectsEditor extends BaseEditor {

    public final static String ID = ObjectsEditor.class.getName();
    public final static String HEADER = "Bucket: ";

    @Override
    public void createPartControl(Composite parent) {
        ScrolledForm form = super.initForm(parent);
        ObjectsTable ot = new ObjectsTable(form.getBody(), SWT.None,
                ((EditorInput)getEditorInput()).getNameOnly());
        ot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }
}

