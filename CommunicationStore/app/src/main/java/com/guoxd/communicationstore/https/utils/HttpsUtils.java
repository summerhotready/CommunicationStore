package com.guoxd.communicationstore.https.utils;

/**
 * Created by guoxd on 2018/4/13.
 */

public class HttpsUtils {
    static HttpsUtils utils = new HttpsUtils();
    private HttpsUtils(){}
    public HttpsUtils getIntence(){
        return utils;
    }

}
