package tannguyen.st.ueh.edu.vn.socialapp_dack.Model;

public class ModelUser {
    private String name;
    private String email;
    private String password;  // Thêm thuộc tính password
    private String phone;     // Thêm thuộc tính phone
    private String image;     // Thêm thuộc tính image (URL)
    private String cover;
    private String uid;       // Thêm thuộc tính uid

    // Constructor mặc định (Firebase yêu cầu phải có constructor mặc định)
    public ModelUser() {
    }

    // Constructor có tham số để tạo đối tượng User
    public ModelUser(String name, String email, String password, String phone, String image, String uid, String coverIv) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.image = image;
        this.uid = uid;
        this.cover = coverIv;
    }

    // Getter và Setter cho các thuộc tính
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCoverIv() {
        return cover;
    }

    public void setCoverIv(String coverIv) {
        this.cover = coverIv;
    }
}
