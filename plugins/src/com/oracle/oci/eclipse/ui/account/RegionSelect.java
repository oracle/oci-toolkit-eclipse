/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.UIElement;

import com.oracle.bmc.Region;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.account.PreferencesWrapper;
import com.oracle.oci.eclipse.ui.explorer.NavigatorDoubleClick;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.ObjStorageContentProvider;

public class RegionSelect extends AbstractHandler implements IElementUpdater{

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        if ( null == event || ! ( event.getTrigger() instanceof Event ) ) { return null;}

        Event eventWidget = (Event)event.getTrigger();
        // Makes sure event came from a ToolItem.
        if ( eventWidget.widget instanceof ToolItem )  {
            return SelectUtils.toolItemClickHandler(eventWidget);
        }

        if (HandlerUtil.matchesRadioState(event))
            return null; // we are already in the updated state - do nothing

        String region = event.getParameter(RadioState.PARAMETER_ID);
        Region r = Region.fromRegionId(region);
        AuthProvider.getInstance().updateRegion(r.getRegionId());

        // Must refresh buckets and close open bucket windows
        ObjStorageContentProvider.getInstance().getBucketsAndRefresh();
        NavigatorDoubleClick.closeAllBucketWindows();

        HandlerUtil.updateRadioState(event.getCommand(), region);
        return null;
    }

    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {

        Region region = Region.fromRegionId(PreferencesWrapper.getRegion());
        if (!parameters.containsKey("org.eclipse.ui.commands.radioStateParameter")) {
            element.setIcon(Activator.getImageDescriptor(RegionOptions.iconMap.get(region.getRegionId())));
        } else {
            if (region.getRegionId().equals(parameters.get("org.eclipse.ui.commands.radioStateParameter")))
                element.setChecked(true);
        }
    }
}
