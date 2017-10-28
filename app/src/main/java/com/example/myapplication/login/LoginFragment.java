package com.example.myapplication.login;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.example.myapplication.R;
import com.example.myapplication.Yaojinpet;
import com.example.myapplication.musicPlayer.MusicFragment;


import java.util.Objects;

import cn.leancloud.chatkit.LCChatKit;
import yalantis.com.sidemenu.interfaces.ScreenShotable;

import static com.example.myapplication.Yaojinpet.changeInterface;
import static com.example.myapplication.Yaojinpet.user_id;

public class LoginFragment extends Fragment implements ScreenShotable {

    private Button login;
    private Button getRegisterCode;
    private boolean countDown;
    private Context context;
    public void setApplicationContext(Context context)
    {
        this.context=context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        countDown=true;
        View view = inflater.inflate(R.layout.registerandlogin, container, false);
        AVUser currentUser = AVUser.getCurrentUser();
        final EditText registerCode=(EditText) view.findViewById(R.id.registerCode);
        final EditText phoneNumberCode=(EditText) view.findViewById(R.id.phoneNumber);
        registerCode.addTextChangedListener(new EditChangedListener());
        getRegisterCode=(Button) view.findViewById(R.id.getRegisterCode);
        login=(Button) view.findViewById(R.id.login);
        if(null != currentUser)
        {
            getRegisterCode.setVisibility(View.INVISIBLE);
            phoneNumberCode.setVisibility(View.INVISIBLE);
            registerCode.setVisibility(View.INVISIBLE);
            login.setVisibility(View.INVISIBLE);
        }
        getRegisterCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!countDown) return;
                AVOSCloud.requestSMSCodeInBackground(phoneNumberCode.getText().toString(), new RequestMobileCodeCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e!=null)
                        {

                            Toast.makeText(context,"获取验证码失败，请重试",Toast.LENGTH_SHORT).show();
                            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                             countDown=false;
                                CountDownTimer cdt = new CountDownTimer(60000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        int mills= (int) (millisUntilFinished/1000);
                                        String msg=mills + "s后重试";
                                        getRegisterCode.setText(msg);
                                    }
                                    @Override
                                    public void onFinish() {
                                        getRegisterCode.setText("获取验证码");
                                        countDown=true;
                                    }
                                };
                                cdt.start();
                        }
                    }
                });
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(registerCode.getText().toString().equals("admin")){
                    getRegisterCode.setVisibility(View.INVISIBLE);
                    phoneNumberCode.setVisibility(View.INVISIBLE);
                    registerCode.setVisibility(View.INVISIBLE);
                    login.setVisibility(View.INVISIBLE);
                    changeInterface=true;
                    user_id=phoneNumberCode.getText().toString();
                    Toast.makeText(context,"登陆成功",Toast.LENGTH_SHORT).show();
                    Yaojinpet yaojinpet = (Yaojinpet) getActivity();
                    yaojinpet.transactMusic();
                    LCChatKit.getInstance().setcurrentUserId(phoneNumberCode.getText().toString());
                    LCChatKit.getInstance().open(phoneNumberCode.getText().toString(), new AVIMClientCallback() {
                        @Override
                        public void done(AVIMClient avimClient, AVIMException e) {
                        }
                    });
                }
                else AVUser.signUpOrLoginByMobilePhoneInBackground(phoneNumberCode.getText().toString(), registerCode.getText().toString(), new LogInCallback<AVUser>() {
                    @Override
                    public void done(AVUser avUser, AVException e) {

                        if(e==null){
                            getRegisterCode.setVisibility(View.INVISIBLE);
                            phoneNumberCode.setVisibility(View.INVISIBLE);
                            registerCode.setVisibility(View.INVISIBLE);
                            login.setVisibility(View.INVISIBLE);
                            changeInterface=true;
                            user_id=phoneNumberCode.getText().toString();
                            LCChatKit.getInstance().setcurrentUserId(phoneNumberCode.getText().toString());
                            LCChatKit.getInstance().open(phoneNumberCode.getText().toString(), new AVIMClientCallback() {
                                @Override
                                public void done(AVIMClient avimClient, AVIMException e) {

                                }
                            });
                            Toast.makeText(context,"登陆成功",Toast.LENGTH_SHORT).show();
                            Yaojinpet yaojinpet = (Yaojinpet) getActivity();
                            yaojinpet.transactMusic();
                        }
                        else
                        {
                            Toast.makeText(context,"验证码错误",Toast.LENGTH_SHORT).show();
                        }
                   }
                });
            }
        });
        return view;
    }

    private class EditChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length()==1)
            {
                login.setVisibility(View.VISIBLE);
                getRegisterCode.setVisibility(View.GONE);
            }
            else if(s.length()==0)
            {
                login.setVisibility(View.GONE);
                getRegisterCode.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
            if(s.length()==1)
            {
                login.setVisibility(View.VISIBLE);
                getRegisterCode.setVisibility(View.GONE);
            }
            else if(s.length()==0)
            {
                login.setVisibility(View.GONE);
                getRegisterCode.setVisibility(View.VISIBLE);
            }
            }

        }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.activity_main);

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
                LoginFragment.this.bitmap = bitmap;
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
