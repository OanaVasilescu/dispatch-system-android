package com.example.dispatchsystem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.dispatchsystem.api.RetrofitClient;
import com.example.dispatchsystem.model.ArduinoData;
import com.example.dispatchsystem.model.Credentials;
import com.example.dispatchsystem.model.Globals;
import com.example.dispatchsystem.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectedThread  extends Thread{
    private static final String TAG = "FrugalLogs";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String valueRead;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        //Input and Output streams members of the class
        //We wont use the Output stream of this project
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public String getValueRead(){
        return valueRead;
    }

    public void run() {

        byte[] buffer = new byte[1024];
        int bytes = 0; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        //We just want to get 1 temperature readings from the Arduino
        while (true) {
            try {
                buffer[bytes] = (byte) mmInStream.read();
                String readMessage;
                // If I detect a "\n" means I already read a full measurement
                if (buffer[bytes] == '\n') {
                    readMessage = new String(buffer, 0, bytes);
                    Log.e(TAG, readMessage);
                    //Value to be read by the Observer streamed by the Obervable
                    valueRead=readMessage;
                    bytes = 0;

                    ArduinoData arduinoData = new ArduinoData();
                    arduinoData.setData(valueRead);
                    arduinoData.setUserId(Globals.currentUser.getId().toString());
                    makeCall(arduinoData);
                } else {
                    bytes++;
                }

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }

    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }



    private void makeCall (ArduinoData arduinoData){
        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getAPI()
                .arduinoData(arduinoData);

        call.enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Boolean success;
                success = response.isSuccessful();
                int requestCode = response.code();

            }
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("failure: " + t.getMessage());
            }
        });
    }
}
