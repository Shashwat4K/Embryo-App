package com.myproject.dummy;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.BsonValue;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

//import android.widget.CheckBox;
//import android.widget.CompoundButton;

public class DeviceListAdapter extends ArrayAdapter<Device> {
    private final RemoteMongoCollection _deviceSource;
    private List<BsonValue> pendingChanges;
    private int c;
    /*@Override
    public int getViewTypeCount() {
        //Count=Size of ArrayList.
        return 1;
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }*/
    public DeviceListAdapter(
            final Context context,
            final int resource,
            final List<Device> devices,
            final RemoteMongoCollection deviceSource
    ) {
        super(context, resource, devices);
        c = devices.size();
        _deviceSource = deviceSource;
        pendingChanges = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(
            final int position,
            final View convertView,
            @NonNull final ViewGroup ignored
    ) {
        final View row;

        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.device, null);
        } else {
            row = convertView;
        }


        try {


            final Device dev = this.getItem(position);
            ((TextView) row.findViewById(R.id.devname)).setText(dev.getDeviceName());
            ((TextView) row.findViewById(R.id.devid)).setText(dev.getDeviceId());

            final CheckBox onoff = row.findViewById(R.id.Switch);
            final CheckedTextView tick = row.findViewById(R.id.checkedTextView2);
            //onoff.setOnCheckedChangeListener(null);
            onoff.setChecked(dev.isOn());
            //tick.setChecked(dev.isOn());


            onoff.setOnCheckedChangeListener((CompoundButton, b) -> {

                final Document query = new Document();
                query.put("_id", dev.getId());

                final Document update = new Document();
                final Document set = new Document();
                set.put("checked", b);
                update.put("$set", set);

                _deviceSource.sync().updateOne(query, update);


            });



        } catch (NullPointerException e) {
            Log.e("NullPointer", "Null Pointer Exception occured while setting text");
        }
        return row;

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
