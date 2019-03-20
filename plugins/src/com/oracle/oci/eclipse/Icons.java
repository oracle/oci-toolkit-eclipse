/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse;

public enum Icons {
    BUCKET("icons/bucket.png"),
    TOOLBAR_LOGIN("icons/toolbar-login.png"),
    COMPUTE("icons/compute.png"),
    COMPUTE_INSTANCE("icons/compute-instance.png"),
    OBJECT_STORAGE("icons/object-storage.png"),
    BLOCK_STORAGE("icons/block-storage.png"),
    REGION_US("icons/regions/us-orb.png"),
    REGION_GERMANY("icons/regions/germany-orb.png"),
    REGION_UK("icons/regions/uk-orb.png"),
	REGION_CANADA("icons/regions/canada-flag.png");

    private String path;

    private Icons(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
