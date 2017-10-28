package com.example.myapplication.bookShelf;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;


public class BookReadView extends AppCompatActivity {

    BufferedReader br;
    File file;
    TextView bookview;
    private boolean stop=true;
    private int Num=8;
    private float Speed=2;
    private StringBuilder rowContent=new StringBuilder();
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.readbookview);
        Intent intent = getIntent();
        path = intent.getStringExtra("bookId");
        Log.d("BookReadView.java",path);
        file = new File(path);
        try {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file),charsetDetect(path)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bookview=(TextView) findViewById(R.id.bookview);
        final EditText frontSize=(EditText) findViewById(R.id.frontSize);
        final EditText frontNum=(EditText) findViewById(R.id.frontNumber);
        final EditText frontSpeed=(EditText) findViewById(R.id.frontSpeed);
        Button control=(Button) findViewById(R.id.stopandstart);
        control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stop)
                {
                    bookview.setTextSize(Float.valueOf(frontSize.getText().toString()));
                    Num=Integer.valueOf(frontNum.getText().toString());
                    Speed=Float.valueOf(frontSpeed.getText().toString());
                    stop=false;
                    timer=new Timer();
                    float a=1000/Speed;
                    int b=(int) a;
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            // 需要做的事:发送消息
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                    };
                    timer.schedule(task, 1000,b);
                }
                else
                {
                    stop=true;
                    timer.cancel();
                    timer=null;
                }
            }
        });
    }
    // handler类接收数据
     final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if(rowContent==null||rowContent.length()==0)
                {
                    rowContent.append(readBook());
                    Log.d("BookReadView：",rowContent.toString());
                }
                if(Num>rowContent.length())
                {
                    bookview.setText(rowContent.substring(0,rowContent.length()));
                    rowContent.delete(0,rowContent.length());
                }
                else if(2*Num>rowContent.length())
                {
                    int a=rowContent.length()/2;
                    bookview.setText(rowContent.substring(0,a));
                    rowContent.delete(0,a);
                }
                else
                {
                    bookview.setText(rowContent.substring(0,Num));
                    rowContent.delete(0,Num);
                }
                Log.d("BookReadView：",rowContent.toString());
            }
        }
    };

   String readBook()
    {
        if(!file.exists()) return "无此文件";
        String line;
        try {
            if (br == null) return "无此文件";
                if((line = br.readLine())!=null)
                    return line;
                else
                {
                    br.close();
                    br=new BufferedReader(new InputStreamReader(new FileInputStream(file),charsetDetect(path)));
                    return "即将开始重复播放";
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "无此文件";
    }

    Timer timer;

    public String charsetDetect(String path) {
        String _charset="";
        try {
            File file = new File(path);
            InputStream fs = new FileInputStream(file);
            byte[] buffer = new byte[3];
            fs.read(buffer);
            fs.close();

            if (buffer[0] == -17 && buffer[1] == -69 && buffer[2] == -65)
                _charset="UTF-8";
            else
                _charset="GBK";
        }
        catch (IOException e) {e.printStackTrace(); }
        return _charset;
    }

}
