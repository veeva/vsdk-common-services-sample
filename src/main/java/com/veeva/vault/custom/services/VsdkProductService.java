/*
 * --------------------------------------------------------------------
 * UserDefinedService:	VsdkProduct
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

import java.util.List;

@UserDefinedServiceInfo
public interface VsdkProductService extends UserDefinedService {
    void updateProductApplicationProductType(List<RecordChange> recordChanges);
}