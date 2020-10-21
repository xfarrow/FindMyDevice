package de.nulide.findmydevice.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.WhiteList;

public class WhiteListViewAdapter extends BaseAdapter {

    private WhiteList whitelist;
    private LayoutInflater inflater;

    public WhiteListViewAdapter(Context context, WhiteList whitelist) {
        this.whitelist = whitelist;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return whitelist.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.whitelist_item, null);
        TextView name = view.findViewById(R.id.textViewWLItem1);
        TextView number = view.findViewById(R.id.textViewWLItem2);
        name.setText(whitelist.get(position).getName());
        number.setText(whitelist.get(position).getNumber());
        return view;    }
}
