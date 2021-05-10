package com.partsilicon.bixolon_printer_plugin;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private  Context context;
    SharedPreferences sharedPreferences;
    public Settings(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("bixolonprinter",Context.MODE_PRIVATE);
    }

    String getSetting(String key){
        return  sharedPreferences.getString(key, "");
    }
    void saveSetting(String key , String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key ,value );
        editor.apply();
    }

    public String getDeviceName(){
        return  getSetting("deviceName");
    }
    public void saveDeviceName(String name){
         saveSetting("deviceName" , name);
    }

    public String getDeviceAddress(){
        return  getSetting("deviceAddress");
    }
    public void saveDeviceAddress(String address){
        saveSetting("deviceAddress" , address);
    }


}
