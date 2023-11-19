package com.example.petdiary.activity;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.petdiary.R;
import com.example.petdiary.adapter.ViewPageAdapterDetail;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;


/**
 * 이미지 크게 보여 주는 화면
 **/

// todo 화면 확대 축소는 되지만 원하는 영역으로 확대가 안됨
// todo 추후에 추가 할 것

public class Main_Expand_ImageView extends AppCompatActivity {

    private String imageUrl1;
    private ImageView imageView;

    private ScaleGestureDetector scaleGestureDetector;

    private float scaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_expandimage);
        overridePendingTransition(R.anim.fade_in, R.anim.none);

        Intent intent = getIntent();
        imageUrl1 = intent.getStringExtra("imageUrl1");

        imageView = findViewById(R.id.detail_imageView);
        Glide.with(this).load(imageUrl1).into(imageView);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

            imageView.setScaleX(scaleFactor);
            imageView.setScaleY(scaleFactor);

            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.none, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}