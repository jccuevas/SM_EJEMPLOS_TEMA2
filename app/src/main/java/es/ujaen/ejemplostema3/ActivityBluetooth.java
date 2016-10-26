package es.ujaen.ejemplostema3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class ActivityBluetooth extends AppCompatActivity {

    public static final int  REQUEST_ENABLE_BT = 100;
    private BroadcastReceiver mReceiver=null;
    private BluetoothAdapter mBTadapter = null;
    private ListView mDeviceList = null;
    private ArrayAdapter<String> mArrayAdapter = null;
    public static final String[] DEFAULT_VALUES = new String[]{"no hay dispositivos"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);


        mDeviceList = (ListView)findViewById(R.id.bluetooth_list);
        mArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        mDeviceList.setAdapter(mArrayAdapter);

        mDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mac = mArrayAdapter.getItem(position);
                mac= mac.substring(mac.indexOf("MAC="));
                Toast.makeText(ActivityBluetooth.this,"Emparejando con "+mac+"...",Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void initializeReceiver(){
        mReceiver= new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    String deviceName= device.getName();
                    String deviceAddress= device.getAddress();
                    mArrayAdapter.add(deviceName + "\nMAC=" + deviceAddress);
                    mArrayAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode==RESULT_OK){//El usuario ha activado el BT

                    initializeReceiver();
                    // Register the BroadcastReceiver
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                }else
                    Toast.makeText(this,getString(R.string.fragment_bt_error_user),Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void onSearch(View view){
        mBTadapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTadapter != null) {
            if (!mBTadapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else{
                if(mReceiver==null) {
                    initializeReceiver();
                    // Register the BroadcastReceiver
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                }
                if(!mBTadapter.isDiscovering()) {
                    mArrayAdapter.clear();
                    mArrayAdapter.notifyDataSetChanged();
                    mBTadapter.startDiscovery();
                }
                else
                    Toast.makeText(this, getString(R.string.fragment_bt_discovering), Toast.LENGTH_LONG).show();            }
        } else {
            Toast.makeText(this, getString(R.string.fragment_bt_error_nobt), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
            mReceiver=null;
        }
    }
}
