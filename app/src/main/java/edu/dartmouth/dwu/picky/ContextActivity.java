package edu.dartmouth.dwu.picky;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContextActivity extends AppCompatActivity {
    private static final String TAG = "Picky";

    private static final String contextValueDefault = "Type";
    private static final String contextMatchesDefault = "Comparator";
    private static final String contextMatchesValueDefault = "Value";
    private static final String actionMessageDefault = "Value";
    private static final String actionUidDefault = "App";
    private static final String actionActionDefault = "Action";

    private Spinner sContextValue, sContextMatches, sContextMatchesValue;
    private final String[] contextValuesArray = {contextValueDefault, "Wifi state", "Wifi ssid", "Bluetooth state", "App running"};
    private final String[] contextMatchesArray = {contextMatchesDefault, "Status", "Matches"};
    private final String[] contextMatchesValueArray = {contextMatchesValueDefault, "On", "Off", "Enter value", "Pick app"};

    private Spinner sActionMessage, sActionUid, sActionAction;
    private ArrayList<String> actionMessagesArray = new ArrayList<>();
    private ArrayList<String> actionUidsArray = new ArrayList<>();
    //private String[] actionActionsArray = {actionActionDefault, "Block", "Unblock", "Modify", "Unmodify"};
    private final String[] actionActionsArray = {actionActionDefault, "Block", "Modify"};

    private ArrayAdapter<String> contextDataAdapter;
    private ArrayAdapter<String> actionMessageAdapter;
    private ArrayAdapter<String> apps;
    private final int positionOfCustomValue = 3;
    private final int positionOfCustomAppValue = 4;
    private int positionOfCustomAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRule();
            }
        });
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryDark));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        addSpinnerItems();
    }

    public void addSpinnerItems() {
        sContextValue = (Spinner) findViewById(R.id.spinnerContextValue);
        sContextMatches = (Spinner) findViewById(R.id.spinnerContextMatches);
        sContextMatchesValue = (Spinner) findViewById(R.id.spinnerContextMatchesValue);
        sActionMessage = (Spinner) findViewById(R.id.spinnerMessage);
        sActionUid = (Spinner) findViewById(R.id.spinnerUid);
        sActionAction = (Spinner) findViewById(R.id.spinnerAction);

        // context values
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, contextValuesArray);
        dataAdapter.setDropDownViewResource(R.layout.spiner_tv_layout);
        sContextValue.setAdapter(dataAdapter);

        // context matches
        ArrayAdapter<String> dataAdapter2  = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, contextMatchesArray);
        dataAdapter2.setDropDownViewResource(R.layout.spiner_tv_layout);
        sContextMatches.setAdapter(dataAdapter2);

        // context matches values
        contextDataAdapter = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, contextMatchesValueArray);
        contextDataAdapter.setDropDownViewResource(R.layout.spiner_tv_layout);
        sContextMatchesValue.setAdapter(contextDataAdapter);

        // action messages
        actionMessagesArray.add(actionMessageDefault);
        for (PolicyMessage pm : Policy.messages) {
            actionMessagesArray.add(pm.displayMessage);
        }
        actionMessagesArray.add("Enter value");
        positionOfCustomAction = actionMessagesArray.size() - 1;
        actionMessageAdapter = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, actionMessagesArray);
        actionMessageAdapter.setDropDownViewResource(R.layout.spiner_tv_layout);
        sActionMessage.setAdapter(actionMessageAdapter);

        // action uids
        actionUidsArray.add(actionUidDefault);
        actionUidsArray.addAll(MainActivity.allApps);
        apps = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, actionUidsArray);
        apps.setDropDownViewResource(R.layout.spiner_tv_layout);
        sActionUid.setAdapter(apps);

        // action actions
        ArrayAdapter<String> dataAdapter6 = new ArrayAdapter<>(this,
                R.layout.spiner_tv_layout, actionActionsArray);
        dataAdapter6.setDropDownViewResource(R.layout.spiner_tv_layout);
        sActionAction.setAdapter(dataAdapter6);

        // custom string listener
        sContextMatchesValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == positionOfCustomValue) {
                    promptUserForString(true);
                } else if (position == positionOfCustomAppValue) {
                    promptUserForApp();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // custom action value
        sActionMessage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == positionOfCustomAction) {
                    promptUserForString(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void promptUserForApp() {
        AlertDialog.Builder appDialog = new AlertDialog.Builder(this);

        final Spinner appsSpinner = new Spinner(this);
        appsSpinner.setAdapter(apps);
        appsSpinner.setGravity(Gravity.CENTER);
        appsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                position -= 1;  // starts with the string "App"
                Spinner valSpinner = (Spinner) findViewById(R.id.spinnerContextMatchesValue);
                int uid = MainActivity.nameToUid.get(MainActivity.allApps.get(position));
                String packageName = MainActivity.packageManager.getPackagesForUid(uid)[0];

                contextMatchesValueArray[positionOfCustomAppValue] = packageName;
                contextDataAdapter.notifyDataSetChanged();
                valSpinner.setSelection(positionOfCustomAppValue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        appDialog.setPositiveButton("enter", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // needed to exit out of spinner without clicking on empty space
            }
        });
        appDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        appDialog.setView(appsSpinner);
        appDialog.show();
    }

    public void promptUserForString(final boolean isContext) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Custom value:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setGravity(Gravity.CENTER);
        dialog.setView(input);

        dialog.setPositiveButton("enter", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Spinner s;

                if (isContext) {
                    s = (Spinner) findViewById(R.id.spinnerContextMatchesValue);
                } else {
                    s = (Spinner) findViewById(R.id.spinnerMessage);
                }

                String val = input.getText().toString();
                if (val == null) {
                    val = "";
                }

                if (isContext) {
                    contextMatchesValueArray[positionOfCustomValue] = val;
                    contextDataAdapter.notifyDataSetChanged();
                    s.setSelection(positionOfCustomValue);
                } else {
                    actionMessagesArray.set(positionOfCustomAction, val);
                    actionMessageAdapter.notifyDataSetChanged();
                    s.setSelection(positionOfCustomAction);
                }
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
    }

    public static String generateContextDisplayString(String context, String contextComparator, String contextValue,
                                               String actionValue, String actionApp, String actionAction) {
        return "When " + context.toLowerCase() + " " + contextComparator.toLowerCase() + " \"" +
                contextValue + "\", " + actionAction.toLowerCase() + " <" + actionValue + "> for app " + actionApp;
    }
    public static String generateContextDisplayString(FilterLine filter) {
        StringBuilder ret = new StringBuilder();
        ret.append("When ");

        String contextString = "<unknown context>";
        if (filter.context == Policy.CONTEXT_WIFI_STATE) {
            contextString = "wifi state";
        } else if (filter.context == Policy.CONTEXT_WIFI_SSID) {
            contextString = "wifi ssid";
        } else if (filter.context == Policy.CONTEXT_BT_STATE) {
            contextString = "bluetooth state";
        } else if (filter.context == Policy.CONTEXT_APP_RUNNING) {
            contextString = "app running";
        }
        ret.append(contextString);

        if (filter.contextType == Policy.CONTEXT_TYPE_INT) {
            ret.append(" status");
            ret.append(" \"");
            if (filter.contextIntValue == Policy.CONTEXT_STATE_ON) {
                ret.append("On");
            } else if (filter.contextIntValue == Policy.CONTEXT_STATE_OFF) {
                ret.append("Off");
            } else {
                ret.append("<unknown state>");
            }
            ret.append("\", ");
        } else if (filter.contextType == Policy.CONTEXT_TYPE_STRING) {
            ret.append(" matches");
            ret.append(" \"");
            ret.append(filter.contextStringValue);
            ret.append("\", ");
        } else {
            ret.append(" <unknown comparator>");
        }

        if (filter.action == Policy.BLOCK_ACTION) {ret.append("block");}
        if (filter.action == Policy.UNBLOCK_ACTION) {ret.append("unblock");}
        if (filter.action == Policy.MODIFY_ACTION) {ret.append("modify");}
        if (filter.action == Policy.UNMODIFY_ACTION) {ret.append("unmodify");}

        ret.append(" <");
        String display = "";
        for (int i=0; i<Policy.messages.size(); i++) {
            PolicyMessage pm = Policy.messages.get(i);
            if (filter.message.equals(pm.filterMessage)) {
                display = pm.displayMessage;
            }
        }
        if (display.equals("")) {
            // this is a custom message that the user established
            ret.append(filter.message);
        } else {
            ret.append(display);
        }
        ret.append("> for app");

        ret.append(MainActivity.uidToName.get(filter.uid));

        return ret.toString();
    }

    public void saveRule() {
        String contextType = sContextValue.getSelectedItem().toString();
        String contextComparator = sContextMatches.getSelectedItem().toString();
        String contextValue = sContextMatchesValue.getSelectedItem().toString();
        String actionValue = sActionMessage.getSelectedItem().toString();
        String actionApp = sActionUid.getSelectedItem().toString();
        String actionAction = sActionAction.getSelectedItem().toString();

        String ruleString = generateContextDisplayString(contextType, contextComparator, contextValue,
                actionValue, actionApp, actionAction);
        Log.i(TAG, "saveRule: " + ruleString);

        if (!validateRule(contextType, contextComparator, contextValue,
                actionValue, actionApp, actionAction)) {
            return;
        }

        int intContext = 0, intContextIntOrString = 0, intContextIntValue = 0;
        String stringContextStringValue = "";
        if (contextType.equals("Wifi state")) {
            intContext = Policy.CONTEXT_WIFI_STATE;
            intContextIntOrString = Policy.CONTEXT_TYPE_INT;
        }
        if (contextType.equals("Wifi ssid")) {
            intContext = Policy.CONTEXT_WIFI_SSID;
            intContextIntOrString = Policy.CONTEXT_TYPE_STRING;
        }
        if (contextType.equals("Bluetooth state")) {
            intContext = Policy.CONTEXT_BT_STATE;
            intContextIntOrString = Policy.CONTEXT_TYPE_INT;
        }
        if (contextType.equals("App running")) {
            intContext = Policy.CONTEXT_APP_RUNNING;
            intContextIntOrString = Policy.CONTEXT_TYPE_STRING;
        }

        if (contextValue.equals("On")) {
            intContextIntValue = Policy.CONTEXT_STATE_ON;
        } else if (contextValue.equals("Off")) {
            intContextIntValue = Policy.CONTEXT_STATE_OFF;
        } else {
            stringContextStringValue = contextValue;
        }

        int intApp = MainActivity.nameToUid.get(actionApp);
        int intActionAction = -1;

        String stringActionMessage = "";
        for (int i=0; i<Policy.messages.size(); i++) {
            PolicyMessage pm = Policy.messages.get(i);
            if (actionValue.equals(pm.displayMessage)) {
                stringActionMessage = pm.filterMessage;
            }
        }
        if (stringActionMessage.equals("")) {
            // custom value
            stringActionMessage = actionMessagesArray.get(actionMessagesArray.size()-1);
        }

        if (actionAction.equals("Block")) {intActionAction = Policy.BLOCK_ACTION;}
        if (actionAction.equals("Unblock")) {intActionAction = Policy.UNBLOCK_ACTION;}
        if (actionAction.equals("Modify")) {intActionAction = Policy.MODIFY_ACTION;}
        if (actionAction.equals("Unmodify")) {intActionAction = Policy.UNMODIFY_ACTION;}

        FilterLine filter = new FilterLine(intApp, intActionAction, stringActionMessage, "",
                intContext, intContextIntOrString, intContextIntValue, stringContextStringValue);
        Policy.setContextFilterLine(filter);

        MainActivity.savedRules.put(ruleString, filter);
        CustomTabFragment.adapter.notifyDataSetChanged();
        CustomTabFragment.adapter.updateList();

        // go back to Custom Tab view
        finish();
    }

    // returns false on invalid rule
    private boolean validateRule(String contextType, String contextComparator, String contextValue,
                                        String actionValue, String actionApp, String actionAction) {
        boolean ret = true;
        StringBuilder retString = new StringBuilder();

        if (contextType.equals(contextValueDefault)) {
            retString.append("Context type cannot be set to default value.\n");
            ret = false;
        }
        if (contextComparator.equals(contextMatchesDefault)) {
            retString.append("Context comparator cannot be set to default value.\n");
            ret = false;
        }
        if (contextValue.equals(contextMatchesValueDefault)) {
            retString.append("Context value cannot be set to default value.\n");
            ret = false;
        }
        if (actionValue.equals(actionMessageDefault)) {
            retString.append("Action value cannot be set to default value.\n");
            ret = false;
        }
        if (actionApp.equals(actionUidDefault)) {
            retString.append("Action app cannot be set to default value.\n");
            ret = false;
        }
        if (actionAction.equals(actionActionDefault)) {
            retString.append("Action value cannot be set to default value.\n");
            ret = false;
        }

        // type and comparator
        if (contextType.equals("Wifi state") && contextComparator.equals("Matches")) {
            retString.append("Wifi state must correspond to \"Status\" comparator.\n");
            ret = false;
        }
        if (contextType.equals("Bluetooth state") && contextComparator.equals("Matches")) {
            retString.append("Bluetooth state must correspond to \"Status\" comparator.\n");
            ret = false;
        }
        if (contextType.equals("Wifi ssid") && contextComparator.equals("Status")) {
            retString.append("Wifi ssid must correspond to \"Matches\" comparator.\n");
            ret = false;
        }
        if (contextType.equals("App running") && contextComparator.equals("Status")) {
            retString.append("App running must correspond to \"Matches\" comparator.\n");
            ret = false;
        }

        // type and value
        if (contextType.equals("Wifi state") && !contextValue.equals("On") && !contextValue.equals("Off")) {
            retString.append("Wifi state must correspond to On or Off value.\n");
            ret = false;
        }
        if (contextType.equals("Bluetooth state") && !contextValue.equals("On") && !contextValue.equals("Off")) {
            retString.append("Bluetooth state must correspond to On or Off value.\n");
            ret = false;
        }
        if (contextType.equals("Wifi ssid") && (contextValue.equals("On") || contextValue.equals("Off"))) {
            retString.append("Wifi ssid must correspond to custom value (Enter value).\n");
            ret = false;
        }
        if (contextType.equals("App running") && (contextValue.equals("On") || contextValue.equals("Off"))) {
            retString.append("App running must correspond to custom value (Enter value).\n");
            ret = false;
        }

        // comparator and value
        if (contextComparator.equals("Status") && !(contextValue.equals("On") || contextValue.equals("Off"))) {
            retString.append("Status comparator must correspond to On or Off value.\n");
            ret = false;
        }
        if (contextComparator.equals("Matches") && (contextValue.equals("On") || contextValue.equals("Off"))) {
            retString.append("Matches comparator must correspond to custom value (Enter value).\n");
            ret = false;
        }

        if (contextType.equals("Wifi ssid") && contextType.length() > 32) {
            retString.append("Wifi ssid max length is specified to be 32 characters.\n");
            ret = false;
        }

        // custom action
        int flag = -1;
        for (int i=0; i<Policy.messages.size(); i++) {
            PolicyMessage pm = Policy.messages.get(i);
            if (actionValue.equals(pm.displayMessage)) {
                flag = 0;
            }
        }
        if (flag == -1) {
            // if custom action, don't allow modify
            if (actionAction.equals("Modify")) {
                retString.append("Custom action message with Modify action currently unsupported.\n");
                ret = false;
            }
        }

        // modify with non supported message
        if (actionAction.equals("Modify")) {
            if (!actionValue.equals("Camera") || !actionValue.equals("Microphone")) {
                retString.append("Modify action not supported for " + actionValue);
                ret = false;
            }
        }

        if (ret == false) {
            Toast.makeText(this, retString.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MainActivity.viewPager.setCurrentItem(1, false);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        MainActivity.viewPager.setCurrentItem(1,false);
        finish();
    }

}
