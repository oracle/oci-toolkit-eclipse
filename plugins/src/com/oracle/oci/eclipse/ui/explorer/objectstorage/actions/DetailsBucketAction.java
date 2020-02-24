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

import com.oracle.bmc.objectstorage.model.Bucket;
import com.oracle.bmc.objectstorage.responses.GetBucketResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable.TablePair;
public class DetailsBucketAction extends BaseAction {


    private String bucketName = null;
    private GetBucketResponse objDetails = null;
    private String title = "Object Details";

    public DetailsBucketAction (String bucketName){
        this.bucketName = bucketName;
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    protected void runAction() {
        new Job("Get Bucket Details") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    objDetails = ObjStorageClient.getInstance().getBucketDetails(bucketName);

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            List<TablePair> dataList = createDataList(objDetails);
                            DetailsTable detailsTable= new DetailsTable(title, dataList);
                            detailsTable.openTable();
                        }
                    });

                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to get bucket details. " + e.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    protected List<TablePair> createDataList(GetBucketResponse bucketDetails) {
        Bucket bucket = bucketDetails.getBucket();
        List<TablePair> data = new ArrayList<TablePair>();
        data.add(new TablePair("Bucket Name", this.bucketName));
        data.add(new TablePair("Storage Tier", getArchivalState(bucket.getStorageTier().getValue())));
        data.add(new TablePair("Visibility", bucket.getPublicAccessType().getValue()));
        data.add(new TablePair("Namespace", bucket.getNamespace()));
        data.add(new TablePair("ETag", bucket.getEtag()));
        data.add(new TablePair("Time Created", bucket.getTimeCreated().toLocaleString()));
        return data;
    }
    private String getArchivalState(String state) {
        if (state == null)
            return "Standard";
        return state;
    }

}
