package com.myproject.dummy;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Device {
    private final ObjectId _id;
    private final String _devicename;
    private final String _deviceid;
    //private String _devicestatus;
    private boolean _isOn;

    public Device(final Document document){
        _id = document.getObjectId("_id");
        _devicename = document.getString("devicename");
        _deviceid = document.getString("deviceid");
        if (document.containsKey("Checked")){
            _isOn = document.getBoolean("checked");
        }else{
            _isOn = false;
        }
    }
    public void setSwitch(boolean b){
        _isOn = b;
    }
    public ObjectId getId(){return _id;}
    public String getDeviceName() {return _devicename;}
    public String getDeviceId() {return _deviceid;}
    public boolean isOn() {return _isOn;}
}
