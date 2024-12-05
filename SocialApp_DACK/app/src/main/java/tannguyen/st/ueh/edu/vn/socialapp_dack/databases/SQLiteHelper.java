package tannguyen.st.ueh.edu.vn.socialapp_dack.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserProfileDB";
    private static final int DATABASE_VERSION = 2; // Tăng version để kích hoạt `onUpgrade`

    // Bảng Users
    public static final String TABLE_USERS = "Users";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_COVER = "cover";

    // Bảng Posts
    private static final String TABLE_POSTS = "posts";
    private static final String COLUMN_POST_ID = "post_id"; // Cột mới
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private SQLiteDatabase writableDb; // Cơ sở dữ liệu ghi

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + "(" +
                "uid TEXT PRIMARY KEY, " +  // Sử dụng uid từ Firebase
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_IMAGE + " TEXT, " +
                COLUMN_COVER + " TEXT" + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
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

    public void addPost(Post post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, post.getId());
        values.put(COLUMN_TITLE, post.getTitle());
        values.put(COLUMN_CONTENT, post.getContent());
        values.put(COLUMN_TIMESTAMP, post.getTimestamp());
        db.insert(TABLE_POSTS, null, values);
        db.close();
    }
}
