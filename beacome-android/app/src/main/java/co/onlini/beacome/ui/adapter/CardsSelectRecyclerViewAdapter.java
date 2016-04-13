package co.onlini.beacome.ui.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.List;

import co.onlini.beacome.R;
import co.onlini.beacome.databinding.ItemSelectableCardBinding;
import co.onlini.beacome.model.CardByUserItem;
import co.onlini.beacome.ui.SelectionWrapper;

public class CardsSelectRecyclerViewAdapter extends RecyclerView.Adapter<CardsSelectRecyclerViewAdapter.BeaconLinksViewHolder> {

    private final String mOwner;
    private final String mTranslator;
    private List<SelectionWrapper<CardByUserItem>> mData;
    private Context mContext;

    public CardsSelectRecyclerViewAdapter(Context context) {
        setHasStableIds(true);
        mData = new ArrayList<>();
        mContext = context;
        mOwner = context.getString(R.string.owner);
        mTranslator = context.getString(R.string.translator);
    }

    public void setData(List<SelectionWrapper<CardByUserItem>> data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        mData = data;
    }

    @Override
    public BeaconLinksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSelectableCardBinding binding = ItemSelectableCardBinding.inflate(inflater, parent, false);
        return new BeaconLinksViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(BeaconLinksViewHolder holder, int position) {
        SelectionWrapper<CardByUserItem> card = mData.get(position);
        holder.mBinding.setItem(card.getItem());
        holder.mBinding.tvTitle.setText(card.getItem().getTitle());
        holder.mBinding.tvDescription.setText(card.getItem().getDescription());
        holder.mBinding.tvBeaconsCount.setText(String.valueOf(card.getItem().getBeaconsCount()));
        holder.mBinding.tvMyRole.setText(card.getItem().isCurrentUserOwner() ? mOwner : mTranslator);
        holder.mBinding.setIsSelected(card.isSelected());

        Glide.with(mContext)
                .load(card.getItem().getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new StringSignature(String.valueOf(card.getItem().getVersion())))
                .into(holder.mBinding.ivLogo);
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
        public ItemSelectableCardBinding mBinding;

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
