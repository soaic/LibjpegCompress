使用：
Gradle

```
compile 'me.xiaosai:image_compress:1.0.1'
```

代码
```
ImageCompress.with(this)
          .load(filePath)
          .setTargetDir(externalStorageDirectory)
          .ignoreBy(150)
          .setOnCompressListener(new ImageCompress.OnCompressListener() {
              @Override
              public void onStart() {
                  Log.e("compress","onStart");
              }
              @Override
              public void onSuccess(String filePath) {
                  Log.e("compress","onSuccess="+filePath);
                  choose_bit.setImageBitmap(BitmapFactory.decodeFile(filePath));
              }
              @Override
              public void onError(Throwable e) {
                  e.printStackTrace();
                  Log.e("compress","onError");
              }
          }).launch();
```
