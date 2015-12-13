package com.genericdrawerLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Material���Ĳ˵���ť
 */
public class MaterialMenuButton extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    private ValueAnimator animator;

    public enum Status {
        CLOSED, OPENING, OPENED, CLOSING
    }
    private static final int HEIGHT_LINE_DIP = 2;
    private static final int MIN_WIDTH = 50;
    private static final int MIN_HEIGHT = 50;

    private int mLineHeight;
    private int mLineColor = Color.WHITE;
    private int mMinWidth, mMinHeight;
    private int mWidth, mHeight;
    private View mFirstLineView, mSecondLineView, mThirdLineView;
    boolean mIsReverse;
    private boolean isInitialization;
    private KeyFrameSet mFirstLineXValues, mFirstLineYValues, mThirdLineXValues, mThirdLineYValues,
            mSecondLineRotation, mFirstLineRotation, mThirdLineRotation, mFirstOrThirdLineWidth;

    public MaterialMenuButton(Context context) {
        this(context, null);
    }

    public MaterialMenuButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialMenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mFirstLineView = new View(context);
        mSecondLineView = new View(context);
        mThirdLineView = new View(context);
        mFirstLineView.setBackgroundColor(mLineColor);
        mSecondLineView.setBackgroundColor(mLineColor);
        mThirdLineView.setBackgroundColor(mLineColor);
        addView(mFirstLineView);
        addView(mSecondLineView);
        addView(mThirdLineView);
        mLineHeight = dip2px(context, HEIGHT_LINE_DIP);

        mMinWidth = dip2px(context, MIN_WIDTH);
        mMinHeight = dip2px(context, MIN_HEIGHT);
        setMinimumWidth(mMinWidth);
        setMinimumHeight(mMinHeight);

        getViewTreeObserver().addOnGlobalLayoutListener(this);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animator == null) {
                    animator = ObjectAnimator.ofFloat(0, 100);
                    animator.setDuration(800);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            update(animation.getAnimatedFraction());
                        }
                    });
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsReverse = !mIsReverse;
                        }
                    });
                }
                if (animator.isStarted()) {
                    return;
                }
                animator.start();
            }
        });
    }

    public void setLineColor(int color) {
        this.mLineColor = color;
    }

    public void update(float fraction) {
        updateFirstLineRotation(fraction);
        updateFirstLineY(fraction);
        updateFirstOrThirdLineWidth(fraction);
        updateFistLineX(fraction);
        updateSecondLinRotation(fraction);
        updateThirdLineRotation(fraction);
        updateThirdLineX(fraction);
        updateThirdLineY(fraction);
    }

    public void updateFistLineX(float fraction) {
        ViewHelper.setX(mFirstLineView, mFirstLineXValues.getCurrentVlaue(fraction));
    }

    private void updateThirdLineX(float fraction) {
        ViewHelper.setX(mThirdLineView, mThirdLineXValues.getCurrentVlaue(fraction));
    }

    private void updateFirstLineY(float fraction) {
        ViewHelper.setY(mFirstLineView, mFirstLineYValues.getCurrentVlaue(fraction));
    }

    private void updateThirdLineY(float fraction) {
        ViewHelper.setY(mThirdLineView, mThirdLineYValues.getCurrentVlaue(fraction));
    }

    private void updateFirstLineRotation(float fraction) {
        ViewHelper.setRotation(mFirstLineView, mFirstLineRotation.getCurrentVlaue(fraction));
    }

    private void updateSecondLinRotation(float fraction) {
        ViewHelper.setRotation(mSecondLineView, mSecondLineRotation.getCurrentVlaue(fraction));
    }

    private void updateThirdLineRotation(float fraction) {
        ViewHelper.setRotation(mThirdLineView, mThirdLineRotation.getCurrentVlaue(fraction));
    }

    private void updateFirstOrThirdLineWidth(float fraction) {
        int value = (int) mFirstOrThirdLineWidth.getCurrentVlaue(fraction);
        ViewGroup.LayoutParams lp = mFirstLineView.getLayoutParams();
        lp.width = value;
        mFirstLineView.requestLayout();

        lp = mThirdLineView.getLayoutParams();
        lp.width = value;
        mThirdLineView.requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = resolveSize(mMinWidth, widthMeasureSpec);
        int measureHeight = resolveSize(mMinHeight, heightMeasureSpec);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    public void onGlobalLayout() {
        int width = getWidth();
        int height = getHeight();
        if (!isInitialization && width != 0 && height != 0) {
            if (mWidth != width || mHeight != height) {
                int mLineMaxWidth = (int) (width * 1.0f / 2.0f);
                // mLineMinWidth = (int) (mLineMaxWidth * 2.0f / 3.0f);
                float mCenterX = width / 2.0f;
                float mCenterY = height / 2.0f;
                float mLineLeft = mCenterX - mLineMaxWidth / 2.0f;
                float mFirstLineStartY = mCenterY - Math.min(width, height) / 8.0f;
                float mThirdLineStartY = mCenterY + Math.min(width, height) / 8.0f;
                mWidth = width;
                mHeight = height;

                ViewHelper.setX(mFirstLineView, mLineLeft);
                ViewHelper.setY(mFirstLineView, mFirstLineStartY);
                ViewHelper.setX(mSecondLineView, mLineLeft);
                ViewHelper.setY(mSecondLineView, mCenterY);
                ViewHelper.setX(mThirdLineView, mLineLeft);
                ViewHelper.setY(mThirdLineView, mThirdLineStartY);

                ViewGroup.LayoutParams lp;
                lp = mFirstLineView.getLayoutParams();
                lp.width = mLineMaxWidth;
                lp.height = mLineHeight;
                mFirstLineView.setLayoutParams(lp);
                lp = mSecondLineView.getLayoutParams();
                lp.width = mLineMaxWidth;
                lp.height = mLineHeight;
                mSecondLineView.setLayoutParams(lp);
                lp = mThirdLineView.getLayoutParams();
                lp.width = mLineMaxWidth;
                lp.height = mLineHeight;
                mThirdLineView.setLayoutParams(lp);

                mFirstLineRotation = new KeyFrameSet(0, 225);
                mFirstLineXValues = new KeyFrameSet(mLineLeft, mLineLeft - mLineMaxWidth * 0.1f);
                mFirstLineYValues = new KeyFrameSet(mFirstLineStartY, mThirdLineStartY - 2);
                mThirdLineXValues = new KeyFrameSet(mLineLeft, mLineLeft - mLineMaxWidth * 0.1f);
                mThirdLineYValues = new KeyFrameSet(mThirdLineStartY, mFirstLineStartY + 2);
                mThirdLineRotation = new KeyFrameSet(0, 135);
                mSecondLineRotation = new KeyFrameSet(0, 180);
                mFirstOrThirdLineWidth = new KeyFrameSet(mLineMaxWidth, (int) (mLineMaxWidth * 0.7f));
            }
            isInitialization = true;
        }
    }

    private class KeyFrameSet {
        float start;
        float end;

        public KeyFrameSet(float start, float end) {
            this.start = start;
            this.end = end;
        }

        public float getStart() {
            return mIsReverse ? end : start;
        }

        public float getEnd() {
            return mIsReverse ? start : end;
        }

        public float getCurrentVlaue(float fraction) {
            return mIsReverse ? end + fraction * (start - end) : start + fraction * (end - start);
        }
    }
}
