package es.ujaen.ejemplostema3;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentoAcercade extends DialogFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public FragmentoAcercade() {
        // Required empty public constructor
    }

    /**
     * Esta es un ejemplo para instanciar un fragmento el cual puede
     * requerir el paso de ciertos parámetros. En este caso los parámetros
     * no son usados
     *
     * @return A new instance of fragment FragmentoAcercade.
     */
    public static FragmentoAcercade newInstance() {
        FragmentoAcercade fragment = new FragmentoAcercade();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_fragment_acercade, container, false);

    }

}
