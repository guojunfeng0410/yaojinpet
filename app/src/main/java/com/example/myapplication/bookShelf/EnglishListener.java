package com.example.myapplication.bookShelf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class EnglishListener extends AppCompatActivity {
    BufferedReader br;
    File file;
    String path;
    boolean start=false;
    int position=0;
    private  ListView list;
    private  boolean cycle=false;
    final List<String> arr_data=new ArrayList<>();
    private EditText editText;

    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_englishlistener);
        Intent intent = getIntent();
        path = intent.getStringExtra("bookId");
        file = new File(path);

        editText=(EditText) findViewById(R.id.EnglishWord);
        final LinearLayout linearLayout=(LinearLayout) findViewById(R.id.speakControl);
        Button button1=(Button) findViewById(R.id.ListenerFaster);
        Button button2=(Button) findViewById(R.id.ListenerSlower);
        final Button button3=(Button) findViewById(R.id.cycle);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ACTION = "com.yaojinpet.say";
                Intent intent=new Intent(ACTION);
                intent.putExtra("speed","add");
                intent.putExtra("speed","add");
                sendBroadcast(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ACTION = "com.yaojinpet.say";
                Intent intent=new Intent(ACTION);
                intent.putExtra("speed","reduce");
                sendBroadcast(intent);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cycle)
                {
                    cycle=false;
                    button3.setText("循环");
                }
                else {
                    cycle=true;
                    button3.setText("不循环");
                }
            }
        });
        list=(ListView) findViewById(R.id.word_listview);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position_, long id) {
                position=position_-1;
                if(position==arr_data.size()-1)
                    position=-1;
                list.setSelection(position);
                String ACTION = "com.yaojinpet.say";
                Intent intent=new Intent(ACTION);
                intent.putExtra("operation","stop");
                sendBroadcast(intent);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(file),charsetDetect(path)));
                    String line;
                    while((line =myReadLine())!=null)
                    {
                        arr_data.add(line);
                    }
                    arr_data.add("\n\n\n\n\n\n\n\n\n\n\n\\n");
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                handler.sendMessage(message);
            }
        }).start();

         button=(Button) findViewById(R.id.startListener);
        //新建一个数组适配器ArrayAdapter绑定数据，参数(当前的Activity，布局文件，数据源)

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.yaojinpet.EnglishListener");
        registerReceiver(myReceiver, filter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start)
                {
                    start=false;
                    button.setText("继续");
                    String ACTION = "com.yaojinpet.say";
                    Intent intent=new Intent(ACTION);
                    intent.putExtra("operation","stop");
                    sendBroadcast(intent);
                    return;
                }
                button.setText("暂停");
                start=true;

                String ACTION = "com.yaojinpet.say";
                Intent intent=new Intent(ACTION);
                intent.putExtra("operation","EnglishListener");
                intent.putExtra("source","EnglishListener");
                if(editText.getText().toString().length()!=0)
                {
                    intent.putExtra("words",editText.getText().toString());
                }
                else intent.putExtra("words",getEnglish(arr_data.get(position)));
                sendBroadcast(intent);
                editText.setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
            }
        });
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: //
                        CountDownTimer cdt = new CountDownTimer(10000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }
                            @Override
                            public void onFinish() {
                               linearLayout.setVisibility(View.GONE);
                                editText.setVisibility(View.GONE);
                            }
                        };
                        cdt.start();
                        //System.out.println("停止...");
                        //System.out.println("停止...");
                        break;

                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        linearLayout.setVisibility(View.VISIBLE);
                        editText.setVisibility(View.VISIBLE);
                        //System.out.println("正在滑动...");
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        linearLayout.setVisibility(View.VISIBLE);
                        editText.setVisibility(View.VISIBLE);
                       // System.out.println("开始滚动...");
                        break;

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }
    public String myReadLine() throws IOException{
        StringBuilder sb = new StringBuilder();
        int ch = 0;
        while ((ch =br.read()) != -1) {
            if (ch == '\n'&&sb.length()>0)
                return sb.toString();
            else if(ch == '\n'&&sb.length()==0)continue;
            sb.append((char) ch);
            //if (ch == '.'||ch == '。')
              //  return sb.toString();

        }
        return  null;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }
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
    private BroadcastReceiver myReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getStringExtra("next")!=null&&start)
            {
                if(editText.getText().toString().length()!=0)
                {
                    String ACTION = "com.yaojinpet.say";
                    intent=new Intent(ACTION);
                    intent.putExtra("operation","EnglishListener");
                    intent.putExtra("source","EnglishListener");
                    intent.putExtra("words",editText.getText().toString());
                    sendBroadcast(intent);
                    return;
                }
                if(!cycle) position++;
                if(position==arr_data.size()-1) position=0;
                String ACTION = "com.yaojinpet.say";
                intent=new Intent(ACTION);
                intent.putExtra("operation","EnglishListener");
                intent.putExtra("source","EnglishListener");
                intent.putExtra("words",getEnglish(arr_data.get(position)));
                sendBroadcast(intent);
                list.setSelection(position);
            }
            else if(intent.getStringExtra("vol")!=null)
            {
                Toast.makeText(context,"当前音速为:"+intent.getStringExtra("vol")+"%",Toast.LENGTH_SHORT).show();
            }
        }

    };
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ArrayAdapter<String> arr_adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arr_data);

            //视图(ListView)加载适配器
            list.setAdapter(arr_adapter);
            arr_adapter.notifyDataSetChanged();
        }
    };
    String  getEnglish(String sentence)
    {
        int a=0,b=0;
        for(int i=0;i<sentence.length();i++)
        {
            if(a==0)
                if(sentence.charAt(i)>='a'&&sentence.charAt(i)<='z'
                        ||sentence.charAt(i)>='A'&&sentence.charAt(i)<='Z')
                {
                    a=i;
                }
            if(a!=0)
                if(!(sentence.charAt(i)=='-'||sentence.charAt(i)=='\t'||sentence.charAt(i)=='?'
                        ||sentence.charAt(i)=='!'||sentence.charAt(i)=='\''
                        ||sentence.charAt(i)==','||sentence.charAt(i)=='？'||
                        sentence.charAt(i)=='\\'||sentence.charAt(i)=='/'||sentence.charAt(i)=='.'||
                        sentence.charAt(i)=='（'||sentence.charAt(i)==')'||
                        sentence.charAt(i)==' '||sentence.charAt(i)>='a'&&sentence.charAt(i)<='z'
                        ||sentence.charAt(i)>='A'&&sentence.charAt(i)<='Z'
                        ||sentence.charAt(i)>='1'&&sentence.charAt(i)<='9'))
                {
                    b=i;
                    break;
                }
        }
        return  sentence.substring(a,b );
    };
}
