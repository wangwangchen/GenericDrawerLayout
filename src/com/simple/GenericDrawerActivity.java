package com.simple;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.genericdrawerLayout.R;
import com.genericdrawerLayout.GenericDrawerLayout;

import java.util.ArrayList;

public class GenericDrawerActivity extends Activity {

    private static final String TAG = GenericDrawerActivity.class.getSimpleName();
    private GenericDrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_drawer);

        initView();

    }

    private void initView() {
        final TextView msgTV = (TextView) findViewById(R.id.message);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String [] locs ={"左侧","上方","右侧","下方"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locs);
        //设置下拉列表风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将适配器添加到spinner中去
        spinner.setAdapter(adapter);
        spinner.setVisibility(View.VISIBLE);//设置默认显示
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int gravity = Gravity.LEFT;
                switch (position) {
                    case 0:
                        gravity = Gravity.LEFT;
                        break;
                    case 1:
                        gravity = Gravity.TOP;
                        break;
                    case 2:
                        gravity = Gravity.RIGHT;
                        break;
                    case 3:
                        gravity = Gravity.BOTTOM;
                        break;
                    default:
                        break;
                }
                mDrawerLayout.setDrawerGravity(gravity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mDrawerLayout = (GenericDrawerLayout) findViewById(R.id.genericdrawerlayout);

        // 视图1
        mDrawerLayout.setContentLayout(View.inflate(this, R.layout.layout_content, null));
        initContentLayout();

        // 视图二
        // mDrawerLayout.setContentLayout(new MFrameLayout(this));

        // 可以设置打开时响应Touch的区域范围
        /*mDrawerLayout.setTouchSizeOfOpened(Util.dip2px(this, 100));
        mDrawerLayout.setTouchSizeOfClosed(Util.dip2px(this, 300));*/

        // 设置随着位置的变更，背景透明度也改变
        mDrawerLayout.setOpaqueWhenTranslating(true);

        // 设置抽屉是否可以打开
        // mDrawerLayout.setOpennable(false);

        // 设置抽屉的空白区域大小
         float v = getResources().getDisplayMetrics().density * 100 + 0.5f; // 100DIP
         mDrawerLayout.setDrawerEmptySize((int) v);

        // 设置黑色背景的最大透明度
        // mDrawerLayout.setMaxOpaque(0.6f);

        // 设置事件回调
        mDrawerLayout.setDrawerCallback(new GenericDrawerLayout.DrawerCallbackAdapter() {
            @Override
            public void onStartOpen() {
                Log.i(TAG, "onStartOpen");
                msgTV.setText("onStartOpen");
            }

            @Override
            public void onEndOpen() {
                Log.i(TAG, "onEndOpen");
                msgTV.setText("onEndOpen");
            }

            @Override
            public void onStartClose() {
                Log.i(TAG, "onStartClose");
                msgTV.setText("onStartOpen");
            }

            @Override
            public void onEndClose() {
                Log.i(TAG, "onEndClose");
                msgTV.setText("onEndClose");
            }

            @Override
            public void onPreOpen() {
                Log.i(TAG, "onPreOpen");
                msgTV.setText("onPreOpen");
            }

            @Override
            public void onTranslating(int gravity, float translation, float fraction) {
                Log.i(TAG, "onTranslating gravity = " + gravity + "\n  translation = " + translation + "\n fraction = " + fraction);
                msgTV.setText("onTranslating gravity = " + gravity + "\n  translation = " + translation + "\n fraction = " + fraction);
            }
        });
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.close();
            }
        });
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

}
