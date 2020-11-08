package de.nulide.findmydevice;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.nulide.findmydevice.utils.Permission;

public class PermissionsActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonSMSPerm;
    Button buttonContactPerm;
    Button buttonGPSPerm;
    Button buttonDNDPerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        buttonSMSPerm = findViewById(R.id.buttonSMSPermission);
        buttonSMSPerm.setOnClickListener(this);
        buttonContactPerm = findViewById(R.id.buttonContactPermission);
        buttonContactPerm.setOnClickListener(this);
        buttonGPSPerm = findViewById(R.id.buttonGPSPermission);
        buttonGPSPerm.setOnClickListener(this);
        buttonDNDPerm = findViewById(R.id.buttonDNDPermission);
        buttonDNDPerm.setOnClickListener(this);
        update();
    }

    private void update(){
        if(Permission.checkGPSPermission(this)){
            buttonGPSPerm.setEnabled(false);
            buttonGPSPerm.setTextColor(Color.GREEN);
        }
        if(Permission.checkContactsPermission(this)){
            buttonContactPerm.setEnabled(false);
            buttonContactPerm.setTextColor(Color.GREEN);
        }
        if(Permission.checkSMSPermission(this)){
            buttonSMSPerm.setEnabled(false);
            buttonSMSPerm.setTextColor(Color.GREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Permission.checkDNDPermission(this)){
                buttonDNDPerm.setEnabled(false);
                buttonDNDPerm.setTextColor(Color.GREEN);
            }
        }else{
            buttonDNDPerm.setVisibility(View.INVISIBLE);
        }
        if(Permission.checkAll(this)){
            Intent myIntent = new Intent(this, MainActivity.class);
            finish();
            startActivity(myIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onClick(View v) {
        if(v == buttonSMSPerm){
            Permission.requestSMSPermission(this);
        }else if(v == buttonContactPerm){
            Permission.requestContactPermission(this);
        }else if(v == buttonGPSPerm){
            Permission.requestGPSPermission(this);
        }else if(v == buttonDNDPerm){
            Permission.requestDNDPermission(this);
        }
        update();
    }
}
