package com.example.myapplication.musicPlayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.List;
import java.util.Map;


public class StackViewAdapter extends BaseAdapter{
    //private List<Integer> mImages;
    private List<Map<String,String>> songs;
    private LayoutInflater mInflater;
    public StackViewAdapter(List<Map<String,String>> songs, Context context){
        //this.mImages = mImages;
        this.songs=songs;
        this.mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.music_information_card, null);
            //holder.img = (ImageView)convertView.findViewById(R.id.img)
            holder.musicName = (TextView)convertView.findViewById(R.id.musicName);
            holder.singer = (TextView)convertView.findViewById(R.id.singer);
            //holder.img.setImageResource(R.drawable.ic_launcher);
            holder.singer.setText("许嵩");
            holder.musicName.setText(songs.get(position).get("name"));
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
            //holder.img.setImageResource(R.drawable.ic_launcher);
            holder.singer.setText("许嵩");
            holder.musicName.setText(songs.get(position).get("name"));
        }

        return convertView;
    }
    private static class ViewHolder
    {
        //ImageView img;
        TextView musicName;
        TextView singer;
    }
}
