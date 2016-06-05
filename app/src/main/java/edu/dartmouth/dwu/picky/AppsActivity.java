package edu.dartmouth.dwu.picky;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AppsActivity extends AppCompatActivity {
    private static final String TAG = "Picky";

    public static final String EXTRA_TYPE = "type";

    private int type = -1;
    private CustomArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView messageTitle = (TextView)findViewById(R.id.toolbar_title2);

        Intent intent = getIntent();
        if (type == -1 && intent != null) {
            type = intent.getIntExtra(AppsActivity.EXTRA_TYPE, -1);
        }

        if (type == -1) {
            messageTitle.setText("Error: incorrect message type");
        } else {
            messageTitle.setText(Policy.messages.get(type).displayMessage);
        }

        populateList(type);
    }

    private void populateList(int type) {
        ListView lv = (ListView) findViewById(R.id.AppsListView);

        // special check for install packages (only Google Play Store can install apps)
        // unless app requests another permission and is located in /system/app
        // but that can only be done by a root attacker, TODO: more to discuss
        if (Policy.isInstallPackagesType(type)) {
            ArrayList<String> l = new ArrayList<String>();
            l.add("Google Play Store");
            adapter = new CustomArrayAdapter(l, this, type);
        } else {
            adapter = new CustomArrayAdapter(MainActivity.allApps, this, type);
        }

        // set saved policy
        List<FilterLine> savedPolicyForMessageType = MainActivity.savedPolicies.get(type);
        for (FilterLine filter : savedPolicyForMessageType) {
            String name = MainActivity.uidToName.get(filter.uid);
            int position = MainActivity.allApps.indexOf(name);

            //Log.i(TAG, "name: " + name + ", uid: " + filter.uid + ", position: " + position);

            if (filter.action == Policy.BLOCK_ACTION) {
                adapter.blockCheckedPositions.put(position, true);
                ToggleButton button = MainActivity.blockButtons.get(type).get(position);

                if (button == null) {
                    MainActivity.blockButtonsToSet.get(type).add(position);
                } else {
                    button.setChecked(true);
                }
            } else if (filter.action == Policy.MODIFY_ACTION) {
                adapter.allowCheckedPositions.put(position, true);
                ToggleButton button = MainActivity.allowButtons.get(type).get(position);

                if (button == null) {
                    MainActivity.allowButtonsToSet.get(type).add(position);
                } else {
                    button.setChecked(true);
                }
            }
        }

        lv.setAdapter(adapter);
    }



}
