package edu.dartmouth.dwu.picky;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyActivity extends AppCompatActivity {
    public static final String EXTRA_APP = "uid";
    private int type = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView messageTitle = (TextView)findViewById(R.id.toolbar_title3);
        messageTitle.setText("Modify Message");

        Intent intent = getIntent();
        int uid = intent.getIntExtra(EXTRA_APP, -1);
        type = intent.getIntExtra(AppsActivity.EXTRA_TYPE, -1);

        TextView messageText = (TextView)findViewById(R.id.modifyMessageText);

        messageText.setText("uid: " + uid + ", type: " + type);
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
