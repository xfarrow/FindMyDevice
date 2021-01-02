package de.nulide.findmydevice.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import de.nulide.findmydevice.MainActivity;
import de.nulide.findmydevice.R;

public class MainPageViewAdapter extends PagerAdapter {

    public MainActivity context;

    public MainPageViewAdapter(MainActivity context) {
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        int resId = 0;
        switch (position) {
            case 0:
                resId = R.layout.main_info_layout;
                break;
            case 1:
                resId = R.layout.main_whitelist_layout;
                break;
            case 2:
                resId = R.layout.main_settings_layout;
                break;
        }
        View view = collection.findViewById(resId);
        if (collection.getChildAt(position) != view) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(resId, collection, false);
            collection.addView(view, position);
            context.reloadViews();
            context.updateViews();
        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }


    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((View) object);
    }

}
