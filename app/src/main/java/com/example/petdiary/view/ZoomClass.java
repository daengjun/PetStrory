package com.example.petdiary.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

// 확대 축소 이동 이미지 뷰
public class ZoomClass extends AppCompatImageView implements View.OnTouchListener,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    // shared constructing
    private Context mContext;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private Matrix mMatrix;
    private float[] mMatrixValues;
    private int mode = NONE;

    // Scales
    private float mSaveScale = 1f;
    private float mMinScale = 1f;
    private float mMaxScale = 4f;

    // view dimensions
    private float origWidth = 0f;
    private float origHeight = 0f;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private PointF mLast = new PointF();
    private PointF mStart = new PointF();

    public ZoomClass(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public ZoomClass(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    public ZoomClass(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        mContext = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();
        mMatrixValues = new float[9];
        setImageMatrix(mMatrix);
        setScaleType(ScaleType.MATRIX);
        mGestureDetector = new GestureDetector(context, this);
        setOnTouchListener(this);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float prevScale = mSaveScale;
            mSaveScale *= mScaleFactor;
            if (mSaveScale > mMaxScale) {
                mSaveScale = mMaxScale;
                mScaleFactor = mMaxScale / prevScale;
            } else if (mSaveScale < mMinScale) {
                mSaveScale = mMinScale;
                mScaleFactor = mMinScale / prevScale;
            }
            if (origWidth * mSaveScale <= viewWidth
                    || origHeight * mSaveScale <= viewHeight) {
                mMatrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2f,
                        viewHeight / 2f);
            } else {
                mMatrix.postScale(mScaleFactor, mScaleFactor,
                        detector.getFocusX(), detector.getFocusY());
            }
            fixTranslation();
            return true;
        }
    }

    private void fitToScreen() {
        mSaveScale = 1f;
        float scale;
        if (getDrawable() == null || getDrawable().getIntrinsicWidth() == 0 ||
                getDrawable().getIntrinsicHeight() == 0) {
            return;
        }
        int imageWidth = getDrawable().getIntrinsicWidth();
        int imageHeight = getDrawable().getIntrinsicHeight();
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;
        scale = Math.min(scaleX, scaleY);
        mMatrix.setScale(scale, scale);

        // Center the image
        float redundantYSpace = (float) viewHeight - scale * imageHeight;
        float redundantXSpace = (float) viewWidth - scale * imageWidth;
        redundantYSpace /= 2f;
        redundantXSpace /= 2f;
        mMatrix.postTranslate(redundantXSpace, redundantYSpace);
        origWidth = viewWidth - 2f * redundantXSpace;
        origHeight = viewHeight - 2f * redundantYSpace;
        setImageMatrix(mMatrix);
    }

    private void fixTranslation() {
        mMatrix.getValues(mMatrixValues);
        float transX = mMatrixValues[Matrix.MTRANS_X];
        float transY = mMatrixValues[Matrix.MTRANS_Y];
        float fixTransX = getFixTranslation(transX, (float) viewWidth, origWidth * mSaveScale);
        float fixTransY = getFixTranslation(transY, (float) viewHeight, origHeight * mSaveScale);
        if (fixTransX != 0f || fixTransY != 0f) {
            mMatrix.postTranslate(fixTransX, fixTransY);
        }
    }

    private float getFixTranslation(float trans, float viewSize, float contentSize) {
        float minTrans;
        float maxTrans;
        if (contentSize <= viewSize) {
            minTrans = 0f;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0f;
        }
        if (trans < minTrans) {
            return -trans + minTrans;
        }
        if (trans > maxTrans) {
            return -trans + maxTrans;
        }
        return 0f;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        return contentSize <= viewSize ? 0f : delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mSaveScale == 1f) {
            fitToScreen();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        PointF currentPoint = new PointF(event.getX(), event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLast.set(currentPoint);
                mStart.set(mLast);
                mode = DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    float dx = currentPoint.x - mLast.x;
                    float dy = currentPoint.y - mLast.y;
                    float fixTransX = getFixDragTrans(dx, (float) viewWidth, origWidth * mSaveScale);
                    float fixTransY = getFixDragTrans(dy, (float) viewHeight, origHeight * mSaveScale);
                    mMatrix.postTranslate(fixTransX, fixTransY);
                    fixTranslation();
                    mLast.set(currentPoint.x, currentPoint.y);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }
        setImageMatrix(mMatrix);
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        fitToScreen();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    // Image States
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
}