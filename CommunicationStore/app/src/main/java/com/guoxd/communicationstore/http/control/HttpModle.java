package com.guoxd.communicationstore.http.control;

import com.guoxd.communicationstore.http.info.OnHttpListener;

/**
 * Created by guoxd on 2018/4/9.
 */

public interface HttpModle {
    void Post(String url,String data,ModleSendListener listener);
    void Get(String url,ModleSendListener listener);
}
