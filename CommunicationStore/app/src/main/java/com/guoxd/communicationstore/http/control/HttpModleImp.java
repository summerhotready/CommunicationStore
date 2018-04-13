package com.guoxd.communicationstore.http.control;

import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.guoxd.communicationstore.basepackage.utils.ToastUtils;
import com.guoxd.communicationstore.http.info.OnHttpListener;
import com.guoxd.communicationstore.http.utils.HttpUtils;

/**
 * Created by guoxd on 2018/4/9.
 */

public class HttpModleImp implements HttpModle {
    final String tag="HTTP";
    @Override
    public void Post(String url, String data, final ModleSendListener listener) {
        HttpUtils.Companion.getHttpUtils().httpPost(url, "", new OnHttpListener() {
            @Override
            public void onTimeOut() {
            }

            @Override
            public void onSuccess(Bundle b) {
                String data =b.getString("data");
                AnswerModle modle = new Gson().fromJson(data,AnswerModle.class);
                if(modle.result == 0) {
                    listener.onPostCallBack(modle.content);
                }
            }

            @Override
            public void onFailure(Bundle b) {
            }
        });
    }

    @Override
    public void Get(String url, final ModleSendListener listener) {
        ToastUtils.Companion.getIntence().showLog(tag,"get"+url);
        HttpUtils.Companion.getHttpUtils().httpGet(url, new OnHttpListener() {
            @Override
            public void onTimeOut() {
                ToastUtils.Companion.getIntence().showLog(tag,"onTimeOut");
            }

            @Override
            public void onSuccess(Bundle b) {
                String data =b.getString("data");
                ToastUtils.Companion.getIntence().showLog(tag,"onSuccess"+data);
                AnswerModle modle = new Gson().fromJson(data,AnswerModle.class);
                if(modle.result == 0) {
                    listener.onGetCallBack(modle.content);
                }
            }

            @Override
            public void onFailure(Bundle b) {
                ToastUtils.Companion.getIntence().showLog(tag,"onFailure:"+b.getString("data"));
            }
        });
    }

    //问答model
    class AnswerModle{
        int result;
        String content;
    }


}
