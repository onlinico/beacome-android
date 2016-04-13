package co.onlini.beacome.ui.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.onlini.beacome.R;
import co.onlini.beacome.databinding.ItemHistoryBinding;
import co.onlini.beacome.model.HistoryCardBase;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.ui.OnItemClickListener;
import co.onlini.beacome.util.SortVariants;
import co.onlini.beacome.util.TimeFormatter;
import co.onlini.beacome.util.comparator.HistoryComparator;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {

    private final Context mContext;
    private List<HistoryCardBase> mData;
    private List<HistoryCardBase> mPreparedData;
    private Set<HistoryCardBase> mNearHistoryCards;
    private Collection<String> mNearBeaconsCardsUuids;
    private int mPage;
    private int mSortBy;
    private String mFilter;
    private OnItemClickListener<HistoryCardBase> mOnItemClickListener;
    private OnItemActionClickListener<HistoryCardBase> mOnItemActionClickListener;
    private Map<String, String> mDateHeaders;
    private TextView mTvEmptyListHolder;
    private boolean mIsScannerRunning;

    public HistoryRecyclerViewAdapter(Context context, int page, int sortBy) {
        mContext = context;
        mData = new ArrayList<>();
        mPreparedData = new ArrayList<>();
        mNearHistoryCards = new HashSet<>();
        setHasStableIds(true);
        setHistoryPage(page);
        setSortBy(sortBy);
    }

    private static boolean isSameDay(Calendar calendar, Calendar calendar1) {
        if (calendar == null || calendar1 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (calendar.get(Calendar.ERA) == calendar1.get(Calendar.ERA) &&
                calendar.get(Calendar.YEAR) == calendar1.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == calendar1.get(Calendar.DAY_OF_YEAR));
    }

    public void setTvEmptyListHolder(TextView tvEmptyListHolder) {
        mTvEmptyListHolder = tvEmptyListHolder;
    }

    public void setData(List<HistoryCardBase> data) {
        synchronized (this) {
            mData = data;
        }
    }

    public void setHistoryPage(int page) {
        if (!(page == Page.ALL || page == Page.FAVORITE || page == Page.NEAR)) {
            throw new IllegalArgumentException();
        }
        mPage = page;
    }

    public void setSortBy(int sortBy) {
        mSortBy = sortBy;
    }

    public void setFilter(String filter) {
        if (filter != null) {
            mFilter = filter.toLowerCase();
        }
    }

    @Override
    public long getItemId(int position) {
        return mPreparedData.get(position).getUuid().hashCode();
    }

    public void setOnItemClickListener(OnItemClickListener<HistoryCardBase> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemMenuClickListener(OnItemActionClickListener<HistoryCardBase> onItemClickListener) {
        mOnItemActionClickListener = onItemClickListener;
    }

    public void prepareData() {
        mPreparedData.clear();
        boolean hasFilter = !TextUtils.isEmpty(mFilter);

        mNearHistoryCards.clear();
        for (HistoryCardBase dataItem : mData) {
            for (String nearCardUuid : mNearBeaconsCardsUuids) {
                if (nearCardUuid.equals(dataItem.getUuid())) {
                    mNearHistoryCards.add(dataItem);
                }
            }
        }
        if (mPage == Page.ALL) {
            if (hasFilter) {
                for (HistoryCardBase dataItem : mData) {
                    if (dataItem.getTitle().toLowerCase().contains(mFilter)) {
                        mPreparedData.add(dataItem);
                    }
                }
            } else {
                mPreparedData = new ArrayList<>(mData);
            }
        } else if (mPage == Page.FAVORITE) {
            for (HistoryCardBase dataItem : mData) {
                if (dataItem.isFavorite()) {
                    if (hasFilter) {
                        if (dataItem.getTitle().toLowerCase().contains(mFilter)) {
                            mPreparedData.add(dataItem);
                        }
                    } else {
                        mPreparedData.add(dataItem);
                    }
                }
            }
        } else if (mPage == Page.NEAR) {
            for (HistoryCardBase dataItem : mData) {
                if (mNearHistoryCards.contains(dataItem)) {
                    if (hasFilter) {
                        if (dataItem.getTitle().toLowerCase().contains(mFilter)) {
                            mPreparedData.add(dataItem);
                        }
                    } else {
                        mPreparedData.add(dataItem);
                    }
                }
            }
        }

        Comparator<HistoryCardBase> comparator = new HistoryComparator(mSortBy);
        Collections.sort(mPreparedData, comparator);

        if (mSortBy == SortVariants.BY_DATE && mPreparedData.size() > 0) {
            mDateHeaders = new HashMap<>(mPreparedData.size());
            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();
            for (int i = 0; i < mPreparedData.size(); i++) {
                long curTimestamp = mPreparedData.get(i).getLastDiscoveryDate();
                calendar.setTimeInMillis(curTimestamp);
                calendar2.setTimeInMillis(System.currentTimeMillis());
                String headerText;
                if (isSameDay(calendar, calendar2)) {
                    headerText = mContext.getString(R.string.item_history_header);
                } else {
                    headerText = TimeFormatter.getFormattedDate(new Date(curTimestamp)).toUpperCase();
                }
                mDateHeaders.put(mPreparedData.get(i).getUuid(), headerText);
            }
        }
        String text = null;
        int drawableRes = 0;
        if (mPreparedData.size() == 0) {
            switch (mPage) {
                case Page.ALL:
                    text = mContext.getString(R.string.empty_list_stub_history);
                    drawableRes = R.drawable.ic_nav_history_92dp;
                    break;
                case Page.NEAR:
                    text = mIsScannerRunning ? mContext.getString(R.string.empty_list_stub_scanner_turn_on) :
                            mContext.getString(R.string.empty_list_stub_scanner_turn_off);
                    drawableRes = R.drawable.ic_nav_scan_92dp;
                    break;
                case Page.FAVORITE:
                    text = mContext.getString(R.string.empty_list_stub_favorite);
                    drawableRes = R.drawable.ic_nav_favorites_92dp;
                    break;
            }
        }

        final int finalDrawableRes = drawableRes;
        final String finalText = text;
        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (finalDrawableRes != 0) {
                            mTvEmptyListHolder.setCompoundDrawablesRelativeWithIntrinsicBounds(0, finalDrawableRes, 0, 0);
                            mTvEmptyListHolder.setText(finalText);
                            mTvEmptyListHolder.setVisibility(View.VISIBLE);
                        } else {
                            mTvEmptyListHolder.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                }
        );
    }

    public void setScannerState(boolean isRunning) {
        mIsScannerRunning = isRunning;
        if (mPage == Page.NEAR && mTvEmptyListHolder.getVisibility() == View.VISIBLE) {
            mTvEmptyListHolder.setText(mIsScannerRunning ? mContext.getString(R.string.empty_list_stub_scanner_turn_on) :
                    mContext.getString(R.string.empty_list_stub_scanner_turn_off));
        }
    }

    @Override
    public HistoryRecyclerViewAdapter.HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(inflater, parent, false);
        return new HistoryViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        HistoryCardBase historyCard = mPreparedData.get(position);
        holder.mBinding.setItem(historyCard);
        holder.mBinding.setIsNear(mNearHistoryCards.contains(historyCard));
        if (mPage != Page.NEAR && mSortBy == SortVariants.BY_DATE) {
            if (mDateHeaders != null) {
                String date = mDateHeaders.get(historyCard.getUuid());
                if (date == null) {
                    holder.mBinding.setShowDate(false);
                } else if (position == 0 || !mDateHeaders.get(mPreparedData.get(position - 1).getUuid()).equals(date)) {
                    holder.mBinding.setShowDate(true);
                    holder.mBinding.tvDateHeader.setText(date);
                } else {
                    holder.mBinding.setShowDate(false);
                }
            }
        } else {
            holder.mBinding.setShowDate(false);
        }
        holder.mBinding.tvNear.setVisibility(mPage != Page.NEAR ? View.VISIBLE : View.INVISIBLE);

        Glide.with(mContext)
                .load(historyCard.getImageUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new StringSignature(String.valueOf(historyCard.getCardVersion())))
                .into(holder.mBinding.ivLogo);
    }

    @Override
    public int getItemCount() {
        return mPreparedData.size();
    }

    public void setNearBeaconsCardsUuids(Collection<String> uuids) {
        mNearBeaconsCardsUuids = uuids;
    }

    public HistoryCardBase getCardByUuid(String cardUuid) {
        HistoryCardBase cardBase = null;
        for (HistoryCardBase card : mPreparedData) {
            if (card.getUuid().equals(cardUuid)) {
                cardBase = card;
                break;
            }
        }
        return cardBase;
    }

    public static class Page {
        public static final int NEAR = 0;
        public static final int ALL = 1; //history
        public static final int FAVORITE = 2;
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        public ItemHistoryBinding mBinding;

        public HistoryViewHolder(final View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mBinding.getItem(), v);
                    }
                }
            });
            mBinding.ivFavorite.setOnClickListener(new View.OnClickListener() {
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