/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.telecomm.testapps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telecomm.PhoneAccount;
import android.telecomm.TelecommConstants;
import android.telecomm.VideoCallProfile;
import android.util.Log;

/**
 * Class used to create, update and cancel the notification used to display and update call state
 * for {@link TestConnectionService}.
 */
public class CallServiceNotifier {
    private static final CallServiceNotifier INSTANCE = new CallServiceNotifier();

    /**
     * Static notification id.
     */
    private static final int CALL_NOTIFICATION_ID = 1;

    /**
     * Whether the added call should be started as a video call. Referenced by
     * {@link TestConnectionService} to know whether to provide a call video provider.
     */
    public static boolean mStartVideoCall;

    /**
     * Singleton accessor.
     */
    public static CallServiceNotifier getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a CallService & initializes notification manager.
     */
    private CallServiceNotifier() {
    }

    /**
     * Updates the notification in the notification pane.
     */
    public void updateNotification(Context context) {
        log("adding the notification ------------");
        getNotificationManager(context).notify(CALL_NOTIFICATION_ID, getNotification(context));
    }

    /**
     * Cancels the notification.
     */
    public void cancelNotification(Context context) {
        log("canceling notification");
        getNotificationManager(context).cancel(CALL_NOTIFICATION_ID);
    }

    /**
     * Returns the system's notification manager needed to add/remove notifications.
     */
    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Creates a notification object out of the current calls state.
     */
    private Notification getNotification(Context context) {
        final Notification.Builder builder = new Notification.Builder(context);
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_HIGH);

        final PendingIntent intent = createIncomingCallIntent(context, false /* isVideoCall */);
        builder.setContentIntent(intent);

        builder.setSmallIcon(android.R.drawable.stat_sys_phone_call);
        builder.setContentText("Test calls via CallService API");
        builder.setContentTitle("TestConnectionService");

        addAddCallAction(builder, context);
        addAddVideoCallAction(builder, context);
        addExitAction(builder, context);

        return builder.build();
    }

    /**
     * Creates the intent to remove the notification.
     */
    private PendingIntent createExitIntent(Context context) {
        final Intent intent = new Intent(CallNotificationReceiver.ACTION_CALL_SERVICE_EXIT, null,
                context, CallNotificationReceiver.class);

        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    /**
     * Creates the intent to add an incoming call through Telecomm.
     */
    private PendingIntent createIncomingCallIntent(Context context, boolean isVideoCall) {
        log("Creating incoming call pending intent.");

        // Create intent for adding an incoming call.
        Intent intent = new Intent(TelecommConstants.ACTION_INCOMING_CALL);
        // TODO(santoscordon): Use a private @hide permission to make sure this only goes to
        // Telecomm instead of setting the package explicitly.
        intent.setPackage("com.android.telecomm");

        PhoneAccount phoneAccount = new PhoneAccount(
                new ComponentName(context, TestConnectionService.class),
                null /* id */,
                null /* handle */,
                PhoneAccount.CAPABILITY_CALL_PROVIDER);
        intent.putExtra(TelecommConstants.EXTRA_PHONE_ACCOUNT, phoneAccount);

        mStartVideoCall = isVideoCall;

        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    /**
     * Adds an action to the Notification Builder for adding an incoming call through Telecomm.
     * @param builder The Notification Builder.
     */
    private void addAddCallAction(Notification.Builder builder, Context context) {
        // Set pending intent on the notification builder.
        builder.addAction(0, "Add Call", createIncomingCallIntent(context, false /* isVideoCall */));
    }

    /**
     * Adds an action to the Notification Builder to add an incoming video call through Telecomm.
     */
    private void addAddVideoCallAction(Notification.Builder builder, Context context) {
        builder.addAction(0, "Add Video", createIncomingCallIntent(context, true /* isVideoCall */));
    }

    /**
     * Adds an action to remove the notification.
     */
    private void addExitAction(Notification.Builder builder, Context context) {
        builder.addAction(0, "Exit", createExitIntent(context));
    }

    public boolean shouldStartVideoCall() {
        return mStartVideoCall;
    }

    private static void log(String msg) {
        Log.w("testcallservice", "[CallServiceNotifier] " + msg);
    }
}
