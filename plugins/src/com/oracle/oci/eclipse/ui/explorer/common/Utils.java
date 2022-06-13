package com.oracle.oci.eclipse.ui.explorer.common;

import org.eclipse.jface.layout.AbstractColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;

public class Utils {

    public static void setColumnLayout(TableViewerColumn column, AbstractColumnLayout tableColumnLayout,  int weight) {
        column.getColumn().pack();
        int width = column.getColumn().getWidth();
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(weight, width, true));
    }

    public static TableViewerColumn createColumn(TableViewer parent, String title)
    {
        TableViewerColumn column = new TableViewerColumn(parent, SWT.NONE);
        column.getColumn().setText(title);
        return column;
    }

}
