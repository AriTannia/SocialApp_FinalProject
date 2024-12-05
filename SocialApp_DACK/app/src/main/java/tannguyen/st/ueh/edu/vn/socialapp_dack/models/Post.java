package tannguyen.st.ueh.edu.vn.socialapp_dack.models;

public class Post {
    private String id;
    private String title;
    private String content;
    private long timestamp;
    private String posterName;

    // Constructor mặc định cần cho Firebase
    public Post() {}

    public Post(String id, String title, String content, long timestamp,String posterName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.posterName = posterName;
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
    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }
}

