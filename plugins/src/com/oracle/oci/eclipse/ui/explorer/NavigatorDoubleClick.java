/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.ui.explorer.common.EditorInput;
import com.oracle.oci.eclipse.ui.explorer.compute.BlockStorageElement;
import com.oracle.oci.eclipse.ui.explorer.compute.ComputeInstanceElement;
import com.oracle.oci.eclipse.ui.explorer.compute.editor.InstanceEditor;
import com.oracle.oci.eclipse.ui.explorer.compute.editor.VolumeEditor;
import com.oracle.oci.eclipse.ui.explorer.container.ContainerClustersElement;
import com.oracle.oci.eclipse.ui.explorer.container.editor.ContainerClustersEditor;
import com.oracle.oci.eclipse.ui.explorer.database.ADBRootElement;
import com.oracle.oci.eclipse.ui.explorer.database.editor.ADBInstanceEditor;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowApplicationElement;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowPrivateEndPointsElement;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowRunElement;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.ApplicationEditor;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointEditor;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunEditor;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.editor.ObjectsEditor;

public class NavigatorDoubleClick implements IDoubleClickListener{

    TreeViewer viewer;
    static IEditorPart editorPartCompute = null;
    static IEditorPart editorPartBlockStorage = null;
    static IEditorPart editorPartADBInstance = null;
    static IEditorPart editorPartContainerClusters  = null;
    IEditorPart editorPartBucket = null;
    static IEditorPart editorPartDataflowApplication = null;
    static IEditorPart editorPartDataflowRun = null;
    static IEditorPart editorPartDataflowPrivateEndPoints = null;
    static ConcurrentHashMap<String, IEditorPart> bucketsEditorsMap = new ConcurrentHashMap<String, IEditorPart>();

    public static ConcurrentHashMap<String, IEditorPart> getBucketsEditorsMap() {
        return bucketsEditorsMap;
    }

    public NavigatorDoubleClick(TreeViewer viewer) {
        this.viewer = viewer;
    }

    // Add any new windows here to get closed when switching region or compartment
    public static void closeAllWindows() {
        closeAllComputeWindows();
        closeAllDatabaseWindows();
        closeAllContainerClustersWindows();
        closeAllBucketWindows();
        closeAllDataflowWindows();

    }
    private static void closeAllComputeWindows() {
        closeWindow(editorPartCompute);
        closeWindow(editorPartBlockStorage);
    }

    private static void closeAllDatabaseWindows() {
        closeWindow(editorPartADBInstance);
    }

    private static void closeAllContainerClustersWindows() {
        closeWindow(editorPartContainerClusters);
    }

    public static void closeAllBucketWindows() {
        for(Entry<String, IEditorPart> entry: bucketsEditorsMap.entrySet()) {
            closeBucketWindow(entry.getKey());
        }
    }
    public static void closeBucketWindow(String bucketName) {
        closeWindow(bucketsEditorsMap.get(bucketName));
        bucketsEditorsMap.remove(bucketName);
    }
    
    //
    private static void closeAllDataflowWindows() {
        closeWindow(editorPartDataflowApplication);
        closeWindow(editorPartDataflowRun);
        closeWindow(editorPartDataflowPrivateEndPoints);
    }
    //
    
    public static void closeWindow(IEditorPart currentEditorPart) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    if(currentEditorPart != null &&
                            activeWindow.getActivePage().findEditor(currentEditorPart.getEditorInput()) != null) {
                        activeWindow.getActivePage().closeEditor(currentEditorPart, false);
                    }
                } catch (Exception e) {
                    ErrorHandler.logErrorStack(e.getMessage(), e);
                }

            }
        });
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
        Action doubleClickAction = new Action() {
            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                Object obj = selection.getFirstElement();

                // Object Storage
                if (obj instanceof BucketSummary) {
                    BucketSummary bucket = (BucketSummary) obj;
                    final IEditorInput input = new EditorInput(bucket.getName(), ObjectsEditor.HEADER);
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                // Keep a mapping between open windows IDs and the buckets name
                                editorPartBucket = bucketsEditorsMap.get(bucket.getName());
                                if(editorPartBucket == null ||
                                        activeWindow.getActivePage().findEditor(editorPartBucket.getEditorInput()) == null) {

                                    editorPartBucket = activeWindow.getActivePage().openEditor(input, ObjectsEditor.ID);
                                    bucketsEditorsMap.put(bucket.getName(), editorPartBucket);
                                } else {
                                    activeWindow.getActivePage().activate(editorPartBucket);
                                }
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack(e.getMessage(), e);
                            }
                        }
                    });
                }
                
                // Compute
                if (obj instanceof ComputeInstanceElement) {
                    final IEditorInput input = new EditorInput(InstanceEditor.TITLE, "");
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                // If the window is not created or it is closed, open it
                                // else if the window is already open, activate it
                                if(editorPartCompute == null ||
                                        activeWindow.getActivePage().findEditor(editorPartCompute.getEditorInput()) == null) {
                                    editorPartCompute = activeWindow.getActivePage().openEditor(input, InstanceEditor.ID);
                                } else {
                                    activeWindow.getActivePage().activate(editorPartCompute);
                                }
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack(e.getMessage(), e);
                            }
                        }
                    });
                }
                // Dataflow Application
                if (obj instanceof DataflowApplicationElement) {
                    final IEditorInput input = new EditorInput(ApplicationEditor.TITLE, "");
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                // If the window is not created or it is closed, open it
                                // else if the window is already open, activate it
                                if(editorPartDataflowApplication == null ||
                                        activeWindow.getActivePage().findEditor(editorPartDataflowApplication.getEditorInput()) == null) {
                                    editorPartDataflowApplication = activeWindow.getActivePage().openEditor(input, ApplicationEditor.ID);
                                } else {
                                    activeWindow.getActivePage().activate(editorPartDataflowApplication);
                                }
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack(e.getMessage(), e);
                            }
                        }
                    });
                }
                
                
                // Dataflow PrivateEndPoints
                if (obj instanceof DataflowPrivateEndPointsElement) {
                    final IEditorInput input = new EditorInput(PrivateEndpointEditor.TITLE, "");
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                // If the window is not created or it is closed, open it
                                // else if the window is already open, activate it
                                if(editorPartDataflowPrivateEndPoints == null ||
                                        activeWindow.getActivePage().findEditor(editorPartDataflowPrivateEndPoints.getEditorInput()) == null) {
                                    editorPartDataflowPrivateEndPoints = activeWindow.getActivePage().openEditor(input, PrivateEndpointEditor.ID);
                                } else {
                                    activeWindow.getActivePage().activate(editorPartDataflowPrivateEndPoints);
                                }
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack(e.getMessage(), e);
                            }
                        }
                    });
                }     
                
                // Dataflow Runs
                if (obj instanceof DataflowRunElement) {
                    final IEditorInput input = new EditorInput(RunEditor.TITLE, "");
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                // If the window is not created or it is closed, open it
                                // else if the window is already open, activate it
                                if(editorPartDataflowRun == null ||
                                        activeWindow.getActivePage().findEditor(editorPartDataflowRun.getEditorInput()) == null) {
                                    editorPartDataflowRun = activeWindow.getActivePage().openEditor(input, RunEditor.ID);
                                } else {
                                    activeWindow.getActivePage().activate(editorPartDataflowRun);
                                }
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack(e.getMessage(), e);
                            }
                        }
                    });
                }  
                // Database - ADB instance
                if (obj instanceof ADBRootElement) {
                    final IEditorInput input = new EditorInput(ADBInstanceEditor.TITLE, "");
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                // If the window is not created or it is closed, open it
                                // else if the window is already open, activate it
                                if(editorPartADBInstance == null ||
                                        activeWindow.getActivePage().findEditor(editorPartADBInstance.getEditorInput()) == null) {
                                    editorPartADBInstance = activeWindow.getActivePage().openEditor(input, ADBInstanceEditor.ID);
                                } else {
                                    activeWindow.getActivePage().activate(editorPartADBInstance);
                                }
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack(e.getMessage(), e);
                            }
                        }
                    });
                }

                // BS
                if (obj instanceof BlockStorageElement) {
                    final IEditorInput input = new EditorInput(VolumeEditor.TITLE, "");
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                if(editorPartBlockStorage == null ||
                                        activeWindow.getActivePage().findEditor(editorPartBlockStorage.getEditorInput()) == null) {

                                    editorPartBlockStorage = activeWindow.getActivePage().openEditor(input, VolumeEditor.ID);
                                } else {
                                    activeWindow.getActivePage().activate(editorPartBlockStorage);
                                }
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack(e.getMessage(), e);
                            }
                        }
                    });
                }

                // Container Clusters
                if (obj instanceof ContainerClustersElement) {
                    final IEditorInput input = new EditorInput(ContainerClustersEditor.TITLE, "");
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                if(editorPartContainerClusters == null ||
                                        activeWindow.getActivePage().findEditor(editorPartContainerClusters.getEditorInput()) == null) {

                                    editorPartContainerClusters = activeWindow.getActivePage().openEditor(input, ContainerClustersEditor.ID);
                                } else {
                                    activeWindow.getActivePage().activate(editorPartContainerClusters);
                                }
                            } catch (Exception e) {
                                ErrorHandler.logErrorStack(e.getMessage(), e);
                            }
                        }
                    });
                }
            }
        };
        doubleClickAction.run();
    }
}
