/*
 * --------------------------------------------------------------------
 * UserDefinedService:	VsdkCustomNotification
 * Author:				iskthomas @ Veeva
 * Date:				2022-05-13
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
import com.veeva.vault.sdk.api.job.JobCompletionContext;

import java.util.Set;

@UserDefinedServiceInfo
public interface VsdkCustomNotificationService extends UserDefinedService {
    void sendProductApplicationJobFailureNotification(JobCompletionContext context, Set<String> recipientIds);
}