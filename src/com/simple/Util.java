package com.simple;

import android.content.Context;

/**
 * Created by Chen on 2015/11/8.
 */
public class Util {
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
