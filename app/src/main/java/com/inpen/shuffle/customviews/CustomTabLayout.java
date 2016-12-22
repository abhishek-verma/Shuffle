package com.inpen.shuffle.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Dimension;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.inpen.shuffle.R;

/**
 * Created by Abhishek on 12/22/2016.
 */

public class CustomTabLayout extends TabLayout {
    public CustomTabLayout(Context context) {
        super(context);
    }

    public CustomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setupWithViewPager(ViewPager viewPager) {
        super.setupWithViewPager(viewPager);

//        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/NotoSans-Regular.ttf");

//        if (typeface != null) {
        this.removeAllTabs();

        ViewGroup slidingTabStrip = (ViewGroup) getChildAt(0);

        PagerAdapter adapter = viewPager.getAdapter();

        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            Tab tab = this.newTab();
            this.addTab(tab.setText(adapter.getPageTitle(i)));
            AppCompatTextView view = (AppCompatTextView) ((ViewGroup) slidingTabStrip.getChildAt(i)).getChildAt(1);
            view.setTypeface(Typeface.MONOSPACE);
            view.setTextSize(Dimension.SP, getResources().getDimensionPixelSize(R.dimen.tab_text_size));
        }
//        }
    }
}
