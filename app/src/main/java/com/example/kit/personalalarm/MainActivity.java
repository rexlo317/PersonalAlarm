package com.example.kit.personalalarm;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static android.R.attr.button;
import static android.R.attr.color;
import static android.R.attr.data;
import static android.R.attr.inputType;
import static java.sql.Types.NULL;

public class MainActivity extends AppCompatActivity implements LocationListener{ //Author: YAN Tsz Kit (Student ID:54106008)
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket bluetoothSocket = null;
    private Set<BluetoothDevice> pairedDevices;
    private Handler bluetoothIn;
    final int handlerState = 0;
    private StringBuilder recDataString = new StringBuilder();

    ImageView icon_imageview,info_imageview;
    EditText call_edittext,callno_edittext,smsno_textview,sms_edittext;
    Button call_button, sms_button;
    Switch led_switch,sound_switch,call_switch,sms_switch;
    TextView info_textview;

    private ConnectedThread mConnectedThread;



    // SPP UUID service - this should work for most devices

    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();

        bluetoothIn = new Handler(){
            public void handleMessage(android.os.Message msg)
            {
                if(msg.what == handlerState)
                {
                    String helpmessage = (String) msg.obj;
                    recDataString.append(helpmessage);
                    if(recDataString.equals("HELP"))
                    {
                        if(call_switch.isChecked())
                            call(call_edittext.toString());
                        if(sms_switch.isChecked())
                            sendSMS(sms_edittext.toString(),"HELP!");
                    }

                    recDataString.delete(0, recDataString.length());
                }

            }
        };

        info_imageview = (ImageView) findViewById(R.id.info_imageview);
        sms_switch = (Switch) findViewById(R.id.sms_switch);
        sms_switch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!sms_switch.isChecked())
                {
                    sms_edittext.setVisibility(View.INVISIBLE);
                    smsno_textview.setVisibility(View.INVISIBLE);
                    sms_button.setVisibility(View.INVISIBLE);
                }else
                {
                    sms_edittext.setVisibility(View.VISIBLE);
                    smsno_textview.setVisibility(View.VISIBLE);
                    sms_button.setVisibility(View.VISIBLE);
                }
                writeSetting();
            }
        });
        info_textview = (TextView) findViewById(R.id.info_textview);
        sms_switch.setChecked(true);
        led_switch = (Switch) findViewById(R.id.led_switch);
        led_switch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                writeSetting();
            }
        });
        sound_switch = (Switch) findViewById(R.id.sound_switch);
        sound_switch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                writeSetting();
            }
        });
        call_switch = (Switch) findViewById(R.id.call_switch);
        callno_edittext = (EditText) findViewById(R.id.callno_edittext);
        callno_edittext.setEnabled(false);
        smsno_textview = (EditText) findViewById(R.id.smsno_textview);
        smsno_textview.setEnabled(false);
        icon_imageview = (ImageView) findViewById(R.id.icon_imageview);
        icon_imageview.setImageResource(R.drawable.disconnected);
        call_switch.setChecked(true);
        sms_edittext = (EditText) findViewById(R.id.sms_edittext);
        sms_edittext.setEnabled(false);
        call_edittext = (EditText) findViewById(R.id.call_edittext);
        call_edittext.setEnabled(false);
        call_switch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!call_switch.isChecked())
                {
                    call_edittext.setVisibility(View.INVISIBLE);
                    callno_edittext.setVisibility(View.INVISIBLE);
                    call_button.setVisibility(View.INVISIBLE);
                }else
                {
                    call_edittext.setVisibility(View.VISIBLE);
                    callno_edittext.setVisibility(View.VISIBLE);
                    call_button.setVisibility(View.VISIBLE);
                }
                writeSetting();
            }
        });
        call_button = (Button) findViewById(R.id.call_button);
        call_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!call_edittext.isEnabled())
                {
                    call_button.setText("DONE");
                    call_edittext.setEnabled(true);
                    call_edittext.requestFocus();
                    call_edittext.selectAll();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(call_edittext, InputMethodManager.SHOW_IMPLICIT);

                }else
                {
                    call_edittext.setSelectAllOnFocus(false);
                    call_button.setText("EDIT");
                    call_edittext.setEnabled(false);
                    if(call_edittext.getText().toString().isEmpty())
                        call_edittext.setText("999");
                    try {
                        writeSetting();
                        Toast.makeText(getBaseContext(),"Emergency Call Number Edited.",Toast.LENGTH_SHORT).show();
                    }catch(Exception e){};

                }
            }
        });
        sms_button = (Button) findViewById(R.id.sms_button);
        sms_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!sms_edittext.isEnabled())
                {
                    sms_button.setText("DONE");
                    sms_edittext.setEnabled(true);
                    sms_edittext.requestFocus();
                    sms_edittext.selectAll();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(sms_edittext, InputMethodManager.SHOW_IMPLICIT);

                }else
                {
                    sms_edittext.setSelectAllOnFocus(false);
                    sms_button.setText("EDIT");
                    sms_edittext.setEnabled(false);
                    if(sms_edittext.getText().toString().isEmpty())
                        sms_edittext.setText("992");
                    try {
                        writeSetting();
                        Toast.makeText(getBaseContext(),"Emergency SMS Number Edited.",Toast.LENGTH_SHORT).show();
                    }catch(Exception e){};
                }
            }
        });

        readSetting();
        led_switch.setEnabled(false);
        sound_switch.setEnabled(false);
        call_switch.setEnabled(false);
        sms_switch.setEnabled(false);
        call_button.setEnabled(false);
        sms_button.setEnabled(false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
                Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
            }
            pairedDevices = bluetoothAdapter.getBondedDevices();
            final ArrayList addressList = new ArrayList();

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);

            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
            builderSingle.setNegativeButton(
                    "Refresh",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            arrayAdapter.clear();
                            pairedDevices = bluetoothAdapter.getBondedDevices();
                            for (BluetoothDevice bt : pairedDevices) {
                                arrayAdapter.add(bt.getName());
                                addressList.add(bt.getAddress());
                            }
                            arrayAdapter.notifyDataSetChanged();
                            builderSingle.show();
                        }
                    });
            builderSingle.setTitle("Select the device");


            for (BluetoothDevice bt : pairedDevices) {
                arrayAdapter.add(bt.getName());
                addressList.add(bt.getAddress());
            }

            if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
                builderSingle.setAdapter(
                        arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String deviceAddress = addressList.get(which).toString();

                                bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
                                try {

                                    bluetoothSocket = createBluetoothSocket(bluetoothDevice);

                                } catch (IOException e) {

                                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();

                                }

                                try {
                                    bluetoothSocket.connect();
                                } catch (IOException connectException) {
                                    try {
                                        bluetoothSocket.close();
                                    } catch (IOException closeException) {
                                    }
                                }

                                mConnectedThread = new ConnectedThread(bluetoothSocket);
                                mConnectedThread.start();

                                mConnectedThread.write("x"); //test if connected
                            }
                        });
                builderSingle.show();
            }

        }

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
        //Author: YAN Tsz Kit (Student ID:54106008)
    }

    private void writeSetting()
    {//Author: YAN Tsz Kit (Student ID:54106008)
        try {
            FileOutputStream fOut = openFileOutput("config",MODE_WORLD_READABLE);
            String hardwareSetting = "";
            if(led_switch.isChecked())
            {
                fOut.write("T".getBytes());
                hardwareSetting += "T";
            }
            else
            {
                fOut.write("F".getBytes());
                hardwareSetting += "F";
            }
            if(sound_switch.isChecked())
            {
                fOut.write("T".getBytes());
                hardwareSetting += "T";
            }
            else
            {
                fOut.write("F".getBytes());
                hardwareSetting += "F";
            }
            if(call_switch.isChecked())
                fOut.write("T".getBytes());
            else
                fOut.write("F".getBytes());
            if(sms_switch.isChecked())
                fOut.write("T".getBytes());
            else
                fOut.write("F".getBytes());
            fOut.write(call_edittext.getText().toString().getBytes());
            fOut.write("C".getBytes());
            fOut.write(sms_edittext.getText().toString().getBytes());
            fOut.write("M".getBytes());
            fOut.close();
            mConnectedThread.write(hardwareSetting);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void readSetting()
    {//Author: YAN Tsz Kit (Student ID:54106008)
        try{
            FileInputStream fin = openFileInput("config");
            int c, inputcount = 0;
            String temp="";

            while( (c = fin.read()) != -1) {
                if (inputcount < 4)
                {
                    switch (inputcount)
                    {
                        case 0:
                            if ((char) c == 'T')
                                led_switch.setChecked(true);
                            else
                                led_switch.setChecked(false);
                            break;
                        case 1:
                            if ((char) c == 'T')
                                sound_switch.setChecked(true);
                            else
                                sound_switch.setChecked(false);
                            break;
                        case 2:
                            if ((char) c == 'T')
                                call_switch.setChecked(true);
                            else
                            {
                                call_switch.setChecked(false);
                                call_edittext.setVisibility(View.INVISIBLE);
                                callno_edittext.setVisibility(View.INVISIBLE);
                                call_button.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case 3:
                            if ((char) c == 'T')
                                sms_switch.setChecked(true);
                            else
                            {
                                sms_switch.setChecked(false);
                                sms_edittext.setVisibility(View.INVISIBLE);
                                smsno_textview.setVisibility(View.INVISIBLE);
                                sms_button.setVisibility(View.INVISIBLE);
                            }
                            break;
                    }
                }
                else
                {
                    if((char)c == 'C')
                    {
                        call_edittext.setText(temp);
                        temp = "";
                    }else if((char)c == 'M')
                    {
                        sms_edittext.setText(temp);
                        temp = "";
                    }
                    else
                        temp = temp + Character.toString((char)c);
                }

                inputcount++;
            }
        }
        catch(Exception e){
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void call(String phoneNumber)
    {//Author: YAN Tsz Kit (Student ID:54106008)
        {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("callno_edittext:"+phoneNumber));
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

    public void sendSMS(String phoneNumber, String msg)
    {
        //Author: YAN Tsz Kit (Student ID:54106008)
        //The number on which you want to send SMS
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
        }catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(), "Message Cannot Send", Toast.LENGTH_SHORT).show();
        }
    }


    private class ConnectedThread extends Thread {
        //Author: YAN Tsz Kit (Student ID:54106008)
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean firstTime = true;
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tempIn = null;
            OutputStream tempOut = null;


            try
            {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            }catch(IOException e){};

            mmInStream = tempIn;
            mmOutStream = tempOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            while(true)
            {
                try
                {
                    bytes = mmInStream.read(buffer);
                    String helpMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, helpMessage).sendToTarget();
                }catch(IOException e){
                    break;
                }
            }
        }

        public void write(String setting)
        {
            byte[] buffer = setting.getBytes();
            try
            {
                mmOutStream.write(buffer);
                if(firstTime)
                {
                    Toast.makeText(getBaseContext(), "Device Connected", Toast.LENGTH_LONG).show();
                    firstTime = !firstTime;
                    icon_imageview.setImageResource(R.drawable.connected);
                    led_switch.setEnabled(true);
                    sound_switch.setEnabled(true);
                    call_switch.setEnabled(true);
                    sms_switch.setEnabled(true);
                    call_button.setEnabled(true);
                    sms_button.setEnabled(true);
                    info_textview.setText("Connected.");
                    info_imageview.setImageResource(R.drawable.normalinfo);
                    info_textview.setBackgroundColor(getBaseContext().getResources().getColor(R.color.grey));
                }
            }catch (IOException e)
            {
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void permissionCheck()
    {
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
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS} , 5);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN} , 6);
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
        }catch(Exception e)
        {
            //e.printStackTrace();
        };
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
