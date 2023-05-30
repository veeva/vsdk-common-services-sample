# Vault Java SDK Common Services Example

This project provides examples of how to implement commonly used Vault Java SDK services and contains code written to follow Veeva's best practices.

## Maven Plugin

This project uses the Vault Java SDK Maven Plugin to package, validate, import, and deploy Vault Java SDK source code using Maven build goals. Learn more in the [Vault Java SDK Maven Plugin repository](https://github.com/veeva/vaultjavasdk-maven-plugin).

## Overview

Entry point interfaces define how and when Vault executes custom logic, while services interfaces provide getter and setter methods that allow entry point implementations to interact with operations and data in Vault. This project includes implementations of the Vault Java SDK `Job`, `RecordAction`, and `RecordTrigger` entry point interfaces as well as four custom services. Additionally, the project includes a user-defined class (UDC) to get and set *Product* record data.

### Services

This project uses Veeva-recommended design patterns by defining business logic in user-defined services (UDS) to manage runtime memory and provide shared code to SDK triggers, actions, and processors. Learn more about UDS in the [Vault Developer Portal](https://developer.veevavault.com/sdk/#User_Defined_Services).

![Project Structure Flowchart](/vault-object-record-sdk-example-chart.png)

The UDS in this project import methods from the following Vault Java SDK services:

- **Record Service** to access record information and insert/update object records.
- **Document Service** to access document information and insert/update document records.
- **Query Service** to use and process query information returned from a VQL query.
- **Job Service** to initiate and offload data to an asynchronous job.
- **Notification Service** to build and send a notification to predefined users.
- **Log Service** to include debug, error, warning, and info messages in the SDK debug and runtime logs.

### Entry Points

This project applies services logic to Vault data through implementations of the following entry point interfaces:

  - **Record Trigger** to execute `VsdkProductService` logic after a user updates a `vsdk_product__c` object record.
  - **Record Action** to execute methods from `VsdkProductApplicationService` on an object record when invoked by a user through the Vault UI or API. This project provides two record actions, each of which executes a different method from `VsdkProductApplicationService`.
  - **Job** to create an asynchronous job that applies `VsdkCustomNotificationService` and `VsdkRecordService` logic to an object record.

### User Defined Class

The `VsdkProductApplicationObject` provides methods to get and set field values on `vsdk_product__c` records. Learn more about user-defined classes (UDC) in the [Vault Developer Portal](https://developer.veevavault.com/sdk/#User_Defined_Classes).

## Setup

Deploy the [VPK](https://developer.veevavault.com/sdk/#Deploy_VPK) included within the project. This package contains the configuration and code for this project.
To make changes to the code and deploy those changes to a Vault, use the [Vault Java SDK Maven Plugin](https://github.com/veeva/vaultjavasdk-maven-plugin). The pom.xml file includes the plugin details, but you must update the configuration values.

##Veeva Connect
Further discussion about Vault Java SDK can be held in our [Vault for Developers community](https://devcommunity.veevavault.com) on Veeva Connect.

