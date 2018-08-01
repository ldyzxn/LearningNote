package com.ldy.learningnote.utils.img;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class CompressImageUtil implements CompressImage{
    private CompressConfig config;
    Handler mhHandler = new Handler();
    public CompressImageUtil(CompressConfig config) {
        this.config=config==null?CompressConfig.getDefaultConfig():config;
    }
    @Override
    public void compress(String imagePath, CompressListener listener) {
        if (config.isEnablePixelCompress()){
            try {
                compressImageByPixel(imagePath,listener);
            } catch (FileNotFoundException e) {
                listener.onCompressFailed(imagePath,String.format("图片压缩失败,%s",e.toString()));
                e.printStackTrace();
            }
        }else {
            compressImageByQuality(BitmapFactory.decodeFile(imagePath),imagePath,listener);
        }
    }
    /**
     * 多线程压缩图片的质量
     * @param bitmap 内存中的图片
     * @param imgPath 图片的保存路径
     */
    private void compressImageByQuality(final Bitmap bitmap, final String imgPath, final CompressImage.CompressListener listener){
        if(bitmap==null){
            sendMsg(false,imgPath,"像素压缩失败,bitmap is null",listener);
            return;
        }
        //开启多线程进行压缩处理
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int options = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                while (baos.toByteArray().length >config.getMaxSize()) {
                    baos.reset();
                    options -= 5;
                    if(options<=50){ break;}
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                }
                try {
                    FileOutputStream fos = new FileOutputStream(new File(imgPath));
                    fos.write(baos.toByteArray());
                    fos.flush();
                    fos.close();
                    sendMsg(true, imgPath,null,listener);
                } catch (Exception e) {
                    sendMsg(false,imgPath,"质量压缩失败",listener);
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * 按比例缩小图片的像素以达到压缩的目的
     * @param imgPath
     * @return
     */
    private void compressImageByPixel(String imgPath,CompressListener listener) throws FileNotFoundException {
        if(imgPath==null){
            sendMsg(false,imgPath,"要压缩的文件不存在",listener);
            return;
        }
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int width = newOpts.outWidth;
        int height = newOpts.outHeight;
        float maxSize =config.getMaxPixel();
        int be = 1;
        //缩放比,用高或者宽其中较大的一个数据进行计算
        if (width >= height && width > maxSize) {
            be = (int) (newOpts.outWidth / maxSize);
            be++;
        } else if (width < height && height > maxSize) {
            be = (int) (newOpts.outHeight / maxSize);
            be++;
        }
        newOpts.inSampleSize =be;
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        if (config.isEnableQualityCompress()){
            compressImageByQuality(bitmap,imgPath,listener);
        }else {
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,new FileOutputStream(new File(imgPath)));
            listener.onCompressSuccess(imgPath);
        }
    }
    /**
     * 发送压缩结果的消息
     * @param isSuccess 压缩是否成功
     * @param imagePath
     * @param message
     */
    private void sendMsg(final boolean isSuccess, final String imagePath,final String message, final CompressImage.CompressListener listener){
        mhHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isSuccess){
                    listener.onCompressSuccess(imagePath);
                }else{
                    listener.onCompressFailed(imagePath,message);
                }
            }
        });
    }
}
