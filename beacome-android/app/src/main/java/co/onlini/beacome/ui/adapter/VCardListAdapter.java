package co.onlini.beacome.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.util.List;

import co.onlini.beacome.R;
import co.onlini.beacome.model.Vcard;
import co.onlini.beacome.ui.OnItemActionClickListener;

public class VCardListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Vcard> mData;
    private boolean mShowMenu;
    private OnItemActionClickListener<Integer> mListener;

    public VCardListAdapter(Context context, List<Vcard> contacts, boolean showMenu) {
        mData = contacts;
        mShowMenu = showMenu;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Vcard getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_vcard, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTvName = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.mTvEmail = (TextView) convertView.findViewById(R.id.tv_email_value);
            viewHolder.mTvPhone = (TextView) convertView.findViewById(R.id.tv_role_value);
            viewHolder.mIvImage = (ImageView) convertView.findViewById(R.id.iv_image);
            viewHolder.mBtnMenu = convertView.findViewById(R.id.iv_action);
            viewHolder.mBtnMenu.setVisibility(mShowMenu ? View.VISIBLE : View.GONE);
            viewHolder.mBtnMenu.setOnClickListener(new OnMenuClickListener(viewHolder));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Vcard vcard = mData.get(position);
        viewHolder.mPosition = position;
        viewHolder.mTvName.setText(vcard.getName());
        viewHolder.mTvEmail.setText(vcard.getEmail());
        viewHolder.mTvPhone.setText(vcard.getPhone());

        Glide.with(mContext)
                .load(vcard.getImageFile())
                .signature(new StringSignature(String.valueOf(vcard.getTimestamp())))
                .error(R.drawable.ic_userpic)
                .into(viewHolder.mIvImage);
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
        TextView mTvPhone;
        ImageView mIvImage;
        View mBtnMenu;
    }
}
