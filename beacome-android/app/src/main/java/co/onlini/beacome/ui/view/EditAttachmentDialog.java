package co.onlini.beacome.ui.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import co.onlini.beacome.R;
import co.onlini.beacome.web.Conventions;

public class EditAttachmentDialog {

    public static void showShareDialog(Activity activity, String description, int type, final EditAttachmentDialogOkClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialog);
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.dialog_edit_attachment, null);
        final EditText etDescription = ((EditText) dialogContent.findViewById(R.id.et_description));
        etDescription.setText(description);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final String attachment = activity.getString(R.string.document);
        final String discount = activity.getString(R.string.discount);
        adapter.addAll(attachment, discount);
        final Spinner spinner = ((Spinner) dialogContent.findViewById(R.id.sp_attachment_type));
        spinner.setAdapter(adapter);
        spinner.setSelection(type == Conventions.ATTACHMENT_TYPE_DOCUMENT ? 0 : 1);
        final AlertDialog shareDialog = builder.setTitle(activity.getString(R.string.dialog_add_attachment_title))
                .setView(dialogContent)
                .setPositiveButton(activity.getString(R.string.btn_ok), null)
                .setNegativeButton(activity.getString(R.string.btn_cancel), null)
                .create();

        shareDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                shareDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String description = etDescription.getText().toString();
                        if (!TextUtils.isEmpty(description)) {
                            int type = Conventions.ATTACHMENT_TYPE_DOCUMENT;
                            if (spinner.getSelectedItemPosition() == 1) {
                                type = Conventions.ATTACHMENT_TYPE_DISCOUNT;
                            }
                            listener.onOkClickListener(type, description);
                            dialog.dismiss();
                        } else {
                            etDescription.setError(v.getContext().getString(R.string.et_error_empty));
                        }
                    }
                });
                shareDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                listener.onCancelClickListener();
                                dialog.dismiss();
                            }
                        }
                );
                etDescription.requestFocus();
            }
        });
        shareDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        shareDialog.show();
    }

    public interface EditAttachmentDialogOkClickListener {
        void onOkClickListener(int type, String description);

        void onCancelClickListener();
    }
}
