package edu.dartmouth.dwu.picky;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dwu on 5/24/16.
 */
public class RulesArrayAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private Context context;
    private long count = 0;

    public RulesArrayAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.rules_list_row, null);
        }

        final TextView listItemText = (android.widget.TextView) view.findViewById(R.id.rule_list_item_string);
        listItemText.setText(list.get(position));

        final Button cancelButton = (Button) view.findViewById(R.id.rulesListRowButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Policy.removeContextFilterLine(list.get(position));
                notifyDataSetChanged();
            }
        });

        return view;
    }

    public void updateList() {
        ArrayList<String> rules = new ArrayList<>();
        rules.addAll(MainActivity.savedRules.keySet());
        this.list = rules;
    }
}
