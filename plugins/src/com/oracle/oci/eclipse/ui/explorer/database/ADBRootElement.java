/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

public class ADBRootElement {
	
	private static final String ELEMENT_NAME = "Autonomous Database";
	
	public ADBRootElement() { }

    static public String getName() {
        return ELEMENT_NAME;
    }

}
