package co.onlini.beacome.ui.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import co.onlini.beacome.R;
import co.onlini.beacome.model.Contact;
import co.onlini.beacome.model.ContactType;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.util.ContactUtil;

public class ContactEditListAdapter extends BaseAdapter {

    public static final int PHONES = 0x1;
    public static final int EMAILS = 0x2;
    public static final int URLS = 0x3;

    private List<Contact> mData;
    private List<ContactType> mTypesList;
    private String hint;
    private OnItemActionClickListener<Contact> mOnItemClickListener;
    private int mType;

    public ContactEditListAdapter(Context context, List<Contact> contacts, int type) {
        if (type >= 4 && type < 0) {
            throw new IllegalArgumentException("Invalid list type");
        }
        mData = contacts;
        mType = type;
        mTypesList = new ArrayList<>();
        switch (type) {
            case URLS:
                mTypesList.add(ContactUtil.getContactType(context, 301));
                mTypesList.add(ContactUtil.getContactType(context, 302));
                hint = context.getString(R.string.item_edit_card_contact_hint_url);
                break;
            case EMAILS:
                mTypesList.add(ContactUtil.getContactType(context, 201));
                mTypesList.add(ContactUtil.getContactType(context, 202));
                mTypesList.add(ContactUtil.getContactType(context, 203));
                hint = context.getString(R.string.item_edit_card_contact_hint_email);
                break;
            case PHONES:
            default:
                mTypesList.add(ContactUtil.getContactType(context, 101));
                mTypesList.add(ContactUtil.getContactType(context, 102));
                mTypesList.add(ContactUtil.getContactType(context, 103));
                mTypesList.add(ContactUtil.getContactType(context, 104));
                mTypesList.add(ContactUtil.getContactType(context, 105));
                mTypesList.add(ContactUtil.getContactType(context, 106));
                hint = context.getString(R.string.item_edit_card_contact_hint_phone);
        }
    }

    public void setOnItemDeleteClickListener(OnItemActionClickListener<Contact> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_contact_edit, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mSpType = (Spinner) convertView.findViewById(R.id.sp_contact_type);
            viewHolder.mEtValue = (EditText) convertView.findViewById(R.id.et_contact_value);
            convertView.setTag(viewHolder);
            viewHolder.mIvDelete = convertView.findViewById(R.id.ib_delete);
            if (mType == PHONES) {
                viewHolder.mEtValue.setInputType(InputType.TYPE_CLASS_PHONE);
            } else if (mType == EMAILS) {
                viewHolder.mEtValue.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else {
                viewHolder.mEtValue.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
            ArrayAdapter<ContactType> adapter = new ArrayAdapter<>(parent.getContext(), R.layout.spinner_item, mTypesList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            viewHolder.mSpType.setAdapter(adapter);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Contact contact = mData.get(position);

        viewHolder.mIvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemActionClick(contact, v);
                }
            }
        });
        viewHolder.mSpType.setOnItemSelectedListener(new SpinnerItemSelectListener(position));
        viewHolder.mSpType.setSelection(mTypesList.indexOf(new ContactType(contact.getContactType(),
                ContactUtil.getContactTypeName(parent.getContext(), contact.getContactType()))));
        viewHolder.mEtValue.setHint(hint);
        viewHolder.mEtValue.setText(contact.getData());
        viewHolder.mEtValue.addTextChangedListener(new TextWatcher(position));

        return convertView;
    }

    private class ViewHolder {
        Spinner mSpType;
        EditText mEtValue;
        View mIvDelete;
    }

    private class SpinnerItemSelectListener implements AdapterView.OnItemSelectedListener {

        private int mPosition;

        public SpinnerItemSelectListener(int position) {
            mPosition = position;
        }


        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ContactType type = (ContactType) parent.getAdapter().getItem(position);
            mData.get(mPosition).setContactType(type.getCode());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class TextWatcher implements android.text.TextWatcher {

        private int mPosition;

        public TextWatcher(int position) {
            mPosition = position;
        }

        public int getPosition() {
            return mPosition;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mData.get(mPosition).setData(s.toString());
        }
    }

}
