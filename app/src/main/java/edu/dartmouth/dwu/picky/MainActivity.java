package edu.dartmouth.dwu.picky;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Picky";


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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        String ret = nativeSetUpPermissions();
        Log.i(TAG, ret);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //http://ph0b.com/new-android-studio-ndk-support/
    // Android Studio->preferences->build,execution,deployment->build tools->gradle->use local gradle distrib
    // download gradle 2.10, ndk tools
    public native String nativeWriteUserFilter(String[] intents, int level_noBT, int level_withBT);
    public native String nativeSetUpPermissions();

    static {
        System.loadLibrary("picky-jni");
    }

    public void setTempFilter(View view) {
        //String ret = nativeWriteUserFilter(new String[]{"android.intent.action.VOICE_ASSIST"}, 100, 100);
        //String ret = nativeWriteUserFilter(new String[]{"android.permission.RECORD_AUDIO"}, 100, 100);
        //http://androidxref.com/6.0.1_r10/xref/frameworks/av/media/libmedia/IMediaRecorder.cpp#256
        //Log.i(TAG, ret);
    }

    public void setUserFilter(View view) {
        UserFilter userFilter = getUserFieldsOrDefault();
        if (userFilter == null) {
            return;
        }

        String[] intents = generateIntentsArray(userFilter);

        String ret = nativeWriteUserFilter(intents, userFilter.levelNoBt, userFilter.levelWithBt);
        Log.i(TAG, ret);
    }

    public UserFilter getUserFieldsOrDefault() {
        EditText mEditBToff = (EditText)findViewById(R.id.batteryLevelBToff);
        EditText mEditBTon = (EditText)findViewById(R.id.batteryLevelBTon);

        String blOff = mEditBToff.getText().toString();
        String blOn = mEditBTon.getText().toString();
        int intLevelOff, intLevelOn;

        if (blOff.equals("")) {
            intLevelOff = UserFilter.LEVEL_NO_BT_DEFAULT;
        } else {
            try {
                intLevelOff = Integer.parseInt(blOff);
            } catch (NumberFormatException e) {
                Log.v(TAG, "parseInt fail");
                return null;
            }
        }
        if (blOn.equals("")) {
            intLevelOn = UserFilter.LEVEL_WITH_BT_DEFAULT;
        } else {
            try {
                intLevelOn = Integer.parseInt(blOn);
            } catch (NumberFormatException e) {
                Log.v(TAG, "parseInt fail");
                return null;
            }
        }

        Switch camera = (Switch) findViewById(R.id.cameraSwitch);
        Switch bt = (Switch) findViewById(R.id.btSwitch);
        Switch battery = (Switch) findViewById(R.id.batterySwitch);
        Switch audio = (Switch) findViewById(R.id.blockRecordAudioSwitch);

        UserFilter userFilter = new UserFilter();
        userFilter.levelNoBt = intLevelOff;
        userFilter.levelWithBt = intLevelOn;

        userFilter.blockCamera = camera.isChecked();
        userFilter.blockBtStatus = bt.isChecked();
        userFilter.blockBatteryLevel = battery.isChecked();
        userFilter.blockRecordAudio = audio.isChecked();

        return userFilter;
    }

    public static String[] generateIntentsArray(UserFilter userFilter) {
        ArrayList<String> list = new ArrayList<String>();

        if (userFilter.blockCamera) {
            list.add(MediaStore.ACTION_IMAGE_CAPTURE);
        }

        if (userFilter.blockBtStatus) {
            list.add(BluetoothAdapter.ACTION_STATE_CHANGED);
        }

        if (userFilter.blockBatteryLevel) {
            list.add(Intent.ACTION_BATTERY_CHANGED);
        }

        if (userFilter.blockRecordAudio) {
            list.add("android.permission.RECORD_AUDIO");
        }

        return list.toArray(new String[0]);
    }

}
