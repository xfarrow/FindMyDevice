package de.nulide.findmydevice.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.Whitelist;

public class WhiteListViewAdapter extends BaseAdapter {

    private Whitelist whitelist;
    private LayoutInflater inflater;

    public WhiteListViewAdapter(Context context, Whitelist whitelist) {
        this.whitelist = whitelist;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return whitelist.getContacts().size();
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
        name.setText(whitelist.getContacts().get(position).getName());
        number.setText(whitelist.getContacts().get(position).getNumber());
        return view;    }
}
