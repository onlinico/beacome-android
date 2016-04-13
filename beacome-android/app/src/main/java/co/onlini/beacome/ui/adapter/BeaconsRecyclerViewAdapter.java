package co.onlini.beacome.ui.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.onlini.beacome.databinding.ItemBeaconBinding;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.ui.OnItemClickListener;
import co.onlini.beacome.util.SortVariants;

public class BeaconsRecyclerViewAdapter extends RecyclerView.Adapter<BeaconsRecyclerViewAdapter.BeaconLinksViewHolder> {

    private List<Beacon> mData;
    private List<Beacon> mPreparedData;
    private OnItemClickListener<String> mItemClickListener;
    private String mFilter;
    private int mSortVariant;

    public BeaconsRecyclerViewAdapter() {
        this.setHasStableIds(true);
        mData = new ArrayList<>();
        mPreparedData = new ArrayList<>();
    }

    public void setData(List<Beacon> data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        mData = data;
    }

    public void setFilter(String filter) {
        //beaconUuid contains only upperCase
        mFilter = filter.toLowerCase();
    }

    public void setSortVariant(int sortVariant) {
        mSortVariant = sortVariant;
    }

    public void updateList() {
        mPreparedData.clear();
        boolean hasFilter = !TextUtils.isEmpty(mFilter);
        for (Beacon beacon : mData) {
            if (!hasFilter || beacon.getBeaconUuid().contains(mFilter)) {
                mPreparedData.add(beacon);
            }
        }
        Collections.sort(mPreparedData, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon lhs, Beacon rhs) {
                int compareResult;
                switch (mSortVariant) {
                    case SortVariants.BY_NAME_DESC:
                        compareResult = rhs.getBeaconUuid().compareTo(lhs.getBeaconUuid());
                        break;
                    case SortVariants.BY_CARDS_COUNT:
                        compareResult = rhs.getCardLinks().length - lhs.getCardLinks().length;
                        if (compareResult == 0) {
                            compareResult = lhs.getBeaconUuid().compareTo(rhs.getBeaconUuid());
                        }
                        break;
                    default:
                        compareResult = lhs.getBeaconUuid().compareTo(rhs.getBeaconUuid());
                }
                return compareResult;
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public BeaconLinksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBeaconBinding binding = ItemBeaconBinding.inflate(inflater, parent, false);
        return new BeaconLinksViewHolder(binding.getRoot());
    }

    @Override
    public long getItemId(int position) {
        return mPreparedData.get(position).hashCode();
    }

    @Override
    public void onBindViewHolder(BeaconLinksViewHolder holder, int position) {
        Beacon beacon = mPreparedData.get(position);
        holder.mBinding.setBeacon(beacon);
        holder.mBinding.tvLinkedCardsCount.setText(String.valueOf(beacon.getCardLinks().length));
    }

    public void setItemClickListener(OnItemClickListener<String> itemClickListener) {
        mItemClickListener = itemClickListener;
    }


    @Override
    public int getItemCount() {
        return mPreparedData.size();
    }

    public class BeaconLinksViewHolder extends RecyclerView.ViewHolder {
        public ItemBeaconBinding mBinding;

        public BeaconLinksViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(mBinding.getBeacon().getBeaconUuid(), v);
                    }
                }
            };
            mBinding.getRoot().setOnClickListener(listener);
        }
    }

}
