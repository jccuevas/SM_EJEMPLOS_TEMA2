package es.ujaen.ejemplostema2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class NFCMainActivity extends Activity {

	private TextView nfctext = null;
	private TextView statustext = null;
	private IntentFilter[] intentFiltersArray=null;
	private String[][] techListsArray=null;
	private NfcAdapter mAdapter;
	private PendingIntent pendingIntent=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfcmain);

		nfctext = (TextView) findViewById(R.id.nfctabout_textview_centertext);
		statustext = (TextView) findViewById(R.id.nfcabout_departament);
		
		mAdapter= NfcAdapter.getDefaultAdapter(this);

		//Se prepara la actividad para recibir el Intent cuando está en primer plano
        iniNFCForeground();

        //Si la aplicación se abre al leer una etiqueta recibirá un Intent con la información
		// que será procesada con onNewIntent()
        Intent nfcIntent = getIntent();
        if(nfcIntent!=null)
            onNewIntent(nfcIntent);

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
        catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[] {ndef, };

        techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfcmain, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		

		return super.onMenuItemSelected(featureId, item);
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

            nfctext.setText(result);
            statustext.setText(getResources().getString(
                    R.string.nfcmain_tagdetected));
            nfctext.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nfctext.setText("");
                }

            }, 10000);
            statustext.postDelayed(new Runnable() {

                @Override
                public void run() {
                    statustext.setText("");
                }
            }, 5000);

		}
	}

    /**
     * Extrae el contenido de las etiquetas NFC leídas
     * @param rawMsgs
     * @return
     */
	private String analizaMensajes(Parcelable[] rawMsgs){
		NdefMessage msgs[];
		String textmessages="";
		int n;

		msgs = new NdefMessage[rawMsgs.length];

		for (int i = 0; i < rawMsgs.length; i++) {
			msgs[i] = (NdefMessage) rawMsgs[i];
			NdefRecord ndefr[] = msgs[i].getRecords();
			for (n = 0; n < ndefr.length; n++) {

				if (ndefr[n].getTnf() == NdefRecord.TNF_MIME_MEDIA) {
					String mimetype = new String(ndefr[n].getType());
					String textcontent = new String(
							ndefr[n].getPayload());
					textmessages = textmessages + "Message MIME="
							+ mimetype + "\r\nContent=" + textcontent
							+ "\r\n";
				}

				if (ndefr[n].getTnf() == NdefRecord.TNF_WELL_KNOWN) {
					String rtdtype = new String(ndefr[n].getType());

					if (rtdtype.equals(new String(NdefRecord.RTD_TEXT))) {

						byte languagelen = (byte) ((ndefr[n]
								.getPayload()[0]) & 0x1f);
						String country = new String(
								ndefr[n].getPayload(), 1, languagelen);
						String textcontent = new String(
								ndefr[n].getPayload(), 1 + languagelen,
								ndefr[n].getPayload().length-1-languagelen);

						textmessages = textmessages
								+ "Message RTD_TEXT\r\nLanguage="
								+ country + "\r\nContent="
								+ textcontent + "\r\n";
					}
                    if (rtdtype.equals(new String(NdefRecord.RTD_URI))) {

                        byte languagelen = (byte) ((ndefr[n]
                                .getPayload()[0]) & 0x1f);
                        String country = new String(
                                ndefr[n].getPayload(), 1, languagelen);
                        String textcontent = new String(
                                ndefr[n].getPayload(), 1 + languagelen,
                                ndefr[n].getPayload().length-1-languagelen);

                        textmessages = textmessages
                                + "Message RTD_TEXT\r\nLanguage="
                                + country + "\r\nContent="
                                + textcontent + "\r\n";
                    }
				}
				textmessages = "Number of records=" + n + "\r\n" + textmessages;
			}

		}

		return textmessages;

	}
}
