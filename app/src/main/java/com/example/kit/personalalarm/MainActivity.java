package com.example.kit.personalalarm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Set;

import static android.R.attr.button;
import static java.sql.Types.NULL;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ImageView icon;
    Switch emgencyCall;
    EditText editCall;
    Button buttonCall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        icon = (ImageView) findViewById(R.id.icon);
        emgencyCall = (Switch) findViewById(R.id.switch3);
        emgencyCall.setChecked(true);
        editCall = (EditText) findViewById(R.id.editcall);
        editCall.setEnabled(false);
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
                }
            }
        });
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


}
