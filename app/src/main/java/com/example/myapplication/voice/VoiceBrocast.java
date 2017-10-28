package com.example.myapplication.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.MyQueue;

import java.util.Objects;

public class VoiceBrocast extends BroadcastReceiver {
    MyQueue queue=new MyQueue();
    Voice voice;
    boolean send=true;
    int speed=50;
    class Storage
    {
        private String source,operation ,words;
        Storage(String source,String operation,String words)
        {
            this.operation=operation;
            this.words=words;
            this.source=source;
        }

        String getOperation() {
            return operation;
        }

        public String getSource() {
            return source;
        }

        String getWords() {
            return words;
        }
    }

    @Override
    public synchronized void onReceive(final Context context, Intent intent) {
        if(intent.getStringExtra("operation")!=null&& Objects.equals(intent.getStringExtra("operation"), "stop"))
        {
            if(voice!=null)
            voice.finish();
            return;
        }
        voice=new Voice(context,speed);
        if(intent.getStringExtra("speed")!=null)
        {
            if(Objects.equals(intent.getStringExtra("speed"), "add"))
            {
                speedAdd(context);
            }
            else
            {
                speedReduce(context);
            }
            return;
        }
        if(intent.getStringExtra("end")!=null)
        {
            send=true;
            operation();

            return;
        }
        queue.enQueue(new Storage(intent.getStringExtra("source"),intent.getStringExtra("operation"),intent.getStringExtra("words")));
        operation();
        send=false;
    }

    void operation()
    {
        if(send&&!queue.QueueEmpty())
        {
            Storage storage= (Storage) queue.deQueue();
            voice.operation(storage.getSource(),storage.getOperation(),storage.getWords());
        }

    }
    public void  speedAdd(Context context)
    {
        if(speed<=50)speed+=50;
        Intent intent = new Intent("com.yaojinpet.EnglishListener");
        intent.putExtra("vol",String.valueOf(speed));
        context.sendBroadcast(intent);
    }
    public void  speedReduce(Context context)
    {
        if( speed>=50) speed-=50;
        Intent intent = new Intent("com.yaojinpet.EnglishListener");
        intent.putExtra("vol",String.valueOf(speed));
        context.sendBroadcast(intent);
    }

}
