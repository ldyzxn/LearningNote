package com.ldy.learningnote.base;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;



public abstract class BaseActivity extends AppCompatActivity {
    protected String TAG;


    private String getTAG(){
        return getClass().getSimpleName();
    }

    /**
     * 定义所有activitylog日志输出方法
     * @param msg
     */
    protected void log(String msg){
        Log.d(getTAG(),msg);
    }



}
