package com.libjpegcompress.util.camera;

import java.io.File;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.libjpegcompress.util.Utils;

/**
 * Created on 15/9/22.
 * 拍照核心处理
 */
public class CameraCore {

    //调用系统相机的Code
    private static final int REQUEST_TAKE_PHOTO_CODE = 1001;
    //拍照裁剪的Code
    private static final int REQUEST_TAKE_PHOTO_CROP_CODE = 1003;
    //调用系统图库的Code
    private static final int REQUEST_TAKE_PICTRUE_CODE = 1002;
    //调用系统图库裁剪Code
    private static final int REQUEST_TAKE_PICTRUE_CROP_CODE = 1004;
    //裁剪的Code
    private static final int REQUEST_TAKE_CROP_CODE = 1005;
    //截取图片的高度
    private static final int REQUEST_HEIGHT = 400;
    //截取图片的宽度
    private static final int REQUEST_WIDTH = 400;
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
    private Intent startTakePhoto(Uri photoURL) {
        this.photoURL = photoURL;
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT,photoURL);//将拍取的照片保存到指定URI
        //7.0
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

    //调用系统图库,对Intent参数进行封装
    private Intent startTakePicture() {
//        this.photoURL = photoURL;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

    //调用系统裁剪图片，对Intent参数进行封装
    private Intent takeCropPicture(Uri photoURL, int with, int height) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //7.0
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(photoURL, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", with);
        intent.putExtra("outputY", height);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);//黑边
        photoURL = Uri.fromFile(new File(Utils.getTempFilePath()));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURL);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection",true); // no face detection
        return intent;
    }


    //拍照
    public void getPhoto2Camera(final Uri uri) {
        activity.startActivityForResult(startTakePhoto(uri),REQUEST_TAKE_PHOTO_CODE);        
    }

    //拍照后截屏
    public void getPhoto2CameraCrop(final Uri uri) {
        Intent intent = startTakePhoto(uri);
        activity.startActivityForResult(intent,REQUEST_TAKE_PHOTO_CROP_CODE);
    }

    //获取系统相册
    public void getPhoto2Album() {
        activity.startActivityForResult(startTakePicture(), REQUEST_TAKE_PICTRUE_CODE);
    }

    //获取系统相册后裁剪
    public void getPhoto2AlbumCrop() {
        activity.startActivityForResult(startTakePicture(), REQUEST_TAKE_PICTRUE_CROP_CODE);
    }


    //处理onActivityResult
    public void onResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                //选择系统图库
                case REQUEST_TAKE_PICTRUE_CODE:
                    //获取系统返回的照片的Uri
                    photoURL = intent.getData();
                    if("file".equals(photoURL.getScheme())){
                        //解决小米手机获取URL失败
                        cameraResult.onCameraSuccess(photoURL.getEncodedPath());
                        return;
                    }
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    //从系统表中查询指定Uri对应的照片
                    Cursor cursor = activity.getContentResolver().query(photoURL, filePathColumn, null, null, null);
                    if(cursor!=null&&cursor.moveToFirst()){
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);  //获取照片路径
                        cursor.close();
                        if(!TextUtils.isEmpty(picturePath)){
                            cameraResult.onCameraSuccess(picturePath);
                        }else{
                            cameraResult.onCameraFail("文件没找到");
                        }
                    }else{
                        cameraResult.onCameraFail("文件没找到");
                    }
                    break;
                //选择系统图库.裁剪
                case REQUEST_TAKE_PICTRUE_CROP_CODE:
                    photoURL = intent.getData();
                    activity.startActivityForResult(takeCropPicture(photoURL, REQUEST_WIDTH,REQUEST_HEIGHT), REQUEST_TAKE_CROP_CODE);
                    break;
                //调用相机
                case REQUEST_TAKE_PHOTO_CODE:
                    String pathCamera = parseOwnUri(activity, photoURL);
                    cameraResult.onCameraSuccess(pathCamera);
                    break;
                //调用相机,裁剪
                case REQUEST_TAKE_PHOTO_CROP_CODE:
                    activity.startActivityForResult(takeCropPicture(photoURL,REQUEST_WIDTH,REQUEST_HEIGHT),REQUEST_TAKE_CROP_CODE);
                    break;
                //裁剪之后的回调
                case REQUEST_TAKE_CROP_CODE:
                    photoURL = intent.getData();
                    String path = parseOwnUri(activity, photoURL);
                    cameraResult.onCameraSuccess(path);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 将TakePhoto 提供的Uri 解析出文件绝对路径
     * @param uri
     * @return
     */
    private static String parseOwnUri(Context context,Uri uri){
        if(uri==null)return null;
        String path;
        if(TextUtils.equals(uri.getAuthority(), Utils.getFileProviderName(context))){
            path=new File(uri.getPath().replace("camera_photos/","")).getAbsolutePath();

            if(!path.contains(Environment
                    .getExternalStorageDirectory().getAbsolutePath())){
                path = Environment.getExternalStorageDirectory().getAbsolutePath()+ path;
            }
        }else {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};
            Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
            //也可用下面的方法拿到cursor
            //Cursor cursor = this.context.managedQuery(uri, filePathColumn, null, null, null);
            if(cursor!=null){
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                cursor.moveToFirst();
                path = cursor.getString(columnIndex);
                cursor.close();
                return TextUtils.isEmpty(path)?uri.getPath():path;
            }else{
                path = uri.getPath();
            }
        }
        return path;
    }


    /**
     * 解决小米手机上获取图片路径为null的情况  
     * @param intent
     * @return
     */
    public Uri getUri(Intent intent) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = activity.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Images.ImageColumns._ID },
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value    
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    // do nothing    
                } else {
                    Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
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
    	void onCameraSuccess(String filePath);
    	//失败
    	void onCameraFail(String message);
    } 
}