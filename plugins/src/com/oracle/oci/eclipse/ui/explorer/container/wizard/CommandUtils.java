/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.container.wizard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.SystemPropertiesUtils;

public class CommandUtils {

    public static class CommandUtilsInstance {
        public static List<String> getDockerImages() {
            List<String> dockerImagesArr = new ArrayList<>();
            List<String> dockerCommandOutput = executeCommand("docker images");
            for (String item : dockerCommandOutput) {
                StringTokenizer st1 = new StringTokenizer(item);
                int count = 0;
                StringBuilder imageNameVer = new StringBuilder();
                while (st1.hasMoreTokens() && count < 2) {
                    if (count == 1) imageNameVer.append(":");
                    imageNameVer.append(st1.nextToken());
                    count++;
                }
                dockerImagesArr.add(imageNameVer.toString());
            }
            return dockerImagesArr;
        }

        public static IProxyService getProxyService() {
            IProxyService service = null;
            try {
                BundleContext bc = Activator.getDefault().getBundle().getBundleContext();
                ServiceReference serviceReference = bc.getServiceReference(IProxyService.class.getName());
                service = (IProxyService) bc.getService(serviceReference);
            } catch (Exception ex) {
                ErrorHandler.logErrorStack("Proxy Error: ", ex);
            }
            return service;
        }
        public static Map<String, String> getProxy() {
            Map<String, String> outputResult = new HashMap<>();

            IProxyService proxyService = null;
            if (proxyService == null) {
                proxyService = getProxyService();
            }

            if(proxyService.isProxiesEnabled()) {
                IProxyData[] proxyData = proxyService.getProxyData();
                for (IProxyData data : proxyData) {
                    if (IProxyData.HTTP_PROXY_TYPE.equals(data.getType())) {
                        outputResult.put("http_proxy","http://" + data.getHost() + ":" + data.getPort());
                    }
                    else if (IProxyData.HTTPS_PROXY_TYPE.equals(data.getType())) {
                        outputResult.put("https_proxy","http://" + data.getHost() + ":" + data.getPort());
                        ErrorHandler.logInfo("add proxy: " + data.getHost());
                    }
                }
            }
            return outputResult;
        }

        // Run commands
        public static List<String> executeCommand(String command) {
            List<String> outputResult = new ArrayList<>();
            String s = null;
            try {
                String executePath = "";
                if (SystemPropertiesUtils.isMac() || SystemPropertiesUtils.isLinux()) {
                    executePath = "/usr/local/bin/";
                }

                List<String> commandArgs = new ArrayList<>();
                if (SystemPropertiesUtils.isWindows()) {
                    commandArgs.add("cmd.exe");
                    commandArgs.add("/c");
                } else {
                    commandArgs.add("/bin/sh");
                    commandArgs.add("-c");
                }
                commandArgs.add(command);

                ProcessBuilder p = new ProcessBuilder();
                // Set Environment variables
                Map<String, String> env = p.environment();
                env.putAll(getProxy());
                if(!executePath.isEmpty()) {
                    env.put("PATH", executePath + ":" + System.getenv("PATH"));
                }
                // Run commands
                p.command(commandArgs);

                //Get Result
                Process result = p.start();
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(result.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(result.getErrorStream()));

                // read the output from the command
                while ((s = stdInput.readLine()) != null) {
                    outputResult.add(s);
                }

                // read any errors from the attempted command
                while ((s = stdError.readLine()) != null) {
                    outputResult.add(s);
                }
            }
            catch (IOException e) {
                ErrorHandler.reportException("Unable to run command: " + e.getMessage(), e);
                e.printStackTrace();
            }
            return outputResult;
        }

        public static void executeCommandBG(String commandStr, Text outputLabel) {
            new Job("Run shell command") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    StringBuilder commandOutput = new StringBuilder();
                    try {
                        List<String> output = CommandUtilsInstance.executeCommand(commandStr);
                        output = checkCommandOutput(commandStr, output);
                        for (String s : output) {
                            commandOutput.append(s + "\n");
                        }
                    } catch (Exception e) {
                        return ErrorHandler.reportException("Unable to run command: " + e.getMessage(), e);
                    }
                    try {
                        Display.getDefault().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                if(outputLabel != null) {
                                    outputLabel.append("Running: " + commandStr + "\n\n");
                                    outputLabel.append(commandOutput.toString());
                                    outputLabel.append( "-------------------------------------------------------------------------------------------\n");
                                }
                            }
                        });
                    } catch (Exception e) {
                        return ErrorHandler.reportException("Unable to run command: " + e.getMessage(), e);
                    }
                    return Status.OK_STATUS;
                }
            }.schedule();
        }

        public static List<String> checkCommandOutput(String commandStr, List<String> rawOutput) {
            List<String> output = new ArrayList<String>();
            for (String line : rawOutput) {
                if (line.startsWith("Cannot connect to the Docker daemon")) {
                    output.add(line);
                } else if (line.startsWith("WARNING")) {
                    break;
                }
                else {
                    output.add(line);
                }
            }
            return output;
        }
    }
}
