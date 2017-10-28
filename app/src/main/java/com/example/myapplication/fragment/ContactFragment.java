package com.example.myapplication.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leancloud.chatkit.LCChatKit;
import cn.leancloud.chatkit.activity.LCIMConversationActivity;
import cn.leancloud.chatkit.utils.LCIMConstants;

import static com.example.myapplication.Yaojinpet.user_id;

public class ContactFragment extends Fragment {

    private List<Map<String, Object>> list;
    private View view;
    private Context applicationContext;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contact_information_list, container, false);
        initial(applicationContext);
        return view;
    }

    private void initial(final Context context)
    {

        SimpleAdapter simpleAdapter=new SimpleAdapter(context,getData(),R.layout.contact_information,
                new String[]{"name"},new int[]{R.id.contact_information});
        ListView listView = (ListView) view.findViewById(R.id.contact_information_list);
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                LCChatKit.getInstance().open(user_id, new AVIMClientCallback() {
                    @Override
                    public void done(AVIMClient avimClient, AVIMException e) {
                        if (null == e) {
                            Intent intent = new Intent(context, LCIMConversationActivity.class);
                            intent.putExtra(LCIMConstants.PEER_ID,list.get(position).get("phoneNum").toString());
                            startActivity(intent);
                        } else {
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    public void setApplicationContext(Context context)
    {
        applicationContext=context;
    }

    private List<Map<String, Object>> getData() {
        Cursor cursor;
        cursor = getContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null,
                null);

        list = new ArrayList<>();

        Map<String, Object> map_ = new HashMap<>();
        map_.put("name","测试");
        map_.put("phoneNum","111");
        list.add(map_);
        if(cursor!=null&&cursor.moveToFirst())
            while (!cursor.isAfterLast()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                map.put("phoneNum", cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                list.add(map);
                cursor.moveToNext();
            }
        cursor.close();


        return list;
    }
}
