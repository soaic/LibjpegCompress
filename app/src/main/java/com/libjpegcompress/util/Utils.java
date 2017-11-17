package com.libjpegcompress.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 *
 * Created by DDY-03 on 2017/11/17.
 */

public class Utils {
    /**
     * 获取文件Uri
     * @param context
     * @param file
     * @return
     */
    public static Uri getFileUri(Context context, File file){
        Uri uri;
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, getFileProviderName(context), file);//通过FileProvider创建一个content类型的Uri
        }else{
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static String getFileProviderName(Context context) {
        return context.getPackageName() + ".fileprovider";
    }

    /**
     * 获取临时存储的图片地址
     * @return
     */
    public static String getTempFilePath(){
        return Environment.getExternalStorageDirectory().getPath()+"/picture/" + "temp_" + System.currentTimeMillis() / 1000+".jpg";
    }
}
