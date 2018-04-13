package com.guoxd.communicationstore.ble.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by guoxd on 2018/3/16.
 */

public class ShardUtils {
    private static ShardUtils utils;
    private static SharedPreferences sharedPreferences;
    private ShardUtils(Context context){
        sharedPreferences = context.getSharedPreferences("macs",0);
    }
    public static ShardUtils init(Context context){
        if(sharedPreferences == null){
            utils =new ShardUtils(context);
        }
        return utils;
    }
    public  void setString(String key,String value){
        sharedPreferences.edit().putString(key,value).commit();
    }
    public  String getString(String key,String defValue){
        return sharedPreferences.getString(key,"");
    }
}
