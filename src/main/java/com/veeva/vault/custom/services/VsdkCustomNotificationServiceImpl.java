/*
 * --------------------------------------------------------------------
 * UserDefinedService:	VsdkCustomNotificationService
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

import com.veeva.vault.custom.userdefinedclasses.VsdkProductApplicationObject;
import com.veeva.vault.sdk.api.core.*;
import com.veeva.vault.sdk.api.job.*;
import com.veeva.vault.sdk.api.notification.NotificationParameters;
import com.veeva.vault.sdk.api.notification.NotificationService;
import com.veeva.vault.sdk.api.notification.NotificationTemplate;
import com.veeva.vault.sdk.api.query.Query;

import java.util.List;
import java.util.Set;

@UserDefinedServiceInfo
public class VsdkCustomNotificationServiceImpl implements VsdkCustomNotificationService {

    public void sendProductApplicationJobFailureNotification(JobCompletionContext context, Set<String> recipientIds) {
        NotificationService notificationService = ServiceLocator.locate(NotificationService.class);

        List<String> failedProductApplicationUpdates = VaultCollections.newList();

        //Get the tasks that errored and get the ID of the Product Application record that was not updated successfully
        context.getErrorTasks().forEach(failedJobTask ->
                failedJobTask.getItems().forEach(jobItem ->
                        failedProductApplicationUpdates.add(jobItem.getValue("id", JobValueType.STRING))
                )
        );
        //Set the recipients of the failure notification
        NotificationParameters notificationParameters = notificationService.newNotificationParameters();
        notificationParameters.setRecipientsByUserIds(recipientIds);

        //Set the notification template and send the failure notification
        NotificationTemplate notificationTemplate = notificationService.newNotificationTemplate()
                .setTemplateName("vsdk_product_application_failure__c")
                .setTokenValue("failed_tasks", String.valueOf(context.getJobResult().getNumberFailedTasks()))
                .setTokenValue("number_of_tasks", String.valueOf(context.getJobResult().getNumberTasks()))
                .setTokenValue("product_applications", String.join(",", failedProductApplicationUpdates));

        notificationService.send(notificationParameters, notificationTemplate);
    }
}