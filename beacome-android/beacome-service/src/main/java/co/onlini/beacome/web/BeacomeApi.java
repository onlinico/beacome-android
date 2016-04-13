package co.onlini.beacome.web;

import co.onlini.beacome.web.model.AttachmentModel;
import co.onlini.beacome.web.model.BeaconModel;
import co.onlini.beacome.web.model.CardModel;
import co.onlini.beacome.web.model.ExtendedAttachmentModel;
import co.onlini.beacome.web.model.ExtendedCardModel;
import co.onlini.beacome.web.model.HistoryModel;
import co.onlini.beacome.web.model.InviteModel;
import co.onlini.beacome.web.model.LoginModel;
import co.onlini.beacome.web.model.ProviderModel;
import co.onlini.beacome.web.model.UserCardModel;
import co.onlini.beacome.web.model.UserModel;
import co.onlini.beacome.web.model.UserPermissionModel;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;


public interface BeacomeApi {
    @POST("Account/SignIn")
    Call<UserModel> signIn(@Body ProviderModel providerModel);

    @POST("Account/RefreshToken")
    Call<Void> refreshToken(@Header("authorization") String authToken);

    @GET("Beacons/{beaconId}/Cards")
    Call<CardModel[]> getCardByBeacon(@Header("authorization") String authToken, @Path("beaconId") String beaconUuid);

    @GET("Cards")
    Call<CardModel[]> getHistoryCards(@Header("authorization") String authToken, @Query("rowversion") long version, @Query("cardIds") String... cardUuids);

    @POST("Beacons/Add")
    Call<Void> addBeacon(@Header("authorization") String authToken, @Body BeaconModel beacon);

    @PUT("Beacons/{beaconId}")
    Call<Void> updateBeacon(@Header("authorization") String authToken, @Path("beaconId") String beaconUuid, @Body BeaconModel beacon);

    @DELETE("Beacons/{beaconId}")
    Call<Void> deleteBeacon(@Header("authorization") String authToken, @Path("beaconId") String beaconUuid);

    @POST("Cards/Add")
    Call<Void> addCard(@Header("authorization") String authToken, @Body ExtendedCardModel card);

    @PUT("Cards/{cardUuid}")
    Call<Void> setCard(@Header("authorization") String authToken, @Path("cardUuid") String cardUuid, @Body ExtendedCardModel card);

    @DELETE("Cards/{cardUuid}")
    Call<Void> deleteCard(@Header("authorization") String authToken, @Path("cardUuid") String cardUuid);

    @PUT("Cards/{cardId}/{isFavorite}")
    Call<Void> setCardFavorite(@Header("authorization") String authToken, @Path("cardId") String cardUuid, @Path("isFavorite") boolean isFavorite);

    @POST("Cards/{cardId}/Logo")
    @Multipart
    Call<Void> setCardImage(@Header("authorization") String authToken, @Path("cardId") String cardUuid, @Part("logo") RequestBody image);

    @POST("Cards/{cardId}/Permission")
    Call<Void> addPermission(@Header("authorization") String authToken, @Path("cardId") String cardUuid, @Body UserPermissionModel userPermission);

    @PUT("Cards/{cardId}/Permission")
    Call<Void> setPermission(@Header("authorization") String authToken, @Path("cardId") String cardUuid, @Body UserPermissionModel userPermission);

    @DELETE("Cards/{cardId}/Permission/{userId}")
    Call<Void> deletePermission(@Header("authorization") String authToken, @Path("cardId") String cardUuid, @Path("userId") String userUuid);

    @POST("Invites/{inviteId}/Accept")
    Call<Void> acceptInvite(@Header("authorization") String authToken, @Path("inviteId") String inviteUuid);

    @POST("Invites/Add")
    Call<Void> addInvite(@Header("authorization") String authToken, @Body InviteModel invite);

    @PUT("Invites/{inviteId}")
    Call<Void> setInvite(@Header("authorization") String authToken, @Body InviteModel invite, @Path("inviteId") String inviteUuid);

    @DELETE("Invites/{inviteId}")
    Call<Void> deleteInvite(@Header("authorization") String authToken, @Path("inviteId") String inviteUuid);

    @GET("Users/{id}")
    Call<UserModel> getUser(@Header("authorization") String authToken, @Path("id") String userUuid, @Query("rowversion") long version);

    @PUT("Users/Current/Profile")
    Call<Void> setUserProfile(@Header("authorization") String authToken, @Body UserModel userModel);

    @GET("Users/Current/Logins")
    Call<LoginModel[]> getUserSocialAccounts(@Header("authorization") String authToken);

    @POST("Users/Current/Provider")
    Call<Void> addSocialAccount(@Header("authorization") String authToken, @Body ProviderModel provider);

    @POST("/api/v1/Users/Current/Photo")
    @Multipart
    Call<ResponseBody> setUserImage(@Header("authorization") String authToken, @Part("photo") RequestBody image);

    @GET("Users/Current/History")
    Call<HistoryModel[]> getHistory(@Header("authorization") String authToken, @Query("rowversion") long version);

    @GET("Users/Current/Cards")
    Call<UserCardModel[]> getCards(@Header("authorization") String authToken, @Query("rowversion") long version);

    @GET("Users/Current/Cards/Users")
    Call<UserModel[]> getUsersAssignedToCurrentUserCards(@Header("authorization") String authToken, @Query("rowversion") long version);

    @GET("Attachments/{attachmentId}")
    Call<AttachmentModel> getAttachment(@Header("authorization") String authToken, @Path("attachmentId") String attachmentUuid, @Query("rowversion") long version);

    @GET("Attachments/{attachmentId}/Content")
    @Streaming
    Call<ResponseBody> getAttachmentContent(@Header("authorization") String authToken, @Path("attachmentId") String attachmentUuid, @Query("rowversion") long version);

    @POST("Attachments/Add")
    Call<Void> addAttachment(@Header("authorization") String authToken,
                             @Body ExtendedAttachmentModel attachmentModel);

    @PUT("Attachments/Update")
    Call<Void> setAttachment(@Header("authorization") String authToken, @Body AttachmentModel attachmentModel);

    @DELETE("Attachments/{attachmentId}/Delete")
    Call<AttachmentModel> deleteAttachment(@Header("authorization") String authToken, @Path("attachmentId") String attachmentUuid);
}
