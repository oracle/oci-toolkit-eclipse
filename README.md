# Oracle Cloud Infrastructure Toolkit for Eclipse

## About

The Oracle Cloud Infrastructure Toolkit for Eclipse is an open source plug-in for the Eclipse Integrated Development Environment (IDE) maintained by Oracle Corp.
The toolkit supports the Compute, Object Storage, Oracle Container Engine for Kubernetes and Autonomous Database services. Users can easily upload/download multiple files, or start and restart the Compute instances from Eclipse. With Autonomous Database service support, users can create ATP/ADW instances, start/stop, terminate, clone, update licence type, change admin password, scale up/down, download client credentials (wallet) and create connection to their Autonomous Databases.
Once the connection is created to the Autonomous Database, users can browse through schema and tables.
It also supports switching between multiple accounts and regions.

## Home page 

The project documentation is [here](https://docs.cloud.oracle.com/iaas/Content/API/SDKDocs/eclipsetoolkit.htm).

## Download and Installation

The toolkit `com.oracle.oci.eclipse.zip` can be downloaded from the [releases section on GitHub](https://github.com/oracle/oci-toolkit-eclipse/releases).

For basic set up and configuration, see [Getting Started](https://docs.cloud.oracle.com/iaas/Content/API/SDKDocs/eclipsegettingstarted.htm).

## Using the Toolkit

For use instructions, see [here](https://docs.cloud.oracle.com/iaas/Content/API/SDKDocs/eclipseusing.htm).

## Help

For details on contributions, questions, or feedback, see [Contact Us](https://docs.cloud.oracle.com/iaas/Content/API/SDKDocs/eclipsetoolkit.htm#ContactUs).

## Changes

See [CHANGELOG](/CHANGELOG.md).

## Contributing

To contribute, see [CONTRIBUTING](/CONTRIBUTING.md) for details.

##### Building the Toolkit

After you clone the repository on GitHub, you will need to build the Toolkit using Maven artifacts that are included with the [OCI Java SDK release](https://github.com/oracle/oci-java-sdk/releases).

1. Change directory to plugins. 
2. Ensure you have set your JAVA_HOME to a 1.8 version of the Java JDK.
3. Run 'mvn -P updatesdk clean process-sources' to copy the needed jar files under plugins/lib.
4. Load the project in Eclipse and verify the classpath and META-INF/MANIFEST file don't have errors. When importing, make sure to only import the single com.oracle.oci.eclipse.plugin
plugin from the plugins directory.  Do not import from the root of the git clone directory.
5. If there are errors then .classpath and/or META-INF/MANIFEST.MF are out of sync with /lib. Do step 5.  If no errors, skip to step 6.
6. In the plugins directory, run 'mvn -P updateClasspath process-sources' to update .classpath and META-INF/MANIFEST.MF.
6a. Recheck the project for errors.
7. Change directory to the parent (the root of the repo) and run 'mvn package'.

## Blog Announcements

- [Announcing Oracle Cloud Infrastructure Toolkit for Eclipse](https://blogs.oracle.com/cloud-infrastructure/announcing-oracle-cloud-infrastructure-toolkit-for-eclipse).

## License

Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.

Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl

See [LICENSE](/LICENSE.txt) for more details.
