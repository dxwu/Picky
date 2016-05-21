package edu.dartmouth.dwu.picky;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by dwu on 5/19/16.
 */
public class SectionPagerAdapter extends FragmentPagerAdapter {
    int numTabs = 3;

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DefaultTabFragment();
            case 1:
                return new CustomTabFragment();
            case 2:
                return new StatsTabFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Defaults";
            case 1:
                return "Custom";
            case 2:
                return "Statistics";
            default:
                return null;
        }
    }
}