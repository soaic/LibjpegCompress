package com.libjpegcompress.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.libjpegcompress.R;
import com.libjpegcompress.util.camera.CameraCore;
import com.libjpegcompress.util.camera.CameraProxy;

import net.bither.util.NativeUtil;

/**
 * @Description TODO
 * @Class MainActivity 
 * @Copyright: Copyright (c) 2016  
 * @author XiaoSai
 * @version V1.0.0
 */
public class MainActivity extends Activity implements CameraCore.CameraResult{
	private Button choose_image;
	private CameraProxy cameraProxy;
	private ImageView choose_bit;
	/** SD卡根目录 */
	private final String externalStorageDirectory = Environment.getExternalStorageDirectory().getPath()+"/picture/";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//压缩后保存临时文件目录
		File tempFile = new File(externalStorageDirectory);
		if(!tempFile.exists()){
			tempFile.mkdirs();
		}
		cameraProxy = new CameraProxy(this, MainActivity.this);
		
		choose_image = (Button)findViewById(R.id.choose_image);
		choose_image.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cameraProxy.getPhoto2Album();
			}
		});
		
		choose_bit = (ImageView)findViewById(R.id.choose_bit);
	}
	
	//拍照选图片成功回调
	@Override
	public void onSuccess(final String filePath) {
		File file = new File(filePath);
        if (file.exists()) {
        	new Thread(){
        		public void run() {
        			final File file = new File(externalStorageDirectory+"/tempCompress.jpg");
        			NativeUtil.compressBitmap(filePath,file.getPath());
        			MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							choose_bit.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
						}
					});
        		};
        	}.start();
        }
	}
	
	//拍照选图片失败回调
	@Override
	public void onFail(String message) {
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		cameraProxy.onResult(requestCode, resultCode, data);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		cameraProxy.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		cameraProxy.onSaveInstanceState(outState);
	}
}
  
