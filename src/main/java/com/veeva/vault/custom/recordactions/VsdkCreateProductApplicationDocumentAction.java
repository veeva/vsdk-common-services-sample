/*
 * --------------------------------------------------------------------
 * RecordAction:	VsdkCreateProductApplicationDocumentAction
 * Object:			vsdk_product_application__c
 * Author:			iskthomas @ Veeva
 * Date:			2022-05-13
 *---------------------------------------------------------------------
 * Description:
 *---------------------------------------------------------------------
 * Copyright (c) 2022 Veeva Systems Inc.  All Rights Reserved.
 *      This code is based on pre-existing content developed and
 *      owned by Veeva Systems Inc. and may only be used in connection
 *      with the deliverable with which it was provided to Customer.
 *---------------------------------------------------------------------
 */
package com.veeva.vault.custom.recordactions;

import com.veeva.vault.custom.services.VsdkProductApplicationService;
import com.veeva.vault.sdk.api.action.*;
import com.veeva.vault.sdk.api.core.LogService;
import com.veeva.vault.sdk.api.core.ServiceLocator;
import com.veeva.vault.sdk.api.core.ValueType;
import com.veeva.vault.sdk.api.core.VaultCollections;
import com.veeva.vault.sdk.api.query.QueryExecutionRequest;
import com.veeva.vault.sdk.api.query.QueryExecutionResult;
import com.veeva.vault.sdk.api.query.QueryService;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;

@RecordActionInfo(object = "vsdk_product_application__c",
        label = "Create Product Application Documents",
        usages = Usage.USER_BULK_ACTION)
public class VsdkCreateProductApplicationDocumentAction implements RecordAction {

    public boolean isExecutable(RecordActionContext context) {
        return true;
    }

    public void execute(RecordActionContext context) {

        VsdkProductApplicationService productApplicationService = ServiceLocator.locate(VsdkProductApplicationService.class);

        productApplicationService.createProductApplicationDocuments(context.getRecords());

    }
}