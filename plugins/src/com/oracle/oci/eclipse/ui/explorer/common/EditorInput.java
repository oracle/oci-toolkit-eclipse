/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.common;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class EditorInput implements IEditorInput {

    private final String name;
    private final String header;
    private final String tooltip = "OCI Window: ";

    public EditorInput(String name, String header) {
        this.name = name;
        this.header = header;
    }

    @Override
    public String getToolTipText() {
        return tooltip + name;
    }

    @Override
    public String getName() {
        return header + name;
    }

    public String getNameOnly() {
        return name;
    }
    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }
}

