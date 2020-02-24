/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.ObjStorageContentProvider;

public class RefreshBucketAction extends BaseAction {

    public RefreshBucketAction() {
        setText("Refresh Buckets List");
    }

    @Override
    protected void runAction() {
        ObjStorageContentProvider.getInstance().getBucketsAndRefresh();
    }
}
