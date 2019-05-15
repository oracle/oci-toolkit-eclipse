# Oracle Cloud Infrastructure Toolkit for Eclipse

## About

The Oracle Cloud Infrastructure Toolkit for Eclipse is an open source plug-in for the Eclipse Integrated Development Environment (IDE) maintained by Oracle Corp.
The toolkit supports the Compute and Object Storage services. Users can easily upload/download multiple files, or start and restart the instances from Eclipse. 
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

1. Extract the Java SDK release.
2. Copy the dependency JARs from the Java SDK located in `/lib` and `/thirdparty` to the Toolkit under `/plugins/lib`.
3. Verify that the JARs' versions match the library versions listed in `oci-toolkit-eclipse/plugins/META-INF/MANIFEST.MF`.
4. At the command line, run `mvn package`.

## Blog Announcements

- [Announcing Oracle Cloud Infrastructure Toolkit for Eclipse](https://blogs.oracle.com/cloud-infrastructure/announcing-oracle-cloud-infrastructure-toolkit-for-eclipse).

## License

Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.

Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl

See [LICENSE](/LICENSE.txt) for more details.
