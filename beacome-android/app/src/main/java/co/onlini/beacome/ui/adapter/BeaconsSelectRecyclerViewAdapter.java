package co.onlini.beacome.ui.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import co.onlini.beacome.databinding.ItemSelectableBeaconBinding;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.ui.SelectionWrapper;

public class BeaconsSelectRecyclerViewAdapter extends RecyclerView.Adapter<BeaconsSelectRecyclerViewAdapter.BeaconLinksViewHolder> {

    private List<SelectionWrapper<Beacon>> mData;

    public BeaconsSelectRecyclerViewAdapter() {
        this.setHasStableIds(true);
        mData = new ArrayList<>();
    }

    public void setData(Set<SelectionWrapper<Beacon>> data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        mData = new ArrayList<>(data);
        Collections.sort(mData, new Comparator<SelectionWrapper<Beacon>>() {
            @Override
            public int compare(SelectionWrapper<Beacon> lhs, SelectionWrapper<Beacon> rhs) {
                return lhs.getItem().getBeaconUuid().compareTo(rhs.getItem().getBeaconUuid());
            }
        });
    }

    @Override
    public BeaconLinksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSelectableBeaconBinding binding = ItemSelectableBeaconBinding.inflate(inflater, parent, false);
        return new BeaconLinksViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(BeaconLinksViewHolder holder, int position) {
        SelectionWrapper<Beacon> beaconLink = mData.get(position);
        holder.mBinding.setBeacon(beaconLink.getItem());
        holder.mBinding.setIsSelected(beaconLink.isSelected());
        holder.mBinding.tvLinkedCardsCount.setText(String.valueOf(beaconLink.getItem().getCardLinks().length));
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class BeaconLinksViewHolder extends RecyclerView.ViewHolder {
        public ItemSelectableBeaconBinding mBinding;

        public BeaconLinksViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = !mData.get(getAdapterPosition()).isSelected();
                    mData.get(getAdapterPosition()).setIsSelected(isChecked);
                    mBinding.ivAction.setChecked(isChecked);
                }
            };
            mBinding.ivAction.setOnClickListener(listener);
            mBinding.getRoot().setOnClickListener(listener);
        }
    }

}
