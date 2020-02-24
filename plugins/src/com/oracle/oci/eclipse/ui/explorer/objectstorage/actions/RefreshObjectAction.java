/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsTable;

public class RefreshObjectAction extends BaseAction {

    private final ObjectsTable bucketObjectTable;

    public RefreshObjectAction (ObjectsTable table){
        bucketObjectTable = table;
    }

    @Override
    public String getText() {
        return "Refresh";
    }

    @Override
    protected void runAction() {
        bucketObjectTable.refresh(true);
    }
}
