/*
 * --------------------------------------------------------------------
 * RecordAction:	VsdkCreateProductApplicationAction
 * Object:			vsdk_product__c
 * Author:			iskthomas @ Veeva
 * Date:			2022-04-29
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
import com.veeva.vault.sdk.api.core.ServiceLocator;
import com.veeva.vault.sdk.api.core.ValueType;

@RecordActionInfo(object = "vsdk_product__c",
        label = "VSDK Create Product Application",
        usages = Usage.UNSPECIFIED,
        user_input_object = "vsdk_create_product_application__c")
public class VsdkCreateProductApplicationAction implements RecordAction {

    public boolean isExecutable(RecordActionContext context) {
        return true;
    }

    public void execute(RecordActionContext context) {

        VsdkProductApplicationService productApplicationService = ServiceLocator.locate(VsdkProductApplicationService.class);

        String country = context.getUserInputRecord().getValue("country__c", ValueType.STRING);

        productApplicationService.createNewProductApplication(context.getRecords(), country);


    }
}