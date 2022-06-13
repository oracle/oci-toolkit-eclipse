package com.oracle.eclipse.oci.sdkclients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.oracle.bmc.objectstorage.model.Bucket;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

import tests.utils.SetupConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ObjStorageClientTest {
    String bucketName = "test_eclipse_bucket";
    String objectName = "test_eclipse_object.txt";
    String projectDir = System.getProperty("user.dir");
    String fileName = projectDir + "/resources/files/" + objectName;
    File file = new File(fileName);

    ObjStorageClient objStorageClient = null;
    @Before
    public void setup() {
        SetupConfig.setProxy();
        SetupConfig.init();

        objStorageClient = ObjStorageClient.getInstance();
    }

    @Test
    public void test1_CreateBucket() throws Exception {
        // this gets stomped on between setup() and here, so set it here.
        AuthProvider.getInstance().updateCompartmentId(SetupConfig.COMPARTMENT_ID);
        Bucket result = objStorageClient.createBucket(bucketName);
        assertEquals(result.getName(),bucketName);
    }

    @Test
    public void test2_GetBuckets() throws Exception {
        List<BucketSummary> result = objStorageClient.getBuckets();
        assertTrue(result.size() > 0);
    }

    @Test
    public void test3_UploadObject() throws Exception {
        objStorageClient.uploadObject(bucketName, file);
    }

    @Test
    public void test4_GetBucketObjects() throws Exception {
        List<ObjectSummary> result;

        result = objStorageClient.getBucketObjects(bucketName);
        assertTrue(result.size() > 0);
    }

    @Test
    public void test5_GetObjectDetails() throws Exception {
        GetObjectResponse result = objStorageClient.getObjectDetails(bucketName, objectName);
        assertTrue(result.getContentLength() > 0);
    }

    @Test
    public void test6_DownloadObject() throws Exception {
        String downloadPath = fileName + "download";

        objStorageClient.downloadObject(bucketName, objectName, downloadPath);
        File downloadedFile = new File(downloadPath);
        assertTrue(downloadedFile.exists());
    }

    @Test
    public void test7_DeleteObject() throws Exception {
        objStorageClient.deleteObject(bucketName, objectName);
    }

    @Test
    public void test8_DeleteBucket() throws Exception {
        objStorageClient.deleteBucket(bucketName);
    }

}