/*
 * --------------------------------------------------------------------
 * UserDefinedService:	VsdkRecord
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
import com.veeva.vault.sdk.api.data.Record;

import java.util.List;

@UserDefinedServiceInfo
public interface VsdkRecordService extends UserDefinedService {
    Record newRecord(String objectName);
    Record newRecordWithId(String objectName, String recordId);
    void batchSaveRecords(List<Record> recordList);
}