package com.sanjose.smarttongue;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String TAG=MainActivity.class.getSimpleName();
    TextView Ivalue1,Ivalue2,Ivalue3,Itstatus;
    private BluetoothAdapter btAdapter=BluetoothAdapter.getDefaultAdapter();
    //6E400001-B5A3-F393-E0A9-E50E24DCCA9E
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler handlerBlu;
    private static String deviceaddress;
    double saltint=0.0;
    final int handlerState=0;// to identify handle message

    //  private BluetoothSocket btSocket = null;
    private  BluetoothSocket mmSocket;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread connectedThread = null;
    private ConnectThread   mmConnectThread = null;
//progress bar
private static final int PROGRESS = 0x1;

    private ProgressBar mProgress;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.progressbar_activity);

        Ivalue1 = (TextView) findViewById(R.id.value1);
        Ivalue2 = (TextView) findViewById(R.id.value2);
        Ivalue3 = (TextView) findViewById(R.id.value3);
        Itstatus=(TextView)findViewById(R.id.tstatus);
        Intent intent=getIntent();

        Button startbtn=(Button)findViewById(R.id.startbutton);

        startbtn.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v) {
                String msg="Start";
                byte[] str=msg.getBytes();
                connectedThread.write(str);

            }
        });

        deviceaddress= intent.getStringExtra(DeviceList.EXTRA_DEVICE_ADDRESS);// address of the blluetooth module
        //create device and set the MAC address

        BluetoothDevice device = btAdapter.getRemoteDevice(deviceaddress);
        if(mmConnectThread!=null)
        {
            mmConnectThread.cancel();
        }

        if(connectedThread != null)
        {
            connectedThread.cancel();
        }

        mmConnectThread = new ConnectThread(device);
        mmConnectThread.start();

        handlerBlu = new Handler() {
            public void handleMessage(android.os.Message msg) {


                if(msg.what==handlerState)

                {

                    //if message is what we want
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    Log.d("msg",readMessage);//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");  // determine the end-of-line
                    if (endOfLineIndex > 0)
                    {                         // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                       // String value3in=null;
                        char val=recDataString.charAt(0);

                        switch(val)
                        {

                            case '+' :
                                int end2=recDataString.indexOf("~");
                                String value2in=recDataString.substring(1,end2);
                                Ivalue2.setText("Conductance=" + value2in);
                                break;

                            case '&':
                                int end1= recDataString.indexOf("~");
                                String value1in=recDataString.substring(1, end1);
                                Ivalue1.setText("Temperature=" + value1in);
                                break;

                            case '$':
                                int end3=recDataString.indexOf("~");

                                String value3in =  recDataString.substring(1, end3);
                                Ivalue3.setText("Salinity=" + value3in);

                                String salt=value3in.replaceAll("[^\\d.]", "");
                                Log.d("sal",salt);
                                saltint = Double.parseDouble(salt);

                               /* if (saltint < 0.5) {
                                    Toast.makeText(getApplicationContext(), "Low", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "High", Toast.LENGTH_SHORT).show();
                                }
*/


                                break;

                            case 'S':
                                Itstatus.setText("Calculating..");
                                break;

                            case 'D':
                                Itstatus.setText("Calculated");
                               // Log.d("salt", value3in);

                                break;
                            case 'N':

                                Itstatus.setText("");
                                break;
                            default:
                                break;



                        }


                        recDataString.delete(0, recDataString.length());
                        dataInPrint = "";


                    }



                }
            }
        };


        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setMax(2);
        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (mProgressStatus < 100) {
                    mProgressStatus =(int)saltint;

                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgress.setProgress(mProgressStatus);
                        }
                    });
                }
            }
        }).start();

    };



    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }


   /* private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }*/

    private class ConnectThread extends Thread {

        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            mmDevice = device;
            BluetoothSocket tmp = null;


            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(BTMODULEUUID);

            } catch (IOException e) {
                Log.e("socket create","failed");
            }

            try {
                tmp = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                Log.d("connect", "newtmp");//delete later
            }
            catch(Exception e)
            {

            }
            mmSocket = tmp;
            Log.d("connect", "tmp");
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            Log.i("connectthread","beginconnectthread");
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d("connectthread","before socket connect");
                mmSocket.connect();
                Log.d("connectthread","after socket connect");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out

                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("connectthread1","not able to close the socket");
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            Log.d("connectedt","going to call connected");
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();


        }


        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d("Myapp", "connected");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI activity
                    handlerBlu.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try
            {
                Log.d("strt","hi");
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        } //uncomment later
    }


}

