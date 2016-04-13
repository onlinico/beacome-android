package co.onlini.beacome.ui.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.onlini.beacome.R;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.model.CardLink;
import co.onlini.beacome.ui.OnListItemMenuItemClickListener;

public class BeaconEditListAdapter extends BaseAdapter {

    private List<Beacon> mData;
    private String mStateOn;
    private String mStateOff;
    private OnListItemMenuItemClickListener mMenuItemClickListener;
    private Set<String> mActiveBeacons;

    public BeaconEditListAdapter(Context context, List<Beacon> beacons, String cardUuid) {
        mData = beacons;
        mActiveBeacons = new HashSet<>();
        for (Beacon beacon : beacons) {
            for (CardLink cardLink : beacon.getCardLinks()) {
                if (cardUuid.equals(cardLink.getCardUuid())) {
                    if (cardLink.isActive()) {
                        mActiveBeacons.add(beacon.getBeaconUuid());
                    }
                }
            }
        }
        mStateOn = context.getString(R.string.item_beacon_state_on);
        mStateOff = context.getString(R.string.item_beacon_state_off);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public boolean isActive(int position) {
        return mActiveBeacons.contains(getItem(position).getBeaconUuid());
    }

    @Override
    public Beacon getItem(int position) {
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
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_card_beacon, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTvUuid = (TextView) convertView.findViewById(R.id.tv_uuid);
            viewHolder.mTvState = (TextView) convertView.findViewById(R.id.tv_state);
            viewHolder.mTvLinksCount = (TextView) convertView.findViewById(R.id.tv_linked_cards_count);
            viewHolder.mIvAction = convertView.findViewById(R.id.iv_action);
            viewHolder.mIvAction.setOnClickListener(new OnActionClickListener(viewHolder));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Beacon link = mData.get(position);
        boolean isActive = mActiveBeacons.contains(link.getBeaconUuid());
        viewHolder.mTvUuid.setText(link.getBeaconUuid());
        viewHolder.mTvState.setText(isActive ? mStateOn : mStateOff);
        viewHolder.mTvLinksCount.setText(String.valueOf(link.getCardLinks().length));
        viewHolder.mIsActive = isActive;
        viewHolder.mPos = position;
        viewHolder.mTvState.setEnabled(isActive);
        viewHolder.mTvUuid.setEnabled(isActive);
        viewHolder.mTvLinksCount.setEnabled(isActive);
        return convertView;
    }

    public void setOnMenuItemClickListener(OnListItemMenuItemClickListener listener) {
        mMenuItemClickListener = listener;
    }

    private class ViewHolder {
        boolean mIsActive;
        int mPos;
        TextView mTvUuid;
        TextView mTvState;
        TextView mTvLinksCount;
        View mIvAction;
    }

    private class OnActionClickListener implements View.OnClickListener {
        private BeaconEditListAdapter.ViewHolder mViewHolder;

        public OnActionClickListener(BeaconEditListAdapter.ViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.popup_card_beacon);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (mMenuItemClickListener != null) {
                        mMenuItemClickListener.onMenuItemClick(item.getItemId(), mViewHolder.mPos);
                        return true;
                    }
                    return false;
                }
            });
            popup.show();
        }
    }
}
