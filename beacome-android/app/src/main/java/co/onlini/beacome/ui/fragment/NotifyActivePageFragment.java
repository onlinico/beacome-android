package co.onlini.beacome.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import co.onlini.beacome.R;
import co.onlini.beacome.ui.NavigationActivity;

public abstract class NotifyActivePageFragment extends Fragment {
    private ProgressDialog mProgressDialog;
    private NavigationActivity mNavigationActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof NavigationActivity) {
            mNavigationActivity = (NavigationActivity) getActivity();
        }
    }

    public void setTopFragment(String fragmentTag, int page) {
        if (mNavigationActivity != null) {
            mNavigationActivity.onStartFragment(fragmentTag, page);
        }
    }

    protected void showProgress(@NonNull String message) {
        mProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.dialog_progress_title), message, true, false);
    }

    protected void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}