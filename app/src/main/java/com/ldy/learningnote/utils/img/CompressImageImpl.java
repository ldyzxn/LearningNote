package com.ldy.learningnote.utils.img;

import android.text.TextUtils;

import java.io.File;

public class CompressImageImpl implements CompressImage{
    private CompressImageUtil compressImageUtil;
    public CompressImageImpl(CompressConfig config) {
        compressImageUtil=new CompressImageUtil(config);
    }
    @Override
    public void compress(String imagePath, CompressListener listener) {
        if (TextUtils.isEmpty(imagePath)){
            listener.onCompressFailed(imagePath,"要压缩的文件不存在");
            return;
        }
        File file=new File(imagePath);
        //如果文件不存在，则不做任何处理
        if (file==null||!file.exists()||!file.isFile()){
            listener.onCompressFailed(imagePath,"要压缩的文件不存在");
            return;
        }
        compressImageUtil.compress(imagePath,listener);
    }
}
