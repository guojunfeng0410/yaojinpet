package com.example.myapplication.musicPlayer;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

public class RotateDownPageTransformer implements ViewPager.PageTransformer
{

    private static final float ROT_MAX = 20.0f;
    private float mRot;



    public void transformPage(View view, float position)
    {

        Log.e("TAG", view + " , " + position + "");

        if (position < -1)
        { // [-Infinity,-1)
            // This page is way off-screen to the left.
            ViewHelper.setRotation(view, 0);

        } else if (position <= 1) // a页滑动至b页 ； a页从 0.0 ~ -1 ；b页从1 ~ 0.0
        { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            if (position <= 0) {
                //从右向左滑动为当前View

                //设置旋转中心点；
                ViewHelper.setPivotX(view, view.getMeasuredWidth());
                ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5f);

                //只在Y轴做旋转操作
                ViewHelper.setRotationY(view, 90f * position);
            } else  {
                //从左向右滑动为当前View
                ViewHelper.setPivotX(view, 0);
                ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5f);
                ViewHelper.setRotationY(view, 90f * position);
            }

        // Scale the page down (between MIN_SCALE and 1)

            // Fade the page relative to its size.

        } else
        { // (1,+Infinity]
            // This page is way off-screen to the right.
            ViewHelper.setRotation(view, 0);
        }
    }
}