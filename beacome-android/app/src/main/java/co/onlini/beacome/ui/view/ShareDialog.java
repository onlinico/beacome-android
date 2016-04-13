package co.onlini.beacome.ui.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import co.onlini.beacome.R;
import co.onlini.beacome.util.InputDataValidation;

public class ShareDialog {
    public static void showShareDialog(Activity activity, final ShareDialogConfirmClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialog);
        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.dialog_share_to, null);
        final EditText etEmail = ((EditText) dialogContent.findViewById(R.id.et_email));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        String translator = activity.getString(R.string.translator);
        final String owner = activity.getString(R.string.owner);
        adapter.addAll(translator, owner);
        final Spinner spinner = ((Spinner) dialogContent.findViewById(R.id.sp_permissions));
        spinner.setAdapter(adapter);

        final AlertDialog shareDialog = builder.setTitle(activity.getString(R.string.dialog_share_to_title))
                .setView(dialogContent)
                .setPositiveButton(activity.getString(R.string.dialog_share_to_positive_btn), null)
                .setNegativeButton(activity.getString(R.string.dialog_share_to_negative_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        final String invalidEmail = activity.getString(R.string.et_error_invalid_email);
        shareDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                shareDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = etEmail.getText().toString();
                        if (!TextUtils.isEmpty(email) && InputDataValidation.isEmailValid(email)) {
                            String selectedItem = spinner.getSelectedItem() != null ? (String) spinner.getSelectedItem() : null;
                            boolean isOwner = false;
                            if (selectedItem != null) {
                                isOwner = owner.equals(selectedItem);
                            }
                            listener.onConfirmClickListener(email, isOwner);
                            dialog.dismiss();
                        } else {
                            etEmail.setError(invalidEmail);
                        }
                    }
                });
            }
        });
        shareDialog.show();
    }

    public interface ShareDialogConfirmClickListener {
        void onConfirmClickListener(String email, boolean isOwner);
    }
}
