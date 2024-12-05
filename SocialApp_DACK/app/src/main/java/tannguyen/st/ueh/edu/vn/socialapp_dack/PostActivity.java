package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText;
    private TextView userTextView; // TextView để hiển thị tên người đăng
    private SQLiteHelper databaseHelper;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Khởi tạo các thành phần giao diện
        titleEditText = findViewById(R.id.editTextTitle);
        contentEditText = findViewById(R.id.editTextContent);
        userTextView = findViewById(R.id.textViewUser); // Lấy đối tượng TextView

        databaseHelper = new SQLiteHelper(this);
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("posts");

        // Kiểm tra xem người dùng đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Nếu người dùng đã đăng nhập, lấy tên và hiển thị
            String posterName = currentUser.getDisplayName(); // Lấy tên người dùng từ Firebase
            if (posterName == null || posterName.isEmpty()) {
                posterName = currentUser.getEmail(); // Nếu không có tên, sử dụng email
            }
            // Hiển thị tên người dùng trong TextView
            userTextView.setText("Posted by: " + posterName);
        }

        // Xử lý khi người dùng nhấn nút đăng bài
        findViewById(R.id.buttonPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost();
            }
        });
    }

    private void createPost() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy người dùng đã đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String posterName = currentUser.getDisplayName();
            if (posterName == null || posterName.isEmpty()) {
                posterName = currentUser.getEmail();  // Dự phòng bằng email nếu không có tên hiển thị
            }

            String id = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();

            // Tạo đối tượng Post với posterName
            Post post = new Post(id, title, content, timestamp, posterName);

            // Lưu bài viết vào SQLite
            databaseHelper.addPost(post);

            // Lưu bài viết vào Firebase
            firebaseDatabase.child(id).setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show();
                    finish();  // Đóng activity sau khi tạo bài viết thành công
                } else {
                    Toast.makeText(this, "Failed to post", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}