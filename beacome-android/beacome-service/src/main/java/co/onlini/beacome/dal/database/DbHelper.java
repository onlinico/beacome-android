package co.onlini.beacome.dal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 75;
    private static final String DB_NAME = "Beacome.sqlite";

    private static final String HISTORY_TABLE_CREATE = "CREATE TABLE "
            + DbConst.HistoryTable.TABLE_NAME + " ("
            + DbConst.HistoryTable.CARD_UUID_COLUMN + " TEXT NOT NULL, "
            + DbConst.HistoryTable.USER_UUID_COLUMN + " TEXT NOT NULL,  "
            + DbConst.HistoryTable.LAST_DISCOVERY_DATE_COLUMN + " INTEGER, "
            + DbConst.HistoryTable.IS_FAVORITE_COLUMN + " INTEGER,"
            + DbConst.HistoryTable.VERSION_COLUMN + " INTEGER,"
            + " PRIMARY KEY(" + DbConst.HistoryTable.USER_UUID_COLUMN + ", " + DbConst.HistoryTable.CARD_UUID_COLUMN + "), " +
            "FOREIGN KEY(" + DbConst.HistoryTable.CARD_UUID_COLUMN + ") " +
            "REFERENCES " + DbConst.CardsTable.TABLE_NAME + "(" + DbConst.CardsTable.CARD_UUID_COLUMN + ")" +
            "ON DELETE CASCADE, " +
            "FOREIGN KEY(" + DbConst.HistoryTable.USER_UUID_COLUMN + ") " +
            "REFERENCES " + DbConst.UsersTable.TABLE_NAME + "(" + DbConst.UsersTable.USER_UUID_COLUMN + ")" +
            "ON DELETE CASCADE " +
            ");";

    private static final String CARDS_TABLE_CREATE = "CREATE TABLE "
            + DbConst.CardsTable.TABLE_NAME + " ("
            + DbConst.CardsTable.CARD_UUID_COLUMN + " TEXT PRIMARY KEY, "
            + DbConst.CardsTable.TITLE_COLUMN + " TEXT, "
            + DbConst.CardsTable.DESCRIPTION_COLUMN + " TEXT, "
            + DbConst.CardsTable.VERSION_COLUMN + " INTEGER, "
            + DbConst.CardsTable.IS_DELETED_COLUMN + " INTEGER, "
            + DbConst.CardsTable.IMAGE_URL_COLUMN + " TEXT " +
            ");";

    private static final String ATTRIBUTES_TABLE_CREATE = "CREATE TABLE "
            + DbConst.ContactsTable.TABLE_NAME + " ("
            + DbConst.ContactsTable.CONTACT_UUID_COLUMN + " TEXT PRIMARY KEY, "
            + DbConst.ContactsTable.CARD_UUID_COLUMN + " TEXT NOT NULL, "
            + DbConst.ContactsTable.TYPE_COLUMN + " TEXT, "
            + DbConst.ContactsTable.VALUE_COLUMN + " TEXT, " +
            "FOREIGN KEY(" + DbConst.ContactsTable.CARD_UUID_COLUMN + ") " +
            "REFERENCES " + DbConst.CardsTable.TABLE_NAME + "(" + DbConst.CardsTable.CARD_UUID_COLUMN + ")" +
            "ON DELETE CASCADE);";

    private static final String VCARD_TABLE_CREATE = "CREATE TABLE "
            + DbConst.VCardsTable.TABLE_NAME + " ("
            + DbConst.VCardsTable.VCARD_UUID_COLUMN + " TEXT PRIMARY KEY, "
            + DbConst.VCardsTable.CARD_UUID_COLUMN + " TEXT, "
            + DbConst.VCardsTable.EMAIL_COLUMN + " TEXT, "
            + DbConst.VCardsTable.NAME_COLUMN + " TEXT, "
            + DbConst.VCardsTable.PHONE_COLUMN + " TEXT, "
            + DbConst.VCardsTable.VCF_URI_COLUMN + " TEXT, "
            + DbConst.VCardsTable.IMAGE_URI_COLUMN + " TEXT, "
            + DbConst.VCardsTable.IS_DELETED_COLUMN + " INTEGER, "
            + DbConst.VCardsTable.VERSION_COLUMN + " INTEGER);";

    private static final String ATTACHMENT_TABLE_CREATE = "CREATE TABLE "
            + DbConst.AttachmentTable.TABLE_NAME + " ("
            + DbConst.AttachmentTable.ATTACHMENT_UUID_COLUMN + " TEXT PRIMARY KEY, "
            + DbConst.AttachmentTable.CARD_UUID_COLUMN + " TEXT, "
            + DbConst.AttachmentTable.DESCRIPTION_COLUMN + " TEXT, "
            + DbConst.AttachmentTable.URI_COLUMN + " TEXT, "
            + DbConst.AttachmentTable.LOCAL_COPY_URI_COLUMN + " TEXT, "
            + DbConst.AttachmentTable.TYPE_COLUMN + " INTEGER, "
            + DbConst.AttachmentTable.MIME_TYPE_COLUMN + " TEXT, "
            + DbConst.AttachmentTable.IS_DELETED_COLUMN + " INTEGER, "
            + DbConst.AttachmentTable.VERSION_COLUMN + " INTEGER);";

    private static final String CARD_BEACONS_TABLE_CREATE = "CREATE TABLE "
            + DbConst.CardBeaconsTable.TABLE_NAME + " ("
            + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + " TEXT NOT NULL, "
            + DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + " TEXT NOT NULL, "
            + DbConst.CardBeaconsTable.STATE_COLUMN + " INTEGER, "
            + " PRIMARY KEY (" + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + ", "
            + DbConst.CardBeaconsTable.BEACON_UUID_COLUMN + "), " +
            "FOREIGN KEY(" + DbConst.CardBeaconsTable.CARD_UUID_COLUMN + ") " +
            "REFERENCES " + DbConst.CardsTable.TABLE_NAME + "(" + DbConst.CardsTable.CARD_UUID_COLUMN + ")" +
            "ON DELETE CASCADE);";

    private static final String USERS_TABLE_CREATE = "CREATE TABLE "
            + DbConst.UsersTable.TABLE_NAME + " ("
            + DbConst.UsersTable.USER_UUID_COLUMN + " TEXT PRIMARY KEY, "
            + DbConst.UsersTable.EMAIL_COLUMN + " TEXT, "
            + DbConst.UsersTable.FULL_NAME_COLUMN + " TEXT, "
            + DbConst.UsersTable.IMAGE_URL_COLUMN + " TEXT, "
            + DbConst.UsersTable.VERSION_COLUMN + " INTEGER, "
            + DbConst.UsersTable.HAS_FACEBOOK_LINK_COLUMN + " INTEGER, "
            + DbConst.UsersTable.HAS_GP_LINK_COLUMN + " INTEGER, "
            + DbConst.UsersTable.HAS_TWITTER_LINK_COLUMN + " INTEGER );";

    private static final String USERS_CARDS_TABLE_CREATE = "CREATE TABLE "
            + DbConst.UserCardsTable.TABLE_NAME + " ("
            + DbConst.UserCardsTable.CARD_UUID_COLUMN + " TEXT NOT NULL, "
            + DbConst.UserCardsTable.USER_UUID_COLUMN + " TEXT NOT NULL, "
            + DbConst.UserCardsTable.IS_OWNER_COLUMN + " INTEGER, "
            + " PRIMARY KEY (" + DbConst.UserCardsTable.CARD_UUID_COLUMN + ", "
            + DbConst.UserCardsTable.USER_UUID_COLUMN + "), " +
            "FOREIGN KEY(" + DbConst.UserCardsTable.CARD_UUID_COLUMN + ") " +
            "REFERENCES " + DbConst.CardsTable.TABLE_NAME + "(" + DbConst.CardsTable.CARD_UUID_COLUMN + ")" +
            "ON DELETE CASCADE, " +
            "FOREIGN KEY(" + DbConst.UserCardsTable.USER_UUID_COLUMN + ") " +
            "REFERENCES " + DbConst.UsersTable.TABLE_NAME + "(" + DbConst.UsersTable.USER_UUID_COLUMN + ")" +
            "ON DELETE CASCADE " +
            ");";

    private static final String SHARE_TABLE_CREATE = "CREATE TABLE "
            + DbConst.ShareTable.TABLE_NAME + " ("
            + DbConst.ShareTable.SHARE_UUID_COLUMN + " TEXT NOT NULL UNIQUE, "
            + DbConst.ShareTable.CARD_UUID_COLUMN + " TEXT NOT NULL, "
            + DbConst.ShareTable.EMAIL_COLUMN + " TEXT NOT NULL, "
            + DbConst.ShareTable.IS_OWNER_COLUMN + " INTEGER, " +
            "FOREIGN KEY(" + DbConst.ShareTable.CARD_UUID_COLUMN + ") " +
            "REFERENCES " + DbConst.CardsTable.TABLE_NAME + "(" + DbConst.CardsTable.CARD_UUID_COLUMN + ")" +
            "ON DELETE CASCADE " +
            ");";

    private static final String HISTORY_CARD_VIEW_CREATE = "CREATE VIEW " + DbConst.HistoryCardView.VIEW_NAME + " AS "
            + "SELECT "
            + " c." + DbConst.CardsTable.CARD_UUID_COLUMN + " AS " + DbConst.HistoryCardView.CARD_UUID_COLUMN + ","
            + " h." + DbConst.HistoryTable.USER_UUID_COLUMN + " AS " + DbConst.HistoryCardView.USER_UUID_COLUMN + ","
            + " c." + DbConst.CardsTable.TITLE_COLUMN + " AS " + DbConst.HistoryCardView.TITLE_COLUMN + ","
            + " c." + DbConst.CardsTable.VERSION_COLUMN + " AS " + DbConst.HistoryCardView.VERSION_COLUMN + ","
            + " c." + DbConst.CardsTable.DESCRIPTION_COLUMN + " AS " + DbConst.HistoryCardView.DESCRIPTION_COLUMN + ","
            + " c." + DbConst.CardsTable.IMAGE_URL_COLUMN + " AS " + DbConst.HistoryCardView.IMAGE_URL_COLUMN + ","
            + " c." + DbConst.CardsTable.IS_DELETED_COLUMN + " AS " + DbConst.HistoryCardView.IS_DELETED_COLUMN + ","
            + " c." + DbConst.CardsTable.VERSION_COLUMN + " AS " + DbConst.HistoryCardView.CARD_VERSION_COLUMN + ","
            + " h." + DbConst.HistoryTable.LAST_DISCOVERY_DATE_COLUMN + " AS " + DbConst.HistoryCardView.DISCOVERY_DATE_COLUMN + ","
            + " h." + DbConst.HistoryTable.IS_FAVORITE_COLUMN + " AS " + DbConst.HistoryCardView.IS_FAVORITE_COLUMN
            + " FROM " + DbConst.CardsTable.TABLE_NAME + " c "
            + " LEFT JOIN " + DbConst.HistoryTable.TABLE_NAME + " h"
            + " ON h." + DbConst.HistoryTable.CARD_UUID_COLUMN + "=c." + DbConst.CardsTable.CARD_UUID_COLUMN + ";";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTablesAll(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTablesAll(db);
        createTablesAll(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTablesAll(db);
        createTablesAll(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void createTablesAll(SQLiteDatabase db) {
        db.execSQL(HISTORY_TABLE_CREATE);
        db.execSQL(CARDS_TABLE_CREATE);
        db.execSQL(ATTRIBUTES_TABLE_CREATE);
        db.execSQL(VCARD_TABLE_CREATE);
        db.execSQL(CARD_BEACONS_TABLE_CREATE);
        db.execSQL(USERS_CARDS_TABLE_CREATE);
        db.execSQL(USERS_TABLE_CREATE);
        db.execSQL(SHARE_TABLE_CREATE);
        db.execSQL(HISTORY_CARD_VIEW_CREATE);
        db.execSQL(ATTACHMENT_TABLE_CREATE);
    }

    private void dropTablesAll(SQLiteDatabase db) {
        dropTable(db, DbConst.ContactsTable.TABLE_NAME);
        dropTable(db, DbConst.CardsTable.TABLE_NAME);
        dropTable(db, DbConst.HistoryTable.TABLE_NAME);
        dropTable(db, DbConst.VCardsTable.TABLE_NAME);
        dropTable(db, DbConst.CardBeaconsTable.TABLE_NAME);
        dropTable(db, DbConst.UsersTable.TABLE_NAME);
        dropTable(db, DbConst.UserCardsTable.TABLE_NAME);
        dropTable(db, DbConst.ShareTable.TABLE_NAME);
        dropTable(db, DbConst.AttachmentTable.TABLE_NAME);
        dropView(db, DbConst.HistoryCardView.VIEW_NAME);
    }

    private void dropTable(SQLiteDatabase db, String tableName) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s;", tableName));
    }

    private void dropView(SQLiteDatabase db, String viewName) {
        db.execSQL(String.format("DROP VIEW IF EXISTS %s;", viewName));
    }
}
