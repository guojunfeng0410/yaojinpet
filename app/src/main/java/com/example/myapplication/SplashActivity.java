package com.example.myapplication;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.myapplication.communication.CustomUserProvider;
import com.example.myapplication.slidemenu.MainActivity;
import com.example.myapplication.voice.Voice;


import org.androidannotations.annotations.EActivity;

import java.io.File;

import cn.leancloud.chatkit.LCChatKit;

/**
 * liteplayer by loader
 * @author qibin
 */
@EActivity(R.layout.splash_layout)
public class SplashActivity extends Activity {
    String APP_KEY = "zJMeI9BKMScfD1wLeRiUwbnH";
    String APP_ID = "DYWs0volshC8a6jlt6VA90D5-gzGzoHsz";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 开启播放和下载服务

        Voice voice=new Voice(getApplicationContext(),true);
        LCChatKit.getInstance().setProfileProvider(CustomUserProvider.getInstance());
        LCChatKit.getInstance().init(getApplicationContext(), APP_ID, APP_KEY);
        File sd= Environment.getExternalStorageDirectory();
        String path=sd.getAbsolutePath()+"/yaojinli/lrc";
        File file=new File(path);
        if(!file.exists())
            if(file.mkdirs())
                Log.d("SplashActivity.java","创建成功");
            else
                Log.d("SplashActivity.java","创建失败");
        else
            Log.d("SplashActivity.java","已存在，无需创建");
         path=sd.getAbsolutePath()+"/yaojinli/book";
         file=new File(path);
        if(!file.exists())
            if(file.mkdirs())
                Log.d("SplashActivity.java","创建成功");
            else
                Log.d("SplashActivity.java","创建失败");
        else
            Log.d("SplashActivity.java","已存在，无需创建");
        path=sd.getAbsolutePath()+"/yaojinli/voice";
        file=new File(path);
        if(!file.exists())
            if(file.mkdirs())
                Log.d("SplashActivity.java","创建成功");
            else
                Log.d("SplashActivity.java","创建失败");
        else
            Log.d("SplashActivity.java","已存在，无需创建");
            // 2s跳转到主界面
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();
            }
        }, 2000);
    }
}