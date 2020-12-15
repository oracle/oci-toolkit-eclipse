/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.ADMINPASSWORD;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.CHANGE_WORKLOAD_TYPE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.CREATECLONE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.CREATECONNECTION;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.DOWNLOAD_CLIENT_CREDENTIALS;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.INSTANCE_WALLET;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.REGIONAL_WALLET;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.RESTART;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.RESTORE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.SCALEUPDOWN;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.SERVICE_CONSOLE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.START;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.STOP;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.TERMINATE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.UPDATELICENCETYPE;
import static com.oracle.oci.eclipse.ui.explorer.database.ADBConstants.UPGRADE_INSTANCE_TO_PAID;

import java.beans.PropertyChangeEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.AutonomousContainerDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.DbWorkload;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary.LifecycleState;
import com.oracle.bmc.database.model.AutonomousDatabaseWallet;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseCloneDetails;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.GenerateAutonomousDatabaseWalletDetails;
import com.oracle.bmc.database.model.GenerateAutonomousDatabaseWalletDetails.GenerateType;
import com.oracle.bmc.database.model.RestoreAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.UpdateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.UpdateAutonomousDatabaseWalletDetails;
import com.oracle.bmc.database.requests.CreateAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.DeleteAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.GenerateAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.requests.GetAutonomousDatabaseRegionalWalletRequest;
import com.oracle.bmc.database.requests.GetAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.requests.ListAutonomousContainerDatabasesRequest;
import com.oracle.bmc.database.requests.ListAutonomousDatabaseBackupsRequest;
import com.oracle.bmc.database.requests.ListAutonomousDatabasesRequest;
import com.oracle.bmc.database.requests.RestartAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.RestoreAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.StartAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.StopAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.UpdateAutonomousDatabaseRegionalWalletRequest;
import com.oracle.bmc.database.requests.UpdateAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.UpdateAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.responses.CreateAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseRegionalWalletResponse;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseWalletResponse;
import com.oracle.bmc.database.responses.ListAutonomousContainerDatabasesResponse;
import com.oracle.bmc.database.responses.ListAutonomousDatabaseBackupsResponse;
import com.oracle.bmc.database.responses.ListAutonomousDatabasesResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.database.ADBConstants;
import com.oracle.oci.eclipse.ui.explorer.database.ChangeAdminPasswordADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.ChangeWorkloadTypeWizard;
import com.oracle.oci.eclipse.ui.explorer.database.CloneADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.CreateADBConnectionWizard;
import com.oracle.oci.eclipse.ui.explorer.database.DownloadADBWalletWizard;
import com.oracle.oci.eclipse.ui.explorer.database.DownloadClientCredentialsWizard;
import com.oracle.oci.eclipse.ui.explorer.database.RestartADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.RestoreADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.RotateWalletWizard;
import com.oracle.oci.eclipse.ui.explorer.database.ScaleUpDownADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.StartADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.StopADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.TerminateADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.UpdateLicenseTypeADBWizard;
import com.oracle.oci.eclipse.ui.explorer.database.UpgradeADBInstanceToPaidWizard;

public class ADBInstanceClient extends BaseClient {

    private static ADBInstanceClient single_instance = null;
    private static DatabaseClient databseClient;
    private static Map<String, ADBInstanceWrapper> instancesMap = new LinkedHashMap<String, ADBInstanceWrapper>();

    private ADBInstanceClient() {
        if (databseClient == null) {
            databseClient = createADBInstanceClient();
        }
    }

    public static ADBInstanceClient getInstance() {
        if (single_instance == null) {
            single_instance = new ADBInstanceClient();
        }
        return single_instance;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        databseClient.setRegion(evt.getNewValue().toString());
    }

    @Override
    public void updateClient() {
        close();
        createADBInstanceClient();
    }

    private DatabaseClient createADBInstanceClient() {
        databseClient = new DatabaseClient(AuthProvider.getInstance().getProvider());
        databseClient.setRegion(AuthProvider.getInstance().getRegion());
        return databseClient;
    }

    @Override
    public void close() {
        try {
            if (databseClient != null) {
                databseClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }

    public void runADBInstanceAction(final AutonomousDatabaseSummary instance, final String action) throws Exception {
        if (databseClient != null) {
            switch (action) {
            case START:
                startInstance(instance);
                break;

            case STOP:
                stopInstance(instance);
                break;

            case SCALEUPDOWN:
                scaleUpDownInstance(instance);
                break;

            case ADMINPASSWORD:
                changeAdminPassword(instance);
                break;

            case UPDATELICENCETYPE:
                updateLicenseType(instance);
                break;

            case CREATECLONE:
                createClone(instance);
                break;

            case TERMINATE:
                terminate(instance);
                break;

            case DOWNLOAD_CLIENT_CREDENTIALS:
                downloadClientCredentials(instance);
                break;

            case CREATECONNECTION:
                createADBConnection(instance);
                break;

            case RESTORE:
                restore(instance);
                break;
                
            case UPGRADE_INSTANCE_TO_PAID:
                upgradeInstanceToPaid(instance);
                break;
                
            case SERVICE_CONSOLE:
                launchServiceConsole(instance);
                break;
                
            case CHANGE_WORKLOAD_TYPE:
                changeWorkloadTypeToOLTP(instance);
                break;
                
            case RESTART:
                restartInstance(instance);
                break;

            }
        }

        ErrorHandler.logInfo("Action: "+action + " InstanceId: " + instance.getDbName());
    }

    public List<AutonomousDatabaseSummary> getInstances(DbWorkload workloadType) throws Exception {
        ListAutonomousDatabasesRequest listInstancesRequest =
                ListAutonomousDatabasesRequest.builder()
                .compartmentId(AuthProvider.getInstance().getCompartmentId())
                .dbWorkload(workloadType)
                .sortBy(ListAutonomousDatabasesRequest.SortBy.Timecreated)
                .sortOrder(ListAutonomousDatabasesRequest.SortOrder.Desc)
                .build();
        List<AutonomousDatabaseSummary> instances = new ArrayList<>();

        if(databseClient == null)
            return instances;

        ListAutonomousDatabasesResponse response = null;
        try {
            response = databseClient.listAutonomousDatabases(listInstancesRequest);
        } catch(Throwable e) {
            // To handle forbidden error
            ErrorHandler.logError("Unable to list Autonomous Databases: "+e.getMessage());
        }

        if (response == null) {
            return instances;
        }

        instances = response.getItems();
        final Iterator<AutonomousDatabaseSummary> it = instances.iterator();
        while (it.hasNext()) {
            AutonomousDatabaseSummary instance = it.next();
            if (LifecycleState.Terminated.equals(instance.getLifecycleState())) {
                it.remove();
            } else {
                instancesMap.put(instance.getId(), new ADBInstanceWrapper(instance));
                ErrorHandler.logInfo("Found ADB instance: " + instance.getDbName());
            }
        }

        return instances;
    }
    
    private void startInstance(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new StartADBWizard(instance));
                dialog.setFinishButtonText("Start");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    public void startInstance(final String instanceId) {
        databseClient
        .startAutonomousDatabase(
                StartAutonomousDatabaseRequest.builder().autonomousDatabaseId(instanceId).build())
        .getAutonomousDatabase();
    }
    
    private void stopInstance(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new StopADBWizard(instance));
                dialog.setFinishButtonText("Stop");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    public void stopInstance(final String instanceId) {
        databseClient
        .stopAutonomousDatabase(
                StopAutonomousDatabaseRequest.builder().autonomousDatabaseId(instanceId).build())
        .getAutonomousDatabase();
    }

    public ADBInstanceWrapper getInstanceDetails(final String instanceId) {
        return instancesMap.get(instanceId);
    }

    private void scaleUpDownInstance(final AutonomousDatabaseSummary instance) {

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new ScaleUpDownADBWizard(instance));
            	dialog.setFinishButtonText("Update");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    public void scaleUpDownInstance(final AutonomousDatabaseSummary instance, int cpuCoreCount,
            int dataStorageSizeInTBs, final Boolean isAutoScalingEnabled) {
        UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
                .cpuCoreCount(cpuCoreCount).dataStorageSizeInTBs(dataStorageSizeInTBs)
                .isAutoScalingEnabled(isAutoScalingEnabled).build();

        databseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
                .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(instance.getId()).build());
    }

    private void changeAdminPassword(final AutonomousDatabaseSummary instance) {

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new ChangeAdminPasswordADBWizard(instance));
                dialog.setFinishButtonText("Update");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    public void changeAdminPassword(final AutonomousDatabaseSummary instance, String password) {
        UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
                .adminPassword(password).build();

        databseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
                .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(instance.getId()).build());
    }

    private void updateLicenseType(final AutonomousDatabaseSummary instance) {

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new UpdateLicenseTypeADBWizard(instance));
                dialog.setFinishButtonText("Update");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    public void updateLicenseType(final AutonomousDatabaseSummary instance,
            final UpdateAutonomousDatabaseDetails.LicenseModel licenseModel) {
        UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
                .licenseModel(licenseModel).build();

        databseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
                .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(instance.getId()).build());
    }

    private void createClone(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new CloneADBWizard(instance));
                dialog.setFinishButtonText("Create");
                if (Window.OK == dialog.open()) {
                }
            }
        });

    }

    public void createClone(CreateAutonomousDatabaseCloneDetails cloneRequest) {
        CreateAutonomousDatabaseResponse response =
                databseClient.createAutonomousDatabase(
                        CreateAutonomousDatabaseRequest.builder()
                        .createAutonomousDatabaseDetails(cloneRequest)
                        .build());

    }

    public void createInstance(final CreateAutonomousDatabaseDetails request) {
        CreateAutonomousDatabaseResponse response =
                databseClient.createAutonomousDatabase(
                        CreateAutonomousDatabaseRequest.builder()
                        .createAutonomousDatabaseDetails(request)
                        .build());
    }

    private void terminate(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new TerminateADBWizard(instance));
                dialog.setFinishButtonText("Terminate");
                if (Window.OK == dialog.open()) {
                }
            }
        });

    }

    public void terminate(final String databaseId) {
        databseClient.deleteAutonomousDatabase(
                DeleteAutonomousDatabaseRequest.builder().autonomousDatabaseId(databaseId).build());
    }
    
    private void downloadClientCredentials(final AutonomousDatabaseSummary instance) {
		// Get the instance and regional wallet details to display last rotation time and lifecycle of wallet
    	final Map<String, AutonomousDatabaseWallet> walletTypeMap = new HashMap<String, AutonomousDatabaseWallet>();
    	if(!(instance.getIsDedicated() != null && instance.getIsDedicated())) {
    		try {
    			final GetAutonomousDatabaseRegionalWalletRequest regionalWalletRequest = 
    					GetAutonomousDatabaseRegionalWalletRequest.builder().build();
    			final GetAutonomousDatabaseRegionalWalletResponse regionalWalletResponse = 
    					databseClient.getAutonomousDatabaseRegionalWallet(regionalWalletRequest);
    			final AutonomousDatabaseWallet regionalWallet = regionalWalletResponse.getAutonomousDatabaseWallet();
    			walletTypeMap.put(REGIONAL_WALLET, regionalWallet);
    			} catch (Throwable e) {
    				ErrorHandler.logError("Unable to get Regional Wallet details");
    			}
        	
        	try {
        		final GetAutonomousDatabaseWalletRequest walletRequest = GetAutonomousDatabaseWalletRequest.builder()
        				.autonomousDatabaseId(instance.getId()).build();
        		final GetAutonomousDatabaseWalletResponse walletResponse = 
        				databseClient.getAutonomousDatabaseWallet(walletRequest);
        		final AutonomousDatabaseWallet instanceWallet = walletResponse.getAutonomousDatabaseWallet();
        		walletTypeMap.put(INSTANCE_WALLET, instanceWallet);
        	} catch (Throwable e) {
        		ErrorHandler.logError("Unable to get Instance Wallet details");
    		}
    	}
    	
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new DownloadClientCredentialsWizard(instance, walletTypeMap));
            	dialog.setVisibleFinishButton(false);
            	dialog.setCancelButtonText("Close");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    public void downloadWallet(final AutonomousDatabaseSummary instance, final String walletType) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new DownloadADBWalletWizard(instance, walletType));
            	dialog.setFinishButtonText("Download");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    public void downloadWallet(final AutonomousDatabaseSummary instance, final String walletType, 
    		final String password, final String walletDirectory) {
    	final GenerateAutonomousDatabaseWalletDetails walletDetails;
    	if((instance.getIsDedicated() != null && instance.getIsDedicated())) {
    		walletDetails = GenerateAutonomousDatabaseWalletDetails.builder()
    				.password(password).build();
    	} else {
    		final GenerateType type = ADBConstants.REGIONAL_WALLET.equalsIgnoreCase(walletType) 
        			? GenerateType.All : GenerateType.Single;
            walletDetails = GenerateAutonomousDatabaseWalletDetails.builder()
                    .password(password).generateType(type).build();
    	}
    	
        GenerateAutonomousDatabaseWalletResponse adbWalletResponse = databseClient.generateAutonomousDatabaseWallet(
                GenerateAutonomousDatabaseWalletRequest.builder()
                .generateAutonomousDatabaseWalletDetails(walletDetails)
                .autonomousDatabaseId(instance.getId())
                .build());

        ZipInputStream zin = new ZipInputStream(adbWalletResponse.getInputStream());

        File file = new File(walletDirectory);
        if (!file.exists()) {
            boolean isDirectoryCreated = file.mkdir();
            if (!isDirectoryCreated) {
                ErrorHandler.logError("Unable to create wallet directory : "+ walletDirectory);
                return;
            }
        } else {
            ErrorHandler.logInfo("Wallet directory already exists : "+ walletDirectory);
            try {
                FileUtils.cleanDirectory(file);
            } catch (IOException e) {
                ErrorHandler.logInfo("Could not clean existing wallet directory : "+ walletDirectory);
            }
        }

        Path outDir = Paths.get(walletDirectory);
        byte[] buffer = new byte[2048];
        ZipEntry entry;
        try {
            while ((entry = zin.getNextEntry()) != null) {

                Path filePath = outDir.resolve(entry.getName());

                try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                        BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {

                    int len;
                    while ((len = zin.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                }
            }
            ErrorHandler.logInfo("Downloaded " + walletType + " for database: "+instance.getDbName());
        } catch (Exception e) {
            ErrorHandler.logErrorStack("Error occured while downloading " + walletType + " for ADB: " + instance.getDbName(), e);
        }
    }
    
    public void rotateWallet(final AutonomousDatabaseSummary instance, final String walletType) {
    	Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new RotateWalletWizard(instance, walletType));
            	dialog.setFinishButtonText("Rotate");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }
    
	public void rotateWallet(final String instanceId, final String walletType) {
		final UpdateAutonomousDatabaseWalletDetails details = UpdateAutonomousDatabaseWalletDetails.builder()
				.shouldRotate(Boolean.TRUE).build();
		if (ADBConstants.REGIONAL_WALLET.equalsIgnoreCase(walletType)) {
			final UpdateAutonomousDatabaseRegionalWalletRequest request = UpdateAutonomousDatabaseRegionalWalletRequest
					.builder().updateAutonomousDatabaseWalletDetails(details).build();
			databseClient.updateAutonomousDatabaseRegionalWallet(request);
		} else if (ADBConstants.INSTANCE_WALLET.equalsIgnoreCase(walletType)) {
			final UpdateAutonomousDatabaseWalletRequest request = UpdateAutonomousDatabaseWalletRequest.builder()
					.updateAutonomousDatabaseWalletDetails(details).autonomousDatabaseId(instanceId).build();
			databseClient.updateAutonomousDatabaseWallet(request);
		} else {
			ErrorHandler.logError("Unknown wallet type selected for rotation");
		}
	}

    private void createADBConnection(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new CreateADBConnectionWizard(instance));
            	dialog.setFinishButtonText("Create");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    public Map<String, String> getContainerDatabaseMap(final String compartmentId) {
        final Map<String, String> containerDBMap = new TreeMap<String, String>();

        if(databseClient == null)
            return containerDBMap;

        ListAutonomousContainerDatabasesRequest request = ListAutonomousContainerDatabasesRequest.builder()
                .compartmentId(compartmentId)
                .lifecycleState(AutonomousContainerDatabaseSummary.LifecycleState.Available)
                .build();
        ListAutonomousContainerDatabasesResponse response = null;
        try {
            response = databseClient.listAutonomousContainerDatabases(request);
        } catch (Throwable e) {
            // To handle forbidden error
            ErrorHandler.logError("Unable to list Container Databases: "+e.getMessage());
        }

        if (response == null)
            return containerDBMap;

        List<AutonomousContainerDatabaseSummary> cdb = response.getItems();
        for (AutonomousContainerDatabaseSummary containerDB : cdb) {
            containerDBMap.put(containerDB.getDisplayName(), containerDB.getId());
        }
        return containerDBMap;
    }

    private void restore(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new RestoreADBWizard(instance, getBackupList(instance)));
                dialog.setFinishButtonText("Restore");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }

    private List<AutonomousDatabaseBackupSummary> getBackupList(final AutonomousDatabaseSummary instance) {
        ListAutonomousDatabaseBackupsResponse response = null;
        try {
            final ListAutonomousDatabaseBackupsRequest request = ListAutonomousDatabaseBackupsRequest.builder()
                    .autonomousDatabaseId(instance.getId())
                    .lifecycleState(AutonomousDatabaseBackupSummary.LifecycleState.Active)
                    .sortBy(ListAutonomousDatabaseBackupsRequest.SortBy.Timecreated)
                    .sortOrder(ListAutonomousDatabaseBackupsRequest.SortOrder.Desc)
                    .build();
            response = databseClient.listAutonomousDatabaseBackups(request);
        } catch(Throwable e) {
            ErrorHandler.logError("Unable to get backup list for Database: "+e.getMessage());
        }
        if (response != null)
            return response.getItems();
        return Collections.emptyList();
    }

    public void restore(final String autonomousDatabaseId, final Date restoreTimestamp) {
        RestoreAutonomousDatabaseDetails restoreDetail = RestoreAutonomousDatabaseDetails.builder()
                .timestamp(restoreTimestamp).build();
        RestoreAutonomousDatabaseRequest request = RestoreAutonomousDatabaseRequest.builder()
                .restoreAutonomousDatabaseDetails(restoreDetail)
                .autonomousDatabaseId(autonomousDatabaseId).build();
        databseClient.restoreAutonomousDatabase(request);
    }
    
    private void upgradeInstanceToPaid(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new UpgradeADBInstanceToPaidWizard(instance));
                dialog.setFinishButtonText("Upgrade");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }
    
    public void upgradeInstanceToPaid(final String instanceId) {
        UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
                .isFreeTier(Boolean.FALSE).build();

        databseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
                .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(instanceId).build());
    }
    
    private void changeWorkloadTypeToOLTP(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new ChangeWorkloadTypeWizard(instance));
                dialog.setFinishButtonText("Change");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }
    
    public void changeWorkloadTypeToOLTP(final String instanceId) {
        UpdateAutonomousDatabaseDetails updateRequest = UpdateAutonomousDatabaseDetails.builder()
                .dbWorkload(UpdateAutonomousDatabaseDetails.DbWorkload.Oltp).build();

        databseClient.updateAutonomousDatabase(UpdateAutonomousDatabaseRequest.builder()
                .updateAutonomousDatabaseDetails(updateRequest).autonomousDatabaseId(instanceId).build());
    }
    
    private void restartInstance(final AutonomousDatabaseSummary instance) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
                        new RestartADBWizard(instance));
                dialog.setFinishButtonText("Restart");
                if (Window.OK == dialog.open()) {
                }
            }
        });
    }
    
    public void restartInstance(final String instanceId) {
        databseClient.restartAutonomousDatabase(
                RestartAutonomousDatabaseRequest.builder().autonomousDatabaseId(instanceId).build());
    }
    
    private void launchServiceConsole(final AutonomousDatabaseSummary instance) {
        Program.launch(instance.getServiceConsoleUrl());

    }

}
