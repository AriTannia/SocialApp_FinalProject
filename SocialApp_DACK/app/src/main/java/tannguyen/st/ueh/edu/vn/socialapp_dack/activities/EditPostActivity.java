package tannguyen.st.ueh.edu.vn.socialapp_dack.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;

public class EditPostActivity extends AppCompatActivity {

    private EditText editPostTitle, editPostContent;
    private Button saveButton;
    private String postId;
    private String userId;
    private String originalTitle, originalContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // Lấy dữ liệu từ Intent
        postId = getIntent().getStringExtra("postId");
        originalTitle = getIntent().getStringExtra("postTitle");
        originalContent = getIntent().getStringExtra("postContent");
        userId = getIntent().getStringExtra("userId");

        // Ánh xạ các View
        editPostTitle = findViewById(R.id.editPostTitle);
        editPostContent = findViewById(R.id.editPostContent);
        saveButton = findViewById(R.id.saveButton);

        // Điền dữ liệu ban đầu vào các EditText
        editPostTitle.setText(originalTitle);
        editPostContent.setText(originalContent);

        // Lắng nghe sự kiện nhấn nút Save
        saveButton.setOnClickListener(v -> saveChanges());
    }

    // Hàm lưu thay đổi
    private void saveChanges() {
        String newTitle = editPostTitle.getText().toString().trim();
        String newContent = editPostContent.getText().toString().trim();

        // Kiểm tra nếu có thay đổi
        if (!newTitle.equals(originalTitle) || !newContent.equals(originalContent)) {
            // Thực hiện cập nhật dữ liệu (API, Firebase hoặc database)
            updatePostInDatabase(newTitle, newContent);
        } else {
            // Nếu không có thay đổi, thông báo cho người dùng
            Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
        }
    }

    // Cập nhật bài viết trong cơ sở dữ liệu
    private void updatePostInDatabase(String newTitle, String newContent) {
        // Ví dụ: Cập nhật bài viết trong Firebase
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);

        Map<String, Object> postUpdates = new HashMap<>();
        postUpdates.put("title", newTitle);
        postUpdates.put("content", newContent);

        postRef.updateChildren(postUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditPostActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                finish();  // Trở về màn hình trước
            } else {
                Toast.makeText(EditPostActivity.this, "Failed to update post", Toast.LENGTH_SHORT).show();
            }
        });
    }
}