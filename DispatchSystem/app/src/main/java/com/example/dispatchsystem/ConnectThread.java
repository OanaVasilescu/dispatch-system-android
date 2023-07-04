package com.example.dispatchsystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private static final String TAG = "FrugalLogs";
    public static Handler handler;
    private final static int ERROR_READ = 0;

    private UUID id;
    private BluetoothDevice device;

    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, UUID MY_UUID, Handler handler) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        this.handler = handler;
        id = MY_UUID;
        this.device = device;


        Log.e(TAG, "I'm here ");
        try {
            Log.e(TAG, "I'm here . It tries");
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

            // open API
//            tmp = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
//            // if failed: hidden API
//            createMethod = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] { int.class });
//            tmp = (BluetoothSocket)createMethod.invoke(device, 1);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run() {

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            Log.e(TAG, "Success.");
        } catch (IOException connectException) {
            Log.e(TAG, "Unable to connect; close the socket and return.");
            // Unable to connect; close the socket and return.
            handler.obtainMessage(ERROR_READ, "Unable to connect to the BT device").sendToTarget();
            Log.e(TAG, "connectException: " + connectException);
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket or new conn failed", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public BluetoothSocket getMmSocket(){
        return mmSocket;
    }
}
