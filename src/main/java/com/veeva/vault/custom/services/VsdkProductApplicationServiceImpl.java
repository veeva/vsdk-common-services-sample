/*
 * --------------------------------------------------------------------
 * UserDefinedService:	VsdkProductApplicationService
 * Author:				iskthomas @ Veeva
 * Date:				2022-04-29
 *---------------------------------------------------------------------
 * Description:
 *---------------------------------------------------------------------
 * Copyright (c) 2022 Veeva Systems Inc.  All Rights Reserved.
 *		This code is based on pre-existing content developed and
 * 		owned by Veeva Systems Inc. and may only be used in connection
 *		with the deliverable with which it was provided to Customer.
 *---------------------------------------------------------------------
 */
package com.veeva.vault.custom.services;

import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.data.Record;
import com.veeva.vault.sdk.api.data.RecordBatchSaveRequest;
import com.veeva.vault.sdk.api.data.RecordService;
import com.veeva.vault.sdk.api.document.DocumentService;
import com.veeva.vault.sdk.api.document.DocumentVersion;

import java.util.List;

@UserDefinedServiceInfo
public class VsdkProductApplicationServiceImpl implements VsdkProductApplicationService {

    public void createNewProductApplication(List<Record> recordList, String country) {

        RecordService recordService = ServiceLocator.locate(RecordService.class);
        LogService logger = ServiceLocator.locate(LogService.class);

        List<Record> records = VaultCollections.newList();
        logger.debug("Number of Product records: {}", recordList.size());

        //Create a new Product Application record and set the product__c and country__c fields based on
        //the Product record values.
        recordList.forEach(record -> {
            List<String> productType = record.getValue("product_type__c", ValueType.PICKLIST_VALUES);

            Record productApplicationRecord = recordService.newRecord("vsdk_product_application__c");
            productApplicationRecord.setValue("product__c", record.getValue("id", ValueType.STRING));
            productApplicationRecord.setValue("country__c", country);

            if (productType != null) {
                productApplicationRecord.setValue("product_type__c", productType);
            }
            records.add(productApplicationRecord);

            //Batch save records based on the RecordService limitations
            if (records.size() == 500) {
                RecordBatchSaveRequest saveRequest = recordService.newRecordBatchSaveRequestBuilder().withRecords(records).build();
                recordService.batchSaveRecords(saveRequest)
                        .onErrors(batchOperationErrors ->{
                            batchOperationErrors.stream().findFirst().ifPresent(error -> {
                                String errMsg = error.getError().getMessage();
                                int errPosition = error.getInputPosition();
                                String name = recordList.get(errPosition).getValue("name__v", ValueType.STRING);
                                throw new RollbackException("OPERATION_NOT_ALLOWED", "Unable to create: " + name +
                                        "because of " + errMsg);
                            });
                        })
                        .execute();

                records.clear();
            }
        });

        //Save any remaining records
        if (!records.isEmpty()) {
            RecordBatchSaveRequest saveRequest = recordService.newRecordBatchSaveRequestBuilder().withRecords(records).build();
            recordService.batchSaveRecords(saveRequest)
                    .onErrors(batchOperationErrors ->{
                        batchOperationErrors.stream().findFirst().ifPresent(error -> {
                            String errMsg = error.getError().getMessage();
                            int errPosition = error.getInputPosition();
                            String name = recordList.get(errPosition).getValue("name__v", ValueType.STRING);
                            throw new RollbackException("OPERATION_NOT_ALLOWED", "Unable to create: " + name +
                                    "because of " + errMsg);
                        });
                    })
                    .execute();

            records.clear();
        }

    }

    public void createProductApplicationDocuments(List<Record> recordList) {

        DocumentService documentService = ServiceLocator.locate(DocumentService.class);

        List<DocumentVersion> documentVersionsToCreate = VaultCollections.newList();

        //Create a Product Application type of document and relate it to the record in the list
        recordList.stream().forEach(record -> {
            DocumentVersion documentVersion = documentService.newDocument();

            documentVersion.setValue("type__v", VaultCollections.asList("VSDK Product Application Form"));
            documentVersion.setValue("lifecycle__v", VaultCollections.asList("General Lifecycle"));
            documentVersion.setValue("product__c", VaultCollections.asList(record.getValue("product__c", ValueType.STRING)));
            documentVersion.setValue("product_application__c", VaultCollections.asList(record.getValue("id", ValueType.STRING)));

            documentVersionsToCreate.add(documentVersion);

            if (documentVersionsToCreate.size() == 500) {
                documentService.createDocuments(documentVersionsToCreate);
                documentVersionsToCreate.clear();
            }
        });

        if (!documentVersionsToCreate.isEmpty()) {
            documentService.createDocuments(documentVersionsToCreate);
            documentVersionsToCreate.clear();
        }
    }
}