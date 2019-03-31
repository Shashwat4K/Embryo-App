package com.myproject.dummy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;


import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.BsonValue;
import org.bson.Document;

public class DeviceListAdapter2 extends RecyclerView.Adapter<DeviceListAdapter2.ViewHolder> {
    private Context context;
    private final RemoteMongoCollection _deviceSource;
    private List<Device> devList;
    private List<BsonValue> pendingChanges;
    public DeviceListAdapter2(
            final Context context,
            final int resource,
            final List<Device> devices,
            final RemoteMongoCollection deviceSource
    ){
        this.context = context;
        devList = devices;
        _deviceSource = deviceSource;
        pendingChanges = new ArrayList<>();
    }
    @Override
    public int getItemCount(){
        return devList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position){
        final int pos = position;
        viewHolder.deviceName.setText(devList.get(position).getDeviceName());
        viewHolder.deviceId.setText(devList.get(position).getDeviceId());
        viewHolder.onoff.setChecked(devList.get(position).isOn());
        viewHolder.onoff.setTag(devList.get(position));

        viewHolder.onoff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Switch sw = (Switch) v;
                Device dev = (Device) sw.getTag();
                dev.setSwitch(sw.isChecked());

                Toast.makeText(v.getContext(), "Switch " + sw.isChecked() + "\nDevice id: " + devList.get(position).getId(), Toast.LENGTH_LONG).show();
                final Document query = new Document();
                query.put("_id", devList.get(position).getId());
                final Document update = new Document();
                final Document set = new Document();
                set.put("checked", sw.isChecked());
                update.put("$set", set);

                _deviceSource.sync().updateOne(query, update);
            }
        });
    }


    @Override
    public DeviceListAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.devicerow, null);
        return new ViewHolder(itemLayoutView);
        //return viewHolder;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView deviceName;
        public TextView deviceId;

        public Switch onoff;

        public Device singledevice;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            deviceName = (TextView) itemLayoutView.findViewById(R.id.deviceName2);

            deviceId = (TextView) itemLayoutView.findViewById(R.id.deviceId2);
            onoff = itemLayoutView.findViewById(R.id.Switch2);

        }
    }
    public void clear(){
        devList.removeAll(devList);
        /*for(Device o: devList){
            devList.remove(o);
        }*/
        //devList.clear();
    }
    public void addAll(List<Device> l){
        devList.addAll(l);
        /*for(Device i : l){
            devList.add(i);
        }*/
    }
    public void addToPending(BsonValue id) {
        this.pendingChanges.add(id);
    }

    public void removeFromPending(BsonValue id) {
        this.pendingChanges.remove(id);
    }

    public boolean pendingContains(BsonValue id) {
        return this.pendingChanges.contains(id);
    }


}
