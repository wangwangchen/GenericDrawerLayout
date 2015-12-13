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
 * Material风格的Menu按钮
 */
public class MaterialMenuButton extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    private ValueAnimator animator;

    /** 当前按钮的状态 */
    public enum Status {
        CLOSED, OPENING, OPENED, CLOSING
    }

    /** 默认线高度，单位DIP */
    private static final int HEIGHT_LINE_DIP = 2;
    /** 按钮的最小的宽度 */
    private static final int MIN_WIDTH = 50;
    /** 按钮的最小的高度 */
    private static final int MIN_HEIGHT = 50;

    /** 当前现线高 */
    private int mLineHeight;
    /** 线的颜色 */
    private int mLineColor = Color.WHITE;
    private int mMinWidth, mMinHeight;
    private int mWidth, mHeight;
    private View mFirstLineView, mSecondLineView, mThirdLineView;
    /** 是否已经展开按钮 */
    boolean mIsOpened;
    /** 是否已经初始化宽高等信息 */
    private boolean isInitialization;
    private KeyFrameSet mFirstLineXValues, mFirstLineYValues, mThirdLineXValues, mThirdLineYValues,
            mSecondLineRotation, mFirstLineRotation, mThirdLineRotation, mFirstOrThirdLineWidth;
    /** 外部设置的点击事件 */
    private OnClickListener mOnClickListener;
    /** 是否在点击按钮的时候自动播放动画 */
    private boolean mIsAutoAnimating;

    public MaterialMenuButton(Context context) {
        this(context, null);
    }

    public MaterialMenuButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialMenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化三条线
        mFirstLineView = new View(context);
        mSecondLineView = new View(context);
        mThirdLineView = new View(context);
        mFirstLineView.setBackgroundColor(mLineColor);
        mSecondLineView.setBackgroundColor(mLineColor);
        mThirdLineView.setBackgroundColor(mLineColor);
        addView(mFirstLineView);
        addView(mSecondLineView);
        addView(mThirdLineView);
        // 初始化默认线高
        mLineHeight = dip2px(context, HEIGHT_LINE_DIP);
        // 初始化默认最小宽高
        mMinWidth = dip2px(context, MIN_WIDTH);
        mMinHeight = dip2px(context, MIN_HEIGHT);
        setMinimumWidth(mMinWidth);
        setMinimumHeight(mMinHeight);

        // 添加视图绘制监听器
        getViewTreeObserver().addOnGlobalLayoutListener(this);

        // 添加点击事件
        super.setOnClickListener(new MyOnClickListener());
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        // super.setOnClickListener(l);
        this.mOnClickListener = l;
    }

    /**
     * 设置点击时是否自动播放按钮动画
     */
    public void setAutoAnimating(boolean autoAnimating) {
        this.mIsAutoAnimating = autoAnimating;
    }

    private class MyOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            playAnimation();

            // 调用外部设置的点击事件
            if (mOnClickListener != null) {
                mOnClickListener.onClick(MaterialMenuButton.this);
            }

        }
    }

    /**
     * 播放动画
     */
    private void playAnimation() {
        if (!mIsAutoAnimating) {
            return;
        }
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(0, 100);
            animator.setDuration(800);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // 更新视图
                    update(animation.getAnimatedFraction());
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // 当动画结束的时候，切换当前按钮的打开状态标识
                    mIsOpened = !mIsOpened;
                }
            });
        }
        // 先判断动画是否正在执行
        if (animator.isStarted()) {
            return;
        }
        animator.start();
    }

    /**
     * 设置按钮的线颜色值
     */
    public void setLineColor(int color) {
        this.mLineColor = color;
    }

    /**
     * 根据当前动画执行的百分比，更新视图
     * @param fraction
     */
    public void update(float fraction) {
        if (fraction < 0 || fraction > 1) {
            return;
        }
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
        ViewHelper.setX(mFirstLineView, mFirstLineXValues.getCurrentValue(fraction));
    }

    private void updateThirdLineX(float fraction) {
        ViewHelper.setX(mThirdLineView, mThirdLineXValues.getCurrentValue(fraction));
    }

    private void updateFirstLineY(float fraction) {
        ViewHelper.setY(mFirstLineView, mFirstLineYValues.getCurrentValue(fraction));
    }

    private void updateThirdLineY(float fraction) {
        ViewHelper.setY(mThirdLineView, mThirdLineYValues.getCurrentValue(fraction));
    }

    private void updateFirstLineRotation(float fraction) {
        ViewHelper.setRotation(mFirstLineView, mFirstLineRotation.getCurrentValue(fraction));
    }

    private void updateSecondLinRotation(float fraction) {
        ViewHelper.setRotation(mSecondLineView, mSecondLineRotation.getCurrentValue(fraction));
    }

    private void updateThirdLineRotation(float fraction) {
        ViewHelper.setRotation(mThirdLineView, mThirdLineRotation.getCurrentValue(fraction));
    }

    private void updateFirstOrThirdLineWidth(float fraction) {
        int value = (int) mFirstOrThirdLineWidth.getCurrentValue(fraction);
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
            // 当未初始化信息，并且当前按钮已经绘制完成，能够拿到宽高值
            if (mWidth != width || mHeight != height) {
                int mLineMaxWidth = (int) (width * 1.0f / 2.0f);
                // mLineMinWidth = (int) (mLineMaxWidth * 2.0f / 3.0f);
                float mCenterX = width / 2.0f;
                float mCenterY = height / 2.0f;
                // 三条线的左侧位置
                float mLineLeft = mCenterX - mLineMaxWidth / 2.0f;
                // 第一线线的Y轴位置
                float mFirstLineStartY = mCenterY - Math.min(width, height) / 8.0f;
                // 第三条线的Y轴位置
                float mThirdLineStartY = mCenterY + Math.min(width, height) / 8.0f;
                // 当前按钮的宽高
                mWidth = width;
                mHeight = height;

                // 设置三条线的左上角X，Y轴值
                ViewHelper.setX(mFirstLineView, mLineLeft);
                ViewHelper.setY(mFirstLineView, mFirstLineStartY);
                ViewHelper.setX(mSecondLineView, mLineLeft);
                ViewHelper.setY(mSecondLineView, mCenterY);
                ViewHelper.setX(mThirdLineView, mLineLeft);
                ViewHelper.setY(mThirdLineView, mThirdLineStartY);

                // 设置三条直线的宽高值
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

                // 记录三条线的各种操作起始值和结束值
                mFirstLineRotation = new KeyFrameSet(0, 225);
                mFirstLineXValues = new KeyFrameSet(mLineLeft, mLineLeft - mLineMaxWidth * 0.1f);
                mFirstLineYValues = new KeyFrameSet(mFirstLineStartY, mThirdLineStartY - 2);
                mThirdLineXValues = new KeyFrameSet(mLineLeft, mLineLeft - mLineMaxWidth * 0.1f);
                mThirdLineYValues = new KeyFrameSet(mThirdLineStartY, mFirstLineStartY + 2);
                mThirdLineRotation = new KeyFrameSet(0, 135);
                mSecondLineRotation = new KeyFrameSet(0, 180);
                mFirstOrThirdLineWidth = new KeyFrameSet(mLineMaxWidth, (int) (mLineMaxWidth * 0.7f));
            }
            // 标记已经初始化了
            isInitialization = true;
        }
    }

    /**
     * 记录每一个操作（旋转，位移等）的起始值和终止值，并提供一系列参数获取函数
     */
    private class KeyFrameSet {
        float start;
        float end;

        public KeyFrameSet(float start, float end) {
            this.start = start;
            this.end = end;
        }

        public float getStart() {
            return mIsOpened ? end : start;
        }

        public float getEnd() {
            return mIsOpened ? start : end;
        }

        public float getCurrentValue(float fraction) {
            return mIsOpened ? end + fraction * (start - end) : start + fraction * (end - start);
        }
    }
}
