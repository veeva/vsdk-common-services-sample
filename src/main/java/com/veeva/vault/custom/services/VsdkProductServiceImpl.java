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

import com.veeva.vault.custom.params.VsdkProductApplicationJobParam;
import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.data.RecordChange;
import com.veeva.vault.sdk.api.job.JobParameters;
import com.veeva.vault.sdk.api.job.JobService;
import com.veeva.vault.sdk.api.query.Query;
import com.veeva.vault.sdk.api.query.QueryService;
import com.veeva.vault.sdk.api.token.TokenRequest;
import com.veeva.vault.sdk.api.token.TokenService;

import java.util.List;

@UserDefinedServiceInfo
public class VsdkProductServiceImpl implements VsdkProductService {

    public void updateProductApplicationProductType(List<RecordChange> recordChanges) {

        QueryService queryService = ServiceLocator.locate(QueryService.class);
        JobService jobService = ServiceLocator.locate(JobService.class);
        TokenService tokenService = ServiceLocator.locate(TokenService.class);

        List<String> products = VaultCollections.newList();

        //Add the Product record IDs to a list if the product_type__c value has changed
        recordChanges.stream()
                .filter(recordChange -> hasPicklistValueChanged(recordChange, "product_type__c"))
                .forEach(product -> products.add(product.getNew().getValue("id", ValueType.STRING)));

        if (!products.isEmpty()) {

            TokenRequest tokenRequest = tokenService.newTokenRequestBuilder()
                    .withValue("Custom.products", products)
                    .build();

            //Retrieve the Product Application records related to the Product records that were previously filtered
            List<String> productApplicationList = VaultCollections.newList();
            Query productApplicationQuery = queryService.newQueryBuilder()
                    .withSelect(VaultCollections.asList("id"))
                    .withFrom("vsdk_product_application__c")
                    .withWhere("product__c contains ('${Custom.products}')")
                    .build();

            //Add the Product Application record IDs to a list to be used later
            queryService.query(queryService.newQueryExecutionRequestBuilder()
                    .withQuery(productApplicationQuery)
                    .withTokenRequest(tokenRequest)
                    .build()).onSuccess(queryExecutionResponse -> {
                        queryExecutionResponse.streamResults().forEach(queryExecutionResult -> {
                            productApplicationList.add(queryExecutionResult.getValue("id",ValueType.STRING));
                        });
            }).onError(queryOperationError -> {
                throw new RollbackException("QUERY_FAILURE", "Query " + queryOperationError.getQueryString()
                        + " failed because " + queryOperationError.getMessage());
            }).execute();

            if (!productApplicationList.isEmpty()) {

                //Initiate an asynchronous job with the Product Application IDs retrieved above as a parameter
                JobParameters jobParameters = jobService.newJobParameters("vsdk_product_application_update_job__c");

                VsdkProductApplicationJobParam productApplicationParam = new VsdkProductApplicationJobParam();
                productApplicationParam.setProductApplicationIds(productApplicationList);

                jobParameters.setValue("product_applications", productApplicationParam);

                jobService.run(jobParameters);
            }
        }
    }

    private static boolean hasPicklistValueChanged(RecordChange recordChange, String picklistFieldName) {

        List<String> newPicklistValue = recordChange.getNew().getValue(picklistFieldName, ValueType.PICKLIST_VALUES);
        List<String> oldPicklistValue = recordChange.getOld().getValue(picklistFieldName, ValueType.PICKLIST_VALUES);

        boolean hasChanged = false;

        if ((newPicklistValue == null && oldPicklistValue != null) || (newPicklistValue != null && oldPicklistValue == null)) {
            hasChanged = true;
        } else if (newPicklistValue != null && oldPicklistValue != null && !newPicklistValue.get(0).equals(oldPicklistValue.get(0))) {
            hasChanged = true;
        }

        return hasChanged;
    }
}