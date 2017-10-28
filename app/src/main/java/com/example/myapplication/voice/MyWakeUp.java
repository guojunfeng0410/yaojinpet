package com.example.myapplication.voice;

import java.util.HashMap;
//import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
//import android.util.AndroidRuntimeException;
import android.util.Log;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;

import static java.lang.Thread.sleep;

public class MyWakeUp {

    private String TAG="I/O";
    //public static final String TAG = MyWakeUp.class.getSimpleName();
    private static EventManager mWpEventManager;
    private Context context;

    /**
            * 唤醒构造方法
    *
            * @param context 一个上下文对象
    */
    public MyWakeUp(Context context) {
        this.context = context;
        //create方法示是一个静态方法，还有一个重载方法EventManagerFactory.create(context, name, version)
        //由于百度文档没有给出每个参数具体含义，我们只能按照官网给的demo写了
        mWpEventManager = EventManagerFactory.create(context, "wp");
        //注册监听事件
        mWpEventManager.registerListener(new MyEventListener());
        mAm = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mListener = new MyOnAudioFocusChangeListener();
    }

    /**
     * 开启唤醒功能
     */
    public void start() {
        int result = mAm.requestAudioFocus(mListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        {
            HashMap<String, String> params = new HashMap<>();
            // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
            params.put("kws-file", "assets:///WakeUp.bin");
            mWpEventManager.send("wp.start", new JSONObject(params).toString(), null, 0, 0);
            Log.d(TAG, "----->唤醒已经开始工作了");
        }

    }

    /**
     * 关闭唤醒功能
     */
    public void stop() {
        // 具体参数的百度没有具体说明，大体需要以下参数
        // send(String arg1, byte[] arg2, int arg3, int arg4)
        mAm.abandonAudioFocus(mListener);
        mWpEventManager.send("wp.stop", null, null, 0, 0);
        Log.d(TAG, "----->唤醒已经停止");
    }


    private AudioManager mAm;

    private MyOnAudioFocusChangeListener mListener;
    private class MyOnAudioFocusChangeListener implements
            AudioManager.OnAudioFocusChangeListener
    {
        @Override
        public void onAudioFocusChange(int focusChange)
        {
            Log.i(TAG,"focusChange "+focusChange);
             if(focusChange==AudioManager.AUDIOFOCUS_GAIN)
            {
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                HashMap<String, String> params = new HashMap<>();
                // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
                params.put("kws-file", "assets:///WakeUp.bin");
                mWpEventManager.send("wp.start", new JSONObject(params).toString(), null, 0, 0);
                Log.d(TAG, "----->唤醒已经开始工作了");
            }
            else
             {
                 mWpEventManager.send("wp.stop", null, null, 0, 0);
                 Log.d(TAG, "----->唤醒已经停止");
             }
        }
    }
    private class MyEventListener implements EventListener {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            //try {
                //解析json文件
                //JSONObject json = new JSONObject(params);
                if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                    //String word = json.getString("word"); // 唤醒词
                     /*
                      * 这里大家可以根据自己的需求实现唤醒后的功能，这里我们简单打印出唤醒词
                      */
                    String ACTION = "com.yaojinpet.say";
                    Intent intent=new Intent(ACTION);
                    intent.putExtra("operation","say");
                    intent.putExtra("source","唤醒");
                    intent.putExtra("words","您好!");
                    context.sendBroadcast(intent);
                    Log.i(TAG,"唤醒");
                } //else if ("wp.exit".equals(name)) {
                    // 唤醒已经停止
              //  }
            //} catch (JSONException e) {
                //throw new AndroidRuntimeException(e);
            //}
        }
    }
}
