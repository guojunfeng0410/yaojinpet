package com.example.myapplication.bookShelf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.tdscientist.shelfview.BookModel;
import com.tdscientist.shelfview.ShelfView;

import java.io.File;
//import java.text.DecimalFormat;
import java.util.ArrayList;
//import java.util.HashMap;

import yalantis.com.sidemenu.interfaces.ScreenShotable;

public class BookShelfFragment extends Fragment implements ScreenShotable,ShelfView.BookClickListener {
    private Context context;
    private ArrayList<BookModel> models=new ArrayList<>();
    public BookShelfFragment(){}
    public void setApplicationContext(Context context)
    {
        this.context=context;
        getFileName();
        /*if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();// 获得SD卡路径
            // File path = new File("/mnt/sdcard/");
            final File[] files = path.listFiles();// 读取
            getFileName(files);
        }*/
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookself, container, false);
        ShelfView shelfView = (ShelfView) view.findViewById(R.id.shelfView);
        shelfView.setOnBookClicked(this);
        models.add(new BookModel("http://eurodroid.com/pics/beginning_android_book.jpg", "1", "Beginning Android"));
        shelfView.loadData(models, ShelfView.BOOK_SOURCE_URL);
        return  view;
    }

    private void getFileName() {
        File sd= Environment.getExternalStorageDirectory();
        String path=sd.getAbsolutePath()+"/yaojinli/book";
        File file=new File(path);
        for(File files:file.listFiles())
        if(files.getName().lastIndexOf(".")>0)
        models.add(new BookModel("http://eurodroid.com/pics/beginning_android_book.jpg",
                files.getAbsolutePath(), files.getName().substring(0, files.getName().lastIndexOf("."))));
        else
            models.add(new BookModel("http://eurodroid.com/pics/beginning_android_book.jpg",
                    files.getAbsolutePath(), files.getName()));
    }
    /*private void getFileName(File[] files) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (files != null) {// 先判断目录是否为空，否则会报空指针
            for (File file : files) {
                if (file.isDirectory()) {
                    getFileName(file.listFiles());
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(".txt")&&Double.valueOf(df.format((double) file.length() / 1048576))>1) {
                        models.add(new BookModel("http://eurodroid.com/pics/beginning_android_book.jpg",
                                file.getAbsolutePath(), fileName.substring(0, fileName.lastIndexOf("."))));
                    }
                }
            }
        }
    }*/
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.bookShelf);

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
                BookShelfFragment.this.bitmap = bitmap;
            }
        };
        thread.start();

    }
    @Override
    public void onBookClicked(int position, String bookId, String bookTitle) {
        // handle click events here
        Toast.makeText(context, bookTitle, Toast.LENGTH_SHORT).show();
        //Intent intent=new Intent(context,BookReadView.class);
        Intent intent=new Intent(context,EnglishListener.class);
        intent.putExtra("bookId",bookId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }
    private Bitmap bitmap;
    private View containerView;
    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

}
