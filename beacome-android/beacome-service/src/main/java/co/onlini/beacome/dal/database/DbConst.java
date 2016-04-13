package co.onlini.beacome.dal.database;

public class DbConst {

    public class HistoryTable {
        public static final String TABLE_NAME = "history";

        public static final String USER_UUID_COLUMN = "user_uuid";
        public static final String CARD_UUID_COLUMN = "card_uuid";
        public static final String LAST_DISCOVERY_DATE_COLUMN = "last_discovery_date";
        public static final String IS_FAVORITE_COLUMN = "is_favorite";
        public static final String VERSION_COLUMN = "version";
    }

    public class ShareTable {
        public static final String TABLE_NAME = "share";

        public static final String SHARE_UUID_COLUMN = "share_uuid";
        public static final String IS_OWNER_COLUMN = "share_is_owner";
        public static final String EMAIL_COLUMN = "share_email";
        public static final String CARD_UUID_COLUMN = "share_card_uuid";
    }

    public class CardsTable {
        public static final String TABLE_NAME = "cards";

        public static final String CARD_UUID_COLUMN = "card_uuid";
        public static final String TITLE_COLUMN = "card_title";
        public static final String IS_DELETED_COLUMN = "card_is_deleted";
        public static final String DESCRIPTION_COLUMN = "card_description";
        public static final String IMAGE_URL_COLUMN = "card_image_url";
        public static final String VERSION_COLUMN = "card_version";
    }

    @SuppressWarnings("SpellCheckingInspection")
    public class VCardsTable {
        public static final String TABLE_NAME = "vcards";

        public static final String VCARD_UUID_COLUMN = "vcard_uuid";
        public static final String CARD_UUID_COLUMN = "vcard_card_uuid";
        public static final String NAME_COLUMN = "vcard_name";
        public static final String PHONE_COLUMN = "vcard_phone";
        public static final String EMAIL_COLUMN = "vcard_email";
        public static final String IS_DELETED_COLUMN = "vcard_is_deleted";
        public static final String VCF_URI_COLUMN = "vcard_vcf_uri";
        public static final String IMAGE_URI_COLUMN = "vcard_image_uri";
        public static final String VERSION_COLUMN = "vcard_version";
    }

    public class AttachmentTable {
        public static final String TABLE_NAME = "attachments";

        public static final String ATTACHMENT_UUID_COLUMN = "attachment_uuid";
        public static final String CARD_UUID_COLUMN = "attachment_card_uuid";
        public static final String DESCRIPTION_COLUMN = "attachment_description";
        public static final String IS_DELETED_COLUMN = "attachment_is_deleted";
        public static final String TYPE_COLUMN = "attachment_type";
        public static final String MIME_TYPE_COLUMN = "attachment_mime_type";
        public static final String URI_COLUMN = "attachment_uri";
        public static final String LOCAL_COPY_URI_COLUMN = "attachment_local_copY_uri";
        public static final String VERSION_COLUMN = "attachment_version";
    }

    public class ContactsTable {
        public static final String TABLE_NAME = "card_contacts";

        public static final String CONTACT_UUID_COLUMN = "contact_uuid";
        public static final String CARD_UUID_COLUMN = "contact_card_uuid";
        public static final String VALUE_COLUMN = "contact_value";
        public static final String TYPE_COLUMN = "contact_type";
    }

    public class CardBeaconsTable {
        public static final String TABLE_NAME = "card_beacons";

        public static final String BEACON_UUID_COLUMN = "beacon_uuid";
        public static final String CARD_UUID_COLUMN = "card_uuid";
        public static final String STATE_COLUMN = "state";
    }

    public class UsersTable {
        public static final String TABLE_NAME = "users";

        public static final String USER_UUID_COLUMN = "user_uuid";
        public static final String FULL_NAME_COLUMN = "full_name";
        public static final String EMAIL_COLUMN = "email";
        public static final String IMAGE_URL_COLUMN = "image_url";
        public static final String VERSION_COLUMN = "version";
        public static final String HAS_FACEBOOK_LINK_COLUMN = "facebook_link";
        public static final String HAS_TWITTER_LINK_COLUMN = "twitter_link";
        public static final String HAS_GP_LINK_COLUMN = "google_plus_link";
    }

    public class UserCardsTable {
        public static final String TABLE_NAME = "users_cards";

        public static final String USER_UUID_COLUMN = "user_uuid";
        public static final String CARD_UUID_COLUMN = "card_uuid";
        public static final String IS_OWNER_COLUMN = "is_owner";
    }

    public class HistoryCardView {
        public static final String VIEW_NAME = "history_card_view";

        public static final String CARD_UUID_COLUMN = "card_uuid";
        public static final String USER_UUID_COLUMN = "user_uuid";
        public static final String TITLE_COLUMN = "title";
        public static final String DESCRIPTION_COLUMN = "description";
        public static final String DISCOVERY_DATE_COLUMN = "discovery_date";
        public static final String VERSION_COLUMN = "version";
        public static final String IMAGE_URL_COLUMN = "image_url";
        public static final String IS_FAVORITE_COLUMN = "is_favorite";
        public static final String IS_DELETED_COLUMN = "is_deleted";
        public static final String CARD_VERSION_COLUMN = "card_version";
    }

}