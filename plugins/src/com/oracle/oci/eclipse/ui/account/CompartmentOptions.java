/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.Compartment.LifecycleState;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;

public class CompartmentOptions extends CompoundContributionItem {

    public CompartmentOptions() {
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        List<Compartment> compartmentList = IdentClient.getInstance().getCompartmentList();

        ArrayList<IContributionItem> list = new ArrayList<IContributionItem>();
        IWorkbenchWindow serviceLocator = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        for (Compartment c: compartmentList) {
            if (!(c.getLifecycleState() == LifecycleState.Active))
                continue;
            HashMap<String, String> hmap = new HashMap<String, String>();
            hmap.put("org.eclipse.ui.commands.radioStateParameter", c.getId());
            CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator,
                    "com.oracle.oci.eclipse.commands.selectCompartment.Compartment." + c.getId(),
                    "com.oracle.oci.eclipse.commands.selectCompartment", CommandContributionItem.STYLE_RADIO);
            p.parameters = hmap;
            p.label = c.getName();

            CommandContributionItem cItem = new CommandContributionItem(p);

            cItem.setVisible(true);
            list.add(cItem);
        }

        ICommandService commandService = serviceLocator.getService(ICommandService.class);
        Command command = commandService.getCommand("com.oracle.oci.eclipse.commands.selectCompartment");
        String currentCompartment = AuthProvider.getInstance().getCompartmentId();
        try {
            HandlerUtil.updateRadioState(command, currentCompartment);
        } catch (ExecutionException e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }

        IContributionItem[] result = new IContributionItem[list.size()];
        return list.toArray(result);
    }

    public static void refreshCompartments() {
        Action updateElementAction = new Action() {
            @Override
            public void run() {
                ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
                if (commandService != null) {
                    commandService.refreshElements("com.oracle.oci.eclipse.commands.selectCompartment", null);
                }
            }
        };
        updateElementAction.run();

    }
}
