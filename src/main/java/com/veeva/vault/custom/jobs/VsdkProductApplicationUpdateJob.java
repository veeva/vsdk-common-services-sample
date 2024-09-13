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

import com.veeva.vault.custom.params.VsdkProductApplicationJobParam;
import com.veeva.vault.custom.services.VsdkCustomNotificationService;
import com.veeva.vault.custom.userdefinedclasses.VsdkProductApplicationObject;
import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.data.Record;
import com.veeva.vault.sdk.api.data.RecordBatchSaveRequest;
import com.veeva.vault.sdk.api.data.RecordService;
import com.veeva.vault.sdk.api.job.*;
import com.veeva.vault.sdk.api.query.Query;
import com.veeva.vault.sdk.api.query.QueryService;
import com.veeva.vault.sdk.api.token.TokenRequest;
import com.veeva.vault.sdk.api.token.TokenService;

import java.util.List;
import java.util.Set;

@JobInfo(adminConfigurable = true, idempotent = true, isVisible = true)
public class VsdkProductApplicationUpdateJob implements Job {

    public JobInputSupplier init(JobInitContext context) {

        QueryService queryService = ServiceLocator.locate(QueryService.class);
        JobLogger jobLogger = context.getJobLogger();
        TokenService tokenService = ServiceLocator.locate(TokenService.class);

        VsdkProductApplicationJobParam productApplicationParam = context.getJobParameter("product_applications", VsdkProductApplicationJobParam.class);

        TokenRequest productApplicationTokens = tokenService.newTokenRequestBuilder()
                .withValue("Custom.ids", productApplicationParam.getProductApplicationIds())
                .build();

        //Query the Product Application records that were set as input parameters
        jobLogger.log("Querying product applications for parent ids");
        Query productQuery = queryService.newQueryBuilder()
                .withSelect(VaultCollections.asList("id", "product__c"))
                .withFrom("vsdk_product_application__c")
                .withWhere("id contains (${Custom.ids})")
                .build();

        //Create a Product Application User-Defined Class and set the data for each record returned from the query
        List<VsdkProductApplicationObject> productApplicationObjects = VaultCollections.newList();
        List<String> productsToQuery = VaultCollections.newList();
        queryService.query(queryService.newQueryExecutionRequestBuilder()
                .withQuery(productQuery)
                .withTokenRequest(productApplicationTokens)
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

        TokenRequest productTokens = tokenService.newTokenRequestBuilder()
                .withValue("Custom.ids", productsToQuery)
                .build();

        //Query the parent Product records to retrieve the product_type__c field values.
        jobLogger.log("Querying products for product types");
        Query productApplicationQuery = queryService.newQueryBuilder()
                .withSelect(VaultCollections.asList("id", "product_type__c"))
                .withFrom("vsdk_product__c")
                .withWhere("id contains (${Custom.ids})")
                .build();

        queryService.query(queryService.newQueryExecutionRequestBuilder()
                .withTokenRequest(productTokens)
                .withQuery(productApplicationQuery)
                .build()).onSuccess(queryExecutionResponse -> {
                    queryExecutionResponse.streamResults().forEach(queryExecutionResult -> {
                        List<String> productTypePicklist = queryExecutionResult.getValue("product_type__c", ValueType.PICKLIST_VALUES);
                        String productType = null;

                        if (productTypePicklist != null) {
                            productType = productTypePicklist.get(0);
                        }

                        String product = queryExecutionResult.getValue("id", ValueType.STRING);

                        if (productType != null) {
                            String finalProductType = productType;
                            productApplicationObjects.stream().filter(productApplication -> productApplication.getProduct().equals(product))
                                    .forEach(productApplication -> productApplication.setProductType(VaultCollections.asList(finalProductType)));
                        }
                    });
        }).onError(queryOperationError -> {
            throw new RollbackException("QUERY_FAILURE", "Query " + queryOperationError.getQueryString()
                    + " failed because " + queryOperationError.getMessage());
        }).execute();

        //Create a Job Item for each Product Application record and the new product_type__c field value
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

        RecordService recordService = ServiceLocator.locate(RecordService.class);
        JobLogger jobLogger = context.getJobLogger();

        //Update each Product Application record with the new product_type__c field value
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
                RecordBatchSaveRequest saveRequest = recordService.newRecordBatchSaveRequestBuilder().withRecords(records).build();
                recordService.batchSaveRecords(saveRequest)
                        .onErrors(batchOperationErrors ->{
                            batchOperationErrors.stream().findFirst().ifPresent(error -> {
                                String errMsg = error.getError().getMessage();
                                int errPosition = error.getInputPosition();
                                String name = records.get(errPosition).getValue("name__v", ValueType.STRING);
                                throw new RollbackException("OPERATION_NOT_ALLOWED", "Unable to create: " + name +
                                        "because of " + errMsg);
                            });
                        })
                        .execute();

                records.clear();
            }
        });

        if (!records.isEmpty()) {
            RecordBatchSaveRequest saveRequest = recordService.newRecordBatchSaveRequestBuilder().withRecords(records).build();
            recordService.batchSaveRecords(saveRequest)
                    .onErrors(batchOperationErrors ->{
                        batchOperationErrors.stream().findFirst().ifPresent(error -> {
                            String errMsg = error.getError().getMessage();
                            int errPosition = error.getInputPosition();
                            String name = records.get(errPosition).getValue("name__v", ValueType.STRING);
                            throw new RollbackException("OPERATION_NOT_ALLOWED", "Unable to create: " + name +
                                    "because of " + errMsg);
                        });
                    })
                    .execute();

            records.clear();
        }

    }

    //If the job completes successfully then log the result
    public void completeWithSuccess(JobCompletionContext context) {
    }

    //If the job completes with errors, this sends out a notification to the initiating user
    public void completeWithError(JobCompletionContext context) {

        VsdkCustomNotificationService notificationService = ServiceLocator.locate(VsdkCustomNotificationService.class);
        JobLogger jobLogger = context.getJobLogger();

        Set<String> recipients = VaultCollections.newSet();
        recipients.add(RequestContext.get().getInitiatingUserId());

        jobLogger.log("Some Job Tasks have failed. Sending failure notification");
        notificationService.sendProductApplicationJobFailureNotification(context, recipients);

    }
}