package co.onlini.beacome.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import co.onlini.beacome.R;
import co.onlini.beacome.ui.adapter.ViewPagerAdapter;

public class IntroActivity extends Activity implements ViewPager.OnPageChangeListener {

    private ImageView[] mImageViews;  //dots

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        mImageViews = new ImageView[]{
                (ImageView) findViewById(R.id.iv_dot_0),
                (ImageView) findViewById(R.id.iv_dot_1),
                (ImageView) findViewById(R.id.iv_dot_2)
        };
        findViewById(R.id.tv_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ViewPagerAdapter adapter = new ViewPagerAdapter(new int[]{
                R.layout.page_intro_0,
                R.layout.page_intro_1,
                R.layout.page_intro_2
        });
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(this);

        setIndicatorPage(0);
    }

    private void setIndicatorPage(int page) {
        for (int i = 0; i < mImageViews.length; i++) {
            if (page == i) {
                mImageViews[i].setImageResource(R.drawable.page_dot_selected);
            } else {
                mImageViews[i].setImageResource(R.drawable.page_dot);
            }
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setIndicatorPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
