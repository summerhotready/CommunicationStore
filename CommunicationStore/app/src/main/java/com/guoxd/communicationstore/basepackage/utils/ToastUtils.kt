package com.guoxd.communicationstore.basepackage.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * Created by guoxd on 2018/4/10.
 */

class ToastUtils private constructor() {
//    val intence: ToastUtils
//        get() = utils
    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun showLongToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun showLog(tag:String,msg:String){
        Log.i(tag,msg)
    }

    companion object {
        private val utils = ToastUtils()
        fun getIntence():ToastUtils{
            return utils;
        }
    }
}
