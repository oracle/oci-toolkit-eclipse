/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.account.PreferencesWrapper;
import com.oracle.oci.eclipse.sdkclients.IdentClient;

public class RegionOptions extends CompoundContributionItem {
    public static final String DEFAULT_REGION ="us-phoenix-1";

    @SuppressWarnings("serial")
    public static HashMap<String, String> iconMap = new HashMap<String, String>(){
        {
            put("us-ashburn-1", Icons.REGION_US.getPath());
            put("us-phoenix-1", Icons.REGION_US.getPath());
            put("eu-frankfurt-1", Icons.REGION_GERMANY.getPath());
            put("uk-london-1", Icons.REGION_UK.getPath());
            put("ca-toronto-1", Icons.REGION_CANADA.getPath());
            put("ap-mumbai-1", Icons.REGION_INDIA.getPath());
            put("ap-seoul-1", Icons.REGION_SOUTH_KOREA.getPath());
            put("ap-tokyo-1", Icons.REGION_JAPAN.getPath());
            put("eu-zurich-1", Icons.REGION_SWITZERLAND.getPath());
        }
    };

    public RegionOptions() {}

    @Override
    protected IContributionItem[] getContributionItems() {
        ArrayList<IContributionItem> list = new ArrayList<IContributionItem>();
        IWorkbenchWindow serviceLocator = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        List<RegionSubscription> regionList = IdentClient.getInstance().getRegionsList();

        for (RegionSubscription r: regionList) {
            Map<String, String> hmap = new HashMap<String, String>();
            hmap.put("org.eclipse.ui.commands.radioStateParameter", r.getRegionName());
            CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator,
                    "com.oracle.oci.eclipse.commands.selectRegion.Region." + r.getRegionName(),
                    "com.oracle.oci.eclipse.commands.selectRegion", CommandContributionItem.STYLE_RADIO);
            p.parameters = hmap;
            if (Pattern.matches("\\w{2}-\\w+-\\d+", r.getRegionName())) {
                p.label = getFormattedRegion(r.getRegionName());
            } else {
                p.label = r.getRegionName();
            }
            if (iconMap.get(r.getRegionName()) != null) {
                p.icon = Activator.getImageDescriptor(iconMap.get(r.getRegionName()));
            }
            CommandContributionItem c = new CommandContributionItem(p);
            c.setVisible(true);
            list.add(c);
        }
        ICommandService commandService = serviceLocator.getService(ICommandService.class);
        Command command = commandService.getCommand("com.oracle.oci.eclipse.commands.selectRegion");

        try {
            HandlerUtil.updateRadioState(command, PreferencesWrapper.getRegion());
        } catch (ExecutionException e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }

        IContributionItem[] result = new IContributionItem[list.size()];
        return list.toArray(result);
    }

    private String getFormattedRegion(String regionId) {
        String[] label = regionId.split("-");
        String[] new_label = new String[label.length - 1];
        new_label[0] = label[0].toUpperCase();
        new_label[1] = label[1];
        return String.join(" ", new_label);
    }

    public static void refreshRegions() {
        Action updateElementAction = new Action() {
            @Override
            public void run() {
                ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
                if (commandService != null) {
                    commandService.refreshElements("com.oracle.oci.eclipse.commands.selectRegion", null);
                }
            }
        };
        updateElementAction.run();
    }
}
