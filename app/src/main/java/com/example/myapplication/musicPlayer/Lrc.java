package com.example.myapplication.musicPlayer;


import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Lrc {

    String getLrcPath(String path)
    {
        String TAG = "Lrc.java";
        Log.d(TAG,path);
        String musicName;
        String singerName;
        path=path.substring(path.lastIndexOf('/')+1);
        path=path.substring(0,path.lastIndexOf('.'));
        path=path.replace('-',' ');
        String string[]=path.split(" ");
        musicName=string[string.length-1];
        singerName=string[0];
        Log.d(TAG,singerName+" "+musicName+" "+path);
        return getLrcPath(musicName,singerName);
    }
    private String getLrcPath(final String musicName, final String singerName)
    {
        File sd= Environment.getExternalStorageDirectory();
        String path=sd.getPath()+"/yaojinli/lrc/"+musicName+"-"+singerName;
        File file=new File(path);
        if(file.exists())
            return path;
        new Thread(){
            @Override
            public void run(){
                SearchLRC search = new SearchLRC(musicName, singerName);
                ArrayList result = search.fetchLyric();
                if(result!=null)
                {
                    String word = "";
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            word += result.get(i);
                            word += "\n";
                        }
                    }
                    try {
                        saveToSDCard(musicName+"-"+singerName,word);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
        return path;
    }

    private void saveToSDCard(String filename, String filecontent)throws Exception{
        try {
            if(filecontent.length()<1) return;
            //判断SDcard是否存在并且可读写
            File sd= Environment.getExternalStorageDirectory();
            String path=sd.getPath()+"/yaojinli/lrc/";
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                File file = new File(path,filename);
                if (!file.exists())
                    //在指定的文件夹中创建文件
                    if(file.createNewFile())
                    {
                        OutputStreamWriter oStreamWriter = new OutputStreamWriter(new FileOutputStream(file), "GBK");
                        oStreamWriter.append(filecontent);
                        oStreamWriter.close();
                    }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
