package com.libjpegcompress.util.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.libjpegcompress.util.MPermissionsUtils;
import com.libjpegcompress.util.Utils;

import java.io.File;

/**
 * Created on 15/9/22.
 * 对拍照操作的代理
 */
public class CameraProxy {

    /**
     * 相机核心类
     */
    private CameraCore cameraCore;
    private Activity activity;
    private CameraCore.CameraResult cameraResult;

    public CameraProxy(CameraCore.CameraResult cameraResult, Activity activity) {
        cameraCore = new CameraCore(cameraResult, activity);
        this.cameraResult = cameraResult;
        this.activity = activity;
    }

    /**
     * 拍照
     *
     * @param path
     * @Description:函数描述
     * @author Administrator
     * @date 2016年3月14日 下午1:55:45
     * @version V1.0.0
     */
    public void getPhoto2Camera(final String path) {
        MPermissionsUtils.getInstance().requestPermissions(activity, 100,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, new MPermissionsUtils.PermissionsResultAction() {
                    @Override
                    public void doExecuteSuccess(int requestCode) {
                        Uri uri = Utils.getFileUri(activity,new File(path));
                        cameraCore.getPhoto2Camera(uri);
                    }

                    @Override
                    public void doExecuteFail(int requestCode) {
                        cameraResult.onCameraFail("获取权限失败");
                    }
                });
    }

    /**
     * 拍照截图
     *
     * @param path
     * @Description:函数描述
     * @author Administrator
     * @date 2016年3月14日 下午1:55:42
     * @version V1.0.0
     */
    public void getPhoto2CameraCrop(final String path) {
        MPermissionsUtils.getInstance().requestPermissions(activity, 100,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, new MPermissionsUtils.PermissionsResultAction() {
                    @Override
                    public void doExecuteSuccess(int requestCode) {
                        Uri uri = Utils.getFileUri(activity,new File(path));
                        cameraCore.getPhoto2CameraCrop(uri);
                    }

                    @Override
                    public void doExecuteFail(int requestCode) {
                        cameraResult.onCameraFail("获取权限失败");
                    }
                });
    }

    /**
     * 选择照片
     *
     * @Description:函数描述
     * @author Administrator
     * @date 2016年3月14日 下午1:55:19
     * @version V1.0.0
     */
    public void getPhoto2Album() {
        MPermissionsUtils.getInstance().requestPermissions(activity, 100,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new MPermissionsUtils.PermissionsResultAction() {
                    @Override
                    public void doExecuteSuccess(int requestCode) {
                        cameraCore.getPhoto2Album();
                    }

                    @Override
                    public void doExecuteFail(int requestCode) {
                        cameraResult.onCameraFail("获取权限失败");
                    }
                });

    }

    /**
     * 选择照片，截图
     *
     * @Description:函数描述
     * @author Administrator
     * @date 2016年3月14日 下午1:55:26
     * @version V1.0.0
     */
    public void getPhoto2AlbumCrop() {
        MPermissionsUtils.getInstance().requestPermissions(activity, 100,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, new MPermissionsUtils.PermissionsResultAction() {
                    @Override
                    public void doExecuteSuccess(int requestCode) {
                        cameraCore.getPhoto2AlbumCrop();
                    }

                    @Override
                    public void doExecuteFail(int requestCode) {
                        cameraResult.onCameraFail("获取权限失败");
                    }
                });
    }

    /**
     * 接受ActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @Description:函数描述
     * @author Administrator
     * @date 2016年3月14日 下午1:55:29
     * @version V1.0.0
     */
    public void onResult(int requestCode, int resultCode, Intent data) {
        cameraCore.onResult(requestCode, resultCode, data);
    }


    public void onRestoreInstanceState(Bundle savedInstanceState){
        cameraCore.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState){
        cameraCore.onSaveInstanceState(outState);
    }
}