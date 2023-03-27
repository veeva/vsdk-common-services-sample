/*
 * --------------------------------------------------------------------
 * RecordTrigger:	VsdkProductTrigger
 * Object:			vsdk_product__c
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
package com.veeva.vault.custom.recordtriggers;

import com.veeva.vault.custom.services.VsdkProductService;
import com.veeva.vault.sdk.api.core.LogService;
import com.veeva.vault.sdk.api.core.ServiceLocator;
import com.veeva.vault.sdk.api.core.ValueType;
import com.veeva.vault.sdk.api.core.VaultCollectors;
import com.veeva.vault.sdk.api.data.*;
import com.veeva.vault.sdk.api.http.HttpRequestContentType;
import com.veeva.vault.sdk.api.http.HttpService;

import java.util.List;

@RecordTriggerInfo(object = "vsdk_product__c",
        events = {RecordEvent.AFTER_UPDATE})
public class VsdkProductTrigger implements RecordTrigger {

    public void execute(RecordTriggerContext context) {

        VsdkProductService productService = ServiceLocator.locate(VsdkProductService.class);

        productService.updateProductApplicationProductType(context.getRecordChanges());
    }
}