package com.guoxd.communicationstore.http.info;

import android.os.Bundle;

/**
 * Created by guoxd on 2018/4/9.
 */

public interface OnHttpListener {
    void onTimeOut();
    void onSuccess(Bundle b);
    void onFailure(Bundle b);
}
