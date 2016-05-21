package edu.dartmouth.dwu.picky;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
                // import policy

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

        String ret = Policy.nativeSetUpPermissions();
        Log.i(TAG, ret);

        for (int i=0; i<Policy.messages.length; i++) {
            savedPolicies.add(new ArrayList<FilterLine>());
            blockButtons.add(new HashMap<Integer, ToggleButton>());
            allowButtons.add(new HashMap<Integer, ToggleButton>());

            blockButtonsToSet.add(new ArrayList<Integer>());
            allowButtonsToSet.add(new ArrayList<Integer>());
        }

        // load policy from binderfilter driver into memory
        if (firstLoad) {
            if (Policy.loadPolicy() == -1) {
                Toast.makeText(this, "Error loading policy!", Toast.LENGTH_LONG).show();
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

        return super.onOptionsItemSelected(item);
    }

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
