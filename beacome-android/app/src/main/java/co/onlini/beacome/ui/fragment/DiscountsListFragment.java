package co.onlini.beacome.ui.fragment;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import co.onlini.beacome.DataService;
import co.onlini.beacome.R;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.event.AttachmentLoadResult;
import co.onlini.beacome.event.RetrofitIoExceptionResult;
import co.onlini.beacome.model.Attachment;
import co.onlini.beacome.model.DiscountItem;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.ui.OnItemClickListener;
import co.onlini.beacome.ui.activity.MainActivity;
import co.onlini.beacome.ui.adapter.DiscountsRecyclerViewAdapter;
import co.onlini.beacome.ui.loader.DiscountsAsyncLoader;
import co.onlini.beacome.util.FileUtil;

public class DiscountsListFragment extends NotifyActivePageFragment {

    private static final int LOADER_DISCOUNTS = 0x1;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 0x19;
    private DiscountsRecyclerViewAdapter mAdapter;
    private Attachment mOpenAttachment;
    private LoaderManager.LoaderCallbacks mLoaderCallbacks = new LoaderManager.LoaderCallbacks() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            return new DiscountsAsyncLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {
            if (loader.getId() == LOADER_DISCOUNTS) {
                List<DiscountItem> discounts = (List) data;
                mAdapter.setData(discounts);
                mAdapter.notifyDataSetChanged();
                if (mOpenAttachment != null) {
                    for (DiscountItem item : discounts) {
                        if (item.getAttachmentUuid().equals(mOpenAttachment.getUuid())) {
                            if (item.getLocalFileUri() != null) {
                                mOpenAttachment = new Attachment(item.getAttachmentUuid(), 0, item.getMimeType(), item.getDescription(), item.getLocalFileUri(), item.getAttachmentUri());
                                showAttachment(mOpenAttachment);
                            }
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };
    private OnItemClickListener<DiscountItem> mListener = new OnItemClickListener<DiscountItem>() {
        @Override
        public void onItemClick(DiscountItem item, View view) {
            openAttachment(new Attachment(item.getAttachmentUuid(), 0, item.getMimeType(), item.getDescription(), item.getLocalFileUri(), item.getAttachmentUri()));
        }
    };

    public DiscountsListFragment() {
        // Required empty public constructor
    }

    public static DiscountsListFragment newInstance() {
        return new DiscountsListFragment();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (mOpenAttachment != null) {
                openAttachment(mOpenAttachment);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_DISCOUNTS, null, mLoaderCallbacks);
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        setTopFragment(MainActivity.FRAGMENT_DISCOUNTS, 0);
        getLoaderManager().getLoader(LOADER_DISCOUNTS).forceLoad();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_discounts));
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AttachmentLoadResult result) {
        hideProgress();
        if (result.isSuccess()) {
            getLoaderManager().getLoader(LOADER_DISCOUNTS).forceLoad();
        } else {
            mOpenAttachment = null;
            Toast.makeText(getContext(), R.string.toast_unable_to_load_advertisement, Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RetrofitIoExceptionResult result) {
        hideProgress();
        Toast.makeText(getContext(), R.string.toast_load_attachment_failure, Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beacons, container, false);
        RecyclerView rvBeacons = (RecyclerView) view.findViewById(R.id.rv_beacons);
        rvBeacons.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new DiscountsRecyclerViewAdapter(getContext());
        rvBeacons.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(mListener);
        return view;
    }

    private void openAttachment(Attachment attachment) {
        mOpenAttachment = attachment;
        if (FileUtil.isAttachmentFileExists(attachment)) {
            showAttachment(attachment);
        } else {
            loadAttachment(attachment);
        }
    }

    private void loadAttachment(Attachment attachment) {
        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PermissionChecker.PERMISSION_GRANTED) {
            Session session = SessionManager.getSession(getContext());
            showProgress(getString(R.string.dialog_progress_loading_attachment));
            DataService.loadAttachment(getContext(), session, attachment.getUuid());
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void showAttachment(Attachment attachment) {
        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PermissionChecker.PERMISSION_GRANTED) {
            mOpenAttachment = null;
            Intent intent = FileUtil.getOpenAttachmentIntent(attachment);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

}