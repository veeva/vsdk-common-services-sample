/*
 * --------------------------------------------------------------------
 * UserDefinedService:	VsdkProductService
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
import com.veeva.vault.sdk.api.data.RecordChange;
import com.veeva.vault.sdk.api.job.Job;
import com.veeva.vault.sdk.api.job.JobParameters;
import com.veeva.vault.sdk.api.job.JobService;
import com.veeva.vault.sdk.api.query.Query;
import com.veeva.vault.sdk.api.query.QueryService;

import java.util.List;

@UserDefinedServiceInfo
public class VsdkProductServiceImpl implements VsdkProductService {

    public void updateProductApplicationProductType(List<RecordChange> recordChanges) {

        QueryService queryService = ServiceLocator.locate(QueryService.class);
        JobService jobService = ServiceLocator.locate(JobService.class);

        List<String> products = VaultCollections.newList();

        recordChanges.stream()
                .filter(recordChange -> !recordChange.getNew().getValue("product_type__c", ValueType.PICKLIST_VALUES).get(0)
                        .equals(recordChange.getOld().getValue("product_type__c", ValueType.PICKLIST_VALUES).get(0)))
                .forEach(product -> products.add(product.getNew().getValue("id", ValueType.STRING)));

        if (!products.isEmpty()) {
            String productString = String.join("','", products);

            List<String> productApplicationList = VaultCollections.newList();
            Query productApplicationQuery = queryService.newQueryBuilder()
                    .withSelect(VaultCollections.asList("id"))
                    .withFrom("vsdk_product_application__c")
                    .withWhere("product__c contains ('" + productString + "')")
                    .build();

            queryService.query(queryService.newQueryExecutionRequestBuilder()
                    .withQuery(productApplicationQuery)
                    .build()).onSuccess(queryExecutionResponse -> {
                        queryExecutionResponse.streamResults().forEach(queryExecutionResult -> {
                            productApplicationList.add(queryExecutionResult.getValue("id",ValueType.STRING));

                            if (productApplicationList.size() == 500) {
                                JobParameters jobParameters = jobService.newJobParameters("vsdk_product_application_update_job");

                                //jobParameters.setValue("product_ids", String.join("','", products));
                                jobParameters.setValue("product_application_ids", String.join("','", productApplicationList));

                                jobService.run(jobParameters);

                                productApplicationList.clear();
                            }
                        });
            }).onError(queryOperationError -> {
                throw new RollbackException("QUERY_FAILURE", "Query " + queryOperationError.getQueryString()
                        + " failed because " + queryOperationError.getMessage());
            }).execute();

            if (!productApplicationList.isEmpty()) {
                JobParameters jobParameters = jobService.newJobParameters("vsdk_product_application_update_job__c");

                jobParameters.setValue("product_application_ids", String.join("','", productApplicationList));

                jobService.run(jobParameters);

                productApplicationList.clear();
            }
        }
    }
}