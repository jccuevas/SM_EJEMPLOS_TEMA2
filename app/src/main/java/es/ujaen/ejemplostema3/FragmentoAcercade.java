package es.ujaen.ejemplostema3;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentoAcercade extends DialogFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public FragmentoAcercade() {
        // Required empty public constructor
    }

    /**
     * Esta es un ejemplo para instanciar un fragmento el cual puede
     * requerir el paso de ciertos parámetros. En este caso los parámetros
     * no son usados
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentoAcercade.
     */
    public static FragmentoAcercade newInstance(String param1, String param2) {
        FragmentoAcercade fragment = new FragmentoAcercade();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_fragment_acercade, container, false);
        //TODO Ejercicio: hacer que los parámetros ARG_PARAM1 y ARG_PARAM2 modifiquen el contenido de los campos de texto que se desee
    }


}
