package es.ujaen.ejemplostema3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class ActivityBluetooth extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT = 100;
    private BroadcastReceiver mReceiver = null;
    private BluetoothAdapter mBTadapter = null;
    private ListView mDeviceList = null;
    private TextView mMessages= null;
    private ArrayAdapter<String> mArrayAdapter = null;
    private ArrayList<BluetoothDevice> mDevices=null;
    private Button mServerButton = null;
    public static final String[] DEFAULT_VALUES = new String[]{"no hay dispositivos"};

    private AcceptThread mBTServer = null;

    public static final String SERVICIO = "MensajesBT";
    public static final UUID SERVICIO_UUID = UUID.fromString("11000000-0011-1100-AAAA-0123456789AB");//Código en formato XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX


    private Handler mHandler = null;

    public static final int MESSAGE_NEWBTDATA = 1;
    public static final String MESSAGE_NEWBTDATA_ID = "btmessage";


    private BluetoothServerSocket mServerSocket = null;
    private BluetoothSocket mSocket = null;
    private boolean isServer = false;

    ConnectedThread mBTClient = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_bluetooth);

        mMessages = (TextView)findViewById(R.id.bluetooth_messages);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                String respuesta = "";
                // Obtiene el mensaje de la hebra de conexión.
                switch (inputMessage.what) {
                    case MESSAGE_NEWBTDATA:
                        respuesta = inputMessage.getData().getString(MESSAGE_NEWBTDATA_ID);
                        //Toast.makeText(getApplicationContext(), "Recibido : " + respuesta, Toast.LENGTH_LONG).show();
                        String text = mMessages.getText()+"> "+respuesta+"\r\n";
                        mMessages.setText(text);
                        break;
                }
            }

        };


        Button search = (Button)findViewById(R.id.bluetooth_search);
        search.requestFocus();
        mDevices = new ArrayList<BluetoothDevice>(1);
        mBTadapter = BluetoothAdapter.getDefaultAdapter();

        mDeviceList = (ListView) findViewById(R.id.bluetooth_list);
        mArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        mDeviceList.setAdapter(mArrayAdapter);

        mDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mac = mArrayAdapter.getItem(position);
                BluetoothDevice bdtemp=mDevices.get(position);
                if(bdtemp!=null) {
                    mac = bdtemp.getAddress();//mac.substring(mac.indexOf("MAC=")+ "MAC=".length());
                    //mac = mac.substring(0,mac.indexOf("\n"));

                    BluetoothDevice destino = mBTadapter.getRemoteDevice(mac);
                    if (destino != null) {
                        if (mSocket == null) {
                            Toast.makeText(ActivityBluetooth.this, getString(R.string.fragment_bt_pairing)+ ": "+mac + "...", Toast.LENGTH_LONG).show();
                            new ConnectThread(destino).start();
                        } else {
                            Toast.makeText(ActivityBluetooth.this, getString(R.string.fragment_bt_closing) + mac + "...", Toast.LENGTH_LONG).show();
                            try {
                                mSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                mSocket = null;
                                mBTClient = null;
                            }
                        }
                    }
                }else{
                    Toast.makeText(ActivityBluetooth.this, getString(R.string.fragment_bt_nodevice), Toast.LENGTH_LONG).show();
                }
            }
        });

        mServerButton = (Button) findViewById(R.id.bluetooth_server);
        mServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBTServer == null) {
                    Toast.makeText(ActivityBluetooth.this, getString(R.string.bluetooth_server_start), Toast.LENGTH_LONG).show();
                    mBTServer = new AcceptThread();
                    mBTServer.start();
                    mServerButton.setText(getString(R.string.bluetooth_server_stop));
                } else {
                    Toast.makeText(ActivityBluetooth.this, getString(R.string.bluetooth_server_stop), Toast.LENGTH_LONG).show();
                    mBTServer.interrupt();
                    mBTServer = null;
                    try {
                        mServerSocket.close();
                        mServerSocket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mServerButton.setText(getString(R.string.bluetooth_server_start));
                    }
                }


            }
        });

        Button enviar = (Button) findViewById(R.id.bluetooth_send);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText texto = (EditText) findViewById(R.id.bluetooth_text);
                String textoEnviar = texto.getEditableText().toString();

                if (mBTClient != null) {
                    mBTClient.write(textoEnviar);
                }

            }
        });
    }

    protected void initializeReceiver() {
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();

                    String line = deviceName + "\nMAC=" + deviceAddress + "\n";
                    BluetoothClass clase = device.getBluetoothClass();
                    if (clase.hasService(BluetoothClass.Service.AUDIO))
                        line = line + "AUDIO ";
                    if (clase.hasService(BluetoothClass.Service.NETWORKING))
                        line = line + "NETWORKING ";
                    if (clase.hasService(BluetoothClass.Service.TELEPHONY))
                        line = line + "TELEPHONY ";
                    if (clase.hasService(BluetoothClass.Service.OBJECT_TRANSFER))
                        line = line + "OBJECT TRANSFER ";

                    mDevices.add(device);
                    mArrayAdapter.add(line);
                    mArrayAdapter.notifyDataSetChanged();
                }
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //Ha comenzado la búsqueda
                    Toast.makeText(ActivityBluetooth.this, getString(R.string.bluetooth_search_start), Toast.LENGTH_LONG).show();
                    mDeviceList.setBackgroundColor(Color.RED);
                }
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //Ha comenzado la búsqueda
                    Toast.makeText(ActivityBluetooth.this, getString(R.string.bluetooth_search_end), Toast.LENGTH_LONG).show();
                    mDeviceList.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {//El usuario ha activado el BT

                    initializeReceiver();
                    // Register the BroadcastReceiver
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                } else
                    Toast.makeText(this, getString(R.string.fragment_bt_error_user), Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void onSearch(View view) {

        if (mBTadapter != null) {
            if (!mBTadapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            } else {
                if (mReceiver == null) {
                    initializeReceiver();
                    // Register the BroadcastReceiver
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    IntentFilter filterStartDiscovery = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    IntentFilter filterEndDiscovery = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                    registerReceiver(mReceiver, filterStartDiscovery); // Don't forget to unregister during onDestroy
                    registerReceiver(mReceiver, filterEndDiscovery); // Don't forget to unregister during onDestroy
                }
                if (!mBTadapter.isDiscovering()) {
                    mArrayAdapter.clear();
                    mDevices.clear();
                    mMessages.setText("");
                    mArrayAdapter.notifyDataSetChanged();
                    mBTadapter.startDiscovery();

                } else
                    Toast.makeText(this, getString(R.string.fragment_bt_discovering), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.fragment_bt_error_nobt), Toast.LENGTH_LONG).show();
        }

    }


    public void enviarMensaje(String mensaje) {
        Message recibido = Message.obtain(mHandler, MESSAGE_NEWBTDATA);
        Bundle datos = new Bundle();
        datos.putString(MESSAGE_NEWBTDATA_ID, new String(mensaje));
        recibido.setData(datos);
        recibido.sendToTarget();
    }

    public void enviarMensajeBT(BluetoothSocket bts, String mensaje) {
        try {
            OutputStream os = bts.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(mensaje);
            dos.flush();
            //os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    /**
     * Clase que administra el servidor
     */
    private class AcceptThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBTadapter.listenUsingRfcommWithServiceRecord(SERVICIO, SERVICIO_UUID);
                isServer = true;
                mServerSocket = tmp;
            } catch (IOException e) {
                isServer = false;
                mServerSocket = null;
            }

        }

        public void run() {
            BluetoothSocket socket = null;

            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            while (true) {
                try {
                    socket = mServerSocket.accept();
                    // If a connection was accepted
                    if (socket != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ActivityBluetooth.this, "Conexión entrante", Toast.LENGTH_SHORT).show();
                            }
                        });

                        new ConnectedThread(socket).start();

                        enviarMensajeBT(socket, "Bienvenido!");
                        for (int n = 1; n <= 10; n++) {
                            try {
                                // Read from the InputStream
                                enviarMensajeBT(socket, "Mensaje " + n);
                                sleep(2000);

                            } catch (InterruptedException e) {
                                break;
                            }
                        }

                        socket.close();

                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e("ActivityBluetooth", e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(SERVICIO_UUID);
            } catch (IOException e) {
                Log.e("ActivityBluetooth", e.getMessage());
            }
            mSocket = tmp;

        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBTadapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mSocket.close();
                    mSocket = null;
                } catch (IOException closeException) {
                    Log.e("ActivityBluetooth", closeException.getMessage());
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mBTClient = new ConnectedThread(mSocket);
            mBTClient.start();
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
            } finally {
                mSocket = null;
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("ActivityBluetooth", e.getMessage());
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            DataInputStream dis = new DataInputStream(mmInStream);
            while (true) {
                try {

                    String buffer = dis.readUTF();
                    enviarMensaje(buffer);

                } catch (IOException e) {
                    Log.e("ActivityBluetooth", e.getMessage());
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String mensaje) {
            try {
                DataOutputStream dos = new DataOutputStream(mmOutStream);
                dos.writeUTF(mensaje);
                dos.flush();
            } catch (IOException e) {
                Log.e("ActivityBluetooth", e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("ActivityBluetooth", e.getMessage());
            }
        }
    }
}
