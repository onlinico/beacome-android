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

import co.onlini.beacome.databinding.ItemDiscountBinding;
import co.onlini.beacome.model.DiscountItem;
import co.onlini.beacome.ui.OnItemClickListener;

public class DiscountsRecyclerViewAdapter extends RecyclerView.Adapter<DiscountsRecyclerViewAdapter.DiscountViewHolder> {

    private List<DiscountItem> mData;
    private Context mContext;
    private OnItemClickListener<DiscountItem> mListener;

    public DiscountsRecyclerViewAdapter(Context context) {
        setHasStableIds(true);
        mData = new ArrayList<>();
        mContext = context;
    }

    public void setData(List<DiscountItem> data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        mData = data;
    }

    public void setOnItemClickListener(OnItemClickListener<DiscountItem> listener) {
        mListener = listener;
    }

    @Override
    public DiscountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDiscountBinding binding = ItemDiscountBinding.inflate(inflater, parent, false);
        return new DiscountViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(DiscountViewHolder holder, int position) {
        DiscountItem discount = mData.get(position);
        holder.mBinding.setItem(discount);
        Glide.with(mContext)
                .load(discount.getCardLogoUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new StringSignature(String.valueOf(discount.getVersion())))
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

    public class DiscountViewHolder extends RecyclerView.ViewHolder {
        public ItemDiscountBinding mBinding;

        public DiscountViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(mBinding.getItem(), v);
                    }
                }
            });
        }
    }

}
