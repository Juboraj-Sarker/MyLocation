package com.juborajsarker.mylocation.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.juborajsarker.mylocation.R;


public class NearbyFragment extends Fragment {

    View view;

    Spinner spinnerNearbyChoice;
    Button btnSearch, btnShowOnMap;
    ListView lv;



    public NearbyFragment() {


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_nearby, container, false);

        spinnerNearbyChoice = (Spinner) view.findViewById(R.id.spinner_nearby_choice);
        btnSearch = (Button) view.findViewById(R.id.btn_search);
        btnShowOnMap = (Button) view.findViewById(R.id.btn_show_on_map);
        lv = (ListView) view.findViewById(R.id.lv_nearby);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });



        return view;
    }

}
