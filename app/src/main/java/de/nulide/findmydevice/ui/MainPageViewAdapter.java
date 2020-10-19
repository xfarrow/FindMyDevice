package de.nulide.findmydevice.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import de.nulide.findmydevice.MainInfo;
import de.nulide.findmydevice.MainWhitelist;

public class MainPageViewAdapter extends FragmentPagerAdapter {

    private int numOfTabs;

    public MainPageViewAdapter(@NonNull FragmentManager fm, int numOfTabs) {
        super(fm, numOfTabs);
        this.numOfTabs = numOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new MainInfo();
            case 1:
                return new MainWhitelist();
        }
        return null;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
