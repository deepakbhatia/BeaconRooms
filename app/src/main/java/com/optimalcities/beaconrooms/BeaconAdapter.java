package com.optimalcities.beaconrooms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bluebite.android.eddystone.Url;

import java.util.List;

public class BeaconAdapter extends ArrayAdapter<Url>{

    List<Url> urls;
    Activity mContext;
    int resourceId;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(resourceId, parent, false);

        TextView urlView = (TextView) row.findViewById(R.id.urlView);
        TextView batteryView = (TextView) row.findViewById(R.id.batteryView);
        TextView temperatureView = (TextView) row.findViewById(R.id.temperatureView);
        TextView advertisementCountView = (TextView) row.findViewById(R.id.advertisementCountView);
        TextView onTimeView = (TextView) row.findViewById(R.id.onTimeView);



        Url url = urls.get(position);
        urlView.setText(url.getUrl().toString());

        if(url.getBattery() != null && url.getTemperature() != null && url.getAdvertisementCount() != null && url.getOnTime() != null){

            batteryView.setText("Battery: " + url.getBattery() + "%");
            temperatureView.setText("Temp: " + url.getTemperature() + "˚C");
            advertisementCountView.setText("Packets Sent: " + url.getAdvertisementCount());
            onTimeView.setText("Uptime: " + url.getReadableOnTime());

            temperatureView.setVisibility(View.VISIBLE);
            advertisementCountView.setVisibility(View.VISIBLE);
            onTimeView.setVisibility(View.VISIBLE);
        } else {
            batteryView.setText("No telemetry data");
            temperatureView.setVisibility(View.GONE);
            advertisementCountView.setVisibility(View.GONE);
            onTimeView.setVisibility(View.GONE);
        }


        return row;
    }

    public BeaconAdapter(Context context, int resource, List<Url> objects) {
        super(context, resource, objects);
        urls = objects;
        mContext = (Activity) context;
        resourceId = resource;


    }


}
