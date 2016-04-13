package co.onlini.beacome.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import co.onlini.beacome.databinding.ItemBeaconBinding;
import co.onlini.beacome.model.Beacon;

public class BeaconListAdapter extends BaseAdapter {

    private List<Beacon> mData;

    public BeaconListAdapter(List<Beacon> beaconLinks) {
        mData = beaconLinks;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemBeaconBinding binding = ItemBeaconBinding.inflate(inflater, parent, false);
            viewHolder = new ViewHolder(binding);
            convertView = binding.getRoot();
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Beacon beacon = mData.get(position);
        viewHolder.mBinding.setBeacon(beacon);
        viewHolder.mBinding.tvLinkedCardsCount.setText(String.valueOf(beacon.getCardLinks().length));
        return convertView;
    }

    private class ViewHolder {
        public ItemBeaconBinding mBinding;

        public ViewHolder(ItemBeaconBinding binding) {
            mBinding = binding;
        }
    }

}
