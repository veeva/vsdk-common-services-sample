/*
 * --------------------------------------------------------------------
 * UserDefinedClass:	VsdkProductApplicationParam
 * Author:				iskthomas @ Veeva
 * Date:				2023-05-11
 *---------------------------------------------------------------------
 * Description:
 *---------------------------------------------------------------------
 * Copyright (c) 2023 Veeva Systems Inc.  All Rights Reserved.
 *		This code is based on pre-existing content developed and
 * 		owned by Veeva Systems Inc. and may only be used in connection
 *		with the deliverable with which it was provided to Customer.
 *---------------------------------------------------------------------
 */
package com.veeva.vault.custom.params;

import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.data.*;
import com.veeva.vault.sdk.api.job.JobParamValue;
import com.veeva.vault.sdk.api.job.JobParamValueType;

import java.util.List;

@UserDefinedClassInfo()
public class VsdkProductApplicationJobParam implements JobParamValue {

    private List<String> productApplicationIds;

    public List<String> getProductApplicationIds() {
        return productApplicationIds;
    }

    public void setProductApplicationIds(List<String> productApplicationIds) {
        this.productApplicationIds = productApplicationIds;
    }
}