package tannguyen.st.ueh.edu.vn.socialapp_dack;

public class User {
    private String name;
    private String email;
    private String password;  // Thêm thuộc tính password

    // Constructor mặc định (Firebase yêu cầu phải có constructor mặc định)
    public User() {
    }

    // Constructor có tham số để tạo đối tượng User
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
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
}

