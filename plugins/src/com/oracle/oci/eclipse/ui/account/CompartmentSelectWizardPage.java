/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.account;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;

public class CompartmentSelectWizardPage extends WizardPage {

    private static final String COMPARTMENT_KEY = "compartment";
    private static final String GRAND_CHILDREN_FETCHED = "grandChildrenFetched";
    private ISelection selection;
    private Tree tree;
    private Image IMAGE;

    public CompartmentSelectWizardPage(ISelection selection, boolean showCurrentCompartment) {
        super("wizardPage");
        setTitle("Select Compartment");
        if(showCurrentCompartment)
            setDescription("Current Compartment is : "+AuthProvider.getInstance().getCompartmentName());
        else
            setDescription("Choose the compartment");
        this.selection = selection;
        IMAGE = Activator.getImage(Icons.COMPARTMENT.getPath());
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);

        tree = new Tree(container, SWT.RADIO | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));


        IdentClient identClient = IdentClient.getInstance();
        Compartment rootCompartment = identClient.getRootCompartment();

        Job job = new Job("Get Root compartment children") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // Get root compartment children from server
                CompartmentNode rootCompartmentNode = new CompartmentNode(rootCompartment,
                        identClient.getCompartmentList(rootCompartment));

                // update tree node using UI thread
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // add root compartment node to tree
                            TreeItem treeItem0 = new TreeItem(tree, 0);
                            treeItem0.setText(rootCompartment.getName());
                            treeItem0.setImage(IMAGE);
                            treeItem0.setData(COMPARTMENT_KEY, rootCompartmentNode);

                            for (Compartment compartment : rootCompartmentNode.getChildCompartments()) {
                                Job job = new Job("Get grand children of root compartment") {
                                    @Override
                                    protected IStatus run(IProgressMonitor monitor) {
                                        CompartmentNode compartmentNode = new CompartmentNode(compartment,
                                                identClient.getCompartmentList(compartment));
                                        Display.getDefault().asyncExec(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    TreeItem treeItem = new TreeItem(treeItem0, 0);
                                                    treeItem.setText(compartment.getName());
                                                    treeItem.setImage(IMAGE);
                                                    treeItem.setData(COMPARTMENT_KEY, compartmentNode);
                                                } catch(Exception e) {}
                                            }
                                        });
                                        return Status.OK_STATUS;
                                    }
                                };
                                job.schedule();
                            }

                        } catch(Exception ex) {}
                    }
                });
                return Status.OK_STATUS;
            }
        };
        job.schedule();

        tree.addListener (SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(Event event) {}
        });

        tree.addListener(SWT.Expand, new Listener() {
            @Override
            public void handleEvent(Event e) {
                try {
                    handNodeExpanionEvent(e);
                } catch (Throwable ex) {}
            }
        });

        setControl(container);
    }

    private void handNodeExpanionEvent(Event e) {
        TreeItem treeItem = (TreeItem) e.item;
        synchronized (treeItem) {
            String grandChildrenFetched = (String) treeItem.getData(GRAND_CHILDREN_FETCHED);
            if (grandChildrenFetched != null && grandChildrenFetched.equalsIgnoreCase("true"))
                return;
            treeItem.setData(GRAND_CHILDREN_FETCHED, "true");
        }

        TreeItem children[] = treeItem.getItems();
        if (children == null || (children.length == 0))
            return;

        for (int i = 0; i < children.length; i++) {
            TreeItem childItem = children[i];
            CompartmentNode childCompartmentNode = (CompartmentNode) childItem.getData(COMPARTMENT_KEY);
            List<Compartment> grandChildren = childCompartmentNode.getChildCompartments();
            grandChildren = grandChildren.stream()
                    .sorted(Comparator.comparing(Compartment::getName))
                    .collect(Collectors.toList());

            for (Compartment compartment : grandChildren) {
                Job job = new Job("Get compartments on tree node expansion event") {
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        // network call to fetch compartments
                        CompartmentNode compartmentNode = new CompartmentNode(compartment,
                                IdentClient.getInstance().getCompartmentList(compartment));
                        // update UI in UI thread, after compartments are fetched from server
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                TreeItem grandChildTreeItem = new TreeItem(childItem, 0);
                                grandChildTreeItem.setText(compartment.getName());
                                grandChildTreeItem.setImage(IMAGE);
                                grandChildTreeItem.setData(COMPARTMENT_KEY, compartmentNode);
                            }
                        });
                        // use this to open a Shell in the UI thread
                        return Status.OK_STATUS;
                    }
                };
                job.schedule();

            }
        }
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public Compartment getSelectedCompartment() {
        TreeItem[] items = tree.getSelection();
        if(items !=null && items.length>0) {
            TreeItem selectedItem = items[0];
            CompartmentNode compartmentNode = (CompartmentNode)selectedItem.getData(COMPARTMENT_KEY);
            return compartmentNode.getCompartment();
        }

        return null;
    }

}
