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

    // "Dangerous permissions": https://developer.android.com/guide/topics/security/permissions.html#normal-dangerous
    // all permissions: https://developer.android.com/reference/android/Manifest.permission.html
    private static PolicyMessage[] __messages = {
            new PolicyMessage("Camera", "android.permission.CAMERA"),
            new PolicyMessage("Microphone", "android.permission.RECORD_AUDIO"),
            new PolicyMessage("Get Contacts", "android.permission.READ_CONTACTS"),
            new PolicyMessage("Modify Contacts", "android.permission.WRITE_CONTACTS"),
            new PolicyMessage("Get Contact Account Info", "android.permission.GET_ACCOUNTS"),
            new PolicyMessage("Access Fine Location", "android.permission.ACCESS_FINE_LOCATION"),
            new PolicyMessage("Access Coarse Location", "android.permission.ACCESS_COARSE_LOCATION"),
            new PolicyMessage("Read External Storage", "android.permission.READ_EXTERNAL_STORAGE"),
            new PolicyMessage("Write External Storage", "android.permission.WRITE_EXTERNAL_STORAGE"),
            new PolicyMessage("Install Packages", "com.android.vending.INTENT_PACKAGE_INSTALL_COMMIT"),
            new PolicyMessage("Access Internet", "android.permission.INTERNET"),
            new PolicyMessage("Use Overlays and System Alerts (< Android 6.0)", "android.permission.SYSTEM_ALERT_WINDOW"),
            new PolicyMessage("Write System Settings", "android.permission.WRITE_SETTINGS"),

            new PolicyMessage("Read Phone State", "android.permission.READ_PHONE_STATE"),
            new PolicyMessage("Make Phone Call", "android.permission.CALL_PHONE"),
            new PolicyMessage("Read Call Log", "android.permission.READ_CALL_LOG"),
            new PolicyMessage("Write Call Log", "android.permission.WRITE_CALL_LOG"),
            new PolicyMessage("Send SMS", "android.permission.SEND_SMS"),
            new PolicyMessage("Receive SMS", "android.permission.RECEIVE_SMS"),
            new PolicyMessage("Read SMS", "android.permission.READ_SMS"),
            new PolicyMessage("Receive MMS", "android.permission.RECEIVE_MMS"),
            new PolicyMessage("Receive WAP", "android.permission.RECEIVE_WAP_PUSH"),
            new PolicyMessage("Read Calendar", "android.permission.READ_CALENDAR"),
            new PolicyMessage("Write Calendar", "android.permission.WRITE_CALENDAR"),
            new PolicyMessage("Read Body Sensors", "android.permission.BODY_SENSORS"),

            new PolicyMessage("Access Network State", "android.permission.ACCESS_NETWORK_STATE"),
            new PolicyMessage("Change Network State", "android.permission.CHANGE_NETWORK_STATE"),
            new PolicyMessage("Access Wifi State", "android.permission.ACCESS_WIFI_STATE"),
            new PolicyMessage("Change Wifi State", "android.permission.CHANGE_WIFI_STATE"),
            new PolicyMessage("Read Battery Statistics", "android.permission.BATTERY_STATS"),
            new PolicyMessage("Connect to Bluetooth Devices", "android.permission.BLUETOOTH"),
            new PolicyMessage("Discover and Pair to Bluetooth Devices", "android.permission.BLUETOOTH_ADMIN"),
            new PolicyMessage("Use NFC", "android.permission.NFC"),
            new PolicyMessage("Access Flashlight", "android.permission.FLASHLIGHT"),
            new PolicyMessage("Read Browsing History and Bookmarks", "com.android.browser.permission.READ_HISTORY_BOOKMARKS"),
            new PolicyMessage("Transmit Infared", "android.permission.TRANSMIT_IR"),
            new PolicyMessage("Use VoIP", "android.permission.USE_SIP")
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

    public static boolean isInstallPackagesType(int type) {
        String typeOfMessage = Policy.messages.get(type).filterMessage;
        return typeOfMessage.equals("com.android.vending.INTENT_PACKAGE_INSTALL_COMMIT");
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

    public static int setPolicyInfo(boolean blockButton, boolean isChecked, int type, int position, String data) {
        String uniqueAppName = MainActivity.allApps.get(position);
        int uid = MainActivity.nameToUid.get(uniqueAppName);

//        Log.i(TAG, "blockButton: " + Boolean.toString(blockButton) + ", isChecked: " + isChecked + ", type: " +
//                type + ", pos: " + position + ", uid: " + uid);

        int action;
        if (blockButton) {
            if (isChecked) {
                action = Policy.BLOCK_ACTION;
            } else {
                action = Policy.UNBLOCK_ACTION;
            }
        } else {
            if (isChecked) {
                action = Policy.MODIFY_ACTION;
            } else {
                action = Policy.UNMODIFY_ACTION;
                // get saved data string for this filter to remove
                FilterLine toRemove = new FilterLine(uid, Policy.MODIFY_ACTION, Policy.translateMessage(type), data);
                List<FilterLine> savedPolicyForMessageType = MainActivity.savedPolicies.get(type);
                for (FilterLine filter : savedPolicyForMessageType) {
                    // FilterLine equals() ignores data string
                    if (filter.equals(toRemove)) {
                        data = filter.data;
                    }
                }
            }
        }

        if (Policy.isInstallPackagesType(type)) {
            uid = MainActivity.nameToUid.get("Google Play Store");
        }

        FilterLine filter = new FilterLine(uid, action, Policy.translateMessage(type), data);
        Policy.setFilterLine(filter);

        savePolicyInfo(filter, type, isChecked);
        return uid;
    }

    private static void savePolicyInfo(FilterLine filter, int messageType, boolean isChecked) {
        List<FilterLine> savedPolicyForMessageType = MainActivity.savedPolicies.get(messageType);

        if (isChecked) {
            savedPolicyForMessageType.add(filter);
        } else {
            // flip for comparison for remove()
            if (filter.action == Policy.UNBLOCK_ACTION) {
                filter.action = Policy.BLOCK_ACTION;
            } else if (filter.action == Policy.UNMODIFY_ACTION) {
                filter.action = Policy.MODIFY_ACTION;
            }
            savedPolicyForMessageType.remove(filter);
        }
    }
}
