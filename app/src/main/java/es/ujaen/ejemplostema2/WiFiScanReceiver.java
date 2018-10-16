package es.ujaen.ejemplostema2;

import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiScanReceiver extends BroadcastReceiver {
    private static final String TAG = "WiFiScanReceiver";
    Connectivity wifiDemo;

    public WiFiScanReceiver(Connectivity wifiDemo) {
        super();
        this.wifiDemo = wifiDemo;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        if (intent!=null && intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

            List<ScanResult> results = wifiDemo.wifi.getScanResults();
            ScanResult bestSignal = null;
            String redes = "";

            ViewGroup window = (ViewGroup) wifiDemo.findViewById(R.id.connectivity_wifipower);

            if (results != null) {
                if(results.size()>0) {
                    window.removeAllViews();

                    for (ScanResult result : results) {

                        final TableRow row = (TableRow) wifiDemo.getLayoutInflater().inflate(R.layout.listview_row_wifipower, null);

                        row.setTag(result.SSID + ":" + result.frequency);
                        final ProgressBar power = (ProgressBar) row.findViewById(R.id.network_url_downloadprogress);
                        Drawable draw = wifiDemo.getResources().getDrawable(R.drawable.progress_wifipower);
                        // set the drawable as progress bar

                        power.setProgressDrawable(draw);
                        power.setMax(100);
                        power.setProgress(100 + result.level);

                        final TextView ssid = (TextView) row.findViewById(R.id.wifipower_ssid);
                        ssid.setText(result.SSID);

                        final TextView dbm = (TextView) row.findViewById(R.id.wifipower_power);

                        dbm.setText(String.format(Locale.getDefault(),"%d dBm",result.level));

                        final TextView mhz = (TextView) row.findViewById(R.id.wifipower_frecuency);
                        mhz.setText(String.format(Locale.getDefault(),"%d MHz",result.frequency));


                        if (bestSignal == null
                                || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0) {
                            bestSignal = result;
                        }


                        window.addView(row);
                        redes = redes + "Red " + result.SSID + " Señal=" + result.level + "dBm\r\n";
                    }


                    String message = String.format("%s redes encontradas. %s es la que tiene mejor señal.\r\n" + redes,
                            results.size(), bestSignal.SSID);
                    Toast.makeText(wifiDemo, message, Toast.LENGTH_LONG).show();


                    Log.d(TAG, "onReceive() message: " + message);
                }else
                    Log.d(TAG, "onReceive() message: NO HAY REDES");
            }

        }
    }

}