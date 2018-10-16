package es.ujaen.ejemplostema2;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;


public class FragmentoInfo extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.layout_fragment_info, null);

        WebView mInfo = fragment.findViewById(R.id.fragmentinfo_helptext);
        mInfo.loadUrl("file:///android_asset/www/help.html");
        return fragment;

    }

    public void publica(CharSequence text) {
        TextView t = getActivity().findViewById(R.id.fragmentinfo_helptext);
        if (t != null) {
            t.setText(text);
        }
    }


}
