package co.onlini.beacome.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import co.onlini.beacome.DataService;
import co.onlini.beacome.R;
import co.onlini.beacome.dal.AttachmentHelper;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.databinding.ActivityViewCardBinding;
import co.onlini.beacome.event.AttachmentLoadResult;
import co.onlini.beacome.event.RetrofitIoExceptionResult;
import co.onlini.beacome.event.SetHistoryFavoriteResult;
import co.onlini.beacome.model.Attachment;
import co.onlini.beacome.model.HistoryCardExtended;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.model.Vcard;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.ui.adapter.AttachmentListAdapter;
import co.onlini.beacome.ui.adapter.ContactViewListAdapter;
import co.onlini.beacome.ui.adapter.VCardListAdapter;
import co.onlini.beacome.ui.loader.HistoryCardAsyncLoader;
import co.onlini.beacome.util.FileUtil;
import co.onlini.beacome.util.MeasureUtil;
import co.onlini.beacome.util.ViewGroupUtil;

public class ViewCardActivity extends BaseActivity {

    public static final String EXTRA_HISTORY_CARD = "extra_history_card";
    private static final int LOADER_CARD = 0x1;
    private static final int REQUEST_PERMISSION_WRITE_CONTACTS = 0x1;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 0x2;
    private static final int REQUEST_CODE_EXPORT_VCARD_TO_CONTACTS = 0x32;
    private ActivityViewCardBinding mBinding;
    private VCardListAdapter mAdapter;

    private Vcard mVcardForSaving;
    private boolean mIsFavorite;
    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_up) {
                onBackPressed();
            } else if (v.getId() == R.id.btn_favorite) {
                HistoryCardExtended card = mBinding.getCard();
                if (card != null) {
                    mIsFavorite = !mIsFavorite;
                    final Session session = SessionManager.getSession(ViewCardActivity.this);
                    DataService.setHistoryFavorite(ViewCardActivity.this, session, card.getUuid(), mIsFavorite);
                    mBinding.setIsFavorite(mIsFavorite);
                }
            }
        }
    };
    private Uri mTmpFileUri;
    private OnItemActionClickListener<Integer> mVcardMenuClickListener = new OnItemActionClickListener<Integer>() {
        @Override
        public void onItemActionClick(final Integer item, View view) {
            PopupMenu popup = new PopupMenu(ViewCardActivity.this, view);
            popup.inflate(R.menu.popup_view_card_vcard);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_save) {
                        Vcard vcard = mAdapter.getItem(item);
                        saveVcardToContacts(vcard);
                    }
                    return false;
                }
            });
            popup.show();
        }
    };
    private Attachment mOpenAttachment;
    private OnItemActionClickListener<Attachment> mOnItemActionClickListener = new OnItemActionClickListener<Attachment>() {
        @Override
        public void onItemActionClick(final Attachment item, View view) {
            PopupMenu popup = new PopupMenu(ViewCardActivity.this, view);
            popup.inflate(R.menu.popup_view_card_attachment);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_open) {
                        openAttachment(item);
                    }
                    return false;
                }
            });
            popup.show();
        }
    };
    private String mCardUuid;
    private LoaderManager.LoaderCallbacks<HistoryCardExtended> mHistoryCardLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<HistoryCardExtended>() {
                @Override
                public Loader<HistoryCardExtended> onCreateLoader(int id, Bundle args) {
                    if (id == LOADER_CARD) {
                        return new HistoryCardAsyncLoader(ViewCardActivity.this, mCardUuid);
                    }
                    return null;
                }

                @Override
                public void onLoadFinished(Loader<HistoryCardExtended> loader, HistoryCardExtended data) {
                    setCard(data);
                    if (mOpenAttachment != null) {
                        for (Attachment attachment : data.getAttachments()) {
                            if (attachment.getUuid().equals(mOpenAttachment.getUuid())) {
                                if (attachment.getFileUri() != null) {
                                    mOpenAttachment = attachment;
                                    showAttachment(attachment);
                                }
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<HistoryCardExtended> loader) {

                }
            };

    public static Intent getViewIntent(Context context, String cardUuid) {
        Intent intent = new Intent(context, ViewCardActivity.class);
        intent.putExtra(EXTRA_HISTORY_CARD, cardUuid);
        return intent;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetHistoryFavoriteResult result) {
        if (!result.isSuccess()) {
            Toast.makeText(this, R.string.toast_favorite_unable, Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AttachmentLoadResult result) {
        hideProgress();
        if (result.isSuccess()) {
            getSupportLoaderManager().restartLoader(LOADER_CARD, null, mHistoryCardLoaderCallbacks);
        } else {
            mOpenAttachment = null;
            Toast.makeText(this, R.string.toast_unable_to_load_advertisement, Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RetrofitIoExceptionResult result) {
        hideProgress();
        Toast.makeText(this, R.string.toast_load_attachment_failure, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(ViewCardActivity.EXTRA_HISTORY_CARD)) {
            throw new IllegalArgumentException("No expected extras, cardUuid");
        }
        mCardUuid = extras.getString(EXTRA_HISTORY_CARD);
        getSupportLoaderManager().initLoader(LOADER_CARD, null, mHistoryCardLoaderCallbacks).forceLoad();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_card);
        mBinding.btnUp.setOnClickListener(mListener);
        mBinding.btnFavorite.setOnClickListener(mListener);
        mBinding.layContainer.setMinimumHeight(MeasureUtil.measureContentAreaHeight(this));
        mTmpFileUri = Uri.parse("file://" + getFileStreamPath("temp").getAbsolutePath());
    }

    @SuppressLint("WorldReadableFiles")
    private void saveVcardToContacts(Vcard item) {
        if (checkWriteContactsPermission()) {
            FileOutputStream fOut = null;
            try {
                //noinspection deprecation
                fOut = openFileOutput(mTmpFileUri.getLastPathSegment(), MODE_WORLD_READABLE);
                byte[] data = AttachmentHelper.getInstance(this).getVcardVcfFileContent(item.getVcfFile());
                fOut.write(data);
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fOut != null) {
                        fOut.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(mTmpFileUri, "text/x-vcard");
            startActivityForResult(i, REQUEST_CODE_EXPORT_VCARD_TO_CONTACTS);
        } else {
            mVcardForSaving = item;
            requestWriteContactPermission();
        }
    }

    private void requestWriteContactPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_PERMISSION_WRITE_CONTACTS);
    }

    private boolean checkWriteContactsPermission() {
        return PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PermissionChecker.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_WRITE_CONTACTS) {
            if (permissions.length == 1 && Manifest.permission.WRITE_CONTACTS.equals(permissions[0]) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mVcardForSaving != null) {
                    saveVcardToContacts(mVcardForSaving);
                }
            }
        } else if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (mOpenAttachment != null) {
                openAttachment(mOpenAttachment);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXPORT_VCARD_TO_CONTACTS) {
            File file = new File(mTmpFileUri.getLastPathSegment());
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    private void setCard(HistoryCardExtended card) {
        mIsFavorite = card.isFavorite();
        mBinding.setIsFavorite(mIsFavorite);
        mBinding.setCard(card);
        ViewGroupUtil.fillList(mBinding.lvContacts, new ContactViewListAdapter(this, card.getContacts()));

        boolean hasVcards = card.getVcards().length > 0;
        mBinding.tvVcard.setVisibility(hasVcards ? View.VISIBLE : View.INVISIBLE);
        mBinding.titleDividerPeople.setVisibility(hasVcards ? View.VISIBLE : View.INVISIBLE);
        mAdapter = new VCardListAdapter(this, Arrays.asList(card.getVcards()), true);
        mAdapter.setOnItemActionClickListener(mVcardMenuClickListener);

        ViewGroupUtil.fillList(mBinding.lvVcards, mAdapter);

        boolean hasAttachments = card.getAttachments().length > 0;
        mBinding.tvAttachments.setVisibility(hasAttachments ? View.VISIBLE : View.INVISIBLE);
        mBinding.titleDividerAttachments.setVisibility(hasAttachments ? View.VISIBLE : View.INVISIBLE);
        AttachmentListAdapter mAttachmentAdapter = new AttachmentListAdapter(Arrays.asList(card.getAttachments()), true);
        mAttachmentAdapter.setOnItemActionClickListener(mOnItemActionClickListener);
        ViewGroupUtil.fillList(mBinding.lvAttachments, mAttachmentAdapter);
        Glide.with(this)
                .load(card.getImageUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new StringSignature(String.valueOf(card.getCardVersion())))
                .into(mBinding.ivImage);

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
        if (PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PermissionChecker.PERMISSION_GRANTED) {
            Session session = SessionManager.getSession(this);
            showProgress(getString(R.string.dialog_progress_loading_attachment));
            DataService.loadAttachment(this, session, attachment.getUuid());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void showAttachment(Attachment attachment) {
        if (PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PermissionChecker.PERMISSION_GRANTED) {
            mOpenAttachment = null;
            Intent intent = FileUtil.getOpenAttachmentIntent(attachment);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.toast_no_handler_for_file_type, Toast.LENGTH_LONG).show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

}
