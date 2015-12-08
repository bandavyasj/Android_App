package com.sanjose.smarttongue;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceList extends Activity {
    TextView textConnectionStatus;
    ListView pairedListView;

    private BluetoothAdapter btAdapter=BluetoothAdapter.getDefaultAdapter();
    ArrayAdapter<String> pairedDevicesArrayAdapter;
    public final static String EXTRA_DEVICE_ADDRESS="device_address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
      //  Toast.makeText(getApplicationContext(), "hi", Toast.LENGTH_SHORT).show();
        Init();

    }

    private void Init()
    {
        textConnectionStatus = (TextView) findViewById(R.id.connecting);
        textConnectionStatus.setTextSize(40);
        //Intialoze array adapter for paired devices
        pairedDevicesArrayAdapter= new ArrayAdapter<String>(this, R.layout.device_name);
        pairedListView=(ListView)findViewById(R.id.listView);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(deviceClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBTstatus();
    }

    //method to check if the device has Bluetooth and if it is on.
    //Prompts the user to turn it on if it is off
    private void checkBTstatus()
    {
        //check that device has bluetooth and that it is turned on

        if(btAdapter==null)
        {
            Toast.makeText(getBaseContext(), "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            //prompt user to turn on the bluetooth
            if(!btAdapter.isEnabled())// check if bluetoot is enabled
            {
                Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,1);// to request that bluetooth be enabled

            }
            pairedDevicesArrayAdapter.clear();//clears the array so items aren't duplicated when resuming from onPause
            textConnectionStatus.setText(" ");
            getPairedDevices();
            startDiscovery();
        }

    }
    private void startDiscovery() {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }
    private void getPairedDevices()
    {

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        else
        {
            pairedDevicesArrayAdapter.add("no devices paired");
        }

    }

    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {

            textConnectionStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String str=pairedDevicesArrayAdapter.getItem(position);
            String macStr=str.substring(str.lastIndexOf("\n")+1);

            //String info = ((TextView) v).getText().toString();
           // String address = info.substring(info.length() - 17);

            // Make an intent to start next activity while taking an extra which is the MAC address.
            Intent i = new Intent(DeviceList.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, macStr);
            startActivity(i);
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
