package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserProfileDB";
    private static final int DATABASE_VERSION = 3; // Cập nhật phiên bản cơ sở dữ liệu

    // Bảng người dùng
    public static final String TABLE_USERS = "Users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_COVER = "cover";

    // Bảng bài viết
    private static final String TABLE_POSTS = "posts";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_IMAGE_URL = "imageUrl";
    private static final String COLUMN_POSTER_UID = "userId"; // Thay thế posterName và posterAvatar

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng người dùng
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_IMAGE + " TEXT, " +
                COLUMN_COVER + " TEXT" + ")";
        db.execSQL(createUsersTable);

        // Tạo bảng bài viết
        String createPostsTable = "CREATE TABLE " + TABLE_POSTS + "(" +
                COLUMN_ID + " TEXT PRIMARY KEY, " + // Đặt id làm khóa chính (theo kiểu String)
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_IMAGE_URL + " TEXT, " +
                COLUMN_POSTER_UID + " TEXT" + // Lưu UID của người đăng bài
                ")";
        db.execSQL(createPostsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu có và tạo lại bảng mới khi cần
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    // Thêm hoặc cập nhật thông tin người dùng
    public long insertOrUpdateUser(String name, String email, String phone, String image, String cover) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_IMAGE, image);
        values.put(COLUMN_COVER, cover);

        long result = db.replace(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    // Lấy thông tin người dùng theo email
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
    }

    // Thêm bài viết mới
    public void addPost(Post post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, post.getId());
        values.put(COLUMN_TITLE, post.getTitle());
        values.put(COLUMN_CONTENT, post.getContent());
        values.put(COLUMN_TIMESTAMP, post.getTimestamp());
        values.put(COLUMN_IMAGE_URL, post.getImageUrl());
        values.put(COLUMN_POSTER_UID, post.getuserId()); // Lưu UID thay vì posterName và posterAvatar

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
        return db.query(TABLE_POSTS, null, COLUMN_ID + "=?", new String[]{postId}, null, null, null);
    }

    // Xóa bài viết
    public void deletePost(String postId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_POSTS, COLUMN_ID + "=?", new String[]{postId});
        db.close();
    }
}
