package com.guoxd.communicationstore.basepackage.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by guoxd on 2018/4/10.
 */

public class SharePreaceUtils {
    private static SharePreaceUtils utils;
    private static SharedPreferences sharedPreferences;
    private SharePreaceUtils(Context context){
        sharedPreferences = context.getSharedPreferences("macs",0);
    }
    public static SharePreaceUtils init(Context context){
        if(sharedPreferences == null){
            utils =new SharePreaceUtils(context);
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
