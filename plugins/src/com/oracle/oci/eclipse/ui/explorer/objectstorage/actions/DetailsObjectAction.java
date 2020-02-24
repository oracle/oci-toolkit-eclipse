/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.objectstorage.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.responses.HeadObjectResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable.TablePair;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsTable;
public class DetailsObjectAction extends BaseAction {

    private final ObjectsTable bucketObjectTable;
    private final List<ObjectSummary> objectSelectionList;
    private final String bucketName;
    private String objectName;
    private HeadObjectResponse objDetails = null;
    private String title = "Object Details";

    public DetailsObjectAction (ObjectsTable table){
        bucketObjectTable = table;
        objectSelectionList = (List<ObjectSummary>) bucketObjectTable.getSelectedObjects();
        bucketName = bucketObjectTable.getBucketName();
    }

    @Override
    public String getText() {
        if ( objectSelectionList.size() == 1 ) {
            return "Object Details";
        }
        return "";
    }

    @Override
    protected void runAction() {
        if (objectSelectionList.size() > 0) {
            ObjectSummary object = objectSelectionList.get(0);
            objectName = object.getName();
        }
        new Job("Get Object Details") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    objDetails = ObjStorageClient.getInstance().getObjectHeader(bucketName, objectName);

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            List<TablePair> dataList = createDataList(objDetails, objectName);
                            DetailsTable detailsTable= new DetailsTable(title, dataList);
                            detailsTable.openTable();
                        }
                    });

                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to get object details. " + e.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    protected List<TablePair> createDataList(HeadObjectResponse objDetails, String name) {
        List<TablePair> data = new ArrayList<TablePair>();
        data.add(new TablePair("Bucket Name", this.bucketName));
        data.add(new TablePair("Object Name", name));
        data.add(new TablePair("Storage Tier", getArchivalState(objDetails.getArchivalState())));
        data.add(new TablePair("Content Type", objDetails.getContentType()));
        data.add(new TablePair("Content Length", objDetails.getContentLength().toString()));
        data.add(new TablePair("Content MD5 Hash", objDetails.getContentMd5()));
        data.add(new TablePair("ETag", objDetails.getETag()));
        data.add(new TablePair("Last Modified", objDetails.getLastModified().toLocaleString()));
        return data;
    }
    private String getArchivalState(String state) {
        if (state == null)
            return "Standard";
        return state;
    }

}
