# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## 1.3.3
### Added
- Autononmous Database Support
   - User
      - Add APEX instance type.
   - Adopter
      - New Extension point to add actions to DB dashboard.
      - API support for vaults and client identity.
      - Fix for significant race conditions in OCI access singletons.
- Update to OCI Java SDK 2.29.1.   

## 1.3.2
### Added
- Autonomous Database Support
   - New Update Network Access dialog allows configuration of one-way TLS for ADB instances.
   - DB creation wizard auto-generates and locally secure stores new db passwords.
   - New TNS Strings dialog to get ADB connection strings quickly.
   - New copy admin password to clipboard action.
- Update to OCI Java SDK 2.x

## 1.3.1
### Fixed
- Eclipse Preferences to choose any active profile from the configuration even with no DEFAULT profile.
- Eclipse Preferences to reflect appropriate profile when selected from the Preference page.


## 1.3.0
### Added
- Support for Autonomous JSON Database (AJD) Service.
- Support for Database Restart and Change Workload Type operation.

## 1.2.0
### Added
- Support for deploying applications on Oracle Container Engine for Kubernetes. 
  - Upload Docker Images to Oracle Registry (OCIR).
  - Deploy Docker Images to Oracle Kubernetes Engine (OKE).

## 1.1.0
### Added
- Support for Autonomous Database Services (ADW/ATP): 
  - Access to Autonomous Database Services (ADW/ATP). 
  - Create ATP/ADW databases. 
  - Download Client Credentials (Wallet) zip file.
  - Stop/Terminate/Clone/Restore the database. 
  - Scale Up/Down. 
  - Create connection to ATP/ADW database and browse the schema.
  - Choose compartments and regions. 
  - Other operations: Change admin password, Update license type etc. 

- Support for Oracle Container Engine for Kubernetes. Users can view clusters and
download Kubernetes Config files.

## 1.0.0
### Added
- Initial Release
- Support added for Compute Services, Block Storage Service, Object Storage Service
