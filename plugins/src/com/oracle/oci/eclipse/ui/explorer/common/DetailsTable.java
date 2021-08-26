/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.common;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

public class DetailsTable {
    String title;
    Shell shell;
    Display display;

    public DetailsTable(String title, List<TablePair> data) {
        display =  Display.getDefault();
        shell = new Shell(display);
        this.title = title;

        CreateTable(data);
    }

    public void CreateTable(List<TablePair> data) {
        Clipboard clipboard = new Clipboard(display);
        GridLayout gridLayout = new GridLayout();
        shell.setLayout(gridLayout);
        shell.setText(title);

        /** get the size of the screen */
        Shell parentShell = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell();
        Rectangle bounds = parentShell.getBounds();
        /** get the size of the window */
        Rectangle rect = shell.getBounds();

        /** calculate the centre */
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;

        /** set the new location */
        shell.setLocation(x, y);

        //
        shell.setSize(1000, 1000);
        final ScrolledComposite sc1 = new ScrolledComposite(shell, SWT.H_SCROLL);
        sc1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final Composite c1 = new Composite(sc1, SWT.NONE);
        sc1.setContent(c1);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        c1.setLayout(layout);
        
        GridData gridData = new GridData(GridData.FILL);
        gridData.grabExcessHorizontalSpace = true;

        Label tip = new Label(c1, SWT.FILL);tip.setLayoutData(new GridData());
        tip.setText("Select text to copy.");

        Table table = new Table(c1, SWT.BORDER);table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        c1.setSize(c1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        TableColumn column1 = new TableColumn(table, SWT.NONE);
        TableColumn column2 = new TableColumn(table, SWT.NONE);

        for (TablePair details: data) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { details.getLabel(), details.getValue()});
        }

        column1.pack();
        column2.pack();

        Button closeButton = new Button(shell, SWT.PUSH | SWT.CENTER);
        closeButton.setText("Close");
        closeButton.setLayoutData(gridData);
        closeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1));
        closeButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
                clipboard.dispose();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });

        table.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem [] items = table.getSelection();
                String selectionText = "";
                if (items.length > 0) {
                    selectionText = items[0].getText(1);
                }
                TextTransfer textTransfer = TextTransfer.getInstance();
                clipboard.setContents(new String[]{selectionText}, new Transfer[]{textTransfer});
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }});
        c1.setSize(c1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    public void openTable() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                shell.pack();
                shell.open();

                while (!shell.isDisposed()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            }
        });
    }

    public static class TablePair {
        private String label;
        private String value;

        public TablePair(String label, String value) {
            this.setLabel(label);
            this.setValue(value);
        }
        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}
