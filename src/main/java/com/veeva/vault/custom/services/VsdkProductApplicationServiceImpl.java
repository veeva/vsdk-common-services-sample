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

import com.veeva.vault.custom.userdefinedclasses.VsdkProductApplicationObject;
import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.data.Record;
import com.veeva.vault.sdk.api.data.RecordChange;
import com.veeva.vault.sdk.api.document.DocumentService;
import com.veeva.vault.sdk.api.document.DocumentVersion;
import com.veeva.vault.sdk.api.job.JobItem;
import com.veeva.vault.sdk.api.query.Query;
import com.veeva.vault.sdk.api.query.QueryService;

import java.util.List;
import java.util.Set;

@UserDefinedServiceInfo
public class VsdkProductApplicationServiceImpl implements VsdkProductApplicationService {

    public void createNewProductApplication(List<Record> recordList, String country) {

        VsdkRecordService recordService = ServiceLocator.locate(VsdkRecordService.class);

        List<Record> records = VaultCollections.newList();
        recordList.stream().forEach(record -> {
            List<String> productType = record.getValue("product_type__c", ValueType.PICKLIST_VALUES);

            Record productApplicationRecord = recordService.newRecord("vsdk_product_application__c");
            productApplicationRecord.setValue("product__c", record.getValue("id", ValueType.STRING));
            productApplicationRecord.setValue("country__c", country);

            if (productType.get(0) != null) {
                productApplicationRecord.setValue("product_type__c", productType);
            }
            records.add(productApplicationRecord);

            if (records.size() == 500) {
                recordService.batchSaveRecords(records);
                records.clear();
            }
        });

        if (!records.isEmpty()) {
            recordService.batchSaveRecords(records);
            records.clear();
        }

    }

    public void createProductApplicationDocuments(List<Record> recordList) {

        DocumentService documentService = ServiceLocator.locate(DocumentService.class);

        List<DocumentVersion> documentVersionsToCreate = VaultCollections.newList();

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