package com.example.dispatchsystem;

import static java.util.UUID.fromString;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    // Global variables we will use in the
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    //We will use a Handler to get the BT Connection statys
    public static Handler handler;
    private final static int ERROR_READ = 0; // used in bluetooth handler to identify message update
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable

    private BluetoothLeScanner btScanner;
    BluetoothGatt bluetoothGatt;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //Intances of BT Manager and BT Adapter needed to work with BT in Android.
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        btScanner = bluetoothAdapter.getBluetoothLeScanner();
        //Intances of the Android UI elements that will will use during the execution of the APP
        TextView btReadings = findViewById(R.id.btReadings);
        TextView btDevices = findViewById(R.id.btDevices);
        Button connectToDevice = (Button) findViewById(R.id.connectToDevice);
        Button seachDevices = (Button) findViewById(R.id.seachDevices);
        Button clearValues = (Button) findViewById(R.id.refresh);
        Log.d(TAG, "Begin Execution");

        checkPermissions();

        //Using a handler to update the interface in case of an error connecting to the BT device
        //My idea is to show handler vs RxAndroid
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case ERROR_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        btReadings.setText(arduinoMsg);
                        break;
                }
            }
        };

        // Set a listener event on a button to clear the texts
        clearValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btDevices.setText("");
                btReadings.setText("");
                stopScanning();
            }
        });

        // Create an Observable from RxAndroid
        //The code will be executed when an Observer subscribes to the the Observable
        @SuppressLint("MissingPermission") final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.e(TAG, "Calling connectThread class");
            //Call the constructor of the ConnectThread class
            //Passing the Arguments: an Object that represents the BT device,
            // the UUID and then the handler to update the UI
            Log.e(TAG, arduinoUUID.toString() + arduinoBTModule.getName());
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run();
            //Check if Socket connected
            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");
                //The pass the Open socket as arguments to call the constructor of ConnectedThread
                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();
                if (connectedThread.getValueRead() != null) {
                    // If we have read a value from the Arduino
                    // we call the onNext() function
                    //This value will be observed by the observer
                    emitter.onNext(connectedThread.getValueRead());
                }
                //We just want to stream 1 value, so we close the BT stream
                connectedThread.cancel();
            }
            // SystemClock.sleep(5000); // simulate delay
            //Then we close the socket connection
            connectThread.cancel();
            //We could Override the onComplete function
            emitter.onComplete();

        });

        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btReadings.setText("");
                if (arduinoBTModule != null) {
                    //We subscribe to the observable until the onComplete() is called
                    //We also define control the thread management with
                    // subscribeOn:  the thread in which you want to execute the action
                    // observeOn: the thread in which you want to get the response

//                    connectToDeviceSelected();

//
                    connectToBTObservable.
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribeOn(Schedulers.io()).
                            subscribe(valueRead -> {
                                //valueRead returned by the onNext() from the Observable
                                btReadings.setText(valueRead);
                                //We just scratched the surface with RxAndroid
                            });

                }
            }
        });

        seachDevices.setOnClickListener(new View.OnClickListener() {
            //Display all the linked BT Devices
            @Override
            public void onClick(View view) {
                //Check if the phone supports BT
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    Log.d(TAG, "Device doesn't support Bluetooth");
                } else {
                    Log.d(TAG, "Device support Bluetooth");
                    //Check BT enabled. If disabled, we ask the user to enable BT
                    if (!bluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth is disabled");
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.d(TAG, "We don't BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            Log.d(TAG, "Bluetooth is enabled now");
                        } else {
                            Log.d(TAG, "We have BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            Log.d(TAG, "Bluetooth is enabled now");
                        }

                    } else {
                        Log.d(TAG, "Bluetooth is enabled");
                    }
                    String btDevicesString = "";


                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();


                    startScanning();

                }
                Log.d(TAG, "Button Pressed");
            }
        });
    }

    private void checkPermissions() {
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }


    private ScanCallback leScanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Button connectToDevice = (Button) findViewById(R.id.connectToDevice);
            TextView btDevices = findViewById(R.id.btDevices);
            Log.e("tag", "Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");

            try {
               if( createBond(result.getDevice())){
                   Log.e(TAG, "created bond");
                   String deviceName = result.getDevice().getName();
                   String deviceHardwareAddress = result.getDevice().getAddress(); // MAC address
                   Log.d(TAG, "deviceName:" + deviceName);

                   if (deviceName != null) {
                       Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                       //We append all devices to a String that we will display in the UI
//                btDevicesString = btDevicesString + deviceName + " || " + deviceHardwareAddress + "\n";
                       //If we find the HC 05 device (the Arduino BT module)
                       //We assign the device value to the Global variable BluetoothDevice
                       //We enable the button "Connect to HC 05 device"
                       if (deviceName.equals("MLT-BT05")) {
                           Log.d(TAG, "MLT-BT05 found");
//                    arduinoUUID = fromString(getUUID(result));

//                    List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
//                    arduinoUUID = uuids.get(0).getUuid();
                           Log.e(TAG, getUUID(result));

                           arduinoBTModule = result.getDevice();
                           //HC -05 Found, enabling the button to read results


                           connectToDevice.setEnabled(true);
                           stopScanning();
                           btDevices.setText("MLT-BT05 found");
                       }

                   }
               } else {
                   btDevices.setText("MLT-BT05 could not be paired");
               }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    };

    public String getUUID(ScanResult result){
        String UUIDx = UUID
                .nameUUIDFromBytes(result.getScanRecord().getBytes()).toString();
        return UUIDx;
    }


    public void startScanning() {
        System.out.println("start scanning");
        AsyncTask.execute(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);


            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        AsyncTask.execute(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }
}