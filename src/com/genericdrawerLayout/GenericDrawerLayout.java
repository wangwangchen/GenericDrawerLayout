package com.genericdrawerLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 实现上下左右四个方向的抽屉效果
 */
public class GenericDrawerLayout extends FrameLayout {

	private static final String TAG = GenericDrawerLayout.class.getSimpleName();

	private enum AnimStatus {
		OPENING, CLOSING, CLOSED, OPENED
	}

	/** 打开或者关闭抽屉时的DURATION */
	private static final int DURATION_OPEN_CLOSE = 300;
	/** 默认的响应触摸事件的宽度值，单位DIP */
	private static final int TOUCH_VIEW_SIZE_DIP = 25;
	/** 用来判断的最小消费事件触发距离，单位DIP */
	private static final int MIN_CONSUME_SIZE_DIP = 15;
	/** 响应打开或者关闭的临界值与内容区域的宽度比例 */
	private static final float SCALE_AUTO_OPEN_CLOSE = 0.3f;
	/** 响应打开或者关闭的速率 */
	private static final int VEL = 800;

	private VelocityTracker mVelocityTracker;

	private Context mContext;

	/**
	 * 用来响应触摸事件的透明控件
	 */
	private TouchView mTouchView;
	/** 关闭状态下，响应触摸事件的控件宽度 */
	private int mClosedTouchViewSize;
	/** 打开状态下，响应Touch事件的宽度 */
	private int mOpenedTouchViewSize;
	/** 当前抽屉的Gravity */
	private int mTouchViewGravity = Gravity.RIGHT;
	/** 用来防止内容部分视图的容器 */
	private ContentLayout mContentLayout;
	/** 事件回调 */
	private DrawerCallback mDrawerCallback;
	/** 当前正在播放的动画 */
	private ValueAnimator mAnimator;
	/** 是否正在播放动画 */
	private AtomicBoolean mAnimating = new AtomicBoolean(false);
	/** 当前的动画状态 */
	private AnimStatus mAnimStatus = AnimStatus.CLOSING;
	/** 当前触摸的位置相对屏幕左上角的X,Y轴值 */
	private float mCurTouchX, mCurTouchY;
	/** 用来判断是否消费Touch事件的最小滑动距离 */
	private float mMinDisallowDispatch;
	/** 是否被子View消费Touch事件 */
	private boolean isChildConsumeTouchEvent = false;
	/** 是否消费Touch事件 */
	private boolean isConsumeTouchEvent = false;
	/** 是否在滑动时改变背景透明度 */
	private boolean mIsOpaqueWhenTranslating = false;
	/** 是否可以打开抽屉 */
	private boolean mIsOpenable = true;
	/** 绘制背景透明度的View控件 */
	private DrawView mDrawView;

	public GenericDrawerLayout(Context context) {
		this(context, null);
	}

	public GenericDrawerLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GenericDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		initView();
	}

	private void initView() {
		// 初始化背景色变化控件
		mDrawView = new DrawView(mContext);
		addView(mDrawView, generateDefaultLayoutParams());
		// 初始化用来相应触摸的透明View
		mTouchView = new TouchView(mContext);
		mClosedTouchViewSize = dip2px(mContext, TOUCH_VIEW_SIZE_DIP);
		mOpenedTouchViewSize = mClosedTouchViewSize;
		// 初始化用来存放布局的容器
		mContentLayout = new ContentLayout(mContext);
		mContentLayout.setVisibility(View.INVISIBLE);
		// 添加视图
		addView(mTouchView, generateTouchViewLayoutParams());
		addView(mContentLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mMinDisallowDispatch = dip2px(mContext, MIN_CONSUME_SIZE_DIP);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	private class DrawView extends View {

		Paint paint = new Paint();

		public DrawView(Context context) {
			super(context);
			paint.setColor(Color.BLACK);
			paint.setAlpha(0);
		}

		/**
		 * 设置透明度（0-1）
		 */
		public void setAlpha(float alpha) {
			paint.setAlpha((int) (alpha * 255));
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawPaint(paint);
		}

	}

	private class ContentLayout extends FrameLayout {

		private float mDownX, mDownY;
		private boolean isTouchDown;

		public ContentLayout(Context context) {
			super(context);
		}

		@Override
		public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
			super.requestDisallowInterceptTouchEvent(disallowIntercept);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			if (getVisibility() != View.VISIBLE) {
				// 内容区域不可见
				return super.dispatchTouchEvent(event);
			}

			// TOUCH_DOWN的时候未消化事件
			if (MotionEvent.ACTION_DOWN != event.getAction() && !isTouchDown) {
				isChildConsumeTouchEvent = true;
			}

			// 把事件拦截下来，按条件下发给子View；

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (mAnimating.get()) {
					mAnimating.set(false);
					// 停止播放动画
					mAnimator.end();
					isTouchDown = true;
				} else {
					isTouchDown = isDownInRespondArea(event);
				}
				if (isTouchDown) {
					mDownX = event.getRawX();
					mDownY = event.getRawY();
					performDispatchTouchEvent(event);
				} else {
					isChildConsumeTouchEvent = true;
				}
				super.dispatchTouchEvent(event);
				return true;
			case MotionEvent.ACTION_MOVE:
				if (!isConsumeTouchEvent && !isChildConsumeTouchEvent) {

					// 先下发给子View看看子View是否需要消费
                    boolean b = super.dispatchTouchEvent(event);

                    // 如果自己还没消化掉事件，看看子view是否需要消费事件
					boolean goToConsumeTouchEvent = false;
                    switch (mTouchViewGravity) {
                        case Gravity.LEFT:
                            if ((Math.abs(event.getRawY() - mDownY) >= mMinDisallowDispatch) && b) {
                                isChildConsumeTouchEvent = true;
                            } else if (event.getRawX() - mDownX < -mMinDisallowDispatch) {
                                isConsumeTouchEvent = true;
								goToConsumeTouchEvent = true;
                            }
                            break;
                        case Gravity.RIGHT:
                            if ((Math.abs(event.getRawY() - mDownY) >= mMinDisallowDispatch) && b) {
                                isChildConsumeTouchEvent = true;
                            } else if (event.getRawX() - mDownX > mMinDisallowDispatch) {
                                isConsumeTouchEvent = true;
								goToConsumeTouchEvent = true;
                            }
                            break;
                        case Gravity.BOTTOM:
                            if ((Math.abs(event.getRawX() - mDownX) >= mMinDisallowDispatch) && b) {
                                isChildConsumeTouchEvent = true;
                            } else if (event.getRawY() - mDownY > mMinDisallowDispatch) {
                                isConsumeTouchEvent = true;
								goToConsumeTouchEvent = true;
                            }
                            break;
                        case Gravity.TOP:
                            if ((Math.abs(event.getRawX() - mDownX) >= mMinDisallowDispatch) && b) {
                                isChildConsumeTouchEvent = true;
                            } else if (event.getRawY() - mDownY < -mMinDisallowDispatch) {
                                isConsumeTouchEvent = true;
								goToConsumeTouchEvent = true;
                            }
                            break;
                    }
					if (goToConsumeTouchEvent) {
						// 如果自己消费了事件，则下发TOUCH_CANCEL事件
						MotionEvent obtain = MotionEvent.obtain(event);
						obtain.setAction(MotionEvent.ACTION_CANCEL);
						super.dispatchTouchEvent(obtain);
					}
                }
                break;
			}

			if (isChildConsumeTouchEvent || !isConsumeTouchEvent) {
				// 自己未消费之前，先下发给子View
				super.dispatchTouchEvent(event);
			} else if (isConsumeTouchEvent && !isChildConsumeTouchEvent) {
				// 如果自己消费了，则不给子View
				performDispatchTouchEvent(event);
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if (!isConsumeTouchEvent && !isChildConsumeTouchEvent) {
					// 如果子View以及自己都没消化，则自己消化，防止点击一下，抽屉卡住
					performDispatchTouchEvent(event);
				}
				isConsumeTouchEvent = false;
				isChildConsumeTouchEvent = false;
				isTouchDown = false;
				break;
			}

			return true;
		}

	}

	/** 是否点击在响应区域 */
	private boolean isDownInRespondArea(MotionEvent event) {
		float curTranslation = getCurTranslation();
		float x = event.getRawX();
		float y = event.getRawY();
		switch (mTouchViewGravity) {
			case Gravity.LEFT:
				if (x > curTranslation - mOpenedTouchViewSize && x < curTranslation) {
					return true;
				}
				break;
			case Gravity.RIGHT:
				if (x > curTranslation && x < curTranslation + mOpenedTouchViewSize) {
					return true;
				}
				break;
			case Gravity.BOTTOM:
				if (y > curTranslation && y < curTranslation + mOpenedTouchViewSize) {
					return true;
				}
				break;
			case Gravity.TOP:
				if (y > curTranslation - mOpenedTouchViewSize && y < curTranslation) {
					return true;
				}
				break;
			default:
				break;
		}
		return false;
	}

	/** 设置关闭状态下，响应触摸事件的控件宽度 */
	public void setTouchSizeOfClosed(int width) {
		if (width == 0 || width < 0) {
			mClosedTouchViewSize = dip2px(mContext, TOUCH_VIEW_SIZE_DIP);
		} else {
			mClosedTouchViewSize = width;
		}
		ViewGroup.LayoutParams lp = mTouchView.getLayoutParams();
		if (lp != null) {
			if (isHorizontalGravity()) {
				lp.width = mClosedTouchViewSize;
				lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
			} else {
				lp.height = mClosedTouchViewSize;
				lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
			}
			mTouchView.requestLayout();
		}
	}

	/** 设置打开状态下，响应触摸事件的控件宽度 */
	public void setTouchSizeOfOpened(int width) {
		if (width <= 0) {
			mOpenedTouchViewSize = dip2px(mContext, TOUCH_VIEW_SIZE_DIP);
		} else {
			mOpenedTouchViewSize = width;
		}
	}

	/**
	 * 用来响应拖拽事件的透明View
	 */
	private class TouchView extends View {
		public TouchView(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			if (!mIsOpenable) {
				return super.dispatchTouchEvent(event);
			}
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (getVisibility() == View.INVISIBLE) {
					return super.dispatchTouchEvent(event);
				}
				mContentLayout.setVisibility(View.VISIBLE);
				adjustContentLayout();
				if (mDrawerCallback != null) {
					mDrawerCallback.onPreOpen();
				}
				setVisibility(View.INVISIBLE);
				break;
			}
			performDispatchTouchEvent(event);
			return true;
		}
	}

	private void performDispatchTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		MotionEvent trackerEvent = MotionEvent.obtain(event);
		trackerEvent.setLocation(event.getRawX(), event.getRawY());
		mVelocityTracker.addMovement(trackerEvent);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mCurTouchX = event.getRawX();
			mCurTouchY = event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			float moveX = event.getRawX() - mCurTouchX;
			float moveY = event.getRawY() - mCurTouchY;
			translateContentLayout(moveX, moveY);
			mCurTouchX = event.getRawX();
			mCurTouchY = event.getRawY();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			handleTouchUp();
			break;
		}
	}

	private void translationCallback(float sliding) {
		if (mDrawerCallback != null) {
			mDrawerCallback.onTranslating(mTouchViewGravity, sliding);
		}
		if (mIsOpaqueWhenTranslating) {
			mDrawView.setAlpha(sliding / mContentLayout.getWidth());
		}
	}

	public void setOpaqueWhenTranslating(boolean isOpaque) {
		this.mIsOpaqueWhenTranslating = isOpaque;
	}

	public void setContentLayout(View view) {
		mContentLayout.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}

	private void handleTouchUp() {
		final VelocityTracker velocityTracker = mVelocityTracker;
		velocityTracker.computeCurrentVelocity(1000);
		int velocityX = (int) velocityTracker.getXVelocity();
		int velocityY = (int) velocityTracker.getYVelocity();

		Log.e(TAG, "velocityX = " + velocityX + "  velocityY = " + velocityY);

		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}

		switch (mTouchViewGravity) {
		case Gravity.LEFT:
			// 往右是打开，速度大于0时打开
			if (velocityX > VEL || (getCurTranslation() > -mContentLayout.getWidth() * SCALE_AUTO_OPEN_CLOSE) && velocityX > -VEL) {
				autoOpenDrawer();
			} else {
				autoCloseDrawer();
			}
			break;
		case Gravity.RIGHT:
			if (velocityX < -VEL || (getCurTranslation() < mContentLayout.getWidth() * (1 - SCALE_AUTO_OPEN_CLOSE) && velocityX < VEL)) {
				// 移动距离足够，或者速度够
				autoOpenDrawer();
			} else {
				autoCloseDrawer();
			}
			break;
		case Gravity.TOP:
			if (velocityY > VEL || (getCurTranslation() > -mContentLayout.getHeight() * SCALE_AUTO_OPEN_CLOSE) && velocityY > -VEL) {
				// 往下打开
				autoOpenDrawer();
			} else {
				autoCloseDrawer();
			}
			break;
		case Gravity.BOTTOM:
			if (velocityY < -VEL || (getCurTranslation() < mContentLayout.getHeight() * (1 - SCALE_AUTO_OPEN_CLOSE)) && velocityY < VEL) {
				// 移动距离足够，或者速度够
				autoOpenDrawer();
			} else {
				autoCloseDrawer();
			}
			break;
		}
	}

	/**
	 * 自动打开抽屉
	 */
	private void autoOpenDrawer() {
		mAnimating.set(true);
		mAnimator = ObjectAnimator.ofFloat(getCurTranslation(), getOpenTranslation());
		mAnimator.setDuration(DURATION_OPEN_CLOSE);
		mAnimator.addUpdateListener(new MyAnimatorUpdateListener());
		mAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				if (!AnimStatus.OPENING.equals(mAnimStatus) && !AnimStatus.OPENED.equals(mAnimStatus)) {
					if (mDrawerCallback != null) {
						mDrawerCallback.onStartOpen();
					}
				}
				mAnimStatus = AnimStatus.OPENING;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (!mAnimating.get()) {
					return;
				}
				if (mDrawerCallback != null) {
					mDrawerCallback.onEndOpen();
				}
				mAnimating.set(false);
				mAnimStatus = AnimStatus.OPENED;
			}
		});
		mAnimator.start();
	}

	/**
	 * 关闭抽屉
	 */
	public void close() {
		autoCloseDrawer();
	}

	/**
	 * 打开抽屉
	 */
	public void open() {
		autoOpenDrawer();
	}

	public boolean isOpened() {
		return mAnimStatus == AnimStatus.OPENED;
	}

	private class MyAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			if (!mAnimating.get()) {
				return;
			}
			Float animatedValue = (Float) animation.getAnimatedValue();
			if (isHorizontalGravity()) {
				ViewHelper.setTranslationX(mContentLayout, animatedValue);
				translationCallback(mContentLayout.getWidth() - Math.abs(animatedValue));
			} else if (isVerticalGravity()) {
				ViewHelper.setTranslationY(mContentLayout, animatedValue);
				translationCallback(mContentLayout.getHeight() - Math.abs(animatedValue));
			}
		}
	}

	/**
	 * 设置是否抽屉是否可以打开
	 */
	public void setOpennable(boolean openable) {
		this.mIsOpenable = openable;
	}

	/**
	 * 自动关闭抽屉
	 */
	private void autoCloseDrawer() {
		mAnimating.set(true);
		mAnimator = ObjectAnimator.ofFloat(getCurTranslation(), getCloseTranslation());
		mAnimator.setDuration(DURATION_OPEN_CLOSE);
		mAnimator.addUpdateListener(new MyAnimatorUpdateListener());
		mAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				if (!AnimStatus.CLOSING.equals(mAnimStatus) && !AnimStatus.CLOSED.equals(mAnimStatus)) {
					if (mDrawerCallback != null) {
						mDrawerCallback.onStartClose();
					}
				}
				mAnimStatus = AnimStatus.CLOSING;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (!mAnimating.get()) {
					return;
				}
				if (mDrawerCallback != null) {
					mDrawerCallback.onEndClose();
					mAnimStatus = AnimStatus.CLOSED;
				}
				mTouchView.setVisibility(View.VISIBLE);
				mAnimating.set(false);
			}
		});
		mAnimator.start();
	}

	private boolean isHorizontalGravity() {
		return mTouchViewGravity == Gravity.LEFT || mTouchViewGravity == Gravity.RIGHT;
	}

	private boolean isVerticalGravity() {
		return mTouchViewGravity == Gravity.TOP || mTouchViewGravity == Gravity.BOTTOM;
	}

	/**
	 * 移动视图
	 * @param moveX
	 * @param moveY
	 */
	private void translateContentLayout(float moveX, float moveY) {
		float move;
		switch (mTouchViewGravity) {
		case Gravity.LEFT:
		case Gravity.RIGHT:
			if (Math.abs(getCurTranslation() + moveX) > mContentLayout.getWidth() && getCurTranslation() + moveX > 0) {
				move = mContentLayout.getWidth();
			} else if (mTouchViewGravity == Gravity.LEFT && getCurTranslation() + moveX > 0 || mTouchViewGravity == Gravity.RIGHT
					&& getCurTranslation() + moveX < 0) {
				move = 0;
			} else {
				move = getCurTranslation() + moveX;
			}
			ViewHelper.setTranslationX(mContentLayout, move);
			translationCallback(mContentLayout.getWidth() - Math.abs(move));
			break;
		case Gravity.TOP:
		case Gravity.BOTTOM:
			if (Math.abs(getCurTranslation() + moveY) > mContentLayout.getHeight() && getCurTranslation() + moveY > 0) {
				move = mContentLayout.getHeight();
			} else if (mTouchViewGravity == Gravity.BOTTOM && getCurTranslation() + moveY < 0 || mTouchViewGravity == Gravity.TOP
					&& getCurTranslation() + moveY > 0) {
				move = 0;
			} else {
				move = getCurTranslation() + moveY;
			}
			ViewHelper.setTranslationY(mContentLayout, move);
			translationCallback(mContentLayout.getHeight() - Math.abs(move));
			break;
		}
	}

	/**
	 * 拖拽开始前，调整内容视图位置
	 */
	private void adjustContentLayout() {
		float mStartTranslationX = 0;
		float mStartTranslationY = 0;
		switch (mTouchViewGravity) {
		case Gravity.LEFT:
			mStartTranslationX = -mContentLayout.getWidth();
			mStartTranslationY = 0;
			break;
		case Gravity.RIGHT:
			mStartTranslationX = mContentLayout.getWidth();
			mStartTranslationY = 0;
			break;
		case Gravity.TOP:
			mStartTranslationX = 0;
			mStartTranslationY = -mContentLayout.getHeight();
			break;
		case Gravity.BOTTOM:
			mStartTranslationX = 0;
			mStartTranslationY = mContentLayout.getHeight();
			break;
		}
		ViewHelper.setTranslationX(mContentLayout, mStartTranslationX);
		ViewHelper.setTranslationY(mContentLayout, mStartTranslationY);
	}

	/**
	 * 获取关闭时，移动的距离
	 */
	private float getCloseTranslation() {
		switch (mTouchViewGravity) {
		case Gravity.LEFT:
			return -mContentLayout.getWidth();
		case Gravity.RIGHT:
			return mContentLayout.getWidth();
		case Gravity.TOP:
			return -mContentLayout.getHeight();
		case Gravity.BOTTOM:
			return mContentLayout.getHeight();
		default:
			return 0;
		}
	}

	/**
	 * 获取当前移动距离
	 */
	private float getCurTranslation() {
		float curTranslation = 0;
		switch (mTouchViewGravity) {
		case Gravity.LEFT:
		case Gravity.RIGHT:
			curTranslation = ViewHelper.getTranslationX(mContentLayout);
			break;
		case Gravity.TOP:
		case Gravity.BOTTOM:
			curTranslation = ViewHelper.getTranslationY(mContentLayout);
			break;
		}
		return curTranslation;
	}

	/**
	 * 获取打开时候，移动距离
	 */
	private float getOpenTranslation() {
		return 0f;
	}

	/**
	 * 根据当前Gravity获取触摸视图LayoutParams
	 */
	protected FrameLayout.LayoutParams generateTouchViewLayoutParams() {
		FrameLayout.LayoutParams lp = (LayoutParams) mTouchView.getLayoutParams();
		if (lp == null) {
			lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		switch (mTouchViewGravity) {
		case Gravity.LEFT:
		case Gravity.RIGHT:
			lp.width = mClosedTouchViewSize;
			lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
			break;
		case Gravity.TOP:
		case Gravity.BOTTOM:
			lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
			lp.height = mClosedTouchViewSize;
			break;
		}
		lp.gravity = mTouchViewGravity;
		return lp;
	}

	/**
	 * 设置抽屉的位置
	 * 
	 * @param drawerPosition 抽屉位置
	 * @see Gravity
	 */
	public void setDrawerGravity(int drawerPosition) {
		if (drawerPosition != Gravity.LEFT && drawerPosition != Gravity.TOP && drawerPosition != Gravity.RIGHT && drawerPosition != Gravity.BOTTOM) {
			return;
		}
		this.mTouchViewGravity = drawerPosition;
		mTouchView.setLayoutParams(generateTouchViewLayoutParams());
	}

	public void setDrawerCallback(DrawerCallback drawerCallback) {
		this.mDrawerCallback = drawerCallback;
	}

	public interface DrawerCallback {

		void onStartOpen();

		void onEndOpen();

		void onStartClose();

		void onEndClose();

		void onPreOpen();

		/**
		 * 正在移动回调
		 * @param gravity
		 * @param translation 移动的距离（当前移动位置到边界的距离，永远为正数）
		 */
		void onTranslating(int gravity, float translation);
	}

	public static class DrawerCallbackAdapter implements DrawerCallback {

		@Override
		public void onStartOpen() {

		}

		@Override
		public void onEndOpen() {

		}

		@Override
		public void onStartClose() {

		}

		@Override
		public void onEndClose() {

		}

		@Override
		public void onPreOpen() {

		}

		@Override
		public void onTranslating(int gravity, float translation) {

		}
	}

}
