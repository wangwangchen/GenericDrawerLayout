package com.simple;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.genericdrawerLayout.R;
import com.genericdrawerLayout.GenericDrawerLayout;
import com.genericdrawerLayout.MaterialMenuButton;

/**
 * Material����Activity
 */
public class MaterialMenuActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MaterialMenuActivity.class.getSimpleName();
    private GenericDrawerLayout mGenericDrawerLayout;
    private MaterialMenuButton mMaterialMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_material_menu);

        mGenericDrawerLayout = (GenericDrawerLayout) findViewById(R.id.genericdrawerlayout);
        mMaterialMenuButton = (MaterialMenuButton) findViewById(R.id.materialmenubutton);
        mGenericDrawerLayout.setOpaqueWhenTranslating(true);
        mGenericDrawerLayout.setMaxOpaque(0.6f);

        TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.parseColor("#00A4A6"));
        textView.setGravity(Gravity.CENTER);
        textView.setText("GenericDrawerLayout");
        textView.setTextSize(22);
        mGenericDrawerLayout.setContentLayout(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // 设置抽屉留白
        mGenericDrawerLayout.setDrawerEmptySize((int) (getResources().getDisplayMetrics().density * 120 + 0.5f));
        mMaterialMenuButton.setOnClickListener(this);
        mGenericDrawerLayout.setDrawerCallback(mGenericDrawerCallback);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.materialmenubutton) {
            mGenericDrawerLayout.switchStatus();
        }
    }

    private GenericDrawerLayout.DrawerCallback mGenericDrawerCallback = new GenericDrawerLayout.DrawerCallbackAdapter() {

        @Override
        public void onTranslating(int gravity, float translation, float fraction) {
            super.onTranslating(gravity, translation, fraction);
            Log.e(TAG, "fraction = " + fraction);
            mMaterialMenuButton.update(fraction);
        }
    };
}
