package tannguyen.st.ueh.edu.vn.socialapp_dack.models;

public class Post {
    private String id;          // ID của bài viết
    private String title;       // Tiêu đề bài viết
    private String content;     // Nội dung bài viết
    private long timestamp;     // Dấu thời gian của bài viết
    private String imageUrl;    // Đường dẫn đến hình ảnh của bài viết (nếu có)
    private String userId;      // UID của người đăng bài
    private String posterName;  // Tên người đăng bài (dùng cho mục đích hiển thị)

    // Constructor mặc định (cần thiết cho Firebase)
    public Post() {}

    // Constructor đầy đủ để tạo một bài viết mới
    public Post(String id, String title, String content, long timestamp, String imageUrl, String userId, String posterName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.posterName = posterName;  // Gán tên người đăng
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPosterName() { return posterName; }
    public void setPosterName(String posterName) { this.posterName = posterName; }
}
