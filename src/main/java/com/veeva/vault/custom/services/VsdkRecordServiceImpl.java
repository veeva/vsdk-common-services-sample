/*
 * --------------------------------------------------------------------
 * UserDefinedService:	VsdkRecordService
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
import com.veeva.vault.sdk.api.data.RecordService;

import java.util.List;

@UserDefinedServiceInfo
public class VsdkRecordServiceImpl implements VsdkRecordService {

    public Record newRecord(String objectName) {
        RecordService recordService = ServiceLocator.locate(RecordService.class);

        return recordService.newRecord(objectName);
    }

    public Record newRecordWithId(String objectName, String recordId) {
        RecordService recordService = ServiceLocator.locate(RecordService.class);

        return recordService.newRecordWithId(objectName, recordId);
    }

    public void batchSaveRecords(List<Record> recordList) {
        RecordService recordService = ServiceLocator.locate(RecordService.class);

        recordService.batchSaveRecords(recordList)
                .onErrors(batchOperationErrors ->{
                    batchOperationErrors.stream().findFirst().ifPresent(error -> {
                        String errMsg = error.getError().getMessage();
                        int errPosition = error.getInputPosition();
                        String name = recordList.get(errPosition).getValue("name__v", ValueType.STRING);
                        throw new RollbackException("OPERATION_NOT_ALLOWED", "Unable to create: " + name +
                                "because of " + errMsg);
                    });
                })
                .execute();


    }
}