/*
 * --------------------------------------------------------------------
 * UserDefinedClass:	VsdkProductApplicationObject
 * Author:				iskthomas @ Veeva
 * Date:				2022-05-12
 *---------------------------------------------------------------------
 * Description:
 *---------------------------------------------------------------------
 * Copyright (c) 2022 Veeva Systems Inc.  All Rights Reserved.
 *		This code is based on pre-existing content developed and
 * 		owned by Veeva Systems Inc. and may only be used in connection
 *		with the deliverable with which it was provided to Customer.
 *---------------------------------------------------------------------
 */
package com.veeva.vault.custom.userdefinedclasses;

import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.data.*;

import java.util.List;

@UserDefinedClassInfo()
public class VsdkProductApplicationObject {

    private String id;
    private String product;
    private List<String> productType;

    public String getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public List<String> getProductType() {
        return productType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setProductType(List<String> productType) {
        this.productType = productType;
    }
}