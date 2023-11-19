package com.example.petdiary.util;

import android.view.View;
import android.view.animation.AlphaAnimation;

public class AnimationUtil {

    public static void fade_out(View view){
        //AnimationSet set = new AnimationSet(true);
        AlphaAnimation a = new AlphaAnimation(0.0f,1.0f);
        a.setFillAfter(true); // 에니메이션이 끝난 뒤 상태를 유지하는 설정, 설정하지 않으면 duration 이후 원래 상태로 되돌아감
        a.setDuration(1000);
        view.startAnimation(a);
    }

    public static void fade_int(View view){
        //AnimationSet set = new AnimationSet(true);
        AlphaAnimation a = new AlphaAnimation(1.0f,0.0f);
        a.setFillAfter(true);
        a.setDuration(1000);
        view.startAnimation(a);

    }
}

