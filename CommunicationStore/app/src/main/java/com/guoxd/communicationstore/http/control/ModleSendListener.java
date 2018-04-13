package com.guoxd.communicationstore.http.control;

/**
 * Created by guoxd on 2018/4/10.
 */

public interface ModleSendListener {
    void onPostCallBack(String content);
    void onGetCallBack(String content);
}
