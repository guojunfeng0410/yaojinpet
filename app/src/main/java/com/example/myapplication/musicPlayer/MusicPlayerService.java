package com.example.myapplication.musicPlayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.myapplication.voice.MyWakeUp;

import java.util.Objects;



public class MusicPlayerService extends Service {

    private static final String TAG = "MusicPlayerService";
    private boolean musicdisable=false;
    private MusicePlayerBinder mBinder =new MusicePlayerBinder();
    private static MediaPlayer mediaPlayer=new MediaPlayer();
    private static String string="";//string可能取值为"Playing"、"Stopped"、"Paused"、“”
    private static boolean focus=false;
    private String path;

    boolean pausebyAudioFocusChangeListener=false;

    ///////////////音频冲突///////////////////////////////////
    private AudioManager mAm;

    private MyOnAudioFocusChangeListener mListener;
    private class MyOnAudioFocusChangeListener implements
            AudioManager.OnAudioFocusChangeListener
    {
        @Override
        public void onAudioFocusChange(int focusChange)
        {
            Log.i(TAG,"focusChange "+focusChange);
            if(focusChange==-1)  {
                if(musicdisable) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(mediaPlayer.isPlaying())
                        {
                            pausebyAudioFocusChangeListener=true;
                            mediaPlayer.pause();//暂停播放
                            string="Paused";//设置进度条上显示的字符串为"Paused"
                        }
                        stopSelf();
                    }
                }).start();
            }
            else if(focusChange==1)
            {
                if(musicdisable) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!mediaPlayer.isPlaying()&&pausebyAudioFocusChangeListener)
                        {
                            int result = mAm.requestAudioFocus(mListener,
                                    AudioManager.STREAM_MUSIC,
                                    AudioManager.AUDIOFOCUS_GAIN);

                            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                            {
                                // Start playback.
                                pausebyAudioFocusChangeListener=false;
                                mediaPlayer.start();//开始播放
                                string="Playing";//设置进度条上显示的字符串为"Playing"
                            }
                            else
                            {
                                Log.e(TAG, "requestAudioFocus failed.");
                            }

                        }
                        stopSelf();
                    }
                }).start();
            }
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mAm = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mListener = new MyOnAudioFocusChangeListener();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(focus)
                {
                    mAm.abandonAudioFocus(mListener);
                    focus=false;
                }
            }
        });

    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }
    public class MusicePlayerBinder extends Binder {
        void init()
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    /*File file=new File(Environment.getExternalStorageDirectory(),
                            "/data/K.Will-Melt.mp3");*/
                    try {
                        /////string==""表示mediaplayer尚未初始化，string=="Stopped"表示停止播放////////
                        if(Objects.equals(string, "") || Objects.equals(string, "Stopped"))
                        {
                            mediaPlayer.reset();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(path);
                            mediaPlayer.prepare();
                            ///////////////////实例化mediaPlayer///////////////////////////////
                            //mediaPlayer=MediaPlayer.create(getApplicationContext(),R.raw.a);
                            mediaPlayer.setLooping(false);
                        }
                    }
                    catch(Exception e)
                    {
                        musicdisable=true;
                        System.out.println("HELLO 引用资源失败");
                    }
                    stopSelf();

                }
            }).start();

        }
        ///////////点击PLAY按钮时，将会运行该函数///////////////////////////////
        void play()
        {
            if(musicdisable) return;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(!mediaPlayer.isPlaying())
                    {
                        int result = mAm.requestAudioFocus(mListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN);

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        {
                            Log.i(TAG, "requestAudioFocus successfully.");
                            // Start playback.
                            mediaPlayer.start();//开始播放
                            string="Playing";//设置进度条上显示的字符串为"Playing"
                            focus=true;
                        }
                        else
                        {
                            Log.e(TAG, "requestAudioFocus failed.");
                        }

                    }
                    stopSelf();
                }
            }).start();
        }

        void pause()
        {
            if(musicdisable) return;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer.isPlaying())
                    {
                        if(focus)
                        {
                            mAm.abandonAudioFocus(mListener);
                            focus=false;
                        }
                        mediaPlayer.pause();//暂停播放
                        string = "Paused";//设置进度条上显示的字符串为"Paused"

                    }
                    stopSelf();
                }
            }).start();
        }
        boolean isplaying()
        {
            return mediaPlayer.isPlaying();
        }
        ///////////点击STOP按钮时，将会运行该函数///////////////////////////////
        void stop()
        {
            if(musicdisable) return;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    string="Stopped";//设置进度条上显示的字符串为"Stopped"
                    mediaPlayer.reset();//重置mediaplayer
                    init();//初始化mediaplayer
                    stopSelf();
                }
            }).start();
        }


        /////////////更改音乐播放器播放时间//////////////////////////////////////////////////
        void seek(final int progess)
        {
            if(musicdisable) return;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mediaPlayer.seekTo(progess);
                    stopSelf();
                }
            }).start();
        }
        public String getString()
        {
            return string;
        }
        int getCur()
        {
            if(musicdisable) return 0;
            return mediaPlayer.getCurrentPosition();
        }
        int getDuration()
        {
            if(musicdisable) return 0;
            return mediaPlayer.getDuration();
        }
        void setpath(String musicPath)
        {
            path=musicPath;
        }
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }


}
