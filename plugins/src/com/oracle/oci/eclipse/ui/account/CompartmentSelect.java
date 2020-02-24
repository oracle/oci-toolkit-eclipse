/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.ui.explorer.NavigatorDoubleClick;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.ObjStorageContentProvider;

public class CompartmentSelect extends AbstractHandler implements IElementUpdater {	
	
	Consumer<Compartment> consumer = new Consumer<Compartment>() {
		@Override
		public void accept(Compartment compartment) {
			if (compartment != null) {
				String compartmentId = compartment.getId();
		    	ErrorHandler.logInfo("Changed compartment to: " + compartmentId);
		    	AuthProvider.getInstance().setCompartmentName(compartment.getName());
				AuthProvider.getInstance().updateCompartmentId(compartmentId);

				// Must refresh buckets and close open bucket windows
				ObjStorageContentProvider.getInstance().getBucketsAndRefresh();
				NavigatorDoubleClick.closeAllBucketWindows();
			}
		}
	};
	
	
	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if ( null == event || ! ( event.getTrigger() instanceof Event ) ) { return null;}

        Event eventWidget = (Event)event.getTrigger();
        // Makes sure event came from a ToolItem.
        if ( eventWidget.widget instanceof ToolItem )  {
        	// open the wizard to select the compartment
        	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
        			new CompartmentSelectWizard(consumer, true));
        	dialog.setFinishButtonText("Select");
        	if (Window.OK == dialog.open()) {
        	}
        }

        return null;
    }

    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
    	
    }
}
