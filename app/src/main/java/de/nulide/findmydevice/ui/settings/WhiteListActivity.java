package de.nulide.findmydevice.ui.settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.List;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Contact;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;
import de.nulide.findmydevice.ui.helper.WhiteListViewAdapter;

public class WhiteListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private WhiteList whiteList;
    private Settings Settings;

    private ListView listWhiteList;
    private WhiteListViewAdapter whiteListAdapter;
    private Button buttonAddContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_list);

        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        Settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));

        listWhiteList = findViewById(R.id.listWhiteList);
        whiteListAdapter = new WhiteListViewAdapter(this, whiteList);
        listWhiteList.setAdapter(whiteListAdapter);
        listWhiteList.setOnItemClickListener(this);
        registerForContextMenu(listWhiteList);

        buttonAddContact = findViewById(R.id.buttonAddContact);
        buttonAddContact.setOnClickListener(this);

        if(!(Boolean) Settings.get(Settings.SET_FIRST_TIME_WHITELIST)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.Settings_WhiteList))
                    .setMessage(this.getString(R.string.Alert_First_time_whitelist))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.set(Settings.SET_FIRST_TIME_WHITELIST, true);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(getString(R.string.WhiteList_Select_Action));
        menu.add(0, v.getId(), 0, getString(R.string.Delete));
    }

    @Override
    public void onClick(View v) {
        if (v == buttonAddContact) {
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 1);
            } else {
                intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(this, getString(R.string.not_possible), 5);
                }


            }


        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        if (item.getTitle() == getString(R.string.Delete)) {
            whiteList.remove(index);
            whiteListAdapter.notifyDataSetChanged();
        } else {
            return false;
        }
        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (1):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    String[] projection = new String[]{
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    };
                    Cursor c = managedQuery(contactData, projection, null, null, null);
                    List<Contact> contacts = new LinkedList<>();
                    List<String> numbers = new LinkedList<>();
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME));
                        String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        contacts.add(new Contact(name, phoneNumber));
                        numbers.add(phoneNumber);

                        while (c.moveToNext()) {
                            String cNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String cName = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                            if (!cNumber.isEmpty()) {
                                contacts.add(new Contact(cName, cNumber));
                                numbers.add(cNumber);
                            }
                        }
                    }

                    if(numbers.size() == 1){
                        addContactToWiteList(contacts.get(0));
                    }else{
                        final List<Contact> finalContacts = contacts;
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getString(R.string.WhiteList_Select_Number));
                        String[] numbersArray = numbers.toArray(new String[numbers.size()]);
                        builder.setItems(numbersArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addContactToWiteList(finalContacts.get(which));
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + reqCode);
        }
    }

    private void addContactToWiteList(Contact contact){
        if(contact != null) {
            if (!whiteList.checkForDuplicates(contact)) {
                whiteList.add(contact);
                whiteListAdapter.notifyDataSetChanged();
                if (!(Boolean) Settings.get(Settings.SET_FIRST_TIME_CONTACT_ADDED)) {
                    new AlertDialog.Builder(this)
                            .setTitle("WhiteList")
                            .setMessage(this.getString(R.string.Alert_First_Time_contact_added))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Settings.set(Settings.SET_FIRST_TIME_CONTACT_ADDED, true);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                }
            } else {
                Toast toast = Toast.makeText(this, getString(R.string.Toast_Duplicate_contact), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

}