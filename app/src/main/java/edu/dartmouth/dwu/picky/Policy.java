package edu.dartmouth.dwu.picky;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by dwu on 5/19/16.
 */
public class Policy {
    private static final String TAG = "Picky";

    public static final int BLOCK_ACTION = 1;
    public static final int UNBLOCK_ACTION = 2;
    public static final int MODIFY_ACTION = 3;
    public static final int UNMODIFY_ACTION = 4;

    public static PolicyMessage[] messages = {
            new PolicyMessage("Camera", "MediaStore.ACTION_IMAGE_CAPTURE"),
            new PolicyMessage("Microphone", "android.permission.RECORD_AUDIO"),
            new PolicyMessage("Get Contacts", "android.permission.READ_CONTACTS"),
            new PolicyMessage("Modify Contacts", "android.permission.WRITE_CONTACTS")
    };

    //http://ph0b.com/new-android-studio-ndk-support/
    // Android Studio->preferences->build,execution,deployment->build tools->gradle->use local gradle distrib
    // download gradle 2.10, ndk tools
    public static native String nativeWriteFilterLine(int action, int uid, String message, String data);
    public static native String nativeSetUpPermissions();
    public static native String nativeReadPolicy();
    public static native int nativeInitPolicyPersistFile();

    static {
        System.loadLibrary("picky-jni");
    }

    public static void setTempFilter(View view) {
        String ret = nativeWriteFilterLine(BLOCK_ACTION, 10091, "com.google.android.location.internal.EXTRA_LOCATION_LIST", "");
        Log.i(TAG, ret);
    }

    public static void setFilterLine(FilterLine filter) {
        if (filter == null || filter.message == null) {
            return;
        }
        Log.i(TAG, "setFilterLine: action: " + filter.action + ", uid: " + filter.uid +
                ", message: " + filter.message + ", data: " + filter.data);

        String ret = nativeWriteFilterLine(filter.action, filter.uid, filter.message, filter.data);
        //Log.i(TAG, ret);
    }

    public static String translateMessage(int messageType) {
        if (messageType < 0 || messageType >= messages.length) {
            return null;
        }

        return messages[messageType].filterMessage;

        //GPS: "com.google.android.location.internal.EXTRA_LOCATION_LIST"
        //battery level: "Intent.ACTION_BATTERY_CHANGED"
        //bt status: "BluetoothAdapter.ACTION_STATE_CHANGED"
    }

    // for some reason calling nativeReadPolicy() twice gets "" on the second time...
    // let's just get the app memory policy for now
    public static String getPolicy() {
        StringBuilder policy = new StringBuilder();

        for (List<FilterLine> savedPolicy : MainActivity.savedPolicies) {
            for (FilterLine filter : savedPolicy) {
                policy.append(filter.message + ':' + filter.uid + ':' + filter.action + ":\n");
            }
        }

        return policy.toString();
    }

    // get policy from driver for this app session
    // format: message:uid:action:
    // returns -1 on error
    public static int loadPolicy(boolean fromDriver, String fromUser) {
        String policy;

        if (fromDriver) {
            policy = nativeReadPolicy();
        } else {
            policy = fromUser;
        }
        Log.i(TAG, "loadPolicy: got policy:\n" + policy);

        if (policy == null || policy.equals("empty")) {
            return 0;
        }

        Scanner scanner = new Scanner(policy);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            int endOfMessage = line.indexOf(':');
            String message = line.substring(0, endOfMessage);

            int endOfUid = line.indexOf(':', endOfMessage + 1);
            String uidString = line.substring(endOfMessage + 1, endOfUid);

            int endOfAction = line.indexOf(':', endOfUid + 1);
            String actionString = line.substring(endOfUid + 1, endOfAction);

            int uid = Integer.parseInt(uidString);
            int action = Integer.parseInt(actionString);
            int messageType = -1;
            for (int i=0; i<messages.length; i++) {
                if (message.equals(messages[i].filterMessage)) {
                    messageType = i;
                    break;
                }
            }

            if (messageType == -1) {
                return -1;
            }

            FilterLine filter = new FilterLine(uid, action, message, "");
            MainActivity.savedPolicies.get(messageType).add(filter);
        }
        scanner.close();
        return 0;
    }

}
