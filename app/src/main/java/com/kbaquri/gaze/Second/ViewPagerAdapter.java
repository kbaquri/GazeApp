package com.kbaquri.gaze.Second;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0:
                return MyMapFragment.newInstance();
            case 1:
                return new MyPlacesListFragment();
            case 2:
                return new MyARFragment();
            default:
                return null;
        }
    }

}
