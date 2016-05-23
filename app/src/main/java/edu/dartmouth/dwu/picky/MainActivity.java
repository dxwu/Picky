package edu.dartmouth.dwu.picky;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Picky";
    private boolean firstLoad = true;

    public static ArrayList<String> allApps = null;
    public static List<ApplicationInfo> installedApplications = null;
    public static HashMap<String, Integer> nameToUid = null;
    public static HashMap<Integer, String> uidToName = null;

    private String filepath;

    // one for every kind of PolicyMessage
    // saves policy info for this app session
    // have to store them here because tabs and app activities may be stopped
    public static List<List<FilterLine>> savedPolicies
            = new ArrayList<>(Policy.messages.length);
    public static List<HashMap<Integer, ToggleButton>> blockButtons
            = new ArrayList<>(Policy.messages.length);
    public static List<HashMap<Integer, ToggleButton>> allowButtons
            = new ArrayList<>(Policy.messages.length);

    public static ArrayList<ArrayList<Integer>> blockButtonsToSet
            = new ArrayList<ArrayList<Integer>>(Policy.messages.length);
    public static ArrayList<ArrayList<Integer>> allowButtonsToSet
            = new ArrayList<ArrayList<Integer>>(Policy.messages.length);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importPolicy();
            }
        });

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));

        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setupWithViewPager(viewPager);

        // get rid of left-gravity title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        for (int i=0; i<Policy.messages.length; i++) {
            savedPolicies.add(new ArrayList<FilterLine>());
            blockButtons.add(new HashMap<Integer, ToggleButton>());
            allowButtons.add(new HashMap<Integer, ToggleButton>());

            blockButtonsToSet.add(new ArrayList<Integer>());
            allowButtonsToSet.add(new ArrayList<Integer>());
        }

        // load policy from binderfilter driver into memory
        if (firstLoad) {
            int numTimesToTry = 5;
            String ret = "";
            for (int i=0; i<numTimesToTry; i++) {
                ret = Policy.nativeSetUpPermissions();
                Log.i(TAG, "nativeSetUpPermissions returned: " + ret);

                if (ret.contains("success")) {
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            if (!ret.contains("success")) {
                Toast.makeText(this, "Error getting su permissions!", Toast.LENGTH_LONG).show();
            }

            if (Policy.loadPolicy(true, null) == -1) {
                Toast.makeText(this, "Error loading policy!", Toast.LENGTH_LONG).show();
            }
            if (Policy.nativeInitPolicyPersistFile() == -1) {
                Toast.makeText(this, "Error initializing persistent policy file!", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error initializing persistent policy file");
            }
            firstLoad = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(this, "Created by David Wu", Toast.LENGTH_LONG).show();
            return true;
        }
        if (item.getItemId() == R.id.action_export) {
            String path = exportPolicy();

            if (path != null) {
                Toast.makeText(this, "Policy exported to " + path, Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void importPolicy() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Import Policy");
        dialog.setMessage("Specify policy file location in /data/local/tmp/\nWARNING: this will overwrite existing policy!");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialog.setView(input);

        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                filepath = input.getText().toString();
                setNewPolicy(filepath);
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
    }

    public void setNewPolicy(String name) {
        StringBuilder text = new StringBuilder();
        File file = new File("/data/local/tmp/" + name);
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                Toast.makeText(this, "Error importing policy: Could not read file!", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Toast.makeText(this, "Error importing policy: File does not exist!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

        // clear driver policy
        for (List<FilterLine> savedPolicy : savedPolicies) {
            for (FilterLine filter : savedPolicy) {
                // flip for remove
                if (filter.action == Policy.BLOCK_ACTION) {
                    filter.action = Policy.UNBLOCK_ACTION;
                } else if (filter.action == Policy.MODIFY_ACTION) {
                    filter.action = Policy.UNMODIFY_ACTION;
                }

                Policy.setFilterLine(filter);
            }
        }

        // clear app policy
        for (int i=0; i<Policy.messages.length; i++) {
            savedPolicies.get(i).clear();
            blockButtonsToSet.get(i).clear();
            allowButtonsToSet.get(i).clear();
        }

        if (Policy.loadPolicy(false, text.toString()) == -1) {
            Toast.makeText(this, "Error importing policy: Could not parse!", Toast.LENGTH_LONG).show();
            return;
        }

        // write this policy to driver
        for (List<FilterLine> savedPolicy : savedPolicies) {
            for (FilterLine filter : savedPolicy) {
                Policy.setFilterLine(filter);
            }
        }
    }

    public String exportPolicy() {
        String policy = Policy.getPolicy();

        if (policy == null || policy.equals("empty")) {
            Toast.makeText(this, "Error exporting policy: could not read from driver!", Toast.LENGTH_LONG).show();
        }

            // append time or unique file id
        File exportPath = this.getExternalFilesDir(null);
        String filename = "user.policy";
        File file = new File(exportPath, filename);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(policy.getBytes());
            stream.close();
        }
        catch (IOException e) {
            Toast.makeText(this, "Error exporting policy: could not write to file!", Toast.LENGTH_LONG).show();
        }

        return exportPath.getAbsolutePath() + "/" + filename;
    }
}
