package com.libjpegcompress.util.camera;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * Created on 15/9/22.
 * 拍照核心处理
 */
public class CameraCore {

    //调用系统相机的Code
    public static final int REQUEST_TAKE_PHOTO_CODE = 1001;
    //拍照裁剪的Code
    public static final int REQUEST_TAKE_PHOTO_CROP_CODE = 1003;
    //调用系统图库的Code
    public static final int REQUEST_TAKE_PICTURE_CODE = 1002;
    //调用系统图库裁剪Code
    public static final int REQUEST_TAKE_PICTURE_CROP_CODE = 1004;
    //裁剪的Code
    public static final int REQUEST_TAKE_CROP_CODE = 1005;
    //截取图片的高度
    public static final int REQUEST_HEIGHT = 400;
    //截取图片的宽度
    public static final int REQUEST_WIDTH = 400;
    //用来存储照片的URL
    private Uri photoURL;
    //调用照片的Activity
    private Activity activity;
    //回调函数
    private CameraResult cameraResult;

    public CameraCore(CameraResult cameraResult, Activity activity) {
        this.cameraResult = cameraResult;
        this.activity = activity;
    }

    //调用系统照相机，对Intent参数进行封装
    protected Intent startTakePhoto(Uri photoURL) {
        this.photoURL = photoURL;
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURL);//将拍取的照片保存到指定URI
        return intent;
    }

    //调用系统图库,对Intent参数进行封装
    protected Intent startTakePicture() {
        //this.photoURL = photoURL;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");//从所有图片中进行选择
        return intent;
    }

    //调用系统裁剪图片，对Intent参数进行封装
    protected Intent takeCropPicture(Uri photoURL, int with, int height) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoURL, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", with);
        intent.putExtra("outputY", height);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);//黑边
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURL);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        return intent;
    }


    //拍照
    public void getPhoto2Camera(Uri uri) {
        activity.startActivityForResult(startTakePhoto(uri), REQUEST_TAKE_PHOTO_CODE);
    }

    //拍照后裁剪
    public void getPhoto2CameraCrop(Uri uri) {
        Intent intent = startTakePhoto(uri);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);//将拍取的照片保存到指定URI
        activity.startActivityForResult(intent, REQUEST_TAKE_PHOTO_CROP_CODE);
    }

    //获取系统相册
    public void getPhoto2Album() {
        activity.startActivityForResult(startTakePicture(),REQUEST_TAKE_PICTURE_CODE);
    }

    //获取系统相册后裁剪
    public void getPhoto2AlbumCrop() {
        activity.startActivityForResult(startTakePicture(),REQUEST_TAKE_PICTURE_CROP_CODE);
    }


    //处理onActivityResult
    public void onResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                //选择系统图库
                case REQUEST_TAKE_PICTURE_CODE:
                    //获取系统返回的照片的Uri
                    photoURL = intent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    //从系统表中查询指定Uri对应的照片
                    Cursor cursor = activity.getContentResolver().query(photoURL, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);  //获取照片路径
                    cursor.close();
                    if (!TextUtils.isEmpty(picturePath)) {
                        cameraResult.onSuccess(picturePath);
                    } else {
                        cameraResult.onFail("文件没找到");
                    }
                    break;
                //选择系统图库.裁剪
                case REQUEST_TAKE_PICTURE_CROP_CODE:
                    photoURL = intent.getData();
                    activity.startActivityForResult(takeCropPicture(photoURL, REQUEST_HEIGHT, REQUEST_WIDTH), REQUEST_TAKE_CROP_CODE);
                    break;
                //调用相机
                case REQUEST_TAKE_PHOTO_CODE:
                    cameraResult.onSuccess(photoURL.getPath());
                    break;
                //调用相机,裁剪
                case REQUEST_TAKE_PHOTO_CROP_CODE:
                    activity.startActivityForResult(takeCropPicture(Uri.fromFile(new File(photoURL.getPath())), REQUEST_HEIGHT, REQUEST_WIDTH), REQUEST_TAKE_CROP_CODE);
                    break;
                //裁剪之后的回调
                case REQUEST_TAKE_CROP_CODE:
                    String path = getPic2Uri(photoURL, activity);
                    cameraResult.onSuccess(path);
                    break;
                default:
                    break;
            }
        }
    }

    //通过URI得到图片路径
    public static String getPic2Uri(Uri contentUri, Context context) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			String filePath ="";
            if (cursor != null) {
				int columnIndex = cursor.getColumnIndexOrThrow(proj[0]);
				cursor.moveToFirst();
				filePath = cursor.getString(columnIndex);
				// 4.0以上的版本会自动关闭 (4.0--14; 4.0.3--15)
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					cursor.close();
				}
			}
            return filePath;
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        photoURL = savedInstanceState.getParcelable("photoURL");
    }

    public void onSaveInstanceState(Bundle outState){
        outState.putParcelable("photoURL",photoURL);
    }

    //回调实例
    public interface CameraResult{
    	//成功回调
    	public void onSuccess(String filePaht);
    	//失败
    	public void onFail(String message);
    } 
}