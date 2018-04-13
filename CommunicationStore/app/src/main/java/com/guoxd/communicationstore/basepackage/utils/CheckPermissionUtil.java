package com.guoxd.communicationstore.basepackage.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;


import com.guoxd.communicationstore.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoxd on 2018/1/23.
 */

public class CheckPermissionUtil {
    boolean isPermission=true;
    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            /*Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE*/
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,//照相机
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSON_REQUESTCODE = 0;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    public void onResumeCheck(){
        if (Build.VERSION.SDK_INT >= 23
                && mContext.getApplicationInfo().targetSdkVersion >= 23) {
            if (isNeedCheck) {
                checkPermissions(needPermissions);
            }
        }
    }

    public String[] getNeedPermissions() {
        return needPermissions;
    }

    public void setNeedPermissions(String[] needPermissions) {
        this.needPermissions = needPermissions;
    }

    Activity mContext;
    public CheckPermissionUtil(Activity activity) {
        mContext = activity;
    }

    /**
     *
     * @param permissions
     * @since 2.5.0
     *
     */
    private void checkPermissions(String... permissions) {

        try {
            if (Build.VERSION.SDK_INT >= 23
                    && mContext.getApplicationInfo().targetSdkVersion >= 23) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (null != needRequestPermissonList
                        && needRequestPermissonList.size() > 0) {
                    try {
                        String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                        Method method = mContext.getClass().getMethod("requestPermissions", new Class[]{String[].class,
                                int.class});
                        method.invoke(mContext, array, PERMISSON_REQUESTCODE);
                        Log.d("cheack permission","get permission:");
                    }catch (Exception e){
                        isPermission = false;
                        e.printStackTrace();
                        Log.e("cheack permission","exception:"+e.getMessage());
                    }
                }
            }
        } catch (Throwable e) {
            Log.e("cheack permission","exception:"+e.getMessage());
        }
    }

    public boolean isPermission() {
        return isPermission;
    }


    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     *
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= 23
                && mContext.getApplicationInfo().targetSdkVersion >= 23){
            try {
                for (String perm : permissions) {
                    try {
                        Method checkSelfMethod = mContext.getClass().getMethod("checkSelfPermission", String.class);
                        Method shouldShowRequestPermissionRationaleMethod = mContext.getClass().getMethod("shouldShowRequestPermissionRationale",
                                String.class);
                        if ((Integer) checkSelfMethod.invoke(mContext, perm) != PackageManager.PERMISSION_GRANTED
                                || (Boolean) shouldShowRequestPermissionRationaleMethod.invoke(mContext, perm)) {
                            needRequestPermissonList.add(perm);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("power",e.getMessage());
                    }
                }
            } catch (Throwable e) {

            }
        }
        return needRequestPermissonList;
    }

    /**
     * 通过判断授权结果检测是否所有的权限都已经授权
     * @param grantResults
     * @return
     * @since 2.5.0
     *
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.d("cheack permission","verifyPermissions false resule:"+result);
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    //获得权限后的回调
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        Log.d("cheack permission","onRequestPermissionsResult");
        if (requestCode == PERMISSON_REQUESTCODE) {
            Log.d("cheack permission","requestCode == PERMISSON_REQUESTCODE");
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
                isPermission = false;
            }else{
                isPermission = true;
            }
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     *
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.notifyTitle);
        builder.setMessage(R.string.notifyMsg);

        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isPermission = false;
//                        mContext.finish();
                    }
                });

        builder.setPositiveButton(R.string.setting,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     *  启动应用的设置
     *
     * @since 2.5.0
     *
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
        mContext.startActivity(intent);
    }
}
