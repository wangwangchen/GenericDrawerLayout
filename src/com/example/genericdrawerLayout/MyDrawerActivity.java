package com.example.genericdrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

public class MyDrawerActivity extends Activity {
	
	public static final String EXTRA_GRAVITY = "extra_gravity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int gravity = Gravity.LEFT;
		
		Intent intent = getIntent();
		if (intent != null) {
			gravity = intent.getIntExtra(EXTRA_GRAVITY, Gravity.LEFT);
		}
		
		GenericDrawerLayout drawerLayout = new GenericDrawerLayout(this);
		View view = new View(this);
		view.setBackgroundColor(Color.GREEN);
		drawerLayout.setContentLayout(view);
		setContentView(drawerLayout);
		
		
	}

}
