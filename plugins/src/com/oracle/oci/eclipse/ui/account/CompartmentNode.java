/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import java.util.List;

import com.oracle.bmc.identity.model.Compartment;

public class CompartmentNode {

	private final List<Compartment> childCompartments;
	private final Compartment compartment;

	public CompartmentNode(Compartment node, List<Compartment> childs) {
		this.compartment = node;
		this.childCompartments = childs;
	}

	public Compartment getCompartment() {
		return compartment;
	}

	public List<Compartment> getChildCompartments() {
		return childCompartments;
	}
}
