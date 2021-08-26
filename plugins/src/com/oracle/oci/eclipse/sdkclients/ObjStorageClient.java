/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.Bucket;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.bmc.objectstorage.model.CreateBucketDetails;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetBucketRequest;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.HeadObjectRequest;
import com.oracle.bmc.objectstorage.requests.ListBucketsRequest;
import com.oracle.bmc.objectstorage.requests.ListBucketsRequest.Builder;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.HeadObjectResponse;
import com.oracle.bmc.objectstorage.responses.ListBucketsResponse;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class ObjStorageClient extends BaseClient {

    private static ObjStorageClient single_instance = null;
    private static String namespaceName;
    private static ObjectStorage objectStorageClient;

    private ObjStorageClient() {
        if (objectStorageClient == null) {
            objectStorageClient = createObjectStorageClient();
        }
    }

    public static ObjStorageClient getInstance() {
        if (single_instance == null) {
            single_instance = new ObjStorageClient();
        }
        return single_instance;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        objectStorageClient.setRegion(evt.getNewValue().toString());
    }

    @Override
    public void updateClient() {
        close();
        createObjectStorageClient();
        namespaceName = "";
    }

    public String getNamespace() {
        return this.getNamespace(getObjectStorageClient());
    }
    
    public void setNamespace(String namespace) {
    	this.namespaceName = namespace;
    }

    public String getNamespace(ObjectStorage objectStorageClient) {
        if(namespaceName == null || namespaceName.isEmpty()) {
            GetNamespaceResponse namespaceResponse =
                    objectStorageClient.getNamespace(GetNamespaceRequest.builder().build());
            namespaceName = namespaceResponse.getValue();
        }
        return namespaceName;
    }

    private ObjectStorage createObjectStorageClient(){
        objectStorageClient = new ObjectStorageClient(AuthProvider.getInstance().getProvider());
        objectStorageClient.setRegion(AuthProvider.getInstance().getRegion());
        return objectStorageClient;
    }

    public ObjectStorage getObjectStorageClient() {
        return objectStorageClient;
    }

    @Override
    public void close() {
        try {
            if (objectStorageClient != null) {
                objectStorageClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }

    public List<BucketSummary> getBuckets() throws Exception {
        String nextToken = null;
        List<BucketSummary> bList = new ArrayList<BucketSummary>();
        String namespace = getNamespace(getObjectStorageClient());
        if (namespace == null) return bList;

        Builder listBucketsBuilder =
                ListBucketsRequest.builder()
                .namespaceName(namespace)
                .compartmentId(AuthProvider.getInstance().getCompartmentId());

        do {
            listBucketsBuilder.page(nextToken);
            try {
                ListBucketsResponse listBucketsResponse =
                        objectStorageClient.listBuckets(listBucketsBuilder.build());
                bList.addAll(listBucketsResponse.getItems());
                nextToken = listBucketsResponse.getOpcNextPage();
            } catch(Throwable e) {
                ErrorHandler.logError("Unable to list buckets: " + e.getMessage());
                return bList;
            }

        } while (nextToken != null);

        return bList;
    }
    public List<BucketSummary> getBucketsinCompartment(String CompartmentId) throws Exception {
        String nextToken = null;
        List<BucketSummary> bList = new ArrayList<BucketSummary>();
        String namespace = getNamespace(getObjectStorageClient());
        if (namespace == null) return bList;

        Builder listBucketsBuilder =
                ListBucketsRequest.builder()
                .namespaceName(namespace)
                .compartmentId(CompartmentId);

        do {
            listBucketsBuilder.page(nextToken);
            try {
                ListBucketsResponse listBucketsResponse =
                        objectStorageClient.listBuckets(listBucketsBuilder.build());
                bList.addAll(listBucketsResponse.getItems());
                nextToken = listBucketsResponse.getOpcNextPage();
            } catch(Throwable e) {
                ErrorHandler.logError("Unable to list buckets: " + e.getMessage());
                return bList;
            }

        } while (nextToken != null);

        return bList;
    }
  
    public List<ObjectSummary> getBucketObjects(String bucket) throws Exception {

        String nextToken = null;
        List<ObjectSummary> bList = new ArrayList<ObjectSummary>();
        do {
            // Get files inside bucket
            com.oracle.bmc.objectstorage.requests.ListObjectsRequest.Builder listObjectsBuilder = ListObjectsRequest
                    .builder().namespaceName(getNamespace(getObjectStorageClient())).bucketName(bucket).fields("timeCreated,size");

            ListObjectsResponse listObjectsResponse = objectStorageClient.listObjects(listObjectsBuilder.build());

            bList.addAll(listObjectsResponse.getListObjects().getObjects());

        } while (nextToken != null);
        ErrorHandler.logInfo("Bucket objects count: " + bList.size());
        return bList;
    }

    public Bucket createBucket(String bucketName) {
        CreateBucketResponse createBucketResponse =
                objectStorageClient.createBucket(
                        CreateBucketRequest.builder()
                        .namespaceName(getNamespace(getObjectStorageClient()))
                        .createBucketDetails(
                                CreateBucketDetails.builder()
                                .name(bucketName)
                                .compartmentId(AuthProvider.getInstance().getCompartmentId())
                                .publicAccessType(
                                        CreateBucketDetails.PublicAccessType
                                        .ObjectRead)
                                .build())
                        .build());
        return createBucketResponse.getBucket();
    }
    
    public Bucket createBucket(String bucketName, String namespace) {
        CreateBucketResponse createBucketResponse =
                objectStorageClient.createBucket(
                        CreateBucketRequest.builder()
                        .namespaceName(namespace)
                        .createBucketDetails(
                                CreateBucketDetails.builder()
                                .name(bucketName)
                                .compartmentId(AuthProvider.getInstance().getCompartmentId())
                                .publicAccessType(
                                        CreateBucketDetails.PublicAccessType
                                        .ObjectRead)
                                .build())
                        .build());
        return createBucketResponse.getBucket();
    }


    public void deleteBucket(String bucketName) throws Exception {
        // Delete all objects inside the bucket before deleting the bucket
        List<ObjectSummary> objectsList = getBucketObjects(bucketName);
        for (ObjectSummary objectSummary : objectsList) {
            deleteObject(bucketName, objectSummary.getName());
        }
        // Delete the bucket
        objectStorageClient.deleteBucket(
                DeleteBucketRequest.builder().bucketName(bucketName).namespaceName(this.getNamespace(objectStorageClient)).build());
        ErrorHandler.logInfo("Bucket deleted: " + bucketName);
    }

    public void deleteObject(String bucketName, String objectName) throws Exception {
        objectStorageClient.deleteObject(
                DeleteObjectRequest.builder().bucketName(bucketName).namespaceName(this.getNamespace(objectStorageClient)).objectName(objectName).build());
        ErrorHandler.logInfo("Object deleted: " + objectName);
    }

    public void downloadObject(String bucketName, String objectName, String downloadPath) throws Exception {

        InputStream inStream = getObjectDetails(bucketName, objectName).getInputStream();

        File targetFile = new File(downloadPath);
        java.nio.file.Files.copy(
                inStream,
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        IOUtils.closeQuietly(inStream);

        ErrorHandler.logInfo("Object download: " + bucketName + " - " + objectName );
    }

    public void uploadObject(String bucketName, File file) throws Exception {

        UploadConfiguration uploadConfiguration =
                UploadConfiguration.builder()
                .allowMultipartUploads(true)
                .allowParallelUploads(true)
                .build();

        UploadManager uploadManager = new UploadManager(objectStorageClient, uploadConfiguration);

        PutObjectRequest request =
                PutObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(namespaceName)
                .objectName(file.getName())
                .build();

        UploadRequest uploadDetails =
                UploadRequest.builder(file).allowOverwrite(true).build(request);

        // upload request and print result
        // if multi-part is used, and any part fails, the entire upload fails and will throw BmcException
        UploadResponse response = uploadManager.upload(uploadDetails);
        ErrorHandler.logInfo(response.toString());

    }

    public GetObjectResponse getObjectDetails(String bucketName, String objectName) throws Exception {

        return objectStorageClient.getObject(
                GetObjectRequest.builder()
                .namespaceName(this.getNamespace(objectStorageClient))
                .bucketName(bucketName)
                .objectName(objectName)
                .build());
    }

    public HeadObjectResponse getObjectHeader(String bucketName, String objectName) throws Exception {

        return objectStorageClient.headObject(
                HeadObjectRequest.builder()
                .namespaceName(this.getNamespace(objectStorageClient))
                .bucketName(bucketName)
                .objectName(objectName)
                .build());
    }

    public GetBucketResponse getBucketDetails(String bucketName) throws Exception {

        return objectStorageClient.getBucket(
                GetBucketRequest.builder()
                .namespaceName(this.getNamespace(objectStorageClient))
                .bucketName(bucketName)
                .build());
    }

}
