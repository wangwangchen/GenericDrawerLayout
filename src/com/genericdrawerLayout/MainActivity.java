package com.genericdrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.genericdrawerLayout.R;

import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener {

    private GenericDrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDrawLayout();

    }

    private void initDrawLayout() {
        mDrawerLayout = (GenericDrawerLayout) findViewById(R.id.genericdrawerlayout);

        // 视图1
        mDrawerLayout.setContentLayout(View.inflate(this, R.layout.layout_content, null));
        initContentLayout();

        // 视图二
        // mDrawerLayout.setContentLayout(new MFrameLayout(this));

        // 可以设置打开时响应Touch的区域范围
        mDrawerLayout.setTouchSizeOfOpened(Util.dip2px(this, 100));
        mDrawerLayout.setTouchSizeOfClosed(Util.dip2px(this, 300));

    }

    /**
     * 大家可以使用requestDisallowInterceptTouchEvent(true);方法来拦截侧滑菜单的事件
     */
    class MFrameLayout extends FrameLayout {

        public MFrameLayout(Context context) {
            super(context);
            setBackgroundColor(Color.GREEN);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return true;
        }
    }

    private void initContentLayout() {
        ListView mListView = (ListView) findViewById(R.id.listview);
        String[] strs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
        ArrayAdapter<String> mListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, strs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view != null && TextView.class.isInstance(view)) {
                    ((TextView) view).setMinHeight(Util.dip2px(getApplicationContext(), 60));
                    ((TextView) view).setMinWidth(Util.dip2px(getApplicationContext(), 60));
                }
                return view;
            }
        };
        mListView.setAdapter(mListAdapter);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final ArrayList<View> viewContainter = new ArrayList<View>();
        int[] colors = new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        for (int color : colors) {
            TextView textView = new TextView(this);
            textView.setBackgroundColor(color);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(18);
            textView.setTextColor(Color.WHITE);
            textView.setMinHeight(Util.dip2px(this, 120));
            textView.setMinWidth(Util.dip2px(this, 120));
            textView.setText("Color: " + color);
            viewContainter.add(textView);
        }
        mViewPager.setAdapter(new PagerAdapter() {

            //viewpager中的组件数量
            @Override
            public int getCount() {
                return viewContainter.size();
            }

            //滑动切换的时候销毁当前的组件
            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                ((ViewPager) container).removeView(viewContainter.get(position));
            }

            //每次滑动的时候生成的组件
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ((ViewPager) container).addView(viewContainter.get(position));
                return viewContainter.get(position);
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }

        });
    }

    @Override
    public void onClick(View v) {
        int gravity = Gravity.LEFT;
        switch (v.getId()) {
            case R.id.left:
                gravity = Gravity.LEFT;
                break;
            case R.id.top:
                gravity = Gravity.TOP;
                break;
            case R.id.right:
                gravity = Gravity.RIGHT;
                break;
            case R.id.bottom:
                gravity = Gravity.BOTTOM;
                break;
            default:
                break;
        }
        mDrawerLayout.setDrawerGravity(gravity);
    }

}
