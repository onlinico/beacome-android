package co.onlini.beacome.ui.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.onlini.beacome.R;
import co.onlini.beacome.databinding.ItemCardBinding;
import co.onlini.beacome.model.CardByUserItem;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.ui.OnItemClickListener;
import co.onlini.beacome.util.comparator.CardComparator;

public class CardsSortedRecyclerViewAdapter extends RecyclerView.Adapter<CardsSortedRecyclerViewAdapter.CardViewHolder> {

    private List<CardByUserItem> mData;
    private List<CardByUserItem> mPreparedData;
    private Context mContext;

    private int mPage;
    private String mFilter;
    private int mSortBy;

    private OnItemClickListener<String> mOnItemClickListener;
    private OnItemActionClickListener<CardByUserItem> mOnItemActionClickListener;

    private String mOwner;
    private String mTranslator;

    public CardsSortedRecyclerViewAdapter(Context context, OnItemClickListener<String> onItemClickListener,
                                          OnItemActionClickListener<CardByUserItem> onItemActionClickListener) {
        setHasStableIds(true);
        mOwner = context.getString(R.string.owner);
        mTranslator = context.getString(R.string.translator);
        mData = new ArrayList<>();
        mPreparedData = new ArrayList<>();
        mOnItemClickListener = onItemClickListener;
        mOnItemActionClickListener = onItemActionClickListener;
        mContext = context;
    }

    public void setData(@NonNull List<CardByUserItem> data) {
        mData = data;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public void setSortBy(int sortBy) {
        mSortBy = sortBy;
    }

    public void setFilter(String filter) {
        mFilter = filter.toLowerCase();
    }

    public void updateList() {
        ArrayList<CardByUserItem> cardInfoList = new ArrayList<>();
        boolean hasFilter = !TextUtils.isEmpty(mFilter);
        for (CardByUserItem card : mData) {
            if ((mPage == Page.OWNER && card.isCurrentUserOwner())
                    || mPage == Page.ALL
                    || (mPage == Page.TRANSLATOR && !card.isCurrentUserOwner())) {
                if (!hasFilter || card.getTitle().toLowerCase().contains(mFilter)) {
                    cardInfoList.add(card);
                }
            }
        }
        Comparator<CardByUserItem> comparator = new CardComparator(mSortBy);
        Collections.sort(cardInfoList, comparator);
        mPreparedData = cardInfoList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).hashCode();
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCardBinding binding = ItemCardBinding.inflate(inflater, parent, false);
        return new CardViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        CardByUserItem card = mPreparedData.get(position);
        holder.mBinding.setItem(card);
        holder.mBinding.tvBeaconsCount.setText(String.valueOf(card.getBeaconsCount()));
        holder.mBinding.tvMyRole.setText(card.isCurrentUserOwner() ? mOwner : mTranslator);

        Glide.with(mContext)
                .load(card.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new StringSignature(String.valueOf(card.getVersion())))
                .into(holder.mBinding.ivLogo);
    }

    @Override
    public int getItemCount() {
        return mPreparedData.size();
    }

    public static class Page {
        public static final int ALL = 0x0;
        public static final int OWNER = 0x1;
        public static final int TRANSLATOR = 0x2;
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        public ItemCardBinding mBinding;

        public CardViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mBinding.getItem().getCardUuid(), v);
                    }
                }
            });
            mBinding.ivAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemActionClickListener != null) {
                        mOnItemActionClickListener.onItemActionClick(mBinding.getItem(), v);
                    }
                }
            });
        }
    }

}
