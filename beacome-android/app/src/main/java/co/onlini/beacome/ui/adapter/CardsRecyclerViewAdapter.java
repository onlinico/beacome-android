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
import co.onlini.beacome.databinding.ItemCardBinding;
import co.onlini.beacome.model.CardByUserAndBeaconItem;
import co.onlini.beacome.ui.OnItemActionClickListener;

public class CardsRecyclerViewAdapter extends RecyclerView.Adapter<CardsRecyclerViewAdapter.CardViewHolder> {

    private List<CardByUserAndBeaconItem> mData;
    private Context mContext;
    private String mOwner;
    private String mTranslator;
    private String mOn;
    private String mOff;
    private OnItemActionClickListener<CardByUserAndBeaconItem> mOnItemActionClickListener;

    public CardsRecyclerViewAdapter(Context context) {
        setHasStableIds(true);
        mData = new ArrayList<>();
        mContext = context;
        mOwner = context.getString(R.string.owner);
        mTranslator = context.getString(R.string.translator);
        mOn = context.getString(R.string.on);
        mOff = context.getString(R.string.off);
    }

    public void setData(List<CardByUserAndBeaconItem> data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        mData = data;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCardBinding binding = ItemCardBinding.inflate(inflater, parent, false);
        return new CardViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        CardByUserAndBeaconItem card = mData.get(position);
        holder.mBinding.setItem(card);
        holder.mBinding.tvBeaconsCount.setText(String.valueOf(card.getBeaconsCount()));
        holder.mBinding.setIsStateVisible(true);
        holder.mBinding.tvMyRole.setText(card.isCurrentUserOwner() ? mOwner : mTranslator);
        holder.mBinding.tvState.setEnabled(card.isActive());
        holder.mBinding.tvState.setText(card.isActive() ? mOn : mOff);

        Glide.with(mContext)
                .load(card.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new StringSignature(String.valueOf(card.getVersion())))
                .into(holder.mBinding.ivLogo);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).hashCode();
    }

    public void setOnItemActionClickListener(OnItemActionClickListener<CardByUserAndBeaconItem> onItemActionClickListener) {
        mOnItemActionClickListener = onItemActionClickListener;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        public ItemCardBinding mBinding;

        public CardViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
            mBinding.ivAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemActionClickListener != null) {
                        mOnItemActionClickListener.onItemActionClick((CardByUserAndBeaconItem) mBinding.getItem(), v);
                    }
                }
            });
        }
    }

}
