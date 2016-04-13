package co.onlini.beacome.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import co.onlini.beacome.R;
import co.onlini.beacome.databinding.ItemAttachmentBinding;
import co.onlini.beacome.model.Attachment;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.web.Conventions;

public class AttachmentListAdapter extends BaseAdapter {

    private List<Attachment> mData;
    private OnItemActionClickListener<Attachment> mOnItemActionClickListener;
    private boolean mIsActionMenuBtnVisible;

    public AttachmentListAdapter(List<Attachment> attachments, boolean showActionMenuBtn) {
        mData = attachments;
        mIsActionMenuBtnVisible = showActionMenuBtn;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Attachment getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemAttachmentBinding binding = ItemAttachmentBinding.inflate(inflater, parent, false);
            viewHolder = new ViewHolder(binding);
            convertView = binding.getRoot();
            viewHolder.mBinding.ivAction.setVisibility(mIsActionMenuBtnVisible ? View.VISIBLE : View.INVISIBLE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Attachment attachment = getItem(position);
        viewHolder.mBinding.setItem(attachment);
        viewHolder.mBinding.ivLogo.setImageResource(attachment.getType() == Conventions.ATTACHMENT_TYPE_DOCUMENT ?
                R.drawable.ic_attachment : R.drawable.ic_discount);
        return convertView;
    }

    public void setOnItemActionClickListener(OnItemActionClickListener<Attachment> onItemActionClickListener) {
        mOnItemActionClickListener = onItemActionClickListener;
    }

    private class ViewHolder {
        public ItemAttachmentBinding mBinding;

        public ViewHolder(ItemAttachmentBinding binding) {
            mBinding = binding;
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
