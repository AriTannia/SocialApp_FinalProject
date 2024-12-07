package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserProfileDB";
    private static final int DATABASE_VERSION = 3; // Cập nhật phiên bản cơ sở dữ liệu

    // Bảng người dùng
    public static final String TABLE_USERS = "users"; // Tên bảng người dùng
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IMAGE = "profileImage"; // Hình ảnh hồ sơ
    public static final String COLUMN_COVER = "coverImage"; // Hình ảnh bìa

    // Bảng bài viết
    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_POST_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_IMAGE_URL = "imageUrl";
    public static final String COLUMN_USER_ID = "userId"; // UID người dùng

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng người dùng
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_IMAGE + " TEXT, " +
                COLUMN_COVER + " TEXT" +
                ")";
        db.execSQL(createUsersTable);

        // Tạo bảng bài viết
        String createPostsTable = "CREATE TABLE " + TABLE_POSTS + "(" +
                COLUMN_POST_ID + " TEXT PRIMARY KEY, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_IMAGE_URL + " TEXT, " +
                COLUMN_USER_ID + " TEXT" +
                ")";
        db.execSQL(createPostsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    // Phương thức lấy thông tin người dùng theo email
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USERS, // Tên bảng người dùng
                null, // Lấy tất cả cột
                COLUMN_EMAIL + " = ?", // Điều kiện lọc theo email
                new String[]{email}, // Tham số để thay thế vào dấu hỏi chấm
                null, null, null
        );
    }

    // Phương thức cập nhật thông tin người dùng
    public void updateUserInfo(String userId, String profileOrCoverPhoto, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(profileOrCoverPhoto, filePath); // Cập nhật ảnh hồ sơ hoặc ảnh bìa

        // Cập nhật thông tin người dùng trong bảng
        db.update(
                TABLE_USERS, // Tên bảng người dùng
                values, // Dữ liệu mới cần cập nhật
                COLUMN_ID + " = ?", // Điều kiện lọc theo ID người dùng
                new String[]{userId} // Tham số thay thế vào dấu hỏi chấm
        );
        db.close();
    }

    // Thêm hoặc cập nhật người dùng
    public void insertOrUpdateUser(String uid, String name, String email, String phone, String image, String coverImg) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Kiểm tra xem người dùng đã tồn tại hay chưa
        Cursor cursor = db.query(
                TABLE_USERS, // Tên bảng người dùng
                null, // Lấy tất cả cột
                COLUMN_ID + " = ?", // Điều kiện lọc theo UID
                new String[]{uid}, // Tham số thay thế vào dấu hỏi chấm
                null, null, null
        );

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, uid); // UID người dùng
        values.put(COLUMN_NAME, name); // Tên người dùng
        values.put(COLUMN_EMAIL, email); // Email người dùng
        values.put(COLUMN_PHONE, phone); // Số điện thoại
        values.put(COLUMN_IMAGE, image); // Ảnh hồ sơ
        values.put(COLUMN_COVER, coverImg); // Ảnh bìa

        if (cursor != null && cursor.getCount() > 0) {
            // Nếu người dùng đã tồn tại, thực hiện cập nhật
            db.update(TABLE_USERS, values, COLUMN_ID + " = ?", new String[]{uid});
        } else {
            // Nếu người dùng chưa tồn tại, thực hiện thêm mới
            db.insert(TABLE_USERS, null, values);
        }

        db.close();
    }

    // Thêm bài viết mới
    public void addPost(Post post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_ID, post.getId());
        values.put(COLUMN_TITLE, post.getTitle());
        values.put(COLUMN_CONTENT, post.getContent());
        values.put(COLUMN_TIMESTAMP, post.getTimestamp());
        values.put(COLUMN_IMAGE_URL, post.getImageUrl());
        values.put(COLUMN_USER_ID, post.getUserId());

        db.insert(TABLE_POSTS, null, values);
        db.close();
    }

    // Lấy tất cả bài viết
    public Cursor getAllPosts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_POSTS, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");
    }

    // Lấy bài viết theo ID
    public Cursor getPostById(String postId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_POSTS, null, COLUMN_POST_ID + "=?", new String[]{postId}, null, null, null);
    }

    // Xóa bài viết
    public void deletePost(String postId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_POSTS, COLUMN_POST_ID + "=?", new String[]{postId});
        db.close();
    }
}
