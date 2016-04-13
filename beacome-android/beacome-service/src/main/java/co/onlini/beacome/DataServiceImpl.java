package co.onlini.beacome;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import co.onlini.beacome.dal.AttachmentHelper;
import co.onlini.beacome.dal.CardHelper;
import co.onlini.beacome.dal.HistoryHelper;
import co.onlini.beacome.dal.UserHelper;
import co.onlini.beacome.event.AttachmentLoadResult;
import co.onlini.beacome.event.DeleteCardException;
import co.onlini.beacome.event.GetCardsByBeaconResult;
import co.onlini.beacome.event.LinkBeaconsException;
import co.onlini.beacome.event.PullHistoryResult;
import co.onlini.beacome.event.PullUserAccountDataResult;
import co.onlini.beacome.event.PullUserCardsResult;
import co.onlini.beacome.event.RetrofitIoExceptionResult;
import co.onlini.beacome.event.SaveCardException;
import co.onlini.beacome.event.SetCardLinksToBeaconResult;
import co.onlini.beacome.event.SetHistoryFavoriteResult;
import co.onlini.beacome.event.SignInResult;
import co.onlini.beacome.event.UserAccountUpdatingResult;
import co.onlini.beacome.model.Attachment;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.model.CardExtended;
import co.onlini.beacome.model.CardLink;
import co.onlini.beacome.model.CardUser;
import co.onlini.beacome.model.Contact;
import co.onlini.beacome.model.Credentials;
import co.onlini.beacome.model.LoginCredentials;
import co.onlini.beacome.model.User;
import co.onlini.beacome.model.Vcard;
import co.onlini.beacome.util.BitmapOpUtil;
import co.onlini.beacome.util.FileCacheUtil;
import co.onlini.beacome.web.BeacomeApi;
import co.onlini.beacome.web.Conventions;
import co.onlini.beacome.web.RestServiceFactory;
import co.onlini.beacome.web.model.AttachmentModel;
import co.onlini.beacome.web.model.BeaconModel;
import co.onlini.beacome.web.model.CardModel;
import co.onlini.beacome.web.model.CardRelationsModel;
import co.onlini.beacome.web.model.ContactModel;
import co.onlini.beacome.web.model.ExtendedAttachmentModel;
import co.onlini.beacome.web.model.ExtendedCardModel;
import co.onlini.beacome.web.model.HistoryModel;
import co.onlini.beacome.web.model.InviteModel;
import co.onlini.beacome.web.model.LoginModel;
import co.onlini.beacome.web.model.ProviderModel;
import co.onlini.beacome.web.model.UserCardModel;
import co.onlini.beacome.web.model.UserModel;
import co.onlini.beacome.web.model.UserPermissionModel;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class DataServiceImpl extends DataService {
    private BeacomeApi mBeacomeApi = RestServiceFactory.getRestService();
    private CardHelper mCardHelper;
    private UserHelper mUserHelper;
    private AttachmentHelper mAttachmentHelper;
    private HistoryHelper mHistoryHelper;

    public DataServiceImpl() {
        this("Data_service");
    }

    public DataServiceImpl(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mUserHelper = UserHelper.getInstance(this);
        mCardHelper = CardHelper.getInstance(this);
        mAttachmentHelper = AttachmentHelper.getInstance(this);
        mHistoryHelper = HistoryHelper.getInstance(this);
    }

    @Override
    protected void signIn(@NonNull LoginCredentials loginCredentials) {
        ProviderModel providerModel =
                new ProviderModel(loginCredentials.getAuthProvider(), loginCredentials.getAuthToken(), loginCredentials.getAuthSecret());
        try {
            Response<UserModel> response = mBeacomeApi.signIn(providerModel).execute();
            if (response.isSuccess() && response.body() != null) {
                UserModel model = response.body();
                Uri uri = getUserImageUri(model.getUuid(), model.getVersion());
                mUserHelper.saveUser(model.getUuid(), model.getName(), model.getEmail(), model.getVersion(), uri);
                String token = response.headers().get(Conventions.AUTHORIZATION_HEADER);
                String expires = response.headers().get(Conventions.EXPIRE_DATE_HEADER);
                long expireDate = 0;
                if (expires != null && expires.length() > 0) {
                    expireDate = Long.valueOf(expires);
                }
                if (token == null) {
                    EventBus.getDefault().post(new SignInResult(false, null, true));
                } else {
                    Credentials credentials = new Credentials(model.getUuid(), token, expireDate);
                    EventBus.getDefault().post(new SignInResult(true, credentials, true));
                }
            } else {
                EventBus.getDefault().post(new SignInResult(false, null, true));
            }
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void pullUserCards(@NonNull final Credentials credentials) {
        if (credentials.getToken() == null) {
            return;
        }

        ExecutorService executor = Executors.newCachedThreadPool();

        Future<UserCardModel[]> getCardsFuture = executor.submit(new Callable<UserCardModel[]>() {
            @Override
            public UserCardModel[] call() throws Exception {
                UserCardModel[] cards = null;
                long lastVersion = CardHelper.getInstance(DataServiceImpl.this).getVersion(credentials.getUserUuid());
                try {
                    Response<UserCardModel[]> response = mBeacomeApi.getCards(credentials.getToken(), lastVersion).execute();
                    if (response.isSuccess()) {
                        cards = response.body();
                    }
                } catch (IOException e) {
                    handleIoException(e);
                }
                return cards;
            }
        });

        Future<UserModel[]> getCardUsersFuture = executor.submit(new Callable<UserModel[]>() {
            @Override
            public UserModel[] call() throws Exception {
                UserModel[] userModels = null;
                try {
                    Response<UserModel[]> response = mBeacomeApi.getUsersAssignedToCurrentUserCards(credentials.getToken(), 0).execute();
                    if (response.isSuccess() && response.body() != null) {
                        userModels = response.body();
                    }
                } catch (IOException e) {
                    handleIoException(e);
                }
                return userModels;
            }
        });

        UserCardModel[] cards = null;
        UserModel[] users = null;

        try {
            cards = getCardsFuture.get();
            users = getCardUsersFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(TAG, e.getMessage());
        }

        executor.shutdown();

        if (users != null) {
            for (UserModel user : users) {
                Uri uri = getUserImageUri(user.getUuid(), user.getVersion());
                mUserHelper.saveUser(user.getUuid(), user.getName(), user.getEmail(), user.getVersion(), uri);
            }
        }

        if (cards != null) {
            for (UserCardModel card : cards) {
                String cardUuid = card.getUuid();
                Uri uri = getCardImageUri(cardUuid, card.getVersion());
                if (mCardHelper.isCardExists(cardUuid)) {
                    mCardHelper.updateCard(cardUuid, card.getTitle(), card.getDescription(), card.getVersion(), uri, card.isDelete());
                } else {
                    mCardHelper.insertCard(cardUuid, card.getTitle(), card.getDescription(), card.getVersion(), uri, card.isDelete());
                }
                mCardHelper.setCardContacts(cardUuid, card.getContacts());
                mCardHelper.setCardInvites(cardUuid, card.getInvites());
                mCardHelper.setCardUsers(cardUuid, card.getUsers());
                mCardHelper.setBeacons(cardUuid, card.getBeacons());
                saveAttachments(credentials, cardUuid, card.getAttachments());
            }
        }
        EventBus.getDefault().post(new PullUserCardsResult());
    }

    private void saveAttachments(final Credentials credentials, final String cardUuid, AttachmentModel[] attachments) {
        if (attachments == null || attachments.length == 0) {
            return;
        }
        List<AttachmentModel> vcards = new ArrayList<>();
        for (final AttachmentModel att : attachments) {
            Uri uri = getAttachmentUri(att.getUuid());
            if (att.getType() == Conventions.ATTACHMENT_TYPE_VCARD) {
                vcards.add(att);
            } else {
                mAttachmentHelper.saveAttachment(att.getUuid(), att.getType(), att.getMimeType(), cardUuid,
                        uri.toString(), att.getDescription(), att.getVersion(), att.isDeleted());
            }
        }
        pullVcards(credentials, cardUuid, vcards);
    }

    private Uri getAttachmentUri(String attachmentUuid) {
        return Uri.parse(String.format("%sAttachments/%s/Content", RestServiceFactory.getBaseUri(), attachmentUuid));
    }

    private void pullVcards(final Credentials credentials, final String cardUuid, final List<AttachmentModel> vcardAttachments) {
        if (vcardAttachments == null || vcardAttachments.size() == 0) {
            return;
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Void>> vcardsFutures = new ArrayList<>(vcardAttachments.size());
        for (final AttachmentModel attachment : vcardAttachments) {
            vcardsFutures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Response<ResponseBody> res = mBeacomeApi.getAttachmentContent(credentials.getToken(), attachment.getUuid(), 0).execute();
                    if (res.isSuccess()) {
                        mAttachmentHelper.saveVcard(attachment.getUuid(), cardUuid, res.body().byteStream(), attachment.getVersion(), attachment.isDeleted());
                    }
                    return null;
                }
            }));
        }

        for (Future<Void> future : vcardsFutures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ignore) {
            }
        }
        executor.shutdown();
    }

    private void pullUsers(final Credentials credentials, String[] usersUuids) {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<UserModel>> usersFutures = new ArrayList<>(usersUuids.length);
        for (final String userUuid : usersUuids) {
            usersFutures.add(executor.submit(new Callable<UserModel>() {
                @Override
                public UserModel call() throws Exception {
                    UserModel userModel = null;
                    long userVersion = mUserHelper.getVersion(userUuid);
                    Response<UserModel> response = mBeacomeApi.getUser(credentials.getToken(), userUuid, userVersion).execute();
                    if (response.isSuccess() && response.body() != null) {
                        userModel = response.body();
                    }
                    return userModel;
                }
            }));
        }
        for (Future<UserModel> future : usersFutures) {
            try {
                UserModel user = future.get();
                if (user != null) {
                    Uri uri = getUserImageUri(user.getUuid(), user.getVersion());
                    mUserHelper.saveUser(user.getUuid(), user.getName(), user.getEmail(), user.getVersion(), uri);
                }
            } catch (InterruptedException | ExecutionException ignore) {
            }
        }
        executor.shutdown();
    }

    @Override
    protected void pullHistory(@NonNull final Credentials credentials) {
        long version = mHistoryHelper.getVersion(credentials.getUserUuid());
        try {
            Response<HistoryModel[]> response = mBeacomeApi.getHistory(credentials.getToken(), version).execute();
            HistoryModel[] historyEntries;
            if (response.isSuccess() && (historyEntries = response.body()) != null) {
                ExecutorService executor = Executors.newCachedThreadPool();
                List<Future<Void>> futures = new ArrayList<>(historyEntries.length);
                for (final HistoryModel history : historyEntries) {
                    futures.add(executor.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            long cardVersion = mCardHelper.getCardVersion(history.getCardUuid());
                            Response<CardModel[]> response = mBeacomeApi.getHistoryCards(credentials.getToken(), cardVersion, history.getCardUuid()).execute();
                            if (response.isSuccess() && response.body() != null) {
                                //Expect only one card
                                CardModel[] outdatedHistoryCards = response.body();
                                processHistoryCards(credentials, outdatedHistoryCards);
                            } else {
                                Log.d(TAG, "Unable to get history`s cards " + response.code());
                            }
                            long discoveryDate = history.getReceivedTime() * 1000;
                            mHistoryHelper.insertOrUpdateHistoryRecord(credentials.getUserUuid(), history.getCardUuid(), discoveryDate, history.isFavorite(), history.getVersion());
                            return null;
                        }
                    }));
                }
                for (Future<Void> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.d(TAG, "Unable to get history");
                    }
                }
            }
        } catch (IOException e) {
            handleIoException(e);
        }
        EventBus.getDefault().post(new PullHistoryResult());
    }

    private void processHistoryCards(Credentials credentials, CardModel[] cards) {
        for (CardModel card : cards) {
            String cardUuid = card.getUuid();
            Uri cardImageUri = getCardImageUri(cardUuid, card.getVersion());
            if (mCardHelper.isCardExists(card.getUuid())) {
                mCardHelper.updateCard(cardUuid, card.getTitle(), card.getDescription(), card.getVersion(), cardImageUri, card.isDelete());
            } else {
                mCardHelper.insertCard(cardUuid, card.getTitle(), card.getDescription(), card.getVersion(), cardImageUri, card.isDelete());
            }
            mCardHelper.setCardContacts(cardUuid, card.getContacts());
            saveAttachments(credentials, cardUuid, card.getAttachments());
        }
    }

    @Override
    protected void pullUserAccountData(@NonNull Credentials credentials) {
        if (credentials.getToken() == null) {
            return;
        }
        try {
            Response<UserModel> responseGetUser = mBeacomeApi.getUser(credentials.getToken(), credentials.getUserUuid(), 0).execute();
            if (responseGetUser.isSuccess()) {
                UserModel user = responseGetUser.body();
                Uri uri = getUserImageUri(user.getUuid(), user.getVersion());
                mUserHelper.saveUser(user.getUuid(), user.getName(), user.getEmail(), user.getVersion(), uri);
            }
            Response<LoginModel[]> responseGetSocial = mBeacomeApi.getUserSocialAccounts(credentials.getToken()).execute();
            if (responseGetSocial.isSuccess()) {
                LoginModel[] accounts = responseGetSocial.body();
                if (accounts != null) {
                    boolean hasFacebookAcc = false;
                    boolean hasTwitterAcc = false;
                    boolean hasGoogleAcc = false;
                    for (LoginModel login : accounts) {
                        if (Conventions.AUTH_PROVIDER_FACEBOOK.equalsIgnoreCase(login.getProvider())) {
                            hasFacebookAcc = true;
                        } else if (Conventions.AUTH_PROVIDER_GOOGLE.equalsIgnoreCase(login.getProvider())) {
                            hasGoogleAcc = true;
                        } else if (Conventions.AUTH_PROVIDER_TWITTER.equalsIgnoreCase(login.getProvider())) {
                            hasTwitterAcc = true;
                        }
                    }
                    Map<String, Boolean> links = new HashMap<>();
                    links.put(Conventions.AUTH_PROVIDER_FACEBOOK, hasFacebookAcc);
                    links.put(Conventions.AUTH_PROVIDER_GOOGLE, hasGoogleAcc);
                    links.put(Conventions.AUTH_PROVIDER_TWITTER, hasTwitterAcc);

                    mUserHelper.addSocialNetworkLink(credentials.getUserUuid(), links);
                }
            }
            EventBus.getDefault().post(new PullUserAccountDataResult(responseGetUser.isSuccess() && responseGetSocial.isSuccess()));
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void loadAttachment(Credentials credentials, String attachmentUuid) {
        try {
            Response<ResponseBody> res = mBeacomeApi.getAttachmentContent(credentials.getToken(), attachmentUuid, 0).execute();
            InputStream is;
            if (res.isSuccess() && (is = res.body().byteStream()) != null) {
                Attachment attachment = mAttachmentHelper.getAttachment(attachmentUuid);
                Uri fileUri = FileCacheUtil.saveAttachment(attachment, is);
                is.close();
                if (fileUri != null) {
                    mAttachmentHelper.setAttachmentFileUri(attachmentUuid, fileUri);
                }
                EventBus.getDefault().post(new AttachmentLoadResult(fileUri != null));
            }
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void pullCardsByBeacon(@NonNull Credentials credentials, @NonNull String beaconUuid) {
        GetCardsByBeaconResult event;
        try {
            Response<CardModel[]> response = mBeacomeApi.getCardByBeacon(credentials.getToken(), beaconUuid).execute();
            CardModel[] cards;
            if (response.isSuccess() && (cards = response.body()) != null) {
                processHistoryCards(credentials, cards);
                List<String> cardUuids = new ArrayList<>(cards.length);
                for (CardModel card : cards) {
                    if (!card.isDelete()) {
                        cardUuids.add(card.getUuid());
                    }
                }
                event = new GetCardsByBeaconResult(beaconUuid, cardUuids, true);
            } else {
                event = new GetCardsByBeaconResult(beaconUuid, null, false);
            }
        } catch (IOException e) {
            event = new GetCardsByBeaconResult(beaconUuid, null, false);
            Log.d(TAG, e.getMessage());
        }
        EventBus.getDefault().post(event);
    }

    @Override
    protected void setHistoryFavoriteMark(@NonNull Credentials credentials, @NonNull String cardUuid, boolean isFavorite) {
        if (credentials.getToken() == null) {
            mHistoryHelper.setHistoryRecordFavorite(credentials.getUserUuid(), cardUuid, isFavorite);
            EventBus.getDefault().post(new SetHistoryFavoriteResult(true, cardUuid, isFavorite));
            return;
        }
        try {
            Response<Void> response = mBeacomeApi.setCardFavorite(credentials.getToken(), cardUuid, isFavorite).execute();
            if (response.isSuccess()) {
                mHistoryHelper.setHistoryRecordFavorite(credentials.getUserUuid(), cardUuid, isFavorite);
                EventBus.getDefault().post(new SetHistoryFavoriteResult(true, cardUuid, isFavorite));
            } else {
                EventBus.getDefault().post(new SetHistoryFavoriteResult(false, cardUuid, !isFavorite));
            }
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void setUserData(Credentials credentials, String name, String email) {
        User user = mUserHelper.getUser(credentials.getUserUuid());
        UserModel userModel = new UserModel(user.getUuid(), name, email, user.getVersion());
        try {
            Response<Void> response = mBeacomeApi.setUserProfile(credentials.getToken(), userModel).execute();
            if (response.isSuccess()) {
                mUserHelper.setUserNameAndEmail(user.getUuid(), name, email);
                EventBus.getDefault().post(new UserAccountUpdatingResult(true, UserAccountUpdatingResult.UpdatedDataType.NAME_OR_EMAIL));
            } else {
                EventBus.getDefault().post(new UserAccountUpdatingResult(false, UserAccountUpdatingResult.UpdatedDataType.NAME_OR_EMAIL));
            }
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void setUserImage(@NonNull Credentials credentials, @NonNull Uri imageUri) {
        if (credentials.getToken() == null) {
            return;
        }
        User user = mUserHelper.getUser(credentials.getUserUuid());
        UserModel userModel = new UserModel(user.getUuid(), user.getName(), user.getEmail(), user.getVersion());
        try {
            RequestBody pic = RequestBody.create(MediaType.parse("multipart/form-data"), BitmapOpUtil.getSampledBitmap(this, imageUri));
            Response<ResponseBody> response = mBeacomeApi.setUserImage(credentials.getToken(), pic).execute();
            if (response.isSuccess()) {
                Uri uri = getUserImageUri(userModel.getUuid(), userModel.getVersion() + 1);
                mUserHelper.setUserImage(userModel.getUuid(), uri);
                EventBus.getDefault().post(new UserAccountUpdatingResult(true, UserAccountUpdatingResult.UpdatedDataType.IMAGE));
            } else {
                EventBus.getDefault().post(new UserAccountUpdatingResult(false, UserAccountUpdatingResult.UpdatedDataType.IMAGE));
            }
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void addUserSocialNetworkAccount(Credentials credentials, LoginCredentials[] loginCredentials) {
        if (loginCredentials == null || credentials.getToken() == null) {
            return;
        }
        User user = mUserHelper.getUser(credentials.getUserUuid());
        try {
            for (LoginCredentials loginCredential : loginCredentials) {
                ProviderModel providerModel = new ProviderModel(loginCredential.getAuthProvider(),
                        loginCredential.getAuthToken(), loginCredential.getAuthSecret());
                Response<Void> response = mBeacomeApi.addSocialAccount(credentials.getToken(), providerModel).execute();
                if (response.isSuccess()) {
                    Map<String, Boolean> socialAccounts = new HashMap<>();
                    socialAccounts.put(providerModel.getAuthProvider(), true);
                    mUserHelper.addSocialNetworkLink(user.getUuid(), socialAccounts);
                    EventBus.getDefault().post(new UserAccountUpdatingResult(true, UserAccountUpdatingResult.UpdatedDataType.SOCIAL_NETWORKS_LINKS));
                } else {
                    EventBus.getDefault().post(new UserAccountUpdatingResult(false, UserAccountUpdatingResult.UpdatedDataType.SOCIAL_NETWORKS_LINKS));
                }
            }
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void acceptInvite(@NonNull Credentials credentials, String shareUuid) {
        try {
            mBeacomeApi.acceptInvite(credentials.getToken(), shareUuid).execute();
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void setLinkedCardsToBeacon(@NonNull Credentials credentials, @NonNull String beaconUuid, ArrayList<CardLink> cardLinkList) {
        int size = cardLinkList != null ? cardLinkList.size() : 0;
        CardRelationsModel[] cardRelationsModels = new CardRelationsModel[size];
        if (cardLinkList != null) {
            for (int i = 0; i < cardRelationsModels.length; i++) {
                CardLink cardLink = cardLinkList.get(i);
                cardRelationsModels[i] = new CardRelationsModel(cardLink.getCardUuid(), cardLink.isActive());
            }
        }
        boolean isBeaconExists = mCardHelper.isBeaconExists(beaconUuid);
        BeaconModel beaconModel = new BeaconModel(beaconUuid, cardRelationsModels);
        try {
            Response<Void> response;
            if (isBeaconExists) {
                if (size == 0) {
                    response = mBeacomeApi.deleteBeacon(credentials.getToken(), beaconUuid).execute();
                } else {
                    response = mBeacomeApi.updateBeacon(credentials.getToken(), beaconUuid, beaconModel).execute();
                }
            } else {
                response = mBeacomeApi.addBeacon(credentials.getToken(), beaconModel).execute();
            }
            if (response.isSuccess()) {
                mCardHelper.setCardLinksToBeacon(beaconUuid, cardLinkList);
            }
            EventBus.getDefault().post(new SetCardLinksToBeaconResult(response.isSuccess()));
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void addInvite(Credentials credentials, String cardUuid, String email, boolean isOwner) {
        if (credentials == null || credentials.getToken() == null) {
            return;
        }
        InviteModel inviteModel = new InviteModel(UUID.randomUUID().toString().toLowerCase(), cardUuid, isOwner, email);
        try {
            Response<Void> response = mBeacomeApi.addInvite(credentials.getToken(), inviteModel).execute();
            if (response.isSuccess()) {
                pullUserCards(credentials);
            } else {
                Log.d(TAG, "Unable to add invite, " + response.code());
            }
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    @Override
    protected void saveCard(final Credentials credentials, final CardExtended card) {
        if (credentials.isAnonymous()) {
            return;
        }
        CardExtended storedCard = mCardHelper.getCard(card.getUuid(), credentials.getUserUuid());

        ArrayList<ContactModel> contacts = new ArrayList<>(card.getContacts().length);
        for (Contact contact : card.getContacts()) {
            contacts.add(new ContactModel(contact.getUuid(), contact.getContactType(), contact.getData()));
        }

        ExtendedCardModel cardModel = new ExtendedCardModel(card.getUuid(), card.getTitle(),
                card.getDescription(), contacts.toArray(new ContactModel[contacts.size()]));
        try {
            Response<Void> response;
            if (storedCard == null) {
                response = mBeacomeApi.addCard(credentials.getToken(), cardModel).execute();
            } else {
                response = mBeacomeApi.setCard(credentials.getToken(), card.getUuid(), cardModel).execute();
            }
            if (response.isSuccess()) {
                if (card.getImage() != null) {
                    if (storedCard == null || (!card.getImage().equals(storedCard.getImage()))) {
                        RequestBody pic = RequestBody.create(MediaType.parse("multipart/form-data"), BitmapOpUtil.getSampledBitmap(this, card.getImage()));
                        mBeacomeApi.setCardImage(credentials.getToken(), card.getUuid(), pic).execute();
                    }
                }
                processBeacons(credentials, card.getUuid(), card.getBeaconLinks(), storedCard != null ? storedCard.getBeaconLinks() : new Beacon[0]);
                processCardUsers(credentials, card.getUuid(), storedCard != null ? storedCard.getUsers() : null, card.getUsers());

                Attachment[] storedAttachments = storedCard != null ? storedCard.getAttachments() : null;
                Vcard[] storedVcards = storedCard != null ? storedCard.getVcards() : null;
                processAttachments(credentials, card.getUuid(), storedAttachments, card.getAttachments(), storedVcards, card.getVcards());
                pullUserCards(credentials);
            } else {
                EventBus.getDefault().post(new SaveCardException());
                Log.e(TAG, "Unable to save card");
            }
        } catch (IOException e) {
            handleIoException(e);
        }
    }

    private Attachment vcardToAttachment(Vcard vcard) {
        return new Attachment(vcard.getUuid(), Conventions.ATTACHMENT_TYPE_VCARD, Conventions.MIME_TYPE_VCARD, null, vcard.getVcfFile(), vcard.getVcfFile());
    }

    private void processAttachments(final Credentials credentials, String cardUuid, Attachment[] storedAttachments,
                                    Attachment[] attachments, Vcard[] storedVcards, Vcard[] vcards) {
        ArrayList<Attachment> newAttachments = new ArrayList<>();
        ArrayList<Attachment> modifiedAttachments = new ArrayList<>();
        ArrayList<Attachment> deletedAttachments = new ArrayList<>();

        for (Vcard vcard : vcards) {
            boolean isNew = true;
            boolean isModified = true;
            if (storedVcards != null) {
                for (Vcard storedVcard : storedVcards) {
                    if (storedVcard.getUuid().equals(vcard.getUuid())) {
                        isNew = false;
                        if (storedVcard.getVcfFile().equals(vcard.getVcfFile())) {
                            isModified = false;
                        }
                        break;
                    }
                }
            }
            if (isModified && !isNew) {
                modifiedAttachments.add(vcardToAttachment(vcard));
            } else if (isNew) {
                newAttachments.add(vcardToAttachment(vcard));
            }
        }

        if (storedVcards != null) {
            for (Vcard storedVcard : storedVcards) {
                boolean isDeleted = true;
                for (Vcard vcard : vcards) {
                    if (storedVcard.getUuid().equals(vcard.getUuid())) {
                        isDeleted = false;
                        break;
                    }
                }
                if (isDeleted) {
                    deletedAttachments.add(vcardToAttachment(storedVcard));
                }
            }
        }

        for (Attachment attachment : attachments) {
            boolean isNew = true;
            boolean isModified = true;
            if (storedAttachments != null) {
                for (Attachment storedAttachment : storedAttachments) {
                    if (storedAttachment.getUuid().equals(attachment.getUuid())) {
                        isNew = false;
                        if (storedAttachment.getDescription().equals(attachment.getDescription())
                                && storedAttachment.getType() == attachment.getType()) {
                            isModified = false;
                        }
                        break;
                    }
                }
            }
            if (isModified && !isNew) {
                modifiedAttachments.add(attachment);
            } else if (isNew) {
                newAttachments.add(attachment);
            }
        }

        if (storedAttachments != null) {
            for (Attachment storedAttachment : storedAttachments) {
                boolean isDeleted = true;
                for (Attachment attachment : attachments) {
                    if (storedAttachment.getUuid().equals(attachment.getUuid())) {
                        isDeleted = false;
                        break;
                    }
                }
                if (isDeleted) {
                    deletedAttachments.add(storedAttachment);
                }
            }
        }

        ExecutorService executor = Executors.newCachedThreadPool();

        List<Future<Void>> futures = new ArrayList<>();
        for (final Attachment att : newAttachments) {
            String dataArray = null;
            try {
                InputStream is = getContentResolver().openInputStream(att.getFileUri());
                if (is != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[16384];
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    dataArray = Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT);
                    buffer.close();
                } else {
                    Log.e(TAG, "Unable to read attachment");
                }
            } catch (IOException ignore) {
            }

            final ExtendedAttachmentModel attachmentModel = new ExtendedAttachmentModel(att.getUuid(),
                    cardUuid, att.getDescription(), att.getType(), att.getMimeType(), false, 0, dataArray);

            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (!mBeacomeApi.addAttachment(credentials.getToken(), attachmentModel).execute().isSuccess()) {
                        Log.e(TAG, "Unable to add attachment");
                    }
                    return null;
                }
            }));
        }

        for (final Attachment att : modifiedAttachments) {
            final AttachmentModel attachmentModel = new AttachmentModel(att.getUuid(), att.getDescription(), att.getType(), att.getMimeType(), false, 0);
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (!mBeacomeApi.setAttachment(credentials.getToken(), attachmentModel).execute().isSuccess()) {
                        Log.e(TAG, "Unable to update attachment");
                    }
                    return null;
                }
            }));
        }

        for (final Attachment att : deletedAttachments) {
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (!mBeacomeApi.deleteAttachment(credentials.getToken(), att.getUuid()).execute().isSuccess()) {
                        Log.e(TAG, "Unable to delete attachment");
                    }
                    return null;
                }
            }));
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ignore) {
            }
        }
        executor.shutdown();
    }

    @Override
    protected void linkCardToBeacons(Credentials credentials, String cardUuid, ArrayList<Beacon> beacons) {
        Beacon[] actualBeacons = beacons.toArray(new Beacon[beacons.size()]);
        Beacon[] storedBeacons = mCardHelper.getBeaconsByCard(cardUuid, credentials.getUserUuid());
        processBeacons(credentials, cardUuid, actualBeacons, storedBeacons);
        pullUserCards(credentials);
    }

    private void processBeacons(final Credentials credentials, String cardUuid, Beacon[] actualBeacons, Beacon[] storedBeacons) {
        ArrayList<Beacon> removedBeaconsList = new ArrayList<>();
        for (Beacon storedBeacon : storedBeacons) {
            boolean isRemoved = true;
            for (Beacon newBeacon : actualBeacons) {
                if (storedBeacon.getBeaconUuid().equals(newBeacon.getBeaconUuid())) {
                    isRemoved = false;
                    break;
                }
            }
            if (isRemoved) {
                removedBeaconsList.add(storedBeacon);
            }
        }

        for (int i = 0; i < removedBeaconsList.size(); i++) {
            Beacon beacon = removedBeaconsList.get(i);
            ArrayList<CardLink> cardLinks = new ArrayList<>();
            for (CardLink cardLink : beacon.getCardLinks()) {
                if (!cardLink.getCardUuid().equals(cardUuid)) {
                    cardLinks.add(cardLink);
                }
            }
            removedBeaconsList.set(i, new Beacon(beacon.getBeaconUuid(), cardLinks.toArray(new CardLink[cardLinks.size()])));
        }
        Beacon[] removedBeacons = removedBeaconsList.toArray(new Beacon[removedBeaconsList.size()]);
        Beacon[] resultBeacons = new Beacon[actualBeacons.length + removedBeaconsList.size()];
        System.arraycopy(actualBeacons, 0, resultBeacons, 0, actualBeacons.length);
        System.arraycopy(removedBeacons, 0, resultBeacons, actualBeacons.length, removedBeacons.length);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        List<String> unprocessedBeaconsUuids = new ArrayList<>();
        List<Future<String>> futures = new ArrayList<>(actualBeacons.length);
        for (final Beacon beacon : resultBeacons) {
            futures.add(executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    boolean isSuccess;
                    if (beacon.getCardLinks().length > 0) {
                        ArrayList<CardRelationsModel> relations = new ArrayList<>(beacon.getCardLinks().length);
                        for (CardLink cardLink : beacon.getCardLinks()) {
                            relations.add(new CardRelationsModel(cardLink.getCardUuid(), cardLink.isActive()));
                        }
                        BeaconModel beaconModel = new BeaconModel(beacon.getBeaconUuid(), relations.toArray(new CardRelationsModel[relations.size()]));
                        Response<Void> responseSetBeacon = mBeacomeApi.updateBeacon(credentials.getToken(), beaconModel.getUuid(), beaconModel).execute();
                        if (responseSetBeacon.code() == 400) {
                            responseSetBeacon = mBeacomeApi.addBeacon(credentials.getToken(), beaconModel).execute();

                        }
                        isSuccess = responseSetBeacon.isSuccess();
                    } else {
                        Response<Void> response = mBeacomeApi.deleteBeacon(credentials.getToken(), beacon.getBeaconUuid()).execute();
                        isSuccess = response.isSuccess();
                    }
                    if (!isSuccess) {
                        return beacon.getBeaconUuid();
                    } else {
                        return null;
                    }
                }
            }));
        }

        for (Future<String> future : futures) {
            try {
                String beaconUuid = future.get();
                if (beaconUuid != null) {
                    unprocessedBeaconsUuids.add(beaconUuid);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "Exception occurs with beacon processing");
            }
        }

        if (unprocessedBeaconsUuids.size() > 0) {
            EventBus.getDefault().post(new LinkBeaconsException(unprocessedBeaconsUuids));
        }
    }

    private void processCardUsers(final Credentials credentials, final String cardUuid, CardUser[] storedUsers, CardUser[] actualCardUsers) {
        ArrayList<CardUser> addCardUsers = new ArrayList<>();
        ArrayList<CardUser> updateCardUsers = new ArrayList<>();
        ArrayList<CardUser> deleteCardUsers = new ArrayList<>();

        ArrayList<CardUser> addInvite = new ArrayList<>();
        ArrayList<CardUser> updateInvite = new ArrayList<>();
        ArrayList<CardUser> deleteInvite = new ArrayList<>();

        if (storedUsers != null) {
            for (CardUser actual : actualCardUsers) {
                boolean isNew = true;
                boolean isInvite = actual.getUuid() == null;
                for (CardUser stored : storedUsers) {
                    if (isInvite) {
                        if (actual.getShareUuid() != null && actual.getShareUuid().equals(stored.getShareUuid())) {
                            if (!actual.equals(stored)) {
                                updateInvite.add(actual);
                            }
                            isNew = false;
                            break;
                        }
                    } else {
                        if (actual.getUuid().equals(stored.getUuid())) {
                            if (!actual.equals(stored)) {
                                updateCardUsers.add(actual);
                            }
                            isNew = false;
                            break;
                        }
                    }
                }
                if (isNew) {
                    if (isInvite) {
                        addInvite.add(actual);
                    } else {
                        addCardUsers.add(actual);
                    }
                }
            }
            for (CardUser stored : storedUsers) {
                boolean isDeleted = true;
                boolean isInvite = stored.getUuid() == null;
                for (CardUser actual : actualCardUsers) {
                    if (isInvite) {
                        if (actual.getShareUuid() != null && actual.getShareUuid().equals(stored.getShareUuid())) {
                            isDeleted = false;
                            break;
                        }
                    } else {
                        if (actual.getUuid() != null && actual.getUuid().equals(stored.getUuid())) {
                            isDeleted = false;
                            break;
                        }
                    }
                }
                if (isDeleted) {
                    if (isInvite) {
                        deleteInvite.add(stored);
                    } else {
                        deleteCardUsers.add(stored);
                    }
                }
            }
        } else {
            for (CardUser cardUser : actualCardUsers) {
                if (cardUser.getUuid() != null) {
                    addCardUsers.add(cardUser);
                } else if (cardUser.getShareUuid() != null) {
                    addInvite.add(cardUser);
                }
            }
        }

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Void>> futures = new ArrayList<>();
        for (final CardUser cardUser : addCardUsers) {
            final UserPermissionModel userPermissionModel = new UserPermissionModel(cardUser.getUuid(), cardUser.isOwner());
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mBeacomeApi.addPermission(credentials.getToken(), cardUuid, userPermissionModel).execute();
                    return null;
                }
            }));
        }

        for (final CardUser cardUser : updateCardUsers) {
            final UserPermissionModel userPermissionModel = new UserPermissionModel(cardUser.getUuid(), cardUser.isOwner());
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mBeacomeApi.setPermission(credentials.getToken(), cardUuid, userPermissionModel).execute();
                    return null;
                }
            }));
        }

        for (final CardUser cardUser : deleteCardUsers) {
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        Response<Void> re = mBeacomeApi.deletePermission(credentials.getToken(), cardUuid, cardUser.getUuid()).execute();
                        Log.e(TAG, re.message());
                    } catch (Exception e) {
                        handleIoException(e);
                    }

                    return null;
                }
            }));
        }

        for (final CardUser invite : addInvite) {
            final InviteModel inviteModel = new InviteModel(invite.getShareUuid(), cardUuid, invite.isOwner(), invite.getEmail());
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mBeacomeApi.addInvite(credentials.getToken(), inviteModel).execute();
                    return null;
                }
            }));
        }

        for (final CardUser invite : updateInvite) {
            final InviteModel inviteModel = new InviteModel(invite.getShareUuid(), cardUuid, invite.isOwner(), invite.getEmail());
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mBeacomeApi.setInvite(credentials.getToken(), inviteModel, inviteModel.getUuid()).execute();
                    return null;
                }
            }));
        }

        for (final CardUser invite : deleteInvite) {
            final InviteModel inviteModel = new InviteModel(invite.getShareUuid(), cardUuid, invite.isOwner(), invite.getEmail());
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mBeacomeApi.deleteInvite(credentials.getToken(), inviteModel.getUuid()).execute();
                    return null;
                }
            }));
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "Unable to process card user");
            }
        }
        executor.shutdown();
    }

    @Override
    protected void deleteCard(Credentials credentials, String cardUuid) {
        if (credentials.isAnonymous()) {
            return;
        }
        try {
            Response<Void> response = mBeacomeApi.deleteCard(credentials.getToken(), cardUuid).execute();
            if (response.isSuccess()) {
                pullUserCards(credentials);
            } else {
                EventBus.getDefault().post(new DeleteCardException());
            }
        } catch (IOException e) {
            handleIoException(e);
        }

    }

    @Override
    protected void refreshSession(@NonNull Credentials credentials) {
        Credentials refreshedCredentials = refreshToken(credentials);
        SignInResult result = new SignInResult(refreshedCredentials != null, refreshedCredentials, false);
        EventBus.getDefault().post(result);
    }

    private Uri getCardImageUri(String uuid, long version) {
        return Uri.parse(String.format("%sCards/%s/Logo?v=%s", RestServiceFactory.getBaseUri(), uuid, version));
    }

    private Uri getUserImageUri(String uuid, long version) {
        return Uri.parse(String.format("%sUsers/%s/Photo?v=%s", RestServiceFactory.getBaseUri(), uuid, version));
    }

    private void handleIoException(Throwable e) {
        Log.d(TAG, e.getMessage());
        EventBus.getDefault().post(new RetrofitIoExceptionResult(e.getMessage()));
    }

    private Credentials refreshToken(Credentials credentials) {
        Credentials refreshedCredentials = null;
        try {
            Response<Void> response = mBeacomeApi.refreshToken(credentials.getToken()).execute();
            if (response.isSuccess()) {
                String tokenStr = response.headers().get(Conventions.AUTHORIZATION_HEADER);
                long expireDate = Long.valueOf(response.headers().get(Conventions.EXPIRE_DATE_HEADER));
                if (tokenStr != null) {
                    refreshedCredentials = new Credentials(credentials.getUserUuid(), tokenStr, expireDate);
                }
            }
        } catch (IOException e) {
            handleIoException(e);
        }
        return refreshedCredentials;
    }
}
