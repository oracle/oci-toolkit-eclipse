/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.dragdrop;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.part.PluginTransfer;

import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsTable;

public class ObjectDragSource implements DragSourceListener {

    private final ObjectsTable bucketObjectTable;
    private final TableViewer viewer;
    private final String bucketName;

    public ObjectDragSource(ObjectsTable table, TableViewer viewer) {
        this.viewer = viewer;
        this.bucketName = table.getBucketName();
        this.bucketObjectTable = table;

        DragSource dragSource = new DragSource(viewer.getTable(), DND.DROP_COPY | DND.DROP_DEFAULT | DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] { PluginTransfer.getInstance() });
        dragSource.addDragListener(this);
    }

    @Override
    public void dragStart(DragSourceEvent event) {

    }

    @Override
    public void dragSetData(DragSourceEvent event) {

    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        // TODO Auto-generated method stub

    }

}
