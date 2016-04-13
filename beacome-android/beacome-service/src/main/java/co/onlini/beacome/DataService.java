package co.onlini.beacome;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.model.CardExtended;
import co.onlini.beacome.model.CardLink;
import co.onlini.beacome.model.Credentials;
import co.onlini.beacome.model.LoginCredentials;

public abstract class DataService extends IntentService {
    protected static final String TAG = DataService.class.getSimpleName();
    private static final String EXTRA_COMMAND = "command";
    private static final String EXTRA_PARAMS = "params";

    private static final String COMMAND_SIGN_IN = "command_sign_in";
    private static final String COMMAND_SIGN_IN_TOKEN = "command_refresh_token";
    private static final String COMMAND_PULL_CARDS = "command_pull_cards";
    private static final String COMMAND_ACCEPT_INVITE = "command_accept_share";
    private static final String COMMAND_PULL_CARD_BY_BEACON = "command_pull_card_by_beacon";
    private static final String COMMAND_PULL_HISTORY = "command_pull_history";
    private static final String COMMAND_PULL_USER_CARDS = "command_pull_user_cards";
    private static final String COMMAND_HISTORY_FAVORITE = "command_history_favorite";
    private static final String COMMAND_PULL_USER_ACCOUNT_DATA = "command_pull_user_account_data";
    private static final String COMMAND_SET_USER_ACCOUNT_DATA = "command_set_user_account_data";
    private static final String COMMAND_SET_USER_ACCOUNT_IMAGE = "command_set_user_account_image";
    private static final String COMMAND_ADD_USER_ACCOUNT_LINKS = "command_set_user_account_links";
    private static final String COMMAND_ADD_INVITE = "command_add_invite";
    private static final String COMMAND_SET_CARD_LINKS_TO_BEACON = "command_set_card_links_to_beacon";
    private static final String COMMAND_SAVE_CARD = "command_save_card";
    private static final String COMMAND_DELETE_CARD = "command_delete_card";
    private static final String COMMAND_LINK_CARD_TO_BEACONS = "command_link_card_to_beacons";
    private static final String COMMAND_LOAD_ATTACHMENT = "command_load_attachment";

    private static final String KEY_CREDENTIALS = "credentials_key";
    private static final String KEY_AUTH_CREDENTIALS = "auth_credentials_key";
    private static final String KEY_INVITE_UUID = "share_card_uuid_key";
    private static final String KEY_BEACON_UUID = "beacon_uuid_key";
    private static final String KEY_CARD_UUID = "card_uuid_key";
    private static final String KEY_IS_FAVORITE = "is_favorite_key";
    private static final String KEY_IS_OWNER = "is_owner_key";
    private static final String KEY_USER_NAME = "user_name_key";
    private static final String KEY_EMAIL = "email_key";
    private static final String KEY_USER_IMAGE_URI = "user_email_key";
    private static final String KEY_USER_SOCIAL_ACCOUNTS_LINKS = "user_social_accounts_links_key";
    private static final String KEY_CARD_LINKS = "card_links_key";
    private static final String KEY_BEACON_CARD_LINKS_LIST = "beacon_card_links_list_key";
    private static final String KEY_CARD_EXTENDED = "card_extended_key";
    private static final String KEY_ATTACHMENT_UUID = "attachment_uuid_key";

    public DataService(String name) {
        super("Data_service");
    }

    public static void acceptInvite(@NonNull Context context, @NonNull Credentials credentials,
                                    @NonNull String inviteUuid) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_INVITE_UUID, inviteUuid);
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        sendCommand(context, COMMAND_ACCEPT_INVITE, bundle);
    }

    public static void signIn(@NonNull Context context, @NonNull LoginCredentials loginCredentials) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_AUTH_CREDENTIALS, loginCredentials);
        sendCommand(context, COMMAND_SIGN_IN, bundle);
    }

    public static void refreshSession(@NonNull Context context, @NonNull Credentials credentials) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        sendCommand(context, COMMAND_SIGN_IN_TOKEN, bundle);
    }

    public static void pullCardsByBeacon(@NonNull Context context, @NonNull Credentials credentials,
                                         @NonNull String beaconUuid) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putString(DataService.KEY_BEACON_UUID, beaconUuid);
        sendCommand(context, COMMAND_PULL_CARD_BY_BEACON, bundle);
    }

    public static void loadAttachment(@NonNull Context context, @NonNull Credentials credentials,
                                      @NonNull String attachmentUuid) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putString(DataService.KEY_ATTACHMENT_UUID, attachmentUuid);
        sendCommand(context, COMMAND_LOAD_ATTACHMENT, bundle);
    }

    public static void pullHistory(@NonNull Context context, @NonNull Credentials credentials) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        sendCommand(context, COMMAND_PULL_HISTORY, bundle);
    }

    public static void pullUserCards(@NonNull Context context, @NonNull Credentials credentials) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        sendCommand(context, COMMAND_PULL_USER_CARDS, bundle);
    }

    public static void pullAccountData(@NonNull Context context, @NonNull Credentials credentials) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        sendCommand(context, COMMAND_PULL_USER_ACCOUNT_DATA, bundle);
    }

    public static void setHistoryFavorite(@NonNull Context context, @NonNull Credentials credentials,
                                          @NonNull String cardUuid, boolean isFavorite) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putString(DataService.KEY_CARD_UUID, cardUuid);
        bundle.putBoolean(DataService.KEY_IS_FAVORITE, isFavorite);
        sendCommand(context, COMMAND_HISTORY_FAVORITE, bundle);
    }

    public static void setUserAccountData(@NonNull Context context, @NonNull Credentials credentials,
                                          @NonNull String userName, @NonNull String userEmail) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putString(DataService.KEY_USER_NAME, userName);
        bundle.putString(DataService.KEY_EMAIL, userEmail);
        sendCommand(context, COMMAND_SET_USER_ACCOUNT_DATA, bundle);
    }

    public static void setUserImage(@NonNull Context context, @NonNull Credentials credentials,
                                    @NonNull Uri userImage) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putParcelable(DataService.KEY_USER_IMAGE_URI, userImage);
        sendCommand(context, COMMAND_SET_USER_ACCOUNT_IMAGE, bundle);
    }

    public static void addUserSocialAccountsLinks(@NonNull Context context, @NonNull Credentials credentials,
                                                  @NonNull LoginCredentials[] socialAccountsLinks) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putParcelableArray(DataService.KEY_USER_SOCIAL_ACCOUNTS_LINKS, socialAccountsLinks);
        sendCommand(context, COMMAND_ADD_USER_ACCOUNT_LINKS, bundle);
    }

    public static void addInvite(@NonNull Context context, @NonNull Credentials credentials,
                                 @NonNull String cardUuid, @NonNull String email, boolean isOwner) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putString(DataService.KEY_CARD_UUID, cardUuid);
        bundle.putString(DataService.KEY_EMAIL, email);
        bundle.putBoolean(DataService.KEY_IS_OWNER, isOwner);
        sendCommand(context, COMMAND_ADD_INVITE, bundle);
    }

    public static void setCardLinksToBeacon(@NonNull Context context, @NonNull Credentials credentials,
                                            @NonNull String beaconUuid, @NonNull ArrayList<CardLink> cardLinks) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putString(DataService.KEY_BEACON_UUID, beaconUuid);
        bundle.putParcelableArrayList(DataService.KEY_CARD_LINKS, cardLinks);
        sendCommand(context, COMMAND_SET_CARD_LINKS_TO_BEACON, bundle);
    }

    public static void saveCard(@NonNull Context context, @NonNull Credentials credentials,
                                @NonNull CardExtended cardExtended) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putParcelable(DataService.KEY_CARD_EXTENDED, cardExtended);
        sendCommand(context, COMMAND_SAVE_CARD, bundle);
    }

    public static void deleteCard(@NonNull Context context, @NonNull Credentials credentials,
                                  @NonNull String cardUuid) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putString(DataService.KEY_CARD_UUID, cardUuid);
        sendCommand(context, COMMAND_DELETE_CARD, bundle);
    }

    public static void linkCardToBeacons(@NonNull Context context, @NonNull Credentials credentials,
                                         @NonNull String cardUuid, @NonNull ArrayList<Beacon> beacons) {
        Bundle bundle = new Bundle();
        bundle.putString(DataService.KEY_CARD_UUID, cardUuid);
        bundle.putParcelable(DataService.KEY_CREDENTIALS, credentials);
        bundle.putParcelableArrayList(DataService.KEY_BEACON_CARD_LINKS_LIST, beacons);
        sendCommand(context, COMMAND_LINK_CARD_TO_BEACONS, bundle);
    }

    private static void sendCommand(@NonNull Context context, @NonNull String command, Bundle args) {
        Intent msgIntent = new Intent(context, DataServiceImpl.class);
        msgIntent.putExtra(DataService.EXTRA_COMMAND, command);
        msgIntent.putExtra(DataService.EXTRA_PARAMS, args);
        context.startService(msgIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!intent.hasExtra(EXTRA_COMMAND)) {
            Log.d(TAG, "Unspecified command");
            return;
        }
        Bundle params = intent.getBundleExtra(EXTRA_PARAMS);
        String command = intent.getStringExtra(EXTRA_COMMAND);
        Credentials credentials = params.getParcelable(KEY_CREDENTIALS);

        if (COMMAND_SIGN_IN.equals(command)) {
            LoginCredentials loginCredentials = params.getParcelable(KEY_AUTH_CREDENTIALS);
            if (loginCredentials == null) {
                throwMissedParamsError(command, KEY_AUTH_CREDENTIALS);
            }
            signIn(loginCredentials);
            return;
        }

        if (credentials == null) {
            throwMissedParamsError(command, KEY_CREDENTIALS);
        }
        String cardUuid;
        String email;
        String inviteUuid;
        String beaconUuid;
        boolean isOwner;
        switch (command) {
            case COMMAND_SIGN_IN_TOKEN:
                refreshSession(credentials);
                break;
            case COMMAND_ACCEPT_INVITE:
                inviteUuid = params.getString(KEY_INVITE_UUID);
                if (inviteUuid == null) {
                    throwMissedParamsError(command, KEY_INVITE_UUID);
                }
                acceptInvite(credentials, inviteUuid);
                break;
            case COMMAND_PULL_CARDS:
                pullUserCards(credentials);
                break;
            case COMMAND_PULL_CARD_BY_BEACON:
                beaconUuid = params.getString(KEY_BEACON_UUID);
                if (beaconUuid == null) {
                    throwMissedParamsError(command, KEY_BEACON_UUID);
                }
                pullCardsByBeacon(credentials, beaconUuid);
                break;
            case COMMAND_PULL_HISTORY:
                pullHistory(credentials);
                break;
            case COMMAND_PULL_USER_CARDS:
                pullUserCards(credentials);
                break;
            case COMMAND_PULL_USER_ACCOUNT_DATA:
                pullUserAccountData(credentials);
                break;
            case COMMAND_HISTORY_FAVORITE:
                cardUuid = params.getString(KEY_CARD_UUID);
                if (cardUuid == null) {
                    throwMissedParamsError(command, KEY_CARD_UUID);
                }
                if (!params.containsKey(KEY_IS_FAVORITE)) {
                    throwMissedParamsError(command, KEY_IS_FAVORITE);
                }
                boolean isFavorite = params.getBoolean(KEY_IS_FAVORITE);
                setHistoryFavoriteMark(credentials, cardUuid, isFavorite);
                break;
            case COMMAND_SET_USER_ACCOUNT_DATA:
                String name = params.getString(KEY_USER_NAME);
                if (name == null) {
                    throwMissedParamsError(command, KEY_USER_NAME);
                }
                email = params.getString(KEY_EMAIL);
                if (email == null) {
                    throwMissedParamsError(command, KEY_EMAIL);
                }
                setUserData(credentials, name, email);
                break;
            case COMMAND_SET_USER_ACCOUNT_IMAGE:
                Uri image = params.getParcelable(KEY_USER_IMAGE_URI);
                if (image == null) {
                    throwMissedParamsError(command, KEY_USER_IMAGE_URI);
                }
                setUserImage(credentials, image);
                break;
            case COMMAND_ADD_USER_ACCOUNT_LINKS:
                Parcelable[] parcelables = params.getParcelableArray(KEY_USER_SOCIAL_ACCOUNTS_LINKS);
                if (parcelables != null) {
                    LoginCredentials[] links = new LoginCredentials[parcelables.length];
                    for (int i = 0; i < parcelables.length; i++) {
                        links[i] = (LoginCredentials) parcelables[i];
                    }
                    addUserSocialNetworkAccount(credentials, links);
                } else {
                    throwMissedParamsError(command, KEY_USER_SOCIAL_ACCOUNTS_LINKS);
                }
                break;
            case COMMAND_ADD_INVITE:
                cardUuid = params.getString(KEY_CARD_UUID);
                if (cardUuid == null) {
                    throwMissedParamsError(command, KEY_CARD_UUID);
                }
                email = params.getString(KEY_EMAIL);
                if (email == null) {
                    throwMissedParamsError(command, KEY_EMAIL);
                }
                isOwner = params.getBoolean(KEY_IS_OWNER);
                addInvite(credentials, cardUuid, email, isOwner);
                break;
            case COMMAND_SET_CARD_LINKS_TO_BEACON:
                beaconUuid = params.getString(KEY_BEACON_UUID);
                if (beaconUuid == null) {
                    throwMissedParamsError(command, KEY_BEACON_UUID);
                }
                ArrayList<CardLink> cardLinks = params.getParcelableArrayList(KEY_CARD_LINKS);
                if (cardLinks == null) {
                    throwMissedParamsError(command, KEY_CARD_LINKS);
                }
                setLinkedCardsToBeacon(credentials, beaconUuid, cardLinks);
                break;
            case COMMAND_SAVE_CARD:
                CardExtended cardExtended = params.getParcelable(KEY_CARD_EXTENDED);
                if (cardExtended == null) {
                    throwMissedParamsError(command, KEY_CARD_EXTENDED);
                }
                saveCard(credentials, cardExtended);
                break;
            case COMMAND_DELETE_CARD:
                cardUuid = params.getString(KEY_CARD_UUID);
                if (cardUuid == null) {
                    throwMissedParamsError(command, KEY_CARD_UUID);
                }
                deleteCard(credentials, cardUuid);
                break;
            case COMMAND_LINK_CARD_TO_BEACONS:
                cardUuid = params.getString(KEY_CARD_UUID);
                if (cardUuid == null) {
                    throwMissedParamsError(command, KEY_CARD_UUID);
                }
                ArrayList<Beacon> beacons = params.getParcelableArrayList(KEY_BEACON_CARD_LINKS_LIST);
                if (beacons == null) {
                    throwMissedParamsError(command, KEY_BEACON_CARD_LINKS_LIST);
                }
                linkCardToBeacons(credentials, cardUuid, beacons);
                break;
            case COMMAND_LOAD_ATTACHMENT:
                String attachmentUuid = params.getString(KEY_ATTACHMENT_UUID);
                if (attachmentUuid == null) {
                    throwMissedParamsError(command, KEY_ATTACHMENT_UUID);
                }
                loadAttachment(credentials, attachmentUuid);
                break;
            default:
                Log.d(TAG, "Unknown command");
        }
    }

    private void throwMissedParamsError(@NonNull String command, @NonNull String paramName) {
        throw new IllegalArgumentException(String.format("Command %s does not contains required argument, %s", command, paramName));
    }

    protected abstract void signIn(LoginCredentials credentials);

    protected abstract void refreshSession(Credentials credentials);

    protected abstract void pullUserCards(Credentials credentials);

    protected abstract void pullHistory(Credentials credentials);

    protected abstract void pullUserAccountData(Credentials credentials);

    protected abstract void pullCardsByBeacon(Credentials credentials, String beaconUuid);

    protected abstract void setHistoryFavoriteMark(Credentials credentials, String cardUuid, boolean isFavorite);

    protected abstract void setUserData(Credentials credentials, String name, String email);

    protected abstract void setUserImage(Credentials credentials, Uri imageUri);

    protected abstract void addUserSocialNetworkAccount(Credentials credentials, LoginCredentials[] loginCredentials);

    protected abstract void setLinkedCardsToBeacon(Credentials credentials, String beaconUuid, ArrayList<CardLink> cardLinks);

    protected abstract void acceptInvite(Credentials credentials, String shareUuid);

    protected abstract void addInvite(Credentials credentials, String cardUuid, String email, boolean isOwner);

    protected abstract void saveCard(Credentials credentials, CardExtended card);

    protected abstract void deleteCard(Credentials credentials, String cardUuid);

    protected abstract void linkCardToBeacons(Credentials credentials, String cardUuid, ArrayList<Beacon> beacons);

    protected abstract void loadAttachment(Credentials credentials, String attachmentUuid);
}
