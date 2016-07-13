package edu.dartmouth.dwu.picky;

import android.app.AlertDialog;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModifyActivity extends AppCompatActivity {
    private static final String TAG = "Picky";
    public static final String EXTRA_POSITION = "position";
    private int type = -1;
    private boolean isModifyGPS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        final int position = intent.getIntExtra(EXTRA_POSITION, -1);
        type = intent.getIntExtra(AppsActivity.EXTRA_TYPE, -1);

        if (type == -1) {
            Log.e(TAG, "bad type " + type);
            return;
        }

        TextView messageTitle = (TextView)findViewById(R.id.toolbar_title3);
        if (type == 0) {
            messageTitle.setText("Modify Camera Picture");
            isModifyGPS = false;
        } else if (type == 1) {
            messageTitle.setText("Modify Microphone Recording");
            isModifyGPS = false;
        } else if (type == 5) {
            messageTitle.setText("Modify GPS Location");
            isModifyGPS = true;
        }
        else {
            Log.e(TAG, "bad type " + type);
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Custom value:");

        final EditText input = new EditText(this);
        final EditText input1 = new EditText(this);
        final EditText input2 = new EditText(this);

        if (isModifyGPS) {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            input1.setHint("Latitude");
            layout.addView(input1);

            input2.setHint("Longitude");
            layout.addView(input2);

            input1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            input1.setGravity(Gravity.CENTER);
            input2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            input2.setGravity(Gravity.CENTER);
            dialog.setView(layout);
        } else {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setGravity(Gravity.CENTER);
            dialog.setView(input);
        }

        dialog.setPositiveButton("enter", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isModifyGPS) {
                    String gpsLat = input1.getText().toString();
                    String gpsLong = input2.getText().toString();

                    if (gpsLat == null) {
                        gpsLat = "";
                    }
                    if (gpsLong == null) {
                        gpsLong = "";
                    }

                    String gpsString = getGPSstringForBinder(gpsLat, gpsLong);
                    Policy.setPolicyInfo(false, true, type, position, gpsString);
                } else {
                    String val = input.getText().toString();
                    if (val == null) {
                        val = "";
                    }
                    Policy.setPolicyInfo(false, true, type, position, val);
                }
                finish();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();
    }

    // byte[8] values of latitude,longitude separated by '.' in string form
    private String getGPSstringForBinder(String glat, String glong) {
        double dLat = Double.valueOf(glat);
        double dLong = Double.valueOf(glong);

        byte[] bLat = ByteBuffer.allocate(8).putDouble(dLat).array();
        byte[] bLong = ByteBuffer.allocate(8).putDouble(dLong).array();

        for (int i=0; i<4; i++) {
            byte temp1 = bLat[i];
            bLat[i] = bLat[7-i];
            bLat[7-i] = temp1;

            byte temp2 = bLong[i];
            bLong[i] = bLong[7-i];
            bLong[7-i] = temp2;
        }

        char[] gpsByte = new char[16];
        gpsByte[0] = (char)(bLat[0] & 0xFF);
        gpsByte[1] = (char)(bLat[1] & 0xFF);
        gpsByte[2] = (char)(bLat[2] & 0xFF);
        gpsByte[3] = (char)(bLat[3] & 0xFF);
        gpsByte[4] = (char)(bLat[4] & 0xFF);
        gpsByte[5] = (char)(bLat[5] & 0xFF);
        gpsByte[6] = (char)(bLat[6] & 0xFF);
        gpsByte[7] = (char)(bLat[7] & 0xFF);

        gpsByte[8] = (char)(bLong[0] & 0xFF);
        gpsByte[9] = (char)(bLong[1] & 0xFF);
        gpsByte[10] = (char)(bLong[2] & 0xFF);
        gpsByte[11] = (char)(bLong[3] & 0xFF);
        gpsByte[12] = (char)(bLong[4] & 0xFF);
        gpsByte[13] = (char)(bLong[5] & 0xFF);
        gpsByte[14] = (char)(bLong[6] & 0xFF);
        gpsByte[15] = (char)(bLong[7] & 0xFF);

        String retString = "";
        for (int i=0; i<gpsByte.length; i++) {
            retString = retString + Integer.toString((int)gpsByte[i]) + ".";
        }

        return retString;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, AppsActivity.class);
                intent.putExtra(AppsActivity.EXTRA_TYPE, type);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AppsActivity.class);
        intent.putExtra(AppsActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }

}
