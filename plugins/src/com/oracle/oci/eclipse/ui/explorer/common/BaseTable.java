/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.common;

import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.account.PreferencesWrapper;

public abstract class BaseTable extends Composite {
    protected TableViewer viewer;

    public BaseTable(Composite parent, int style) {
        super(parent, style);
        createTable();
    }

    protected abstract void createColumns(TableColumnLayout tableColumnLayout, Table tree);
    protected abstract void fillMenu(IMenuManager manager);
    protected abstract List<?> getTableData();
    protected abstract List<?> getTableCachedData();
    protected abstract int getTableDataSize();
    protected void addTableLabels(FormToolkit toolkit, Composite left, Composite right) {}
    Label profileLabel;
    Label compartmentLabel;
    Label regionLabel;

    protected void createTable() {
        FormToolkit toolkit = new FormToolkit(Display.getDefault());
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        setLayout(gridLayout);

        Composite sectionComp = toolkit.createComposite(this, SWT.None);
        sectionComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sectionComp.setLayout(new GridLayout(1, false));

        Composite headingComp = toolkit.createComposite(sectionComp, SWT.FILL);
        headingComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        headingComp.setLayout(new GridLayout(2, false));

        Composite left = toolkit.createComposite(headingComp, SWT.LEFT| SWT.FILL | SWT.TOP);
        left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        left.setLayout(new GridLayout(1, true));
        Composite right = toolkit.createComposite(headingComp, SWT.RIGHT | SWT.FILL);
        right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        right.setLayout(new GridLayout(1, true));

        profileLabel = new Label(left, SWT.NONE);
        compartmentLabel = new Label(left, SWT.NONE);
        regionLabel = new Label(left, SWT.NONE);
        updateTableLabels();
        addTableLabels(toolkit, left, right);


        Composite tableHolder = toolkit.createComposite(sectionComp, SWT.None);
        tableHolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        FillLayout layout = new FillLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 10;
        layout.type = SWT.VERTICAL;
        tableHolder.setLayout(layout);

        Composite tableComp = toolkit.createComposite(tableHolder, SWT.None);
        TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableComp.setLayout(tableColumnLayout);

        viewer = new TableViewer(tableComp, SWT.BORDER | SWT.VIRTUAL | SWT.MULTI);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.setUseHashlookup(true);
        viewer.setContentProvider(new TableContentProvider(viewer));

        createColumns(tableColumnLayout, viewer.getTable());
        hookMenu();
    }


    private void updateTableLabels() {
        profileLabel.setText("Profile: " + PreferencesWrapper.getProfile());
        compartmentLabel.setText("Compartment: " + AuthProvider.getInstance().getCompartmentName());
        regionLabel.setText("Region: " + AuthProvider.getInstance().getRegion().toString());
        compartmentLabel.getParent().requestLayout();
    }

    private void hookMenu() {
        MenuManager menuManager = new MenuManager("#PopupMenu");
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new MenuListener());
        Menu menu = menuManager.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        menuManager.createContextMenu(this);
    }
    private final class MenuListener implements IMenuListener {
        @Override
        public void menuAboutToShow(IMenuManager manager) {
            fillMenu(manager);
        }
    }

    public List<?> getSelectedObjects() {
        IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
        return s.toList();
    }

    public void refresh(boolean updateFromCloud) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!viewer.getTable().isDisposed()) {
                    viewer.getTable().deselectAll();
                    if (updateFromCloud) {
                        // Get the new list from cloud
                        viewer.setInput(getTableData());
                    } else {
                        viewer.setInput(getTableCachedData());
                    }
                    viewer.setItemCount(getTableDataSize());
                    viewer.refresh();
                    updateTableLabels();
                    viewer.getTable().layout();
                }
            }
        });
    }


    protected TableColumn createColumn(TableColumnLayout tableColumnLayout, Table tree, String columnName, int size) {
        TableColumn column = new TableColumn(tree, SWT.NONE);
        column.setMoveable(true);
        column.setText(columnName);
        tableColumnLayout.setColumnData(column, new ColumnWeightData(size));

        return column;
    }

    private final class TableContentProvider implements ILazyContentProvider {

        private TableViewer viewer;
        private List<?> elements;

        public TableContentProvider(TableViewer viewer) {
            this.viewer = viewer;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            this.elements = (List<?>) newInput;
            this.viewer = (TableViewer) viewer;
        }

        @Override
        public void updateElement(int index) {
            try {
                viewer.replace(elements.get(index), index);
            } catch (Exception e) {
                e.getMessage();
            }

        }

    }

}