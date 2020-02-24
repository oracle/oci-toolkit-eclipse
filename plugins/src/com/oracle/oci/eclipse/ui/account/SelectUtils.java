/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;

public class SelectUtils {
	
	public static Object toolItemClickHandler(Event eventWidget) {
		ToolItem toolItem = (ToolItem)eventWidget.widget;
		
		// Creates fake selection event.
		Event newEvent = new Event();
		newEvent.button = 1;
		newEvent.widget = toolItem;
		newEvent.detail = SWT.ARROW;
		newEvent.x = toolItem.getBounds().x;
		newEvent.y = toolItem.getBounds().y + toolItem.getBounds().height;
	
		// Dispatches the event.
		toolItem.notifyListeners( SWT.Selection, newEvent );
		return null;
	}

}
