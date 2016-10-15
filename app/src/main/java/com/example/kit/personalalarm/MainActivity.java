package com.example.kit.personalalarm;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;

import static android.R.attr.button;
import static android.R.attr.data;
import static java.sql.Types.NULL;

public class MainActivity extends AppCompatActivity implements LocationListener{
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ImageView icon;
    EditText editCall,tel;
    Button buttonCall;
    Switch led,sound,emgencyCall,msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 4);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();

        led = (Switch) findViewById(R.id.led);
        sound = (Switch) findViewById(R.id.sound);
        emgencyCall = (Switch) findViewById(R.id.call);
        tel = (EditText) findViewById(R.id.tel);
        tel.setEnabled(false);
        icon = (ImageView) findViewById(R.id.icon);
        icon.setImageResource(R.drawable.connected);
        emgencyCall.setChecked(true);
        editCall = (EditText) findViewById(R.id.editcall);
        editCall.setEnabled(false);
        emgencyCall.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!emgencyCall.isChecked())
                {
                    editCall.setVisibility(View.INVISIBLE);
                    tel.setVisibility(View.INVISIBLE);
                    buttonCall.setVisibility(View.INVISIBLE);
                }else
                {
                    editCall.setVisibility(View.VISIBLE);
                    tel.setVisibility(View.VISIBLE);
                    buttonCall.setVisibility(View.VISIBLE);
                }
            }
        });
        buttonCall = (Button) findViewById(R.id.buttoncall);
        buttonCall.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!editCall.isEnabled())
                {
                    buttonCall.setText("DONE");
                    editCall.setEnabled(true);
                    editCall.requestFocus();
                    editCall.selectAll();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editCall, InputMethodManager.SHOW_IMPLICIT);

                }else
                {
                    buttonCall.setText("EDIT");
                    editCall.setEnabled(false);
                    try {
                        FileOutputStream fOut = openFileOutput("config",MODE_WORLD_READABLE);
                        fOut.write(editCall.getText().toString().getBytes());
                        fOut.close();
                        Toast.makeText(getBaseContext(),"Emergency Number Edited.",Toast.LENGTH_SHORT).show();
                        call(editCall.getText().toString());
                    }
                    catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                }
            }
        });

        try{
            FileInputStream fin = openFileInput("config");
            int c;
            String temp="";

            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            editCall.setText(temp);
        }
        catch(Exception e){
            Log.e("Exception", "File write failed: " + e.toString());
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
//            new AlertDialog.Builder(this)
//                    .setTitle("Not compatible")
//                    .setMessage("Your phone does not support Bluetooth")
//                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            System.exit(0);
//                        }
//                    })
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .show();

        }
        else
        {
            if (!bluetoothAdapter.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
                Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
            }
            if(bluetoothAdapter.isEnabled())
                icon.setImageResource(R.drawable.connected);
            pairedDevices = bluetoothAdapter.getBondedDevices();
            ArrayList list = new ArrayList();

            for (BluetoothDevice bt : pairedDevices)
                list.add(bt.getName());
            Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
        }

        // Example of a call to a native method

    }

    private void call(String phoneNumber) {
        {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+phoneNumber));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
            {
                try {
                if (callIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(callIntent);

                } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                }
            }

        }
    }

    private void getLocation()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            System.out.print(location.getLatitude() + "   " + location.getLongitude());
        }catch(Exception e){e.printStackTrace();};
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
