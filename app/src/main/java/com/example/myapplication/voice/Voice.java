package com.example.myapplication.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.example.myapplication.communication.JsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.UserWords;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;



public class  Voice {
    private String TAG="I/O";
    private Context context;
    private static SpeechRecognizer mIat;
    private static SpeechSynthesizer mTts;
    private boolean listenAfterSpeak;
    private boolean end;
    private boolean EnglishListener=false;
    private  int speed=50;
    public Voice()
    {}
    Voice(Context context,int s)
    {
        this.context=context;
        speed=s;
        mTts.setParameter(SpeechConstant.SPEED, String.valueOf(speed));// 设置语速
    }
    public Voice(Context context,Boolean ini)
    {
        if(!ini) return;
        SpeechUtility.createUtility(context, SpeechConstant.APPID +"=584b7ab3");
        mIat = SpeechRecognizer.createRecognizer(context, null);
        //2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        //mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory().getAbsolutePath()+"yaojinli/voice");
        //mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

        mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        //指定引擎类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        mTts= SpeechSynthesizer.createSynthesizer(context, null);
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        mTts.setParameter(SpeechConstant.VOICE_NAME, "catherine"); //设置发音人
        mTts.setParameter(SpeechConstant.SPEED, String.valueOf(speed));// 设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");// 设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
    }

    public void finish()
    {
        mTts.stopSpeaking();
        sayCompleted();
    }

    void operation(String source, String operation, String words)
    {
        end=false;
        if(source.equals("Notification")) listenAfterSpeak=false;
        switch (operation) {
            case "EnglishListener":
                end = true;
                listenAfterSpeak = false;
                EnglishListener = true;
                mTts.startSpeaking(words, mSynListener);

                break;
            case "say":
                mTts.startSpeaking(words, mSynListener);
                listenAfterSpeak = true;
                break;
            case "listen":
                mIat.startListening(mRecoListener);
                listenAfterSpeak = false;
                break;
        }
    }
    private void resultAnalyse()
    {
        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults .get(key));
        }
        if (!resultBuffer.toString().contains("不要") && !resultBuffer.toString().contains("不播放")
                && (resultBuffer.toString().contains("播放") || resultBuffer.toString().contains("要")
                || resultBuffer.toString().contains("好"))) {
            //if (musicplay == null)
           // {
           //     end=true;
           //     mTts.startSpeaking("请您先启动音乐播放器", mSynListener);
          //  }
           // else
            if (resultBuffer.toString().contains("播放")) {

                mTts.startSpeaking("马上为您播放", mSynListener);String ACTION = "com.yaojinpet.music";
                Intent intent = new Intent(ACTION);
                String song=getSong(resultBuffer.toString());
                end=true;
                if(song.equals("-error")) {
                    ACTION = "com.yaojinpet.say";
                    intent = new Intent(ACTION);
                    intent.putExtra("end", "end");
                    context.sendBroadcast(intent);
                }
                intent.putExtra("musicName", getSong(resultBuffer.toString()));
                context.sendBroadcast(intent);
            }

        }
        else
        {
            end=true;
            String ACTION = "com.yaojinpet.say";
            Intent intent = new Intent(ACTION);
            intent.putExtra("end", "end");
            context.sendBroadcast(intent);
        }
    }
    private void sayCompleted()
    {
        if(listenAfterSpeak)
        {
            listenAfterSpeak=false;
            mIat.startListening(mRecoListener);
        }
        else if(end) {
            final String ACTION = "com.yaojinpet.say";
            Intent intent = new Intent(ACTION);
            intent.putExtra("end", "end");
            context.sendBroadcast(intent);
            if(EnglishListener)
            {
                EnglishListener=false;
                intent = new Intent("com.yaojinpet.EnglishListener");
                intent.putExtra("next","next");
                context.sendBroadcast(intent);
            }
        }
    }
    public void updateUsrWords(UserWords userWords)
    {
        if(mIat.updateLexicon("userword", userWords.toString(), lexiconListener)!= ErrorCode.SUCCESS){
            Log.d("here111","上传用户词表失败");
        }
    }
    private SynthesizerListener mSynListener = new SynthesizerListener(){
        public void onCompleted(SpeechError error) {
            sayCompleted();
            Log.i(TAG,"播放结束");
        }
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}
        public void onSpeakBegin() {
            Log.i(TAG,"播放开始");
        }
        public void onSpeakPaused() {}
        public void onSpeakProgress(int percent, int beginPos, int endPos) {}
        public void onSpeakResumed() {}
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
    };
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private RecognizerListener mRecoListener = new RecognizerListener() {
        //听写结果回调接口(返回Json格式结果，用户可参见附录12.1)；
        //一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
        //关于解析Json的代码可参见MscDemo中JsonParser类；
        //isLast等于true时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = results.getResultString(); //为解析的
            String text = JsonParser.parseIatResult(result) ;//解析过后的
            String sn = null;
            // 读取json结果中的 sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString()) ;
                sn = resultJson.optString("sn" );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIatResults .put(sn, text) ;//得到一句，添加到
        }
        //会话发生错误回调接口
        public void onError(SpeechError error) {
            error.getPlainDescription(true);
            //获取错误码描述
            mTts.startSpeaking("听不到你在说什么", mSynListener);
            listenAfterSpeak=true;
            Log.i(TAG,"录音结束");
        }
        //音量值0~30
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }
        //开始录音
        public void onBeginOfSpeech () {
            Log.i(TAG,"开始录音");
        }

        //结束录音
        public void onEndOfSpeech() {
           resultAnalyse();
            Log.i(TAG,"录音结束");
            //myWakeUp.start();
        }
        //扩展用接口
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private String getSong(String args)
    {
        StringBuilder song=new StringBuilder();
        boolean findBeginOfsong=false;//找到歌名开始
        //[`~!@#$%^&*()+=|{}':;',\[\].<>/?~！@#￥%……& amp;*（）——+|{}【】‘；：”“’。，、？|-]
        args = args.replaceAll("[`~!@#$%^&*()+=|{}':;,\\[\\].<>/?！￥… amp（）—【】‘；：”“’。，、？-]","-");
        if(args.contains("播放"))
        {
            String a[]=args.split("播放");
            if(a.length<1) return "-error";
            StringBuilder containSong=new StringBuilder(a[1]);
            Log.d("songFromVoice",containSong.toString());
            for(int i=0;i<containSong.length();i++)
            {
                if(containSong.charAt(i)!='-')
                {
                    findBeginOfsong=true;
                    song.append(containSong.charAt(i));
                    Log.d("songFromVoice",song.toString());
                }
                else if(findBeginOfsong&&containSong.charAt(i)=='-')
                    break;
            }
        }
        return song.toString();
    }
    // 上传用户词表监听器。
    private LexiconListener lexiconListener = new LexiconListener() {
        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
            if(error != null){
                Log.d("lexicon",error.toString());
            }else{
                Log.d("lexicon","上传成功！");
            }
        }
    };
}
