package co.onlini.beacome.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.onlini.beacome.DataService;
import co.onlini.beacome.R;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.event.PullUserCardsResult;
import co.onlini.beacome.event.RetrofitIoExceptionResult;
import co.onlini.beacome.event.SignInResult;
import co.onlini.beacome.model.Credentials;
import co.onlini.beacome.model.LoginCredentials;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.util.GoogleApiHelper;
import co.onlini.beacome.web.Conventions;

public class SignInActivity extends BaseActivity {

    private static final int REQUEST_SIGN_IN_GP = 0x64;
    private CallbackManager mFacebookCallbackManager;
    private TwitterAuthClient mTwitterAuthClient;
    private String mShareCardUuid;
    private GoogleApiClient mGoogleApiClient;
    private FacebookCallback<com.facebook.login.LoginResult> mFacebookCallback =
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    String userToken = loginResult.getAccessToken().getToken();
                    LoginCredentials loginCredentials = new LoginCredentials(Conventions.AUTH_PROVIDER_FACEBOOK, userToken, null);
                    sendSignInRequest(loginCredentials);
                }

                @Override
                public void onCancel() {
                    LoginManager.getInstance().logOut();
                    Toast.makeText(SignInActivity.this, R.string.toast_sign_in_canceled, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(SignInActivity.this, R.string.toast_sign_in_unable, Toast.LENGTH_SHORT).show();
                }
            };
    private View.OnClickListener mViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.lay_facebook:
                    signInFacebook();
                    break;
                case R.id.lay_twitter:
                    signInTwitter();
                    break;
                case R.id.lay_gp:
                    signInGp();
                    break;
                case R.id.lay_anonymous:
                default:
                    signInAsAnonymous();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        if (uri != null) {
            mShareCardUuid = uri.getQueryParameter(getString(R.string.browsable_share_uuid));
            if (mShareCardUuid == null) {
                mShareCardUuid = uri.getLastPathSegment();
            }
            if (mShareCardUuid != null) {
                try {
                    //noinspection ConstantConditions
                    if (UUID.fromString(mShareCardUuid) == null) {
                        mShareCardUuid = null;
                    }
                } catch (Exception ignore) {
                    mShareCardUuid = null;
                }
            }
        }
        if (SessionManager.isFirstStart(this)) {
            startActivity(new Intent(this, IntroActivity.class));
        } else if (!trySignInWithStoredTokens()) {
            LoginManager.getInstance().logOut();
        }
        mTwitterAuthClient = new TwitterAuthClient();
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mGoogleApiClient = GoogleApiHelper.getClient(this);
        setContentView(R.layout.activity_sign_in);
        initControls();
    }

    @SuppressWarnings("ConstantConditions")
    private void initControls() {
        findViewById(R.id.lay_facebook).setOnClickListener(mViewOnClickListener);
        findViewById(R.id.lay_twitter).setOnClickListener(mViewOnClickListener);
        findViewById(R.id.lay_gp).setOnClickListener(mViewOnClickListener);
        findViewById(R.id.lay_anonymous).setOnClickListener(mViewOnClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        EventBus.getDefault().unregister(this);
        hideProgress();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN_GP) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount acct;
            String token;
            if (result != null && result.isSuccess() && ((acct = result.getSignInAccount()) != null) && ((token = acct.getIdToken()) != null)) {
                LoginCredentials loginCredentials = new LoginCredentials(Conventions.AUTH_PROVIDER_GOOGLE, token, null);
                sendSignInRequest(loginCredentials);
            } else {
                Toast.makeText(SignInActivity.this, R.string.toast_sign_in_canceled, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mFacebookCallbackManager != null) {
                mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
            }
            if (mTwitterAuthClient != null) {
                mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PullUserCardsResult result) {
        hideProgress();
        startMainActivity();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RetrofitIoExceptionResult exception) {
        hideProgress();
        Toast.makeText(this, R.string.toast_no_internet_connection, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SignInResult result) {
        hideProgress();
        if (result.isSuccess()) {
            Credentials credentials = result.getCredentials();
            String beaconUuid = SessionManager.getUserBeaconUuid(this, credentials.getUserUuid());
            Session session;
            if (result.isNewSession()) {
                session = new Session(credentials.getUserUuid(), credentials.getToken(), credentials.getExpireDate(), beaconUuid,
                        true, false);
            } else {
                Session prevSession = SessionManager.getSession(this);
                session = new Session(credentials.getUserUuid(), credentials.getToken(), credentials.getExpireDate(), prevSession.getDeviseAsBeaconUuid(),
                        prevSession.isScannerRunning(), prevSession.isAdvertiserRunning());
            }
            startSession(session);
        } else {
            Toast.makeText(SignInActivity.this, R.string.toast_sign_in_auth_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void startSession(Session session) {
        showProgress(getString(R.string.dialog_progress_sync_message));
        SessionManager.startSession(this, session);
        //Requests order is important
        if (!session.isAnonymous()) {
            DataService.pullAccountData(this, session);
            if (mShareCardUuid != null) {
                DataService.acceptInvite(this, session, mShareCardUuid);
            }
            DataService.pullUserCards(this, session);
        } else {
            startMainActivity();
        }
        DataService.pullHistory(this, session);
    }

    private void startMainActivity() {
        hideProgress();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean trySignInWithStoredTokens() {
        boolean result;
        final Session session = SessionManager.getSession(this);
        if (SessionManager.isSavedSessionValid(this)) {
            if (SessionManager.ANONYMOUS_USER_ID.equals(session.getUserUuid())) {
                startSession(session);
                result = true;
            } else {
                showProgress(getString(R.string.dialog_progress_sign_in_text));
                DataService.refreshSession(this, session);
                //And waiting on eventBus result;
                result = true;
            }
        } else {
            result = false;
        }
        return result;
    }

    private void sendSignInRequest(LoginCredentials loginCredentials) {
        showProgress(getString(R.string.dialog_progress_sign_in_text));
        DataService.signIn(this, loginCredentials);
        //And waiting on eventBus result;
    }

    private boolean checkInternetConnection() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        boolean isConnected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        if (!isConnected) {
            new AlertDialog.Builder(this, R.style.AlertDialog)
                    .setMessage(getString(R.string.dialog_no_internet_message))
                    .setTitle(getString(R.string.dialog_no_internet_title))
                    .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        return isConnected;
    }

    private void signInTwitter() {
        if (checkInternetConnection()) {
            mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    TwitterAuthToken authToken = result.data.getAuthToken();
                    LoginCredentials loginCredentials = new LoginCredentials(Conventions.AUTH_PROVIDER_TWITTER, authToken.token, authToken.secret);
                    sendSignInRequest(loginCredentials);
                }

                @Override
                public void failure(TwitterException e) {
                    Toast.makeText(SignInActivity.this, R.string.toast_sign_in_canceled, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void signInGp() {
        if (checkInternetConnection()) {
            if (mGoogleApiClient.isConnected()) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResolvingResultCallbacks<Status>(this, REQUEST_SIGN_IN_GP) {
                    @Override
                    public void onSuccess(Status status) {
                        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                        startActivityForResult(signInIntent, REQUEST_SIGN_IN_GP);
                    }

                    @Override
                    public void onUnresolvableFailure(Status status) {
                        Toast.makeText(SignInActivity.this, R.string.toast_sign_in_error, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
    }

    private void signInFacebook() {
        if (checkInternetConnection()) {
            LoginManager loginManager = LoginManager.getInstance();
            loginManager.setLoginBehavior(LoginBehavior.WEB_ONLY);
            loginManager.registerCallback(mFacebookCallbackManager, mFacebookCallback);
            List<String> permissions = new ArrayList<>();
            permissions.add("public_profile");
            permissions.add("email");
            loginManager.logInWithReadPermissions(SignInActivity.this, permissions);
        }
    }

    private void signInAsAnonymous() {
        if (checkInternetConnection()) {
            Session anonymousSession = new Session(SessionManager.ANONYMOUS_USER_ID, null, 0, null, true, false);
            startSession(anonymousSession);
        }
    }
}
