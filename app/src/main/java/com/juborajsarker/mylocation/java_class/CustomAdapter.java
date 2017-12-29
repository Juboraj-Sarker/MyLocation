package com.juborajsarker.mylocation.java_class;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.juborajsarker.mylocation.R;

import java.util.ArrayList;

/**
 * Created by jubor on 12/28/2017.
 */

public class CustomAdapter extends ArrayAdapter<DataModel> {

    Context mContext;
    private ArrayList<DataModel> dataSet;

    public CustomAdapter(ArrayList<DataModel> data, Context context) {
        super(context, R.layout.details_list_items, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        DataModel dataModel = getItem(position);
        ViewHolder viewHolder;
        final View result;


        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.details_list_items, parent, false);

            viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.tv_addressValue);
            viewHolder.txtCity = (TextView) convertView.findViewById(R.id.tv_cityValue);
            viewHolder.txtSubCity = (TextView) convertView.findViewById(R.id.tv_subCityValue);
            viewHolder.txtPosterCode = (TextView) convertView.findViewById(R.id.tv_posterCodeValue);
            viewHolder.txtCountry = (TextView) convertView.findViewById(R.id.tv_countryValue);
            viewHolder.txtCountryCode = (TextView) convertView.findViewById(R.id.tv_countryCodeValue);
            viewHolder.txtDivision = (TextView) convertView.findViewById(R.id.tv_divisionValue);
            viewHolder.txtLatitude = (TextView) convertView.findViewById(R.id.tv_latitudeValue);
            viewHolder.txtLongitude = (TextView) convertView.findViewById(R.id.tv_longitudeValue);

            result = convertView;

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }


        viewHolder.txtAddress.setText(dataModel.getAddress());
        viewHolder.txtCity.setText(dataModel.getCity());
        viewHolder.txtSubCity.setText(dataModel.getSubCity());
        viewHolder.txtPosterCode.setText(dataModel.getPosterCode());
        viewHolder.txtCountry.setText(dataModel.getCountry());
        viewHolder.txtCountryCode.setText(dataModel.getCountryCode());
        viewHolder.txtDivision.setText(dataModel.getDivision());
        viewHolder.txtLatitude.setText(dataModel.getLatitude());
        viewHolder.txtLongitude.setText(dataModel.getLongitude());


        return convertView;
    }

    private static class ViewHolder {

        TextView txtAddress;
        TextView txtCity;
        TextView txtSubCity;
        TextView txtPosterCode;
        TextView txtDivision;
        TextView txtCountry;
        TextView txtCountryCode;
        TextView txtLatitude;
        TextView txtLongitude;

    }
}
