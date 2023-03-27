/*
 * --------------------------------------------------------------------
 * Job Info:	VsdkProductApplicationUpdateJob
 * Author:			iskthomas @ Veeva
 * Date:			2022-04-29
 *---------------------------------------------------------------------
 * Description:
 *---------------------------------------------------------------------
 * Copyright (c) 2022 Veeva Systems Inc.  All Rights Reserved.
 *		This code is based on pre-existing content developed and
 *		owned by Veeva Systems Inc. and may only be used in connection
 *		with the deliverable with which it was provided to Customer.
 *---------------------------------------------------------------------
 */
package com.veeva.vault.custom.jobs;

import com.veeva.vault.custom.services.VsdkCustomNotificationService;
import com.veeva.vault.custom.services.VsdkRecordService;
import com.veeva.vault.custom.userdefinedclasses.VsdkProductApplicationObject;
import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.data.Record;
import com.veeva.vault.sdk.api.job.*;
import com.veeva.vault.sdk.api.query.Query;
import com.veeva.vault.sdk.api.query.QueryService;

import java.util.List;
import java.util.Set;

@JobInfo(adminConfigurable = true, idempotent = true, isVisible = true)
public class VsdkProductApplicationUpdateJob implements Job {

    public JobInputSupplier init(JobInitContext context) {

        QueryService queryService = ServiceLocator.locate(QueryService.class);
        JobLogger jobLogger = context.getJobLogger();

        String productApplications = context.getJobParameter("product_application_ids", JobParamValueType.STRING.STRING);

        jobLogger.log("Querying product applications for parent ids");
        Query productQuery = queryService.newQueryBuilder()
                .withSelect(VaultCollections.asList("id", "product__c"))
                .withFrom("vsdk_product_application__c")
                .withWhere("id contains ('" + productApplications + "')")
                .build();

        List<VsdkProductApplicationObject> productApplicationObjects = VaultCollections.newList();
        List<String> productsToQuery = VaultCollections.newList();
        queryService.query(queryService.newQueryExecutionRequestBuilder()
                .withQuery(productQuery)
                .build()).onSuccess(queryExecutionResponse -> {
                    queryExecutionResponse.streamResults().forEach(queryExecutionResult -> {
                        String product = queryExecutionResult.getValue("product__c", ValueType.STRING);
                        VsdkProductApplicationObject productApplication = new VsdkProductApplicationObject();
                        productApplication.setId(queryExecutionResult.getValue("id", ValueType.STRING));
                        productApplication.setProduct(product);
                        productApplicationObjects.add(productApplication);
                        productsToQuery.add(product);
                    });
        }).onError(queryOperationError -> {
            throw new RollbackException("QUERY_FAILURE", "Query " + queryOperationError.getQueryString()
                    + " failed because " + queryOperationError.getMessage());
        }).execute();

        jobLogger.log("Querying products for product types");
        Query productApplicationQuery = queryService.newQueryBuilder()
                .withSelect(VaultCollections.asList("id", "product_type__c"))
                .withFrom("vsdk_product__c")
                .withWhere("id contains ('" + String.join("','", productsToQuery) + "')")
                .build();

        queryService.query(queryService.newQueryExecutionRequestBuilder()
                .withQuery(productApplicationQuery)
                .build()).onSuccess(queryExecutionResponse -> {
                    queryExecutionResponse.streamResults().forEach(queryExecutionResult -> {
                        List<String> productType = queryExecutionResult.getValue("product_type__c", ValueType.PICKLIST_VALUES);
                        String product = queryExecutionResult.getValue("id", ValueType.STRING);

                        if (productType != null) {
                            productApplicationObjects.stream().filter(productApplication -> productApplication.getProduct().equals(product))
                                    .forEach(productApplication -> productApplication.setProductType(productType));
                        }
                    });
        }).onError(queryOperationError -> {
            throw new RollbackException("QUERY_FAILURE", "Query " + queryOperationError.getQueryString()
                    + " failed because " + queryOperationError.getMessage());
        }).execute();

        jobLogger.log("Setting Job Items");
        List<JobItem> jobItems = VaultCollections.newList();
        productApplicationObjects.stream().forEach(productApplication -> {
            List<String> productType = productApplication.getProductType();
            if (productType != null) {
                JobItem jobItem = context.newJobItem();
                jobItem.setValue("id", productApplication.getId());
                jobItem.setValue("product_type", String.join(",", productApplication.getProductType()));
                jobItems.add(jobItem);
            }
        });
        return context.newJobInput(jobItems);
    }

    public void process(JobProcessContext context) {

        VsdkRecordService recordService = ServiceLocator.locate(VsdkRecordService.class);
        JobLogger jobLogger = context.getJobLogger();

        jobLogger.log("Updating VSDK Product Application records");
        List<Record> records = VaultCollections.newList();
        context.getCurrentTask().getItems().stream().forEach(jobItem -> {
            Record record = recordService.newRecordWithId("vsdk_product_application__c",
                    jobItem.getValue("id", JobValueType.STRING));
            record.setValue("product_type__c", VaultCollections.asList(
                    StringUtils.split(jobItem.getValue("product_type", JobValueType.STRING), ",")
            ));
            records.add(record);

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

    public void completeWithSuccess(JobCompletionContext context) {

        LogService logger = ServiceLocator.locate(LogService.class);
        logger.debug("All tasks completed successfully");

    }

    public void completeWithError(JobCompletionContext context) {

        VsdkCustomNotificationService notificationService = ServiceLocator.locate(VsdkCustomNotificationService.class);
        JobLogger jobLogger = context.getJobLogger();

        Set<String> recipients = VaultCollections.newSet();
        recipients.add("10692823");

        jobLogger.log("Some Job Tasks have failed. Sending failure notification");
        notificationService.sendProductApplicationJobFailureNotification(context, recipients);

    }
}