package edu.dartmouth.dwu.picky;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;


public class CustomTabFragment extends Fragment {
    private static final String TAG = "Picky";
    public static RulesArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_custom_tab, container, false);

        ListView lv = (ListView) view.findViewById(R.id.CustomRulesList);
        ArrayList<String> rules = new ArrayList<>();
        rules.addAll(MainActivity.savedRules.keySet());

        adapter = new RulesArrayAdapter(rules, view.getContext());
        lv.setAdapter(adapter);

        lv.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        return view;
    }

    public static void addContextRule() {
        Intent intent = new Intent(MainActivity.mContext, ContextActivity.class);
        MainActivity.mContext.startActivity(intent);
    }
}
