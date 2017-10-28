package com.example.myapplication.musicPlayer;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

class SearchLRC {
    private URL url;

    SearchLRC(String musicName, String singerName)
    {
        search(musicName,singerName);
    }
    private  void  search(String musicName, String singerName) {

        try {
            if(singerName!=null)
            singerName = URLEncoder.encode(singerName, "UTF-8");
            String strUrl;
            if(singerName!=null)
                strUrl = "http://gecimi.com/api/lyric/"+URLEncoder.encode(musicName, "UTF-8")+"/"+singerName;
            else strUrl = "http://gecimi.com/api/lyric/"+URLEncoder.encode(musicName, "UTF-8");
            if(strUrl.endsWith("/")) strUrl=strUrl.substring(0,strUrl.length()-1);
            Log.d("CHENGJR1", "strUrl =" + strUrl);
            url = new URL(strUrl);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setRequestMethod("GET");

        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        InputStream inStream;
        String result="";
        try {
            if(conn==null)return;
            inStream = conn.getInputStream();
            InputStreamReader in=new InputStreamReader(inStream);
            BufferedReader bufferedReader=new BufferedReader(in);

            String readLine;
            while((readLine=bufferedReader.readLine())!=null)
            {
                result+=readLine;
            }
            in.close();
           conn.disconnect();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            JSONObject jsonObject2 = (JSONObject)jsonArray.opt(0);
            if(jsonObject2!=null){
                geciURL=jsonObject2.getString("lrc");
                aid=jsonObject2.getString("aid");}
            Log.i("CHENGJR1", "geciURL = null? geciURL=" + geciURL);
            if(geciURL==null&&singerName!=null)
            {
                Log.d("CHENGJR1", "无歌手名查找歌词");
                search(musicName,null);
                return;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private String aid;
    private String geciURL;
    ArrayList fetchLyric() {
        Log.d("CHENGJR1", "geciURL = " + geciURL);
        ArrayList gcContent = new ArrayList();
        String s;
        if(geciURL==null) return gcContent;
        try {
            url = new URL(geciURL);
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(url.openStream(),
                    "utf-8"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (br == null) {
            Log.d("CHENGJR1", "br is  null");
        } else {
            try {
                while ((s = br.readLine()) != null) {
                    gcContent.add(s);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return gcContent;
    }

}
