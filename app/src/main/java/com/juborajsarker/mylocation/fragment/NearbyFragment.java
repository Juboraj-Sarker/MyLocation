package com.juborajsarker.mylocation.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juborajsarker.mylocation.R;


public class NearbyFragment extends Fragment {

    View view;


    public NearbyFragment() {


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_nearby, container, false);

        return view;
    }

}
