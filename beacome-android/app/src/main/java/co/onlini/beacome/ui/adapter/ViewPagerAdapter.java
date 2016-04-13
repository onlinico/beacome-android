package co.onlini.beacome.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class ViewPagerAdapter extends PagerAdapter {
    private int[] mResources;

    public ViewPagerAdapter(int[] resources) {
        mResources = resources;
    }

    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int resource = mResources[position];
        View pageView = LayoutInflater.from(container.getContext()).inflate(resource, container, false);
        container.addView(pageView);
        return pageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
