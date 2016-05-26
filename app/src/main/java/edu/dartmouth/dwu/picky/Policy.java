package edu.dartmouth.dwu.picky;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static final int CONTEXT_NONE = 0;
    public static final int CONTEXT_WIFI_STATE = 1;
    public static final int CONTEXT_WIFI_SSID = 2;
    public static final int CONTEXT_WIFI_NEARBY = 3;
    public static final int CONTEXT_BT_STATE = 4;
    public static final int CONTEXT_BT_CONNECTED_DEVICE = 5;
    public static final int CONTEXT_BT_NEARBY_DEVICE = 6;
    public static final int CONTEXT_LOCATION = 7;
    public static final int CONTEXT_APP_INSTALLED = 8;
    public static final int CONTEXT_APP_RUNNING = 9;
    public static final int CONTEXT_DATE_DAY = 10;

    public static final int CONTEXT_TYPE_INT = 1;
    public static final int CONTEXT_TYPE_STRING = 2;

    public static final int CONTEXT_STATE_ON = 1;
    public static final int CONTEXT_STATE_OFF = 2;

    private static PolicyMessage[] __messages = {
            new PolicyMessage("Camera", "android.permission.CAMERA"),
            new PolicyMessage("Microphone", "android.permission.RECORD_AUDIO"),
            new PolicyMessage("Get Contacts", "android.permission.READ_CONTACTS"),
            new PolicyMessage("Modify Contacts", "android.permission.WRITE_CONTACTS")
    };

    public static ArrayList<PolicyMessage> messages = new ArrayList<PolicyMessage>(Arrays.asList(__messages));

    // Android NDK:
    // http://ph0b.com/new-android-studio-ndk-support/
    // Android Studio->preferences->build,execution,deployment->build tools->gradle->use local gradle distrib
    // download gradle 2.10, ndk tools
    public static native String nativeWriteFilterLine(int action, int uid, String message, String data);
    public static native int nativeWriteContextFilterLine(int action, int uid, String message, String data, int context, int contextType, int contextIntValue);
    public static native int nativeWriteContextFilterLine(int action, int uid, String message, String data, int context, int contextType, String contextStringValue);
    public static native String nativeSetUpPermissions();
    public static native String nativeReadPolicy();
    public static native int nativeInitPolicyPersistFile();

    static {
        System.loadLibrary("picky-jni");
    }

    protected static void setFilterLine(FilterLine filter) {
        if (filter == null || filter.message == null) {
            return;
        }

        printFilterLine(filter);
        String ret = nativeWriteFilterLine(filter.action, filter.uid, filter.message, filter.data);
        //Log.i(TAG, ret);
    }

    protected static void setContextFilterLine(FilterLine filter) {
        if (filter == null || filter.message == null) {
            return;
        }

        printFilterLine(filter);

        int writeLen = -1;
        if (filter.contextType == CONTEXT_TYPE_INT) {
            writeLen = nativeWriteContextFilterLine(filter.action, filter.uid, filter.message,
                    filter.data, filter.context, CONTEXT_TYPE_INT, filter.contextIntValue);
        } else if (filter.contextType == CONTEXT_TYPE_STRING) {
            writeLen = nativeWriteContextFilterLine(filter.action, filter.uid, filter.message,
                    filter.data, filter.context, CONTEXT_TYPE_STRING, filter.contextStringValue);
        } else {
            Log.e(TAG, "Bad context type argument to setContextFilterLine!");
            return;
        }

        Log.i(TAG, "setContextFilterLine: writeLen: " + writeLen);
    }

    protected static void removeContextFilterLine(String stringFilter) {
        if (stringFilter == null) {
            return;
        }

        FilterLine filter = MainActivity.savedRules.get(stringFilter);
        if (filter == null) {
            Log.e(TAG, "removeContextFilterLine: could not get filter for string " + stringFilter);
            return;
        }

        // remove from app memory
        MainActivity.savedRules.remove(stringFilter);
        CustomTabFragment.adapter.notifyDataSetChanged();
        CustomTabFragment.adapter.updateList();

        Log.i(TAG, "removing filter\n");
        printFilterLine(filter);

        int writeLen = -1;

        // flip for remove()
        if (filter.action == Policy.BLOCK_ACTION) {
            filter.action = Policy.UNBLOCK_ACTION;
        } else if (filter.action == Policy.MODIFY_ACTION) {
            filter.action = Policy.UNMODIFY_ACTION;
        }

        // remove from kernel memory
        if (filter.contextType == CONTEXT_TYPE_INT) {
            writeLen = nativeWriteContextFilterLine(filter.action, filter.uid, filter.message,
                    filter.data, filter.context, CONTEXT_TYPE_INT, filter.contextIntValue);
        } else if (filter.contextType == CONTEXT_TYPE_STRING) {
            writeLen = nativeWriteContextFilterLine(filter.action, filter.uid, filter.message,
                    filter.data, filter.context, CONTEXT_TYPE_STRING, filter.contextStringValue);
        } else {
            Log.e(TAG, "Bad context type argument to setContextFilterLine!");
            return;
        }

        Log.i(TAG, "setContextFilterLine: writeLen: " + writeLen);
    }

    public static String translateMessage(int messageType) {
        if (messageType < 0 || messageType >= messages.size()) {
            return null;
        }

        return messages.get(messageType).filterMessage;

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
    // format: message:uid:action:context:context_type:context_val:
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

            FilterLine filter = parsePolicyLine(line);
            if (filter == null) {
                return -1;
            }

            if (filter.context == 0) {
                int messageType = -1;
                for (int i=0; i<messages.size(); i++) {
                    if (filter.message.equals(messages.get(i).filterMessage)) {
                        messageType = i;
                        break;
                    }
                }
                if (messageType == -1) {
                    // if its a message that a user added dynamically, make a new PolicyMessage
                    MainActivity.addNewPolicyMessageToSavedStates(filter.message);
                    messageType = messages.size() - 1;
                }

                MainActivity.savedPolicies.get(messageType).add(filter);
            } else {
                String displayString = ContextActivity.generateContextDisplayString(filter);
                MainActivity.savedRules.put(displayString, filter);
            }

        }
        scanner.close();
        return 0;
    }

    public static FilterLine parsePolicyLine(String line) {
        int endOfMessage = line.indexOf(':');
        String message = line.substring(0, endOfMessage);

        int endOfUid = line.indexOf(':', endOfMessage + 1);
        String uidString = line.substring(endOfMessage + 1, endOfUid);

        int endOfAction = line.indexOf(':', endOfUid + 1);
        String actionString = line.substring(endOfUid + 1, endOfAction);

        int endOfContext = line.indexOf(':', endOfAction + 1);
        String contextString = line.substring(endOfAction + 1, endOfContext);

        int uid = Integer.parseInt(uidString);
        int action = Integer.parseInt(actionString);

        FilterLine filter;
        int context = Integer.parseInt(contextString);
        if (context <= 0) {
            filter = new FilterLine(uid, action, message, "");
        } else {
            int endOfContextType = line.indexOf(':', endOfContext + 1);
            String contextTypeString = line.substring(endOfContext + 1, endOfContextType);
            int contextType = Integer.parseInt(contextTypeString);

            int contextIntValue = 0;
            String contextStringValue = "";

            if (contextType == Policy.CONTEXT_TYPE_INT) {
                int endOfContextValue = line.indexOf(':', endOfContextType + 1);
                String contextIntValueString = line.substring(endOfContextType + 1, endOfContextValue);
                contextIntValue = Integer.parseInt(contextIntValueString);
            } else if (contextType == Policy.CONTEXT_TYPE_STRING) {
                int endOfContextValue = line.indexOf(':', endOfContextType + 1);
                contextStringValue = line.substring(endOfContextType + 1, endOfContextValue);
            } else {
                Log.e(TAG, "loadPolicy: bad context type argument " + contextType);
                return null;
            }

            filter = new FilterLine(uid, action, message, "", context, contextType, contextIntValue, contextStringValue);
        }

        return filter;
    }

    public static void printFilterLine(FilterLine filter) {
        String contextString = "";
        if (filter.context != 0) {
            contextString += ", context: " + filter.context + ", contextType: "
                    + filter.contextType + ", context value: ";
            if (filter.contextType == CONTEXT_TYPE_INT) {
                contextString += filter.contextIntValue;
            } else if (filter.contextType == CONTEXT_TYPE_STRING) {
                contextString += filter.contextStringValue;
            } else {
                contextString += "<INVALID TYPE>";
            }
        }
        Log.i(TAG, "printFilterLine: action: " + filter.action +
                        ", uid: " + filter.uid +
                        ", message: " + filter.message +
                        ", data: " + filter.data +
                        contextString
        );
    }

}
