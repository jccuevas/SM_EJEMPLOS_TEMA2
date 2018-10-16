package es.ujaen.ejemplostema2;

import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Connectivity extends AppCompatActivity {

    private static final String TAG = "Connectivity";
    private static final int REQUEST_COARSE_LOCATION = 1;
    WifiManager wifi;
    private WiFiScanReceiver receiver;

    private TextView textStatus;
    Button buttonScan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_connectivity);

        // Setup UI
        textStatus = findViewById(R.id.connectivity_textView_result);

        // Setup WiFi
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Get WiFi status
        WifiInfo info = wifi.getConnectionInfo();
        textStatus.append("\n\nWiFi Status: " + info.toString());

        // List available networks
        List<WifiConfiguration> configs = wifi.getConfiguredNetworks();

        for (WifiConfiguration config : configs) {
            textStatus.append("\n\n" + config.toString());
        }

        // Se registra el Broadcast Receiver para detectar
        //el final de la búsqueda de redes wifi
        receiver = new WiFiScanReceiver(this);

        registerReceiver(receiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Log.d(TAG, "onCreate()");

    }

    private void checkCoarseLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_COARSE_LOCATION);


                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_COARSE_LOCATION);
                }
            }else
            {
                startScan();
            }
        }else{
            startScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan();
                } else {
                   Toast.makeText(this, "El ejemplo de audio necesita del permiso para buscar redes", Toast.LENGTH_LONG).show();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onScan(View view) {
        checkCoarseLocation();
    }

    private void startScan(){
        Toast.makeText(
                this,
                this.getResources().getString(
                        R.string.connectivity_code_startscan),
                Toast.LENGTH_LONG).show();

        Log.d(TAG, "onClick() wifi.startScan()");
        wifi.startScan();
    }

}
