package com.example.myapplication.musicPlayer;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.voice.Voice;
import com.iflytek.cloud.util.UserWords;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import yalantis.com.sidemenu.interfaces.ScreenShotable;

import static android.content.Context.BIND_AUTO_CREATE;
import static java.lang.Thread.sleep;

public class MusicFragment extends Fragment implements View.OnClickListener ,ScreenShotable {

    ///////////////色子////////////////////////////////
    private StereoView stereoView;
    private ViewPager mViewPager;
    private int[] mImgIds = new int[] {R.drawable.sign_in_background, R.drawable.pet,
            R.drawable.sign_in_background, R.drawable.pet,R.drawable.sign_in_background, R.drawable.pet};
    private List<ImageView> mImageViews = new ArrayList<>();
    //////////////////////////////////////////////////

    private boolean barPressed=false;
    private LrcView mLrcViewOnPage; // 7 lines lrc
    private Timer timer;
    private Context context;
    private ImageButton play;
    private TextView status;
    private TextView duration;
    private SeekBar bar;
    private TextView start;//显示播放时长、播放器状态、当前播放时间
    //private static pl.droidsonroids.gif.GifImageView gift_music_list;
    public MusicPlayerService.MusicePlayerBinder musicplay;//与播放器的binder联系
    public static String path;
    private static boolean re=false;//
    private List<Map<String,String>> musicImformation= new ArrayList<>();
    Cursor cursor;


    public MusicFragment(){}
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /////////获得MusicPlayerService中的binder实例/////////////
            musicplay=(MusicPlayerService.MusicePlayerBinder) service;
            /////////初始化MusicPlayerService/////////////////////////
            //musicplay.sethandler(handler_music);
            musicplay.setpath(path);
            musicplay.init();
            /////////设置当前播放状态/////////////////////////////////
            status.setText(musicplay.getString());

            if(Objects.equals(musicplay.getString(), getString(R.string.Playing))) play.setBackgroundResource(R.drawable.music_pause);

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private SensorEventListener listener =new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float xValue=Math.abs(event.values[0]);
            float yValue=Math.abs(event.values[1]);
            float zValue=Math.abs(event.values[2]);
            if(event.values[2]<-8.5)
            {
                if(status.getText()=="Playing")
                    re=true;
                pause();
            }
            else if(event.values[2]>8.5&& re)
            {
                re=false;
                play();
            }
            ////////加速度大于15m/s^2触发//////////////////////////////
            else if(xValue>15||yValue>15||zValue>15)
            {
                next();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //////////初始化必要操作////////////////////////////////
        View view = inflater.inflate(R.layout.activity_control, container, false);
        ImageButton last = (ImageButton) view.findViewById(R.id.lastsong);
        ImageButton next = (ImageButton) view.findViewById(R.id.nextsong);
        duration = (TextView) view.findViewById(R.id.duration);
        play= (ImageButton) view.findViewById(R.id.play);
        status=(TextView) view.findViewById(R.id.status);
        start=(TextView) view.findViewById(R.id.start);
        bar = (SeekBar) view.findViewById(R.id.progress);

        //gift_music_list= (GifImageView) view.findViewById(R.id.gift_music_list);

        ///////////////////////////色子//////////////////////////////////////////
        stereoView=(StereoView) view.findViewById(R.id.stereoView) ;
        mViewPager = (ViewPager) view.findViewById(R.id.id_viewpager);

        InterceptorFrameLayout interceptorFrameLayout=(InterceptorFrameLayout)
                view.findViewById(R.id.InterceptorFrameLayout);
        interceptorFrameLayout.addInterceptorView(mViewPager,
                InterceptorFrameLayout.ORIENTATION_LEFT
                        | InterceptorFrameLayout.ORIENTATION_RIGHT);
        StereoView stereoView=(StereoView) view.findViewById(R.id.stereoView);
        interceptorFrameLayout.addInterceptorView(stereoView);

        mViewPager.setPageTransformer(true, new RotateDownPageTransformer());

        initData();


        /////////////////////////////////////////////////////////////////////////

        //SlidingUpPanelLayout slidingUpPanelLayout=(SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);


        mLrcViewOnPage = (LrcView) view.findViewById(R.id.lrcView);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        last.setOnClickListener(this);
        ////////////////设置bar的拖动事件/////////////////////////
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /////////显示进度条对应的时间/////////////////////
                Date date = new Date(progress);
                SimpleDateFormat sf = new SimpleDateFormat("mm:ss ", Locale.getDefault());
                start.setText(sf.format(date));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                /////////显示进度条对应的时间/////////////////////
                Date date = new Date(seekBar.getProgress());
                SimpleDateFormat sf = new SimpleDateFormat("mm:ss ", Locale.getDefault());
                start.setText(sf.format(date));
                if(!barPressed)barPressed=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                /////显示进度条对应的时间，并且更改当前播放时间为进度条对应的时间///////////////
                musicplay.seek(seekBar.getProgress());
                Date date = new Date(seekBar.getProgress());
                SimpleDateFormat sf = new SimpleDateFormat(getString(R.string.mmss), Locale.getDefault());
                start.setText(sf.format(date));
                mLrcViewOnPage.onDrag(seekBar.getProgress());
                barPressed=false;
            }
        });
        ListView listView = (ListView) view.findViewById(R.id.musicListView);
        SimpleAdapter adapter;
        adapter = new SimpleAdapter(context,musicImformation,
                R.layout.musiclist,new String[]{"name"},new int[]{R.id.musicName});

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                view.setBackgroundColor(5);
                /////////歌//////////////////
                path=musicImformation.get(i).get("path");
                musicplayingid=i;
                musicplay.setpath(path);
                ///////////停止播放///////////////////
                musicplay.stop();
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ///////////继续播放///////////////////

                play();
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mLrcViewOnPage.setLrcPath(getLrcPath(path));
            }
        });

        ////////////与MusicPlayerService服务绑定//////////////////
        Intent intent;
        intent = new Intent(context, MusicPlayerService.class);
        context.startService(intent);
        context.bindService(intent, connection, BIND_AUTO_CREATE);
        cursor.close();
        String ACTION = "com.yaojinpet.music";
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        context.registerReceiver(myMusicReceiver,filter);
        return view;
    }

    private void initData_beforeViewCreated()
    {
        for (int imgId : mImgIds)
        {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(imgId);
            mImageViews.add(imageView);
        }
    }
    private void initData()
    {
        for (int imgId : mImgIds)
        {
            ImageView imageView2 = new ImageView(context);
            imageView2.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView2.setImageResource(imgId);
            stereoView.addView(imageView2);
        }
        mViewPager.setCurrentItem(1);
        /*for(char a='F';a<='Z';a++)
        {
            textView.setText(String.valueOf(a));
            System.out.println(textView.getHeight()+" "+textView.getWidth());
            Bitmap bitmap = Bitmap.createBitmap(textView.getWidth(),
                    textView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            textView.draw(canvas);
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bitmap);
            mImageViews.add(imageView);
        }*/
        mViewPager.setAdapter(new PagerAdapter()
        {
            @Override
            public Object instantiateItem(ViewGroup container, int position)
            {
                container.addView(mImageViews.get(position));
                return mImageViews.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object)
            {
                container.removeView(mImageViews.get(position));
            }

            @Override
            public boolean isViewFromObject(View view, Object object)
            {
                return view == object;
            }

            @Override
            public int getCount()
            {
                return mImgIds.length;
            }
        });
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.play:
                if(musicplay.isplaying())
                pause();
                else
                play();
                break;
            case R.id.lastsong:
                back();
                break;
            case R.id.nextsong:
                next();
                break;
            default:
                break;
        }
    }
    private UserWords userWords=new UserWords();
    static int musicplayingid=0;
    private boolean getMusic=false;
    public void ScannerMusic()
    {
        //System.out.println("HELLO "+ran.nextInt()%cursor.getCount());
        // 遍历媒体数据库
        //for(int i=Math.abs(ran.nextInt()%cursor.getCount());i>0;i--)
        if(!getMusic) {
            if(cursor.isAfterLast())
            {
                cursor.close();
                return;
            }
            else
            while (!cursor.isAfterLast()) {
                /*// 歌曲编号
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                // 歌曲id
                int trackId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                // 歌曲标题
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                // 歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                // 歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                // 歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                // 歌曲的总播放时长：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                // 歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                // 歌曲文件显示名字
                String disName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                */
                //int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String disName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                disName=getSong(disName);
                // if(duration<300) continue;
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                Map<String, String> imformation = new HashMap<>();
                imformation.put("name", disName);
                imformation.put("path", path);
                userWords.putWord(disName);
                musicImformation.add(imformation);
                cursor.moveToNext();
            }
            if(!getMusic) {
                Voice voice=new Voice();
                voice.updateUsrWords(userWords);
            }

        }
        getMusic=true;
        path=musicImformation.get(musicplayingid++).get("path");
        if(musicplayingid==musicImformation.size()) musicplayingid=0;
    }

    /////////////////从文件名中分割出歌曲名///////////////////////////////////////////////////
    public static String getSong(String args)
    {
        String L;
        int pos=0;
        int prepos=0;
        for (int i = 0; i < args.length(); i++)
        {
            if (args.substring(i, i + 1).equals(" "))
            {
                prepos=i;
            }
            else if (args.substring(i, i + 1).equals("."))
            {
                pos=i;
            }
        }
        L=args.substring(prepos,pos).trim();
        return L;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unbindService(connection);
        context.unregisterReceiver(myMusicReceiver);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(musicplay!=null)
        {
            initial();
            if(musicplay.isplaying()) {
                timer = new Timer();
                timer.scheduleAtFixedRate(new MusicFragment.RefreshTask(), 0, 800);
            }
        }
    }

    public void setApplicationContext(Context context)
    {
        this.context=context;
        assert context != null;
        cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        assert cursor != null;
        cursor.moveToFirst();
        ScannerMusic();
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        try {
            sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        initData_beforeViewCreated();

    }

    class RefreshTask extends TimerTask {

        @Override
        public void run() {
            //////////更新进度条//////////////////
            if(barPressed) return;
            bar.setProgress(musicplay.getCur());
            mLrcViewOnPage.changeCurrent(musicplay.getCur());
        }

    }

    private void initial()
    {
        if(musicplay==null) return;
        musicplay.setpath(path);
        Date date = new Date(musicplay.getDuration());
        SimpleDateFormat sf = new SimpleDateFormat("mm:ss ", Locale.getDefault());
        duration.setText(sf.format(date));
        /////////设置进度条最大值//////////////
        bar.setMax(musicplay.getDuration());
        bar.setProgress(musicplay.getCur());
    }

    private void play()
    {
        if(!musicplay.isplaying()) initial();
        /////切换到播放状态////////////
        re=false;
        musicplay.play();
        timer = new Timer();
        timer.scheduleAtFixedRate(new MusicFragment.RefreshTask(), 0, 800);
        play.setBackgroundResource(R.drawable.music_pause);
        status.setText(R.string.playing);
    }


    private void pause()
    {
        musicplay.pause();
        if (timer != null) {
           timer.cancel();
        }
        play.setBackgroundResource(R.drawable.music_play);
        status.setText(R.string.Stopped);
    }


    private void next()
    {
        /////////歌//////////////////
        if(musicplayingid==musicImformation.size()-1) musicplayingid=-1;
        path=musicImformation.get(++musicplayingid).get("path");
        musicplay.setpath(path);
        mLrcViewOnPage.setLrcPath(getLrcPath(path));
        ///////////停止播放///////////////////
        musicplay.stop();
        try {
            sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ///////////继续播放///////////////////

        play();
        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void back()
    {
        /////////歌//////////////////
        if(musicplayingid==0) musicplayingid=musicImformation.size();
        path=musicImformation.get(--musicplayingid).get("path");
        musicplay.setpath(path);
        mLrcViewOnPage.setLrcPath(getLrcPath(path));
        ///////////停止播放///////////////////
        musicplay.stop();
        try {
            sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ///////////继续播放///////////////////

        play();
        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean specificMusic(String specificMusicName)
    {
        /////////歌//////////////////
        for (Map<String,String> i:
             musicImformation) {
            Log.d("song",i.get("name")+"and"+specificMusicName);
            if(Objects.equals(i.get("name"), specificMusicName))
            {
                path=i.get("path");
                musicplay.setpath(path);
                ///////////停止播放///////////////////
                musicplay.stop();
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ///////////继续播放///////////////////

                play();
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }
    /*private void loop()
    {

    }*/

    private BroadcastReceiver myMusicReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String msg;
            if(intent.getStringExtra("musicName")!=null) {

                msg=intent.getStringExtra("musicName");
                if(!specificMusic(msg))
                {
                    Log.d("sucess","sucessful");
                    final String ACTION = "com.yaojinpet.say";
                    intent=new Intent(ACTION);
                    intent.putExtra("justSayMsg","sorry，没有找到该音乐："+msg);
                    context.sendBroadcast(intent);

                }

            }


        }

    };

    String getLrcPath(final String path)
    {
        int i=0;
        for(int k=path.length()-1;k>0;k--)
        {
            if(path.charAt(k)=='.')
            {
                i=k;
                break;
            }
        }
        File file=new File(path.substring(0,i)+".lrc");
        if(file.exists())
        return path.substring(0,i)+".lrc";
        final Lrc lrc=new Lrc();
        file=new File(lrc.getLrcPath(path));
        if(!file.exists())
        {
            CountDownTimer cdt = new CountDownTimer(10000, 500) {
                @Override
                public void onTick(long millisUntilFinished) {

                }
                @Override
                public void onFinish() {
                    mLrcViewOnPage.setLrcPath(lrc.getLrcPath(path));
                }
            };
            cdt.start();
            return "";
        }
        return lrc.getLrcPath(path);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.activity_control);

    }
    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
               MusicFragment.this.bitmap = bitmap;
            }
        };
        thread.start();

    }
    private Bitmap bitmap;
    private View containerView;
    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }
}