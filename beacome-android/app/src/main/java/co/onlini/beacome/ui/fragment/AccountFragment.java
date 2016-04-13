package co.onlini.beacome.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import co.onlini.beacome.DataService;
import co.onlini.beacome.R;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.databinding.FragmentAccountBinding;
import co.onlini.beacome.event.PullUserAccountDataResult;
import co.onlini.beacome.event.RetrofitIoExceptionResult;
import co.onlini.beacome.event.UserAccountUpdatingResult;
import co.onlini.beacome.model.LoginCredentials;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.model.UserWithLinks;
import co.onlini.beacome.ui.activity.MainActivity;
import co.onlini.beacome.ui.loader.RegisteredUserAsyncLoader;
import co.onlini.beacome.util.FileUtil;
import co.onlini.beacome.util.GoogleApiHelper;
import co.onlini.beacome.util.InputDataValidation;
import co.onlini.beacome.web.Conventions;

public class AccountFragment extends NotifyActivePageFragment {

    private static final String SAVED_STATE_USER_IMAGE = "saved_state_user_image";
    private static final String SAVED_STATE_USER_NAME = "saved_state_user_name";
    private static final String SAVED_STATE_USER_EMAIL = "saved_state_user_email";
    private static final String SAVED_STATE_USER_LINKS = "saved_state_user_links";

    private static final int LOADER_USER_DATA = 0x1;
    private static final int REQUEST_PICK_IMAGE = 0x1;
    private static final int REQUEST_SIGN_IN_GP = 0x128;
    private FragmentAccountBinding mBinding;
    private int mSocialLinkTextColorId;
    private int mTranslucentSocialLinkTextColorId;
    private CallbackManager mFacebookCallbackManager;

    private ArrayList<LoginCredentials> mAddedLinks;
    private Uri mNewImage;
    private String mNewUserName;
    private String mNewUserEmail;
    private final LoaderManager.LoaderCallbacks<UserWithLinks> mUserLoaderCallback = new LoaderManager.LoaderCallbacks<UserWithLinks>() {
        @Override
        public Loader<UserWithLinks> onCreateLoader(int id, Bundle args) {
            String userUuid = SessionManager.getSession(getContext()).getUserUuid();
            return new RegisteredUserAsyncLoader(getContext(), userUuid);
        }

        @Override
        public void onLoadFinished(Loader<UserWithLinks> loader, UserWithLinks user) {
            if (mNewUserEmail == null) {
                mBinding.etEmail.setText(user.getEmail());
            }
            if (mNewUserName == null) {
                mBinding.etName.setText(user.getName());
            }

            showImage(user.getImage(), user.getVersion());

            if (user.isFacebookLinked()) {
                setFacebookLinkViewsState(true);
            }
            if (user.isGpLinked()) {
                setGpLinkViewsState(true);
            }
            if (user.isTwitterLinked()) {
                setTwitterLinkViewsState(true);
            }
            getLoaderManager().destroyLoader(LOADER_USER_DATA);
        }

        @Override
        public void onLoaderReset(Loader<UserWithLinks> loader) {

        }
    };
    private GoogleApiClient mGoogleApiClient;
    private TwitterAuthClient mTwitterAuthClient;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_language_action:
                    showSelectLanguageDialog();
                    break;
                case R.id.btn_choose_image:
                    showImagePickActivity();
                    break;
                case R.id.tv_facebook_link_action:
                    linkFacebookAccount();
                    break;
                case R.id.tv_twitter_link_action:
                    linkTwitterAccount();
                    break;
                case R.id.tv_gp_link_action:
                    linkGpAccount();
                    break;
            }
        }
    };

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    private void showImage(Uri image, long version) {
        Glide.with(getActivity())
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new StringSignature(String.valueOf(version)))
                .error(R.drawable.ic_userpic)
                .into(mBinding.ivImage);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSocialLinkTextColorId = ActivityCompat.getColor(getContext(), R.color.black_text);
        mTranslucentSocialLinkTextColorId = ActivityCompat.getColor(getContext(), R.color.grey_text);
        if (savedInstanceState != null) {
            //noinspection unchecked
            mAddedLinks = savedInstanceState.getParcelableArrayList(SAVED_STATE_USER_LINKS);
            mNewImage = savedInstanceState.getParcelable(SAVED_STATE_USER_IMAGE);
            mNewUserName = savedInstanceState.getString(SAVED_STATE_USER_NAME);
            mNewUserEmail = savedInstanceState.getString(SAVED_STATE_USER_EMAIL);
        }
        if (mAddedLinks == null) {
            mAddedLinks = new ArrayList<>();
        }
        getLoaderManager().initLoader(LOADER_USER_DATA, null, mUserLoaderCallback).forceLoad();
        mTwitterAuthClient = new TwitterAuthClient();
        mGoogleApiClient = GoogleApiHelper.getClient(getContext());
        mFacebookCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mNewImage != null) {
            mBinding.ivImage.setImageURI(mNewImage);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        mBinding.btnChooseImage.setOnClickListener(mOnClickListener);
        mBinding.tvFacebookLinkAction.setOnClickListener(mOnClickListener);
        mBinding.tvTwitterLinkAction.setOnClickListener(mOnClickListener);
        mBinding.tvGpLinkAction.setOnClickListener(mOnClickListener);
        mBinding.tvLanguageAction.setOnClickListener(mOnClickListener);
        mBinding.etEmail.addTextChangedListener(new co.onlini.beacome.util.TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mNewUserEmail = s.toString();
            }
        });
        mBinding.etName.addTextChangedListener(new co.onlini.beacome.util.TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mNewUserName = s.toString();
            }
        });
        setLanguageTextViewText(SessionManager.getLanguage(getContext()));
        return mBinding.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVED_STATE_USER_LINKS, mAddedLinks);
        outState.putParcelable(SAVED_STATE_USER_IMAGE, mNewImage);
        outState.putString(SAVED_STATE_USER_EMAIL, mNewUserEmail);
        outState.putString(SAVED_STATE_USER_NAME, mNewUserName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_action_save, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        setTopFragment(MainActivity.FRAGMENT_ACCOUNT, 0);
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_account));
        }
        mGoogleApiClient.connect();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserAccountUpdatingResult result) {
        if (!result.isSuccess()) {
            hideProgress();
            Toast.makeText(getContext(), R.string.toast_save_acc_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PullUserAccountDataResult result) {
        hideProgress();
        Toast.makeText(getContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();
        mNewImage = null;
        mNewUserEmail = null;
        mNewUserName = null;
        mAddedLinks = null;
        getLoaderManager().initLoader(LOADER_USER_DATA, null, mUserLoaderCallback).forceLoad();
        if (!result.isSuccess()) {
            hideProgress();
            Toast.makeText(getContext(), R.string.toast_save_acc_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RetrofitIoExceptionResult exception) {
        hideProgress();
        Toast.makeText(getContext(), R.string.toast_no_internet_connection, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mGoogleApiClient.disconnect();
        hideKeyboard();
        hideProgress();
    }

    private void setLanguageTextViewText(String localeName) {
        Locale locale = new Locale(localeName);
        mBinding.tvCurrentLanguage.setText(String.format("%s(%s)", locale.getDisplayLanguage(), locale.getLanguage()));
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(getContext());
        }
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setFacebookLinkViewsState(boolean state) {
        mBinding.tvFacebookLinkAction.setVisibility(state ? View.GONE : View.VISIBLE);
        if (state) {
            mBinding.ivFacebookLink.setBackgroundResource(R.drawable.shape_facebook);
            mBinding.tvFacebookLinkName.setTextColor(mSocialLinkTextColorId);
        } else {
            mBinding.ivFacebookLink.setBackgroundResource(R.drawable.shape_facebook_translucent);
            mBinding.tvFacebookLinkName.setTextColor(mTranslucentSocialLinkTextColorId);
        }
    }

    private void setTwitterLinkViewsState(boolean state) {
        mBinding.tvTwitterLinkAction.setVisibility(state ? View.GONE : View.VISIBLE);
        if (state) {
            mBinding.ivTwitterLink.setBackgroundResource(R.drawable.shape_twitter);
            mBinding.tvTwitterLinkName.setTextColor(mSocialLinkTextColorId);
        } else {
            mBinding.ivTwitterLink.setBackgroundResource(R.drawable.shape_twitter_translucent);
            mBinding.tvTwitterLinkName.setTextColor(mTranslucentSocialLinkTextColorId);
        }
    }

    private void setGpLinkViewsState(boolean state) {
        mBinding.tvGpLinkAction.setVisibility(state ? View.GONE : View.VISIBLE);
        if (state) {
            mBinding.ivGpLink.setBackgroundResource(R.drawable.shape_gp);
            mBinding.tvGpLinkName.setTextColor(mSocialLinkTextColorId);
        } else {
            mBinding.ivGpLink.setBackgroundResource(R.drawable.shape_gp_translucent);
            mBinding.tvGpLinkName.setTextColor(mTranslucentSocialLinkTextColorId);
        }
    }

    private void linkGpAccount() {
        if (mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, REQUEST_SIGN_IN_GP);
        } else {
            Toast.makeText(getContext(), R.string.toast_google_api_disconnected, Toast.LENGTH_SHORT).show();
        }
    }

    private void linkTwitterAccount() {
        mTwitterAuthClient.authorize(getActivity(), new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterAuthToken authToken = result.data.getAuthToken();
                linkAccount(new LoginCredentials(Conventions.AUTH_PROVIDER_TWITTER, authToken.token, authToken.secret));
            }

            @Override
            public void failure(TwitterException e) {
                showErrorMessage(getContext().getString(R.string.toast_sign_in_error));
            }
        });
    }

    private void showImagePickActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.title_activity_pick_image)), REQUEST_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    UCrop.Options options = new UCrop.Options();
                    options.setShowCropGrid(false);
                    File tmpFile = FileUtil.getTempFile(getContext());
                    if (tmpFile != null) {
                        UCrop.of(data.getData(), Uri.fromFile(tmpFile))
                                .withAspectRatio(1, 1)
                                .withOptions(options)
                                .start(getActivity());
                    } else {
                        Toast.makeText(getActivity(), R.string.toast_internal_error, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_SIGN_IN_GP:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                GoogleSignInAccount acct;
                String token;
                if (result.isSuccess() && ((acct = result.getSignInAccount()) != null) && ((token = acct.getIdToken()) != null)) {
                    linkAccount(new LoginCredentials(Conventions.AUTH_PROVIDER_GOOGLE, token, null));
                } else {
                    showErrorMessage(getContext().getString(R.string.toast_sign_in_error));
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    importBitmap(resultUri);
                }
                break;
        }
    }

    private void importBitmap(Uri image) {
        mNewImage = image;
        showImage(image, 0);
    }

    private void linkFacebookAccount() {
        FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                if (accessToken != null) {
                    linkAccount(new LoginCredentials(Conventions.AUTH_PROVIDER_FACEBOOK, accessToken.getToken(), null));
                } else {
                    showErrorMessage(getContext().getString(R.string.toast_sign_in_error));
                }
            }

            @Override
            public void onCancel() {
                showErrorMessage(getContext().getString(R.string.toast_sign_in_error));
            }

            @Override
            public void onError(FacebookException error) {
                showErrorMessage(getContext().getString(R.string.toast_sign_in_error));
            }
        };
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.logOut();
        loginManager.registerCallback(mFacebookCallbackManager, facebookCallback);
        loginManager.logInWithReadPermissions(getActivity(), null);
    }

    private void linkAccount(LoginCredentials credentials) {
        mAddedLinks.add(credentials);
        updateLinkControls();
    }

    private void updateLinkControls() {
        if (mAddedLinks != null) {
            for (LoginCredentials credentials : mAddedLinks) {
                switch (credentials.getAuthProvider()) {
                    case Conventions.AUTH_PROVIDER_GOOGLE:
                        setGpLinkViewsState(true);
                        break;
                    case Conventions.AUTH_PROVIDER_TWITTER:
                        setTwitterLinkViewsState(true);
                        break;
                    case Conventions.AUTH_PROVIDER_FACEBOOK:
                        setFacebookLinkViewsState(true);
                        break;
                }
            }
        }
    }

    private void showSelectLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.dialog_choose_language))
                .setItems(R.array.languages, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setLanguage(which);
                    }
                });
        builder.create().show();
    }

    private void setLanguage(int languageIndex) {
        String localeName;
        switch (languageIndex) {
            case 2:
                localeName = "uk";
                break;
            case 1:
                localeName = "ru";
                break;
            case 0:
            default:
                localeName = "en";
        }
        String curLocaleName = SessionManager.getLanguage(getContext());
        if (curLocaleName.equals(localeName)) {
            return;
        }
        SessionManager.setLanguage(getContext(), localeName);
        Locale locale = new Locale(localeName);
        Locale.setDefault(locale);
        Configuration config = getContext().getApplicationContext().getResources().getConfiguration();
        config.locale = locale;
        DisplayMetrics metrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        getContext().getApplicationContext().getResources().updateConfiguration(config, metrics);
        getActivity().recreate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (isDataValid()) {
                saveUserData();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isDataValid() {
        boolean isValid = true;
        String name = mBinding.etName.getText().toString();
        String email = mBinding.etEmail.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mBinding.etName.setError(getString(R.string.et_error_empty));
            mBinding.etName.requestFocus();
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            mBinding.etEmail.setError(getString(R.string.et_error_empty));
            mBinding.etEmail.requestFocus();
            isValid = false;
        } else if (!InputDataValidation.isEmailValid(email)) {
            mBinding.etEmail.setError(getString(R.string.et_error_invalid_email));
            mBinding.etEmail.requestFocus();
            isValid = false;
        }
        return isValid;
    }

    private void saveUserData() {
        hideKeyboard();
        showProgress(getString(R.string.dialog_progress_title));
        boolean hasChanges = false;
        Session session = SessionManager.getSession(getContext());
        if (!TextUtils.isEmpty(mNewUserName) || !TextUtils.isEmpty(mNewUserEmail)) {
            String name = mBinding.etName.getText().toString();
            String email = mBinding.etEmail.getText().toString();
            DataService.setUserAccountData(getContext(), session, name, email);
            hasChanges = true;
        }
        if (mNewImage != null) {
            DataService.setUserImage(getContext(), session, mNewImage);
            hasChanges = true;
        }
        if (mAddedLinks != null && mAddedLinks.size() > 0) {
            LoginCredentials[] loginCredentialsArray = mAddedLinks.toArray(new LoginCredentials[mAddedLinks.size()]);
            DataService.addUserSocialAccountsLinks(getContext(), session, loginCredentialsArray);
            hasChanges = true;
        }
        if (hasChanges) {
            DataService.pullAccountData(getContext(), session);
        }
    }

    private void showErrorMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
