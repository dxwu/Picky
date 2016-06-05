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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyActivity extends AppCompatActivity {
    private static final String TAG = "Picky";
    public static final String EXTRA_POSITION = "position";
    private int type = -1;

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
        } else if (type == 1) {
            messageTitle.setText("Modify Microphone Recording");
        } else {
            Log.e(TAG, "bad type " + type);
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Choose your picture:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setGravity(Gravity.CENTER);
        dialog.setView(input);

        dialog.setPositiveButton("enter", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String val = input.getText().toString();
                if (val == null) {
                    val = "";
                }

                Log.i(TAG, "val: " + val);
                //Policy.nativeWriteFilterLine(Policy.MODIFY_ACTION, uid, Policy.messages.get(type).filterMessage, val);
                Policy.setPolicyInfo(false, true, type, position, val);
                finish();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();
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
