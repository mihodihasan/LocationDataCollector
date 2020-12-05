package com.droiddigger.lushan.locationdatacollector;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by mihodihasan on 6/6/17.
 */

class MyViewHolder extends RecyclerView.ViewHolder {
    TextView userTV, timeTV, latitudeTV, longitudeTV;

    public MyViewHolder(View itemView) {
        super(itemView);

        userTV = (TextView) itemView.findViewById(R.id.user);
        timeTV = (TextView) itemView.findViewById(R.id.time);
        latitudeTV = (TextView) itemView.findViewById(R.id.latitude);
        longitudeTV = (TextView) itemView.findViewById(R.id.longitude);

    }
}