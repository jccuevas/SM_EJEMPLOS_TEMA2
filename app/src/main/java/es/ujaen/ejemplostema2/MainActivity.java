package es.ujaen.ejemplostema2;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static final String FRAGMENTO_DETALLES = "detalles";

    public static final int MENU_CONTEXTUAL_AYUDA = 1;

    FragmentManager mFM = null;


    //NFC
    private IntentFilter[] intentFiltersArray=null;
    private String[][] techListsArray=null;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mFM = getSupportFragmentManager();


        Fragment f = mFM.findFragmentById(R.id.main_container);
        if (f == null) showHelpFragment();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelpFragment();

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //NFC initialization
        mAdapter= NfcAdapter.getDefaultAdapter(this);

        //Se prepara la actividad para recibir el Intent cuando está en primer plano
        iniNFCForeground();

        //Si la aplicación se abre al leer una etiqueta recibirá un Intent con la información
        // que será procesada con onNewIntent()
        Intent nfcIntent = getIntent();
        if(nfcIntent!=null)
            onNewIntent(nfcIntent);
    }

    public void showHelpFragment() {
        FragmentTransaction ft = mFM.beginTransaction();
        FragmentoInfo info = new FragmentoInfo();
        Fragment f = mFM.findFragmentById(R.id.main_container);
        if (f != null && !FragmentoInfo.class.isInstance(f)) {
            ft.remove(f);
            ft.replace(R.id.main_container, info);
        } else {
            ft.add(R.id.main_container, info, "INFO");
        }
        ft.addToBackStack(null);
        ft.commit();
    }


    public void showAboutFragment() {
        FragmentTransaction ft = mFM.beginTransaction();
        FragmentoAcercade about = new FragmentoAcercade();
        Fragment f = mFM.findFragmentById(R.id.main_container);
        if (f != null && !FragmentoAcercade.class.isInstance(f)) {
            ft.remove(f);
            ft.replace(R.id.main_container, about);
        } else {
            ft.add(R.id.main_container, about, "ABOUT");
        }
        ft.addToBackStack(null);
        ft.commit();
    }

    public void showNFCFragment() {
        FragmentTransaction ft = mFM.beginTransaction();
        FragmentoNFC nfc = new FragmentoNFC();
        Fragment f = mFM.findFragmentById(R.id.main_container);
        if (f != null && !FragmentoAcercade.class.isInstance(f)) {
            ft.remove(f);
            ft.replace(R.id.main_container, nfc);
        } else {
            ft.add(R.id.main_container, nfc, "ABOUT");
        }
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Inicializar el Intent para cuando la actividad captura el evento ACTION_NDEF_DISCOVERED estando en primer plano
     */
    private void iniNFCForeground(){
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("text/*");    /* Handles all MIME based dispatches.
	                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[] {ndef, };

        techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
    }


    /**
     * Se controla el evento de pulsación de la tecla de volver, haciendo que si está abierto el
     * menú lateral se cierre, y si ya está cerrado se actúe por defecto
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Ejemplo de creación de un submenú
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_help:
                showHelpFragment();
                return true;
            case R.id.action_about:
                showAboutFragment();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CONTEXTUAL_AYUDA:
                showHelpFragment();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_bluetooth:
                //Opción mostrar fragmento de manejo de Bluetooth
                Intent btactivity = new Intent(this, ActivityBluetooth.class);
                startActivity(btactivity);
                break;
            case R.id.nav_wifipower:
                //Opción mostrar fragmento de manejo de Bluetooth
                Intent wifiactivity = new Intent(this, Connectivity.class);
                startActivity(wifiactivity);
                break;
            case R.id.nav_help:
                //Opción mostrar fragmento de manejo de Bluetooth
                showHelpFragment();
                break;
            case R.id.nav_about:
                //Opción mostrar fragmento de manejo de Bluetooth
                showAboutFragment();
                break;
            case R.id.nav_nfc:
                showNFCFragment();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);

    }

    /**
     *
     */
    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    /**
     * Procesa el Intent recibido cuando se lee una etiqueta NFC
     * @param intent
     */
    public void onNewIntent(Intent intent) {
        String result;

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                result=analizaMensajes(rawMsgs);
            }else
                result="ERROR leyendo tarjeta NFC";

            Toast.makeText(this,"Tarjeta NFC "+result,Toast.LENGTH_LONG).show();
//            nfctext.setText(result);
//            statustext.setText(getResources().getString(
//                    R.string.nfcmain_tagdetected));
//            nfctext.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    nfctext.setText("");
//                }
//
//            }, 10000);
//            statustext.postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    statustext.setText("");
//                }
//            }, 5000);

        }
    }

    /**
     * Extrae el contenido de las etiquetas NFC leídas
     * @param rawMsgs
     * @return
     */
    private String analizaMensajes(Parcelable[] rawMsgs){
        NdefMessage msgs[];
        StringBuffer textmessages = new StringBuffer();
        int n;

        msgs = new NdefMessage[rawMsgs.length];


        //for (NdefMessage ndefMessage:(NdefMessage[])rawMsgs){
        for(int i = 0; i < rawMsgs.length; i++) {
            msgs[i] = (NdefMessage) rawMsgs[i];
            NdefRecord ndefr[] = msgs[i].getRecords();

            for (NdefRecord record: ndefr) {

                if (record.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
                    String mimetype = new String(record.getType());
                    String textcontent = new String(
                            record.getPayload());
                    textmessages.append("Message MIME="
                            + mimetype + "\r\nContent=" + textcontent
                            + "\r\n");
                }

                if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
                    String rtdtype = new String(record.getType());

                    if (rtdtype.equals(new String(NdefRecord.RTD_TEXT))) {

                        byte languagelen = (byte) ((record
                                .getPayload()[0]) & 0x1f);
                        String country = new String(
                                record.getPayload(), 1, languagelen);
                        String textcontent = new String(
                                record.getPayload(), 1 + languagelen,
                                record.getPayload().length-1-languagelen);

                        textmessages.append("Message RTD_TEXT\r\nLanguage="
                                + country + "\r\nContent="
                                + textcontent + "\r\n");
                    }
                    if (rtdtype.equals(new String(NdefRecord.RTD_URI))) {

                        byte languagelen = (byte) ((record
                                .getPayload()[0]) & 0x1f);
                        String country = new String(
                                record.getPayload(), 1, languagelen);
                        String textcontent = new String(
                                record.getPayload(), 1 + languagelen,
                                record.getPayload().length-1-languagelen);

                        textmessages.append("Message RTD_TEXT\r\nLanguage="
                                + country + "\r\nContent="
                                + textcontent + "\r\n");
                    }
                }
                textmessages.insert(0,"Number of records=" + ndefr.length + "\r\n");
            }

        }

        return textmessages.toString();

    }

}
