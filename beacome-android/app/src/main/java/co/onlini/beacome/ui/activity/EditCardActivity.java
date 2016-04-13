package co.onlini.beacome.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import co.onlini.beacome.DataService;
import co.onlini.beacome.R;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.dal.UserHelper;
import co.onlini.beacome.databinding.ActivityEditCardBinding;
import co.onlini.beacome.event.LinkBeaconsException;
import co.onlini.beacome.event.PullUserCardsResult;
import co.onlini.beacome.event.RetrofitIoExceptionResult;
import co.onlini.beacome.event.SaveCardException;
import co.onlini.beacome.model.Attachment;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.model.CardExtended;
import co.onlini.beacome.model.CardLink;
import co.onlini.beacome.model.CardUser;
import co.onlini.beacome.model.Contact;
import co.onlini.beacome.model.ContactType;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.model.User;
import co.onlini.beacome.model.Vcard;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.ui.OnListItemMenuItemClickListener;
import co.onlini.beacome.ui.adapter.AttachmentListAdapter;
import co.onlini.beacome.ui.adapter.BeaconEditListAdapter;
import co.onlini.beacome.ui.adapter.ContactEditListAdapter;
import co.onlini.beacome.ui.adapter.ContactViewListAdapter;
import co.onlini.beacome.ui.adapter.UserListAdapter;
import co.onlini.beacome.ui.adapter.VCardListAdapter;
import co.onlini.beacome.ui.loader.CardAsyncLoader;
import co.onlini.beacome.ui.view.EditAttachmentDialog;
import co.onlini.beacome.ui.view.ShareDialog;
import co.onlini.beacome.util.ContactUtil;
import co.onlini.beacome.util.ContentProviderUtil;
import co.onlini.beacome.util.FileUtil;
import co.onlini.beacome.util.InputDataValidation;
import co.onlini.beacome.util.MeasureUtil;
import co.onlini.beacome.util.ViewGroupUtil;
import co.onlini.beacome.util.comparator.CardUserComparator;
import co.onlini.beacome.util.comparator.VcardComparator;
import co.onlini.beacome.web.Conventions;

public class EditCardActivity extends BaseActivity {

    public static final String EXTRA_CARD_UUID = "co.onlini.beacome.extra_edit_card";
    private static final int REQUEST_PICK_IMAGE = 0x1;
    private static final int REQUEST_EDIT_VCARD = 0x2;
    private static final int REQUEST_GET_CONTACT = 0x4;
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0x8;
    private static final String SAVED_STATE_HAS_CHANGES = "saved_state_has_changes";
    private static final String SAVED_STATE_VCARDS = "saved_state_vcard";
    private static final String SAVED_STATE_ATTACHMENTS = "saved_state_attachments";
    private static final String SAVED_STATE_CONTACTS = "saved_state_contacts";
    private static final String SAVED_STATE_BEACONS = "saved_state_beacons";
    private static final String SAVED_STATE_USERS = "saved_state_users";
    private static final String SAVED_STATE_IMAGE = "saved_state_image";
    private static final String SAVED_STATE_UUID = "saved_state_uuid";
    private static final String SAVED_STATE_TITLE = "saved_state_title";
    private static final String SAVED_STATE_DESCRIPTION = "saved_state_description";
    private static final String SAVED_STATE_VERSION = "saved_state_version";
    private static final int LOADER_CARD = 0x1;
    private static final int REQUEST_PICK_ATTACHMENT = 0x64;
    private static final int REQUEST_LINK_BEACONS = 0x16;
    private static final String LOADER_CARD_ARGS = "loader_cards_arg";
    private ActivityEditCardBinding mBinding;
    private boolean mHasChanges;
    private ArrayList<Vcard> mVcards;
    private ArrayList<Contact> mContacts;
    private ArrayList<Beacon> mBeacons;
    private ArrayList<CardUser> mCardUsers;
    private ArrayList<Attachment> mAttachments;
    private Uri mImage;
    private String mCardUuid;
    private String mTitle;
    private String mDescription;
    private long mVersion;
    private CardUser mCurrentUser;
    private OnItemActionClickListener<Attachment> mOnItemActionClickListener = new OnItemActionClickListener<Attachment>() {
        @Override
        public void onItemActionClick(final Attachment attachment, View view) {
            PopupMenu popup = new PopupMenu(EditCardActivity.this, view);
            popup.inflate(R.menu.popup_attachment);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_item_edit:
                            editAttachment(attachment);
                            break;
                        case R.id.menu_item_delete:
                            mAttachments.remove(attachment);
                            setAttachments(mAttachments);
                            Snackbar.make(mBinding.getRoot(), R.string.snackbar_deleting, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            addAttachment(attachment);
                                        }
                                    }).show();
                            break;
                    }
                    return true;
                }
            });
            popup.show();
        }
    };
    private OnItemActionClickListener<Contact> mOnContactActionClickListener = new OnItemActionClickListener<Contact>() {
        @Override
        public void onItemActionClick(Contact item, View v) {
            removeContact(item);
        }
    };
    private AlertDialog mDialogAddContact;
    private VCardListAdapter mVCardListAdapter;
    private View.OnClickListener mOnDialogAddVcardActionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.action_form:
                    showEditVcardActivity(null);
                    break;
                case R.id.action_import:
                    showChooseContactActivity();
                    break;
            }
            if (mDialogAddContact != null && mDialogAddContact.isShowing()) {
                mDialogAddContact.dismiss();
            }
        }
    };
    private BeaconEditListAdapter mBeaconEditListAdapter;
    private UserListAdapter mUsersListAdapter;
    private TextWatcher mTitleWatcher = new co.onlini.beacome.util.TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            mHasChanges = true;
            mTitle = s.toString();
        }
    };
    private TextWatcher mDescriptionWatcher = new co.onlini.beacome.util.TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            mHasChanges = true;
            mDescription = s.toString();
        }
    };

    private OnItemActionClickListener<Integer> mOnItemUserActionClickListener =
            new OnItemActionClickListener<Integer>() {
                @Override
                public void onItemActionClick(Integer item, View view) {
                    final CardUser cardUser = mUsersListAdapter.getItem(item);
                    PopupMenu popup = new PopupMenu(EditCardActivity.this, view);
                    popup.inflate(R.menu.popup_user);
                    boolean canEdit = false;
                    if (mCurrentUser.isOwner()) {
                        canEdit = true;
                    } else {
                        if (cardUser.getShareUuid() == null) {
                            canEdit = true;
                        }
                    }
                    final boolean isCardUserOwner = cardUser.isOwner();
                    popup.getMenu().findItem(R.id.menu_item_user_remove).setEnabled(canEdit);
                    popup.getMenu().findItem(R.id.menu_item_user_make_translator).setVisible(isCardUserOwner);
                    popup.getMenu().findItem(R.id.menu_item_user_make_owner).setVisible(!isCardUserOwner);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_item_user_remove:
                                    removeUser(cardUser);
                                    break;
                                case R.id.menu_item_user_make_owner:
                                    changeUserPermission(cardUser, true);
                                    break;
                                case R.id.menu_item_user_make_translator:
                                    changeUserPermission(cardUser, false);
                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show();
                }
            };
    private LoaderManager.LoaderCallbacks<CardExtended> mCardLoaderCallbacks = new LoaderManager.LoaderCallbacks<CardExtended>() {
        @Override
        public Loader<CardExtended> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_CARD && args != null && args.containsKey(LOADER_CARD_ARGS)) {
                return new CardAsyncLoader(EditCardActivity.this, args.getString(LOADER_CARD_ARGS));
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<CardExtended> loader, CardExtended data) {
            if (data != null) {
                setCard(data);
                getLoaderManager().destroyLoader(LOADER_CARD);
            } else {
                Toast.makeText(EditCardActivity.this, R.string.toast_unable_load_card, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<CardExtended> loader) {

        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_save:
                    saveCard();
                    break;
                default:
                    onBackPressed();
            }
            return true;
        }
    };
    private AlertDialog mLinkException;
    private View.OnClickListener mOnActionsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_choose_image:
                    showImagePicker();
                    break;
                case R.id.btn_add_phone:
                    addContact(101);
                    break;
                case R.id.btn_add_email:
                    addContact(201);
                    break;
                case R.id.btn_add_url:
                    addContact(301);
                    break;
                case R.id.btn_add_vcard:
                    showAddVCardDialog();
                    break;
                case R.id.btn_link:
                    showLinkBeaconActivity();
                    break;
                case R.id.btn_add_user:
                    showShareDialog();
                    break;
                case R.id.btn_add_attachment:
                    showPickFile();
                    break;
            }
        }
    };


    public static Intent getEditCardIntent(Context context, String cardUuid) {
        Intent intent = new Intent(context, EditCardActivity.class);
        if (cardUuid != null) {
            intent.putExtra(EXTRA_CARD_UUID, cardUuid);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_card);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
        }
        mBinding.layContainer.setMinimumHeight(MeasureUtil.measureContentAreaHeight(this));
        mBinding.etTitle.addTextChangedListener(mTitleWatcher);
        mBinding.etDescription.addTextChangedListener(mDescriptionWatcher);
        mBinding.btnAddAttachment.setOnClickListener(mOnActionsClickListener);
        mBinding.btnAddEmail.setOnClickListener(mOnActionsClickListener);
        mBinding.btnAddPhone.setOnClickListener(mOnActionsClickListener);
        mBinding.btnAddUrl.setOnClickListener(mOnActionsClickListener);
        mBinding.btnAddUser.setOnClickListener(mOnActionsClickListener);
        mBinding.btnAddVcard.setOnClickListener(mOnActionsClickListener);
        mBinding.btnChooseImage.setOnClickListener(mOnActionsClickListener);
        mBinding.btnLink.setOnClickListener(mOnActionsClickListener);

        if (savedInstanceState != null) {
            setCardUuid(savedInstanceState.getString(SAVED_STATE_UUID));
            ArrayList<CardUser> users = savedInstanceState.getParcelableArrayList(SAVED_STATE_USERS);
            applyCurrentCardUser(users);
            setUsers(users);
            mHasChanges = savedInstanceState.getBoolean(SAVED_STATE_HAS_CHANGES);
            ArrayList<Beacon> beaconLinks = savedInstanceState.getParcelableArrayList(SAVED_STATE_BEACONS);
            setBeacons(beaconLinks);
            ArrayList<Contact> contacts = savedInstanceState.getParcelableArrayList(SAVED_STATE_CONTACTS);
            setContacts(contacts);
            ArrayList<Vcard> vcards = savedInstanceState.getParcelableArrayList(SAVED_STATE_VCARDS);
            setVcards(vcards);
            Uri image = savedInstanceState.getParcelable(SAVED_STATE_IMAGE);
            long version = savedInstanceState.getParcelable(SAVED_STATE_VERSION);
            setImage(version, image);
            setCardTitle(savedInstanceState.getString(SAVED_STATE_TITLE));
            setCardDescription(savedInstanceState.getString(SAVED_STATE_DESCRIPTION));
            ArrayList<Attachment> attachments = savedInstanceState.getParcelableArrayList(SAVED_STATE_ATTACHMENTS);
            setAttachments(attachments);
        } else {
            if (extras == null || !extras.containsKey(EditCardActivity.EXTRA_CARD_UUID)) {
                CardExtended card = getBlankCard();
                setCard(card);
            } else {
                String cardUuid = extras.getString(EXTRA_CARD_UUID);
                Bundle args = new Bundle();
                args.putString(LOADER_CARD_ARGS, cardUuid);
                getSupportLoaderManager().initLoader(LOADER_CARD, args, mCardLoaderCallbacks).forceLoad();
            }
        }
    }

    private CardExtended getBlankCard() {
        User user = UserHelper.getInstance(this).getUser(SessionManager.getSession(this).getUserUuid());
        CardUser cardUser = new CardUser(user, true);
        String uuid = UUID.randomUUID().toString();
        return new CardExtended(uuid, null, null, 0, null, new Contact[0],
                new Vcard[0], new Attachment[0], new CardUser[]{cardUser}, new Beacon[0]);
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
    public void onEvent(RetrofitIoExceptionResult exception) {
        hideProgress();
        Toast.makeText(this, R.string.toast_no_internet_connection, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SaveCardException exception) {
        hideProgress();
        Toast.makeText(EditCardActivity.this, R.string.toast_save_card_unable_to_save, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PullUserCardsResult result) {
        hideProgress();
        Toast.makeText(EditCardActivity.this, R.string.toast_save_success, Toast.LENGTH_SHORT).show();
        if (mLinkException == null || !mLinkException.isShowing()) {
            Bundle args = new Bundle();
            args.putString(LOADER_CARD_ARGS, mCardUuid);
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LinkBeaconsException result) {
        hideProgress();
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.dialog_unable_to_link_beacons));
        for (String uuids : result.getUnprocessedBeaconsUuids()) {
            sb.append(uuids);
            sb.append("\n");
        }
        mLinkException = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putString(LOADER_CARD_ARGS, mCardUuid);
                        finish();
                    }
                })
                .setCancelable(false)
                .setTitle(getString(R.string.dialog_unable_to_link_beacons_title))
                .setMessage(sb.toString()).create();
        mLinkException.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_action_save, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_save).setOnMenuItemClickListener(mMenuItemClickListener);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_UUID, mCardUuid);
        outState.putString(SAVED_STATE_TITLE, mTitle);
        outState.putString(SAVED_STATE_DESCRIPTION, mDescription);
        outState.putBoolean(SAVED_STATE_HAS_CHANGES, mHasChanges);
        outState.putParcelableArrayList(SAVED_STATE_BEACONS, mBeacons);
        outState.putParcelableArrayList(SAVED_STATE_CONTACTS, mContacts);
        outState.putParcelableArrayList(SAVED_STATE_USERS, mCardUsers);
        outState.putParcelableArrayList(SAVED_STATE_VCARDS, mVcards);
        outState.putParcelableArrayList(SAVED_STATE_ATTACHMENTS, mAttachments);
        outState.putParcelable(SAVED_STATE_IMAGE, mImage);
        outState.putLong(SAVED_STATE_IMAGE, mVersion);
    }

    private void setCard(CardExtended card) {
        if (card == null) {
            finish();
            return;
        }
        setCardUuid(card.getUuid());
        mVersion = card.getVersion();
        ArrayList<CardUser> users = new ArrayList<>(Arrays.asList(card.getUsers()));
        applyCurrentCardUser(users);
        setUsers(users);
        setCardTitle(card.getTitle());
        setCardDescription(card.getDescription());
        setContacts(new ArrayList<>(Arrays.asList(card.getContacts())));
        setVcards(new ArrayList<>(Arrays.asList(card.getVcards())));
        setBeacons(new ArrayList<>(Arrays.asList(card.getBeaconLinks())));
        setImage(card.getVersion(), card.getImage());
        setAttachments(new ArrayList<>(Arrays.asList(card.getAttachments())));
    }

    private void setBeacons(ArrayList<Beacon> beacons) {
        mHasChanges = true;
        mBeacons = beacons;
        mBeaconEditListAdapter = new BeaconEditListAdapter(this, mBeacons, mCardUuid);
        mBeaconEditListAdapter.setOnMenuItemClickListener(new OnListItemMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int menuItemId, int pos) {
                Beacon beacon = mBeaconEditListAdapter.getItem(pos);
                switch (menuItemId) {
                    case R.id.menu_item_card_beacon_change_state:
                        boolean isActive = mBeaconEditListAdapter.isActive(pos);
                        CardLink[] cardLinks = beacon.getCardLinks();
                        for (CardLink cardLink : cardLinks) {
                            if (cardLink.getCardUuid().equals(mCardUuid)) {
                                cardLink.setIsActive(!isActive);
                            }
                        }
                        break;
                    case R.id.menu_item_card_beacon_unlink:
                        mBeacons.remove(beacon);
                        break;
                }
                setBeacons(mBeacons);
            }
        });
        ViewGroupUtil.fillList(mBinding.lvBeacons, mBeaconEditListAdapter);
    }

    private void setVcards(ArrayList<Vcard> vcards) {
        mHasChanges = true;
        mVcards = vcards;
        Collections.sort(mVcards, new VcardComparator(VcardComparator.COMPARE_BY_TIMESTAMP));
        mVCardListAdapter = new VCardListAdapter(this, mVcards, mCurrentUser.isOwner());
        mVCardListAdapter.setOnItemActionClickListener(new OnItemActionClickListener<Integer>() {
            @Override
            public void onItemActionClick(final Integer item, View view) {
                PopupMenu popup = new PopupMenu(EditCardActivity.this, view);
                popup.inflate(R.menu.popup_attachment);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_item_edit:
                                Vcard vcard = mVCardListAdapter.getItem(item);
                                showEditVcardActivity(vcard);
                                break;
                            case R.id.menu_item_delete:
                                Vcard vcardDel = mVCardListAdapter.getItem(item);
                                deleteVCard(vcardDel);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        ViewGroupUtil.fillList(mBinding.lvVcards, mVCardListAdapter);
    }

    private boolean checkContacts() {
        return checkEmailsIsValid(mBinding.lvEmails)
                & checkIsUrlValid(mBinding.lvUrls)
                && checkPhonesIsValid(mBinding.lvPhones);
    }

    private boolean checkEmailsIsValid(ViewGroup listView) {
        boolean res = true;
        int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = listView.getChildAt(i);
            EditText et = (EditText) v.findViewById(R.id.et_contact_value);
            String text = et.getText().toString();
            if (TextUtils.isEmpty(text)) {
                et.setError(getString(R.string.et_error_empty));
                et.requestFocus();
                res = false;
            } else if (!InputDataValidation.isEmailValid(text)) {
                et.setError(getString(R.string.et_error_invalid_email));
                et.requestFocus();
                res = false;
                break;
            }
        }
        return res;
    }

    private boolean checkPhonesIsValid(ViewGroup listView) {
        boolean res = true;
        int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = listView.getChildAt(i);
            EditText et = (EditText) v.findViewById(R.id.et_contact_value);
            String text = et.getText().toString();
            if (TextUtils.isEmpty(text)) {
                et.setError(getString(R.string.et_error_empty));
                et.requestFocus();
                res = false;
            } else if (!InputDataValidation.isPhoneValid(text)) {
                et.setError(getString(R.string.et_error_invalid_phone));
                et.requestFocus();
                res = false;
                break;
            }
        }
        return res;
    }

    private boolean checkIsUrlValid(ViewGroup listView) {
        boolean res = true;
        int childCount = listView.getChildCount();
        int websiteCode = 301;
        for (int i = 0; i < childCount; i++) {
            View v = listView.getChildAt(i);
            EditText et = (EditText) v.findViewById(R.id.et_contact_value);
            String text = et.getText().toString();
            int type = ((ContactType) ((Spinner) v.findViewById(R.id.sp_contact_type)).getSelectedItem()).getCode();
            if (TextUtils.isEmpty(text)) {
                et.setError(getString(R.string.et_error_empty));
                et.requestFocus();
                res = false;
                break;
            } else if (type == websiteCode && !InputDataValidation.isUrlValid(text)) {
                et.setError(getString(R.string.et_error_invalid_url));
                et.requestFocus();
                res = false;
                break;
            }
        }
        return res;
    }

    private void setContacts(List<Contact> contacts) {
        mContacts = new ArrayList<>(contacts);

        List<Contact> phones = ContactUtil.getPhones(contacts);
        List<Contact> emails = ContactUtil.getEmails(contacts);
        List<Contact> urls = ContactUtil.getUrls(contacts);

        ListAdapter mPhoneListAdapter;
        ListAdapter mEmailListAdapter;
        ListAdapter mUrlListAdapter;
        if (mCurrentUser.isOwner()) {
            mPhoneListAdapter = new ContactEditListAdapter(this, phones, ContactEditListAdapter.PHONES);
            mEmailListAdapter = new ContactEditListAdapter(this, emails, ContactEditListAdapter.EMAILS);
            mUrlListAdapter = new ContactEditListAdapter(this, urls, ContactEditListAdapter.URLS);

            ((ContactEditListAdapter) mPhoneListAdapter).setOnItemDeleteClickListener(mOnContactActionClickListener);
            ((ContactEditListAdapter) mEmailListAdapter).setOnItemDeleteClickListener(mOnContactActionClickListener);
            ((ContactEditListAdapter) mUrlListAdapter).setOnItemDeleteClickListener(mOnContactActionClickListener);
        } else {
            mPhoneListAdapter = new ContactViewListAdapter(this, phones.toArray(new Contact[phones.size()]));
            mEmailListAdapter = new ContactViewListAdapter(this, emails.toArray(new Contact[emails.size()]));
            mUrlListAdapter = new ContactViewListAdapter(this, urls.toArray(new Contact[urls.size()]));
        }

        ViewGroupUtil.fillList(mBinding.lvPhones, mPhoneListAdapter);
        ViewGroupUtil.fillList(mBinding.lvEmails, mEmailListAdapter);
        ViewGroupUtil.fillList(mBinding.lvUrls, mUrlListAdapter);
    }

    private void setAttachments(ArrayList<Attachment> attachments) {
        mAttachments = attachments;
        AttachmentListAdapter adapter = new AttachmentListAdapter(mAttachments, mCurrentUser.isOwner());
        adapter.setOnItemActionClickListener(mOnItemActionClickListener);
        ViewGroupUtil.fillList(mBinding.lvAttachment, adapter);
    }

    private void setImage(long cardVersion, Uri image) {
        mImage = image;
        if (image == null) {
            mBinding.ivImage.setImageResource(R.drawable.ic_cardpic_add);
        } else {
            Glide.with(this)
                    .load(image)
                    .error(R.drawable.ic_cardpic_add)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .signature(new StringSignature(String.valueOf(cardVersion)))
                    .into(mBinding.ivImage);
        }
    }

    private void addUser(CardUser user) {
        mCardUsers.add(user);
        updateUserList();
    }

    private void removeUser(CardUser cardUser) {
        mCardUsers.remove(cardUser);
        updateUserList();
        final CardUser tmpCardUser = cardUser;
        Snackbar.make(mBinding.getRoot(), R.string.snackbar_deleting, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addUser(tmpCardUser);
                    }
                }).show();
    }

    private void changeUserPermission(CardUser cardUser, boolean isOwner) {
        cardUser.setIsOwner(isOwner);
        updateUserList();
    }

    private void setUsers(ArrayList<CardUser> users) {
        mCardUsers = users;
        updateUserList();
    }

    private void updateUserList() {
        ArrayList<CardUser> mUsers = new ArrayList<>();
        for (CardUser cardUser : mCardUsers) {
            if (!mCurrentUser.getUuid().equals(cardUser.getUuid())) {
                mUsers.add(cardUser);
            }
        }
        Collections.sort(mUsers, new CardUserComparator(CardUserComparator.COMPARE_BY_ID));
        mUsersListAdapter = new UserListAdapter(this, mUsers, mCurrentUser.isOwner());
        mUsersListAdapter.setOnItemActionClickListener(mOnItemUserActionClickListener);
        ViewGroupUtil.fillList(mBinding.lvUsers, mUsersListAdapter);
    }

    private void applyCurrentCardUser(ArrayList<CardUser> users) {
        if (mCurrentUser == null) {
            String userId = SessionManager.getSession(this).getUserUuid();
            for (CardUser cardUser : users) {
                if (cardUser.getUuid().equals(userId)) {
                    mCurrentUser = cardUser;
                    setIsEditable(mCurrentUser.isOwner());
                    return;
                }
            }
        }
    }

    private void setIsEditable(boolean value) {
        mBinding.setEditable(value);
    }

    private void showPickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setTypeAndNormalize("*/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/*", "text/*"});
        }
        startActivityForResult(Intent.createChooser(intent, getString(R.string.title_activity_pick_image)), REQUEST_PICK_ATTACHMENT);
    }

    private void showLinkBeaconActivity() {
        startActivityForResult(SelectBeaconActivity.getIntent(this, mBeacons.toArray(new Beacon[mBeacons.size()])), REQUEST_LINK_BEACONS);
    }

    private void showAddVCardDialog() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogLayout = inflater.inflate(R.layout.dialog_import_contact_variants, null);
        dialogLayout.findViewById(R.id.action_cancel).setOnClickListener(mOnDialogAddVcardActionClickListener);
        dialogLayout.findViewById(R.id.action_form).setOnClickListener(mOnDialogAddVcardActionClickListener);
        dialogLayout.findViewById(R.id.action_import).setOnClickListener(mOnDialogAddVcardActionClickListener);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        mDialogAddContact = builder.create();
        mDialogAddContact.show();
    }

    private void showImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.title_activity_pick_image)), REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == Activity.RESULT_OK) {
            final Uri uri;
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    UCrop.Options options = new UCrop.Options();
                    options.setShowCropGrid(false);
                    File tmpFile = FileUtil.getTempFile(this);
                    if (tmpFile != null) {
                        UCrop.of(data.getData(), Uri.fromFile(tmpFile))
                                .withAspectRatio(1, 1)
                                .withOptions(options)
                                .start(this);
                    } else {
                        Toast.makeText(this, R.string.toast_internal_error, Toast.LENGTH_LONG).show();
                    }
                    break;
                case REQUEST_EDIT_VCARD:
                    Vcard vcard = data.getParcelableExtra(EditVcardActivity.EXTRA_RESULT_VCARD_UUID);
                    addVcard(vcard);
                    break;
                case REQUEST_GET_CONTACT:
                    uri = data.getData();
                    String vcardUuid = UUID.randomUUID().toString().toLowerCase();
                    Vcard vImportedCard = ContentProviderUtil.convertContactToVcard(this, uri, vcardUuid);
                    addVcard(vImportedCard);
                    break;
                case REQUEST_LINK_BEACONS:
                    ArrayList<Beacon> selectedBeacons = data.getParcelableArrayListExtra(SelectBeaconActivity.EXTRA_BEACON_LINKS);
                    margeBeaconsLists(selectedBeacons);
                    break;
                case UCrop.REQUEST_CROP:
                    final Uri resultUri = UCrop.getOutput(data);
                    setImage(0, resultUri);
                    break;
                case REQUEST_PICK_ATTACHMENT:
                    uri = data.getData();
                    if (uri != null) {
                        if (ContentProviderUtil.checkFileSize(this, uri, getResources().getInteger(R.integer.attached_file_max_size_bytes))) {
                            mHasChanges = true;
                            String uuid = UUID.randomUUID().toString().toLowerCase();
                            String mimeType = ContentProviderUtil.getMimeType(EditCardActivity.this, uri);
                            Attachment attachment = new Attachment(uuid, Conventions.ATTACHMENT_TYPE_DOCUMENT, mimeType, "", uri, null);
                            addAttachment(attachment);
                            editAttachment(attachment);
                        } else {
                            Toast.makeText(this, R.string.toast_unable_attach_file_max_size_limit, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(EditCardActivity.this, "Unable to load attachment", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    private void editAttachment(final Attachment attachment) {
        EditAttachmentDialog.showShareDialog(this, attachment.getDescription(), attachment.getType(),
                new EditAttachmentDialog.EditAttachmentDialogOkClickListener() {
                    @Override
                    public void onOkClickListener(int type, String description) {
                        attachment.setType(type);
                        attachment.setDescription(description);
                        setAttachments(mAttachments);
                    }

                    @Override
                    public void onCancelClickListener() {
                        if (TextUtils.isEmpty(attachment.getDescription())) {
                            mAttachments.remove(attachment);
                            setAttachments(mAttachments);
                        }
                    }
                });
    }

    private void margeBeaconsLists(ArrayList<Beacon> selectedBeacons) {
        ArrayList<Beacon> resultBeacons = new ArrayList<>(selectedBeacons.size());
        for (Beacon selectedBeacon : selectedBeacons) {
            boolean isBeaconNew = true;
            for (CardLink cardLink : selectedBeacon.getCardLinks()) {
                if (cardLink.getCardUuid().equals(mCardUuid)) {
                    isBeaconNew = false;
                }
            }
            if (isBeaconNew) {
                CardLink[] cardLinks = new CardLink[selectedBeacon.getCardLinks().length + 1];
                System.arraycopy(selectedBeacon.getCardLinks(), 0, cardLinks, 0, selectedBeacon.getCardLinks().length);
                cardLinks[cardLinks.length - 1] = new CardLink(mCardUuid, true);
                resultBeacons.add(new Beacon(selectedBeacon.getBeaconUuid(), cardLinks));
            } else {
                resultBeacons.add(selectedBeacon);
            }
        }
        setBeacons(resultBeacons);
    }

    private void addAttachment(Attachment attachment) {
        mAttachments.add(attachment);
        setAttachments(mAttachments);
    }

    private void addVcard(Vcard vcard) {
        mHasChanges = true;
        if (vcard.getUuid() != null) {
            for (int i = 0; i < mVcards.size(); i++) {
                if (vcard.getUuid().equals(mVcards.get(i).getUuid())) {
                    mVcards.remove(i);
                    break;
                }
            }
        }
        mVcards.add(vcard);
        setVcards(mVcards);
    }

    private void addContact(int type) {
        mHasChanges = true;
        String uuid = UUID.randomUUID().toString().toLowerCase();
        mContacts.add(new Contact(uuid, type, null));
        setContacts(mContacts);
        if (type > 300) {
            setFocusOnLastChild(mBinding.lvUrls);
        } else if (type < 200) {
            setFocusOnLastChild(mBinding.lvPhones);
        } else {
            setFocusOnLastChild(mBinding.lvEmails);
        }
    }

    private void setFocusOnLastChild(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        if (childCount > 0) {
            ViewGroup item = (ViewGroup) viewGroup.getChildAt(viewGroup.getChildCount() - 1);
            View view = item.findViewById(R.id.et_contact_value);
            if (view != null) {
                view.requestFocus();
            }
        }
    }

    private void removeContact(Contact contact) {
        mHasChanges = true;
        final Contact tempContact = contact;
        mContacts.remove(contact);
        setContacts(mContacts);
        if (!TextUtils.isEmpty(tempContact.getData())) {
            Snackbar.make(mBinding.getRoot(), R.string.snackbar_deleting, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mContacts.add(tempContact);
                            setContacts(mContacts);
                        }
                    }).show();
        }
    }

    private void setCardUuid(String cardUuid) {
        mCardUuid = cardUuid;
    }

    private void setCardTitle(String cardTitle) {
        mTitle = cardTitle;
        mBinding.tvTitle.setText(mTitle);
        mBinding.etTitle.setText(mTitle);
    }

    private void setCardDescription(String cardDescription) {
        mDescription = cardDescription;
        mBinding.tvDescription.setText(mDescription);
        mBinding.etDescription.setText(mDescription);
    }

    private void saveCard() {
        if (mCardUuid == null || mHasChanges) {
            Session session = SessionManager.getSession(this);
            if (mCurrentUser.isOwner()) {
                if (checkContacts() && checkTitle()) {
                    showProgress(getString(R.string.dialog_progress_title));
                    String uuid = mCardUuid;
                    if (uuid == null) {
                        uuid = UUID.randomUUID().toString().toLowerCase();
                    }
                    Contact[] contacts = mContacts.toArray(new Contact[mContacts.size()]);
                    Vcard[] vcards = mVcards.toArray(new Vcard[mVcards.size()]);
                    CardUser[] users = mCardUsers.toArray(new CardUser[mCardUsers.size()]);
                    Beacon[] beacons = mBeacons.toArray(new Beacon[mBeacons.size()]);
                    Attachment[] attachments = mAttachments.toArray(new Attachment[mAttachments.size()]);
                    CardExtended card = new CardExtended(uuid, mTitle, mDescription, mVersion, mImage, contacts, vcards, attachments, users, beacons);
                    DataService.saveCard(this, session, card);
                }
            } else {
                showProgress(getString(R.string.dialog_progress_title));
                DataService.linkCardToBeacons(this, session, mCardUuid, mBeacons);
            }
        } else {
            finish();
        }
    }

    private boolean checkTitle() {
        boolean isOk = false;
        if (TextUtils.isEmpty(mTitle)) {
            mBinding.etTitle.setError(getString(R.string.et_error_empty));
        } else {
            isOk = true;
        }
        return isOk;
    }

    private void showShareDialog() {
        ShareDialog.showShareDialog(this, new ShareDialog.ShareDialogConfirmClickListener() {
            @Override
            public void onConfirmClickListener(String email, boolean isOwner) {
                shareTo(email, isOwner);
            }
        });
    }

    private void shareTo(String email, boolean isOwner) {
        mHasChanges = true;
        String uuid = UUID.randomUUID().toString().toLowerCase();
        addUser(new CardUser(null, uuid, getString(R.string.item_user_name_share), email, null, isOwner, 0));
    }

    private void showEditVcardActivity(Vcard vcard) {
        if (vcard == null) {
            vcard = new Vcard(UUID.randomUUID().toString().toLowerCase(), null, null, null, null, null, 0);
        }
        Intent intent = EditVcardActivity.getIntent(this, vcard);
        startActivityForResult(intent, REQUEST_EDIT_VCARD);
    }

    private void showChooseContactActivity() {
        if (PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PermissionChecker.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_VCARD_URI);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            startActivityForResult(intent, REQUEST_GET_CONTACT);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS_PERMISSION);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            if (permissions.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showChooseContactActivity();
            }
        }
    }

    private void deleteVCard(Vcard vcardDel) {
        mHasChanges = true;
        final Vcard vcard = vcardDel;
        mVcards.remove(vcardDel);
        setVcards(mVcards);
        Snackbar.make(mBinding.getRoot(), R.string.snackbar_deleting, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mVcards.add(vcard);
                        setVcards(mVcards);
                    }
                }).show();
    }

}
