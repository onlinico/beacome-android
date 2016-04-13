package co.onlini.beacome.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.util.List;

import co.onlini.beacome.R;
import co.onlini.beacome.model.CardUser;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.web.Conventions;

public class UserListAdapter extends BaseAdapter {

    private List<CardUser> mData;
    private String mOwner;
    private String mTranslator;
    private boolean mIsEditable;
    private OnItemActionClickListener<Integer> mListener;
    private Context mContext;

    public UserListAdapter(Context context, List<CardUser> users, boolean isEditable) {
        mData = users;
        mOwner = context.getString(R.string.owner);
        mTranslator = context.getString(R.string.translator);
        mIsEditable = isEditable;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public CardUser getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
//        return mData.get(position).getUuid();
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_user, parent, false);
            convertView.setOnClickListener(null);
            viewHolder = new ViewHolder();
            viewHolder.mTvName = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.mTvEmail = (TextView) convertView.findViewById(R.id.tv_email_value);
            viewHolder.mTvRole = (TextView) convertView.findViewById(R.id.tv_role_value);
            viewHolder.mIvImage = (ImageView) convertView.findViewById(R.id.iv_image);
            viewHolder.mIvAction = convertView.findViewById(R.id.iv_action);
            viewHolder.mIvAction.setVisibility(mIsEditable ? View.VISIBLE : View.GONE);
            viewHolder.mIvAction.setOnClickListener(new OnMenuClickListener(viewHolder));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        CardUser user = mData.get(position);
        viewHolder.mPosition = position;
        if (Conventions.USER_EMPTY_SHARE_UUID.equals(user.getShareUuid())) {
            viewHolder.mTvName.setText(user.getName());
        } else {
            viewHolder.mTvName.setText(mContext.getString(R.string.item_user_name_share));
        }
        viewHolder.mTvEmail.setText(user.getEmail());
        viewHolder.mTvRole.setText(user.isOwner() ? mOwner : mTranslator);

        if (user.getImage() != null) {
            Glide.with(mContext)
                    .load(user.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .signature(new StringSignature(String.valueOf(user.getVersion())))
                    .error(R.drawable.ic_userpic)
                    .into(viewHolder.mIvImage);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.ic_userpic)
                    .into(viewHolder.mIvImage);
        }
        return convertView;
    }

    public void setOnItemActionClickListener(OnItemActionClickListener<Integer> listener) {
        mListener = listener;
    }

    private class OnMenuClickListener implements View.OnClickListener {
        ViewHolder mHolder;

        public OnMenuClickListener(ViewHolder viewHolder) {
            mHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemActionClick(mHolder.mPosition, v);
            }
        }
    }

    private class ViewHolder {
        Integer mPosition;
        TextView mTvName;
        TextView mTvEmail;
        TextView mTvRole;
        ImageView mIvImage;
        View mIvAction;
    }
}
