package com.droiddigger.lushan.locationdatacollector;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by mihodihasan on 6/6/17.
 */

public class Adapter extends RecyclerView.Adapter<MyViewHolder> {

    Context context;
    List<UserLocation> userLocations;

    public Adapter(Context context, List<UserLocation> userLocations) {
        this.context = context;
        this.userLocations = userLocations;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.list_row,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.userTV.setText(userLocations.get(position).getUsername());
        holder.timeTV.setText(userLocations.get(position).getTimeStamp());
        holder.latitudeTV.setText(userLocations.get(position).getLatitude());
        holder.longitudeTV.setText(userLocations.get(position).getLongitude());
    }

    @Override
    public int getItemCount() {
        return userLocations.size();
    }
}
