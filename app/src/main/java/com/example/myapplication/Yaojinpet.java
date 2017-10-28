package com.example.myapplication;


import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.example.myapplication.communication.CustomUserProvider;
import com.example.myapplication.fragment.ContactFragment;
import com.example.myapplication.login.LoginFragment;
import com.example.myapplication.musicPlayer.MusicFragment;
import com.example.myapplication.floatWindow.FloatWindowService;
import com.example.myapplication.voice.MyWakeUp;
import com.example.myapplication.voice.VoiceBrocast;

import java.util.List;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.activity.LCIMConversationListFragment;


public class Yaojinpet extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private int broadcastmode=0;
    //String APP_KEY="iumACdGj5RY2Kf3aMBxbEz0T";
    //String APP_ID="bIvqtbxvMx4Dplv64kyiSLUU-gzGzoHsz";
    private MyWakeUp myWakeUp;
    public static boolean changeInterface;
    public static String user_id;
    private VoiceBrocast voiceBrocast=new VoiceBrocast();
    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects().
                        penaltyLog()
                        .penaltyDeath()
                        .build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yaojinpet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcastmode++;
                if(broadcastmode==2) broadcastmode=0;
                if(broadcastmode==1)
                {
                    if (serviceIsRunning()) {
                        Toast.makeText(getApplicationContext(),"服务已经在运行！",Toast.LENGTH_SHORT).show();
                    } else {
                        startAccessibilityService();
                    }
                    //myWakeUp.start();
                }
                if(broadcastmode==0)
                {
                    Snackbar.make(view,"关闭播放到来消息", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                  
                }
                if(broadcastmode==2)
                {
                    Snackbar.make(view,"询问播放到来消息", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Intent intent = new Intent(Yaojinpet.this,RobMoney.class);
                    startService(intent);
                }

            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AVUser currentUser = AVUser.getCurrentUser();
        if(null != currentUser)
        {
            LCChatKit.getInstance().open( currentUser.getUsername(), new AVIMClientCallback() {
                @Override
                public void done(AVIMClient avimClient, AVIMException e) {

                }
            });
              changeInterface=true;
              user_id=currentUser.getUsername();
//            MusicFragment fragment=new MusicFragment();
//            fragment.setApplicationContext(getApplicationContext());
//            FragmentManager fragmentManager=getSupportFragmentManager();
//            FragmentTransaction transaction=fragmentManager.beginTransaction();
//            transaction.replace(R.id.contentFragment,fragment);
//            transaction.commit();
        }
        else
        {
            LoginFragment fragment=new LoginFragment();
            fragment.setApplicationContext(getApplicationContext());
            FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction transaction=fragmentManager.beginTransaction();
            transaction.replace(R.id.contentFragment,fragment);
            transaction.commit();
        }
        IntentFilter filter = new IntentFilter();
        String ACTION = "com.yaojinpet.say";
        filter.addAction(ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(voiceBrocast,filter);

        myWakeUp=new MyWakeUp(getApplicationContext());
        myWakeUp.start();
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.yaojinpet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            AVUser.logOut();
            finish();
            return true;
        }
        else
        if (id == R.id.pet) {
            permission();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_user_information)
        {
            transactLogin();
        }
        else if (id == R.id.nav_music) {
            transactMusic();
        }
        else if(!changeInterface)
        {
            Toast.makeText(this,"登录后可用",Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_contact) {
           transactContact();
            // Handle the camera action
        }  else if (id == R.id.nav_note) {
            LCIMConversationListFragment fragment=new LCIMConversationListFragment();
            FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction transaction=fragmentManager.beginTransaction();
            transaction.replace(R.id.contentFragment,fragment);
            transaction.commit();
        } else if (id == R.id.nav_pet) {

        } else if (id == R.id.nav_conservation) {
            transactConversion();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(voiceBrocast);
        myWakeUp.stop();
    }




    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////权限申请////////////////////////////////////////
    public void permission(){
        if (Build.VERSION.SDK_INT >= 23) {
            requestAlertWindowPermission();
            requestContactsPermission();
        } else {
            //Android6.0以下，不用动态声明权限
            Intent intent = new Intent(Yaojinpet.this,FloatWindowService.class);
            startService(intent);
        }
    }
    private static final int REQUEST_CODE_AlertWindow = 1;
    private static final int REQUEST_CODE_Contacts = 1;
    private  void requestAlertWindowPermission() {
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(getApplicationContext())){
            intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_AlertWindow);}
            else
            {
                intent = new Intent(Yaojinpet.this,FloatWindowService.class);
                startService(intent);
            }
        }

    }
    private  void requestContactsPermission() {
        int hasWriteContactsPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_CONTACTS);
            if(hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]
                                    {Manifest.permission.WRITE_CONTACTS}, REQUEST_CODE_Contacts);

        }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AlertWindow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Yaojinpet.this,FloatWindowService.class);
                    startService(intent);
                }
            }
        }
    }
    void transactLogin ()
    {
        fab.setVisibility(View.INVISIBLE);
        LoginFragment fragment=new LoginFragment();
        fragment.setApplicationContext(getApplicationContext());
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFragment,fragment);
        transaction.commit();
    }
    public void transactMusic()
    {
        fab.setVisibility(View.INVISIBLE);
        MusicFragment fragment=new MusicFragment();
        fragment.setApplicationContext(getApplicationContext());
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFragment,fragment);
        transaction.commit();

    }
    void transactContact()
    {
        fab.setVisibility(View.INVISIBLE);
        CustomUserProvider.getInstance().setContact(getApplicationContext());
        //LoginFragment fragment=new LoginFragment();
        ContactFragment fragment=new ContactFragment();
        fragment.setApplicationContext(getApplicationContext());
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFragment,fragment);
        transaction.commit();
    }
    void transactConversion()
    {
        fab.setVisibility(View.INVISIBLE);
        LCIMConversationListFragment fragment=new LCIMConversationListFragment();
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFragment,fragment);
        transaction.commit();
    }

    private boolean serviceIsRunning() {
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(Short.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : services) {
            if (info.service.getClassName().equals(getPackageName() + ".RobMoney")) {
                return true;
            }
        }
        return false;
    }
    private void startAccessibilityService() {
        new AlertDialog.Builder(this)
                .setTitle("开启辅助功能")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("使用此项功能需要您开启辅助功能")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 隐式调用系统设置界面
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    }
                }).create().show();
    }

    ////////////////////////////////////////////////////////////////////////////
}
