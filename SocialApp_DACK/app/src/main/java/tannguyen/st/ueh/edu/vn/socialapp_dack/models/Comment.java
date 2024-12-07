package tannguyen.st.ueh.edu.vn.socialapp_dack.models;

public class Comment {
    private String id;
    private String postId; // ID bài đăng mà bình luận này thuộc về
    private String userId; // ID người dùng đã bình luận
    private String userName; // Tên hiển thị của người dùng
    private String content; // Nội dung bình luận
    private long timestamp; // Thời gian tạo bình luận

    // Constructor không tham số
    public Comment() {
    }

    // Constructor với tham số đầy đủ
    public Comment(String id, String postId, String userId, String userName, String content, long timestamp) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Các getter và setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
