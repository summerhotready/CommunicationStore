package com.guoxd.communicationstore.https;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;


/**
 * Author:    ZhuWenWu
 * Version    V1.0
 * Date:      2014/12/15  16:19.
 * Description:
 * Modification  History:
 * Date         	Author        		Version        	Description
 * -----------------------------------------------------------------------------------
 * 2014/12/15        ZhuWenWu            1.0                    1.0
 * Why & What is modified:
 */
public class HttpClientSslHelper {
    //1、bks
    private static final String KEY_STORE_TYPE_BKS = "bks";//
    private static final String KEY_STORE_TYPE_P12 = "PKCS12";

    private static final String CLIENT_KEY_MANAGER = "X509";//密钥管理器
    private static final String CLIENT_TRUST_MANAGER = "X509";//
    /**
     * 记得添加相应的证书到assets目录下面
     */
    public static final String KEY_STORE_CLIENT_PATH = "outgoing.CertwithKey.pkcs12";//P12文件客户端证书
    public static final String KEY_STORE_PASSWORD = "IoM@1234";//客户端证书密码
    private static final String KEY_STORE_BKS="cas.bks";//自己生成的服务端证书
    private static final String KEY_STORE_TRUST_PASSWORD = "Huawei@123";//服务端密码

    public static final String KEY_CRT_CLIENT_PATH = "ca.crt";//CRT文件

    public static boolean isServerTrusted = true;

    public static OkHttpClient getSslOkHttpClient(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            SSLSocketFactory sf = getSslContext(context).getSocketFactory();
            builder.sslSocketFactory( sf)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            Log.d("HttpClientSslHelper", "hostname = " + hostname);
//                            //如果是全部自己校验逻辑的，需要根据证书状态返回相应的校验结果
                           if ("180.101.147.89".equals(hostname)) {
                              //session is OpenSSLSessionImpl的实例
                                return session.isValid();
                            } else {
//                                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
//                                return hv.verify(hostname, session);
                               return !isServerTrusted;
                            }

                        }
                    });
        } catch (Exception e){
            e.printStackTrace();
        }
        return builder.build();
    }

    private static SSLContext sslContext = null;

    public static SSLContext getSslContext(Context context) {
        if (sslContext == null) {
            try {//取得KeyManagerFactory和TrustManagerFactory的X509密钥管理器实例
                KeyManagerFactory keyManager = KeyManagerFactory.getInstance(CLIENT_KEY_MANAGER);
                TrustManagerFactory trustManager = TrustManagerFactory.getInstance(CLIENT_TRUST_MANAGER);

                // 服务器端需要验证的客户端证书
                KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);
                // 客户端信任的服务器端证书
                KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);

                InputStream ksIn = context.getResources().getAssets().open(KEY_STORE_CLIENT_PATH);
                InputStream tsIn = context.getResources().getAssets().open(KEY_STORE_BKS);
                try {
                    keyStore.load(ksIn, KEY_STORE_PASSWORD.toCharArray());
                    trustStore.load(tsIn, KEY_STORE_TRUST_PASSWORD.toCharArray());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        ksIn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        tsIn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                trustManager.init(trustStore);
                keyManager.init(keyStore, KEY_STORE_PASSWORD.toCharArray());
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManager.getKeyManagers(),trustManager.getTrustManagers(),null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslContext;
    }

    /**
     * 与getSslContext等价
     * @param context
     * @return
     */
    private static SSLSocketFactory getSocketFactory(Context context) {
        try {
            InputStream trust_input = context.getAssets().open(KEY_STORE_BKS);//服务器授信证书
            InputStream client_input = context.getAssets().open(KEY_STORE_CLIENT_PATH);//客户端证书

            SSLContext sslContext = SSLContext.getInstance("TLS");

            // 1 Import the CA certificate of the server
            KeyStore trustStore = KeyStore.getInstance("BKS");
            trustStore.load(trust_input, "Huawei@123".toCharArray());

            // 2 Import your own certificate
            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            keyStore.load(client_input, "IoM@1234".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "IoM@1234".toCharArray());

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            SSLSocketFactory factory = sslContext.getSocketFactory();
            return factory;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //失败，只有客户端无法完成验证
//报错Caused by: java.security.cert.CertificateException: java.security.cert.CertPathValidatorException: Trust anchor for certification path not found.
    public static SSLContext getSslContextByDefaultTrustManager(Context context) {
        if (sslContext == null) {
            try {
                // 服务器端需要验证的客户端证书
                KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);

                AssetManager assetManager = context.getAssets();
                InputStream ksIn = assetManager.open(KEY_STORE_CLIENT_PATH);
                try {
                    keyStore.load(ksIn, KEY_STORE_PASSWORD.toCharArray());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        ksIn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                sslContext = SSLContext.getInstance("TLS");
                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
                keyManagerFactory.init(keyStore, KEY_STORE_PASSWORD.toCharArray());
                sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslContext;
    }
//失败，仅仅使用ca证书无法完成验证
    public static SSLContext getSslContextByCustomTrustManager(Context context) {
        if (sslContext == null) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = new BufferedInputStream(context.getResources().getAssets().open(KEY_CRT_CLIENT_PATH));
                final Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                } finally {
                    caInput.close();
                }

                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new X509TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                        Log.d("HttpClientSslHelper", "checkClientTrusted --> authType = " + authType);
                        //校验客户端证书
                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                        Log.d("HttpClientSslHelper", "checkServerTrusted --> authType = " + authType);
                        //校验服务器证书
                        for (X509Certificate cert : chain) {
                            cert.checkValidity();
                            try {
                                cert.verify(ca.getPublicKey());
                                isServerTrusted = true;
                            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                                e.printStackTrace();
                                isServerTrusted = false;
                            }
                        }
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }}, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslContext;
    }

//失败，网传的验证手法，失败
    public static SSLContext initSSLALL() throws KeyManagementException, NoSuchAlgorithmException, IOException {

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { new TrustAllManager() }, null);

        return context;
    }

    private static class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
