package edu.dartmouth.dwu.picky;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dwu on 5/18/16.
 * from http://stackoverflow.com/questions/17525886/listview-with-add-and-delete-buttons-in-each-row-in-android
 */
public class CustomArrayAdapter extends BaseAdapter implements ListAdapter {
    private static final String TAG = "Picky";

    private ArrayList<String> list = new ArrayList<String>();
    private Context context;
    private final int type;
    private long count = 0;

    public HashMap<Integer, Boolean> blockCheckedPositions;
    public HashMap<Integer, Boolean> allowCheckedPositions;

    public CustomArrayAdapter(ArrayList<String> list, Context context, int type) {
        this.list = list;
        this.context = context;
        this.type = type;

        blockCheckedPositions = new HashMap<Integer, Boolean>();
        allowCheckedPositions = new HashMap<Integer, Boolean>();
        for (int i = 0; i < MainActivity.allApps.size(); i++) {
            blockCheckedPositions.put(i, false);
            allowCheckedPositions.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return count++;
    }

    // because of scrolling listview, we need to store the value in our own data struct
    // and set on/off based on that (ListView recycled Ids)
    // see http://stackoverflow.com/questions/19533098/how-to-list-all-installed-applications-with-icon-and-check-box-in-android/19533315#19533315
    final CompoundButton.OnCheckedChangeListener bToggleButtonChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            blockCheckedPositions.put((Integer) buttonView.getTag(), isChecked);
            if (isChecked && allowCheckedPositions.get(buttonView.getTag())) {
                buttonView.setChecked(false);
                blockCheckedPositions.put((Integer) buttonView.getTag(), false);
            }
        }
    };
    final CompoundButton.OnCheckedChangeListener aToggleButtonChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            allowCheckedPositions.put((Integer) buttonView.getTag(), isChecked);
            if (isChecked && blockCheckedPositions.get(buttonView.getTag())) {
                buttonView.setChecked(false);
                allowCheckedPositions.put((Integer) buttonView.getTag(), false);
            }
        }
    };

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.app_list_row, null);
        }

        final TextView listItemText = (TextView) view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position));

        final ToggleButton blockButton = (ToggleButton) view.findViewById(R.id.blockToggleButton);
        final ToggleButton allowButton = (ToggleButton) view.findViewById(R.id.allowToggleButton);

        // set the tag so we can identify the correct row in the listener
        blockButton.setTag(Integer.valueOf(position));
        blockButton.setOnCheckedChangeListener(bToggleButtonChangeListener);
        blockButton.setChecked(blockCheckedPositions.get(position));

        // set the tag so we can identify the correct row in the listener
        allowButton.setTag(Integer.valueOf(position));
        allowButton.setOnCheckedChangeListener(aToggleButtonChangeListener);
        allowButton.setChecked(allowCheckedPositions.get(position));

        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allowButton.isChecked()) {
                    return;
                }
                AppsActivity.setAppResult(true, blockButton.isChecked(), type, position);

                if (!blockButton.isChecked()) {
                    ArrayList<Integer> toSet =  MainActivity.blockButtonsToSet.get(type);
                   if (toSet.contains(position)) {
                       toSet.remove(toSet.indexOf(position));
                   }
                }
            }
        });
        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blockButton.isChecked()) {
                    return;
                }
                int uid = AppsActivity.setAppResult(false, allowButton.isChecked(), type, position);

                if (!allowButton.isChecked()) {
                    ArrayList<Integer> toSet =  MainActivity.allowButtonsToSet.get(type);
                    if (toSet.contains(position)) {
                        toSet.remove(toSet.indexOf(position));
                    }
                }

                Intent intent = new Intent(v.getContext(), ModifyActivity.class);
                intent.putExtra(ModifyActivity.EXTRA_APP, uid);
                intent.putExtra(AppsActivity.EXTRA_TYPE, type);
                v.getContext().startActivity(intent);
            }
        });

        MainActivity.blockButtons.get(type).put(position, blockButton);
        MainActivity.allowButtons.get(type).put(position, allowButton);

        if (MainActivity.blockButtonsToSet.get(type).contains(position)) {
            blockButton.setChecked(true);
        }
        if (MainActivity.allowButtonsToSet.get(type).contains(position)) {
            allowButton.setChecked(true);
        }

        return view;
    }
}
