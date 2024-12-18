package tannguyen.st.ueh.edu.vn.socialapp_dack.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.MessageModel;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserProfileDB";
    private static final int DATABASE_VERSION = 4; // Tăng version để kích hoạt `onUpgrade`

    // Bảng Users
    public static final String TABLE_USERS = "Users";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_COVER = "cover";

    // Bảng Posts
    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_POSTER_NAME = "poster_name";

    // Bảng Messages
    public static final String TABLE_MESSAGES = "Messages";
    public static final String COLUMN_MESSAGE_ID = "messageId";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECEIVER = "receiver";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_MESSAGE_TIMESTAMP = "timestamp";
    public static final String COLUMN_IS_SEEN = "isSeen";

    private SQLiteDatabase writableDb; // Cơ sở dữ liệu ghi

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_UID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_IMAGE + " TEXT, " +
                COLUMN_COVER + " TEXT)";
        db.execSQL(createUsersTable);

        // Tạo bảng Posts
        String createPostsTable = "CREATE TABLE " + TABLE_POSTS + "(" +
                COLUMN_POST_ID + " TEXT PRIMARY KEY, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_IMAGE_URL + " TEXT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_POSTER_NAME + " TEXT" +
                ")";
        db.execSQL(createPostsTable);

        // Tạo bảng Messages
        String createMessagesTable = "CREATE TABLE " + TABLE_MESSAGES + "(" +
                COLUMN_MESSAGE_ID + " TEXT PRIMARY KEY, " +
                COLUMN_SENDER + " TEXT, " +
                COLUMN_RECEIVER + " TEXT, " +
                COLUMN_MESSAGE + " TEXT, " +
                COLUMN_MESSAGE_TIMESTAMP + " TEXT, " +
                COLUMN_IS_SEEN + " INTEGER)";
        db.execSQL(createMessagesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ khi nâng cấp cơ sở dữ liệu
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // Lấy SQLiteDatabase ghi và lưu trữ để tái sử dụng
    private SQLiteDatabase getWritableDb() {
        if (writableDb == null || !writableDb.isOpen()) {
            writableDb = this.getWritableDatabase();
        }
        return writableDb;
    }

    public boolean isUserExists(String uid) {
        SQLiteDatabase db = getWritableDb();
        Cursor cursor = db.query(
                TABLE_USERS,            // Table name
                new String[]{COLUMN_UID}, // Select uid column
                COLUMN_UID + "=?",     // WHERE clause
                new String[]{uid},     // WHERE arguments
                null, null, null       // Group by, having, order by
        );
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public void insertOrUpdateUser(String uid, String name, String email, String phone, String imagePath, String coverPath) {
        SQLiteDatabase db = getWritableDb();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_IMAGE, imagePath); // Lưu đường dẫn nội bộ thay vì URL
        values.put(COLUMN_COVER, coverPath); // Lưu đường dẫn nội bộ thay vì URL

        if (isUserExists(uid)) {
            int rowsAffected = db.update(TABLE_USERS, values, COLUMN_UID + "=?", new String[]{uid});
            Log.d("SQLiteHelper", "Updated user with UID: " + uid + ", Rows affected: " + rowsAffected);
        } else {
            long rowId = db.insert(TABLE_USERS, null, values);
            Log.d("SQLiteHelper", "Inserted new user with UID: " + uid + ", Row ID: " + rowId);
        }
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = getWritableDb();
        return db.query(TABLE_USERS, null, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
    }

    public void updateUserInfo(String uid, String key, String value) {
        SQLiteDatabase db = getWritableDb();
        ContentValues values = new ContentValues();

        // Tùy thuộc vào key, bạn cập nhật giá trị tương ứng trong SQLite
        switch (key) {
            case "name":
                values.put(COLUMN_NAME, value);
                break;
            case "phone":
                values.put(COLUMN_PHONE, value);
                break;
            case "email":
                values.put(COLUMN_EMAIL, value);
                break;
            case "image":
                values.put(COLUMN_IMAGE, value);
                break;
            case "cover":
                values.put(COLUMN_COVER, value);
                break;
        }

        // Cập nhật thông tin vào SQLite
        db.update(TABLE_USERS, values, COLUMN_UID + "=?", new String[]{uid});
    }

    // Đóng kết nối khi không còn cần thiết
    @Override
    public synchronized void close() {
        if (writableDb != null && writableDb.isOpen()) {
            writableDb.close();
        }
        super.close();
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }

    public void insertMessage(MessageModel message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(COLUMN_SENDER, message.getSender());
        values.put(COLUMN_RECEIVER, message.getReceiver());
        values.put(COLUMN_MESSAGE, message.getMessage());
        values.put(COLUMN_MESSAGE_TIMESTAMP, message.getTimestamp());
        values.put(COLUMN_IS_SEEN, message.isSeen() ? 1 : 0); // Convert boolean to int

        // Chèn dữ liệu
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public Cursor getMessages(String senderUid, String receiverUid) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_MESSAGES +
                        " WHERE (" + COLUMN_SENDER + "=? AND " + COLUMN_RECEIVER + "=?) OR " +
                        "(" + COLUMN_SENDER + "=? AND " + COLUMN_RECEIVER + "=?)",
                new String[]{senderUid, receiverUid, receiverUid, senderUid});
    }

    public boolean isMessageExists(String messageId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{COLUMN_MESSAGE_ID}, COLUMN_MESSAGE_ID + "=?",
                new String[]{messageId}, null, null, null);
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }

    public Cursor getAllPosts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_POSTS, null);
    }

    public void insertOrUpdatePost(Post post) {
        SQLiteDatabase db = getWritableDb();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_ID, post.getId());
        values.put(COLUMN_TITLE, post.getTitle());
        values.put(COLUMN_CONTENT, post.getContent());
        values.put(COLUMN_TIMESTAMP, post.getTimestamp());
        values.put(COLUMN_IMAGE_URL, post.getImageUrl());
        values.put(COLUMN_USER_ID, post.getUserId());
        values.put(COLUMN_POSTER_NAME, post.getPosterName());

        // Kiểm tra bài viết đã tồn tại
        if (isPostExists(post.getId())) {
            int rowsAffected = db.update(TABLE_POSTS, values, COLUMN_POST_ID + "=?", new String[]{post.getId()});
            if (rowsAffected > 0) {
                Log.d("SQLiteHelper", "Updated post: " + post.getId());
            } else {
                Log.e("SQLiteHelper", "Failed to update post: " + post.getId());
            }
        } else {
            long rowId = db.insert(TABLE_POSTS, null, values);
            if (rowId != -1) {
                Log.d("SQLiteHelper", "Inserted new post: " + post.getId());
            } else {
                Log.e("SQLiteHelper", "Failed to insert post: " + post.getId());
            }
        }
    }

    public boolean isPostExists(String postId) {
        SQLiteDatabase db = getWritableDb();
        Cursor cursor = db.query(
                TABLE_POSTS,
                new String[]{COLUMN_POST_ID},
                COLUMN_POST_ID + "=?",
                new String[]{postId},
                null, null, null
        );
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }


    // Phương thức để lấy bài viết của người dùng theo UID
    public Cursor getPostsByUserId(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn lấy bài viết của người dùng
        String query = "SELECT * FROM " + TABLE_POSTS + " WHERE " + COLUMN_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{userId});
    }

    // Lưu avatar của người dùng
    public void saveUserAvatar(String uid, String avatarPath) {
        SQLiteDatabase db = getWritableDb();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_IMAGE, avatarPath);

        if (isUserExists(uid)) {
            db.update(TABLE_USERS, values, COLUMN_UID + "=?", new String[]{uid});
        } else {
            values.put(COLUMN_NAME, "Unknown"); // Thêm giá trị mặc định nếu người dùng chưa tồn tại
            db.insert(TABLE_USERS, null, values);
        }
    }

    // Lấy đường dẫn avatar từ SQLite
    public String getUserAvatar(String uid) {
        SQLiteDatabase db = getWritableDb();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_IMAGE},
                COLUMN_UID + "=?",
                new String[]{uid},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
            cursor.close();
            return avatarPath;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null; // Không có avatar
    }

    // Lưu hoặc cập nhật tên người dùng
    public void saveUserName(String uid, String name) {
        SQLiteDatabase db = getWritableDb();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_NAME, name);

        if (isUserExists(uid)) {
            db.update(TABLE_USERS, values, COLUMN_UID + "=?", new String[]{uid});
        } else {
            db.insert(TABLE_USERS, null, values);
        }
    }

    // Lấy tên người dùng từ SQLite
    public String getUserName(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_NAME},
                COLUMN_UID + "=?",
                new String[]{uid},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            cursor.close();
            return name;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null; // Không có tên trong SQLite
    }


}
