package co.onlini.beacome.ui.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.onlini.beacome.R;
import co.onlini.beacome.model.Contact;
import co.onlini.beacome.util.ContactUtil;

public class ContactViewListAdapter extends BaseAdapter {

    private List<Contact> mData;
    private Context mContext;

    public ContactViewListAdapter(Context context, Contact[] contacts) {
        mContext = context;
        List<Contact> contactList = new ArrayList<>(Arrays.asList(contacts));
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getContactType() - rhs.getContactType();
            }
        });
        mData = contactList;
    }

    private static void skype(final String number, final Context ctx) {
        new AlertDialog.Builder(ctx).setTitle(String.format(ctx.getString(R.string.dialog_skype_to), number))
                .setPositiveButton(ctx.getString(R.string.btn_call), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent skype = new Intent(Intent.ACTION_VIEW);
                            skype.setData(Uri.parse("skype:" + number));
                            ctx.startActivity(skype);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(ctx, R.string.toast_skype_app_not_found, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(ctx.getString(R.string.btn_dismiss), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Contact getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_contact_view, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTvType = (TextView) convertView.findViewById(R.id.tv_type);
            viewHolder.mTvValue = (TextView) convertView.findViewById(R.id.tv_contact_value);
            convertView.setTag(viewHolder);
            convertView.setOnClickListener(null);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Contact contact = getItem(position);
        final String value = contact.getData();
        viewHolder.mTvValue.setOnClickListener(null);
        viewHolder.mTvType.setText(ContactUtil.getContactTypeName(parent.getContext(), contact.getContactType()));
        int type = contact.getContactType();
        if (type < 200) {
            viewHolder.mTvValue.setAutoLinkMask(Linkify.PHONE_NUMBERS);
            SpannableString string = new SpannableString(value);
            string.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    showDialer(((TextView) widget).getText().toString());
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.mTvValue.setText(string);
            viewHolder.mTvValue.setMovementMethod(LinkMovementMethod.getInstance());
        } else if (type >= 300) {
            viewHolder.mTvValue.setAutoLinkMask(Linkify.ALL);
            if (type == 302) {
                SpannableString string = new SpannableString(value);
                string.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        //do nothing
                    }
                }, 0, string.length(), 0);
                viewHolder.mTvValue.setText(string);
                viewHolder.mTvValue.setClickable(true);
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skype(value, v.getContext());
                    }
                };
                viewHolder.mTvValue.setOnClickListener(onClickListener);
            } else {
                viewHolder.mTvValue.setText(Html.fromHtml(value));
            }
        } else {
            viewHolder.mTvValue.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
            viewHolder.mTvValue.setText(value);
        }
        viewHolder.mTvValue.invalidate();
        return convertView;
    }

    private void showDialer(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        mContext.startActivity(intent);
    }

    private class ViewHolder {
        TextView mTvType;
        TextView mTvValue;
    }
}
