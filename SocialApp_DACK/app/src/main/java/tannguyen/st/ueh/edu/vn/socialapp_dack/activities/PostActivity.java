package tannguyen.st.ueh.edu.vn.socialapp_dack.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.databases.SQLiteHelper;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText, imageUrlEditText;
    private TextView userTextView;
    private SQLiteHelper databaseHelper;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Khởi tạo các thành phần giao diện
        titleEditText = findViewById(R.id.editTextTitle);
        contentEditText = findViewById(R.id.editTextContent);
        imageUrlEditText = findViewById(R.id.editTextImageUrl);
        userTextView = findViewById(R.id.textViewUser);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference("posts");

        // Kiểm tra xem người dùng đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String posterName = currentUser.getDisplayName();  // Lấy tên người dùng từ Firebase
            if (posterName == null || posterName.isEmpty()) {
                posterName = currentUser.getEmail(); // Dự phòng bằng email nếu không có tên hiển thị
            }
            // Hiển thị tên người dùng trong TextView
            userTextView.setText("Posted by: " + posterName);
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để đăng bài.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý khi người dùng nhấn nút đăng bài
        findViewById(R.id.buttonPost).setOnClickListener(v -> createPost());
    }



    private void createPost() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String posterName = currentUser.getDisplayName();
            if (posterName == null || posterName.isEmpty()) {
                posterName = currentUser.getEmail();
            }

            String id = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();
            String imageUrl = imageUrlEditText.getText().toString().trim();;  // Nếu có URL ảnh thì cần thêm

            // Tạo bài viết mới
            Post post = new Post(id, title, content, timestamp, imageUrl, userId, posterName);

            // Lưu bài viết vào Firebase
            firebaseDatabase.child(id).setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Đăng bài thành công.", Toast.LENGTH_SHORT).show();
                    finish();  // Quay lại màn hình chính sau khi đăng bài
                } else {
                    Toast.makeText(this, "Đăng bài thất bại.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
