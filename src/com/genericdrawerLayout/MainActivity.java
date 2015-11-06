package com.genericdrawerLayout;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.example.genericdrawerLayout.R;

public class MainActivity extends Activity implements OnClickListener {

	private GenericDrawerLayout mDrawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initDrawLayout();
		
	}

	private void initDrawLayout() {
		mDrawerLayout = new GenericDrawerLayout(this);
		View view = new View(this);
		view.setBackgroundColor(Color.GREEN);
		mDrawerLayout.setContentLayout(view);
		
		// 添加到跟视图
		((FrameLayout) findViewById(android.R.id.content)).addView(mDrawerLayout); 
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
