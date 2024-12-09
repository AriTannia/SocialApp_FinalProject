package tannguyen.st.ueh.edu.vn.socialapp_dack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;

public class EditPostActivity extends AppCompatActivity {

    private EditText editPostTitle, editPostContent, editImageUrl;
    private ImageView editPostImage;
    private Button saveButton;

    private String postId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Xử lý nút quay lại
        toolbar.setNavigationOnClickListener(v -> handleBackNavigation());

        // Lấy ID bài viết từ Intent
        postId = getIntent().getStringExtra("postId");

        // Ánh xạ các View
        editPostTitle = findViewById(R.id.editPostTitle);
        editPostContent = findViewById(R.id.editPostContent);
        editImageUrl = findViewById(R.id.editImageUrl);
        editPostImage = findViewById(R.id.editPostImage);
        saveButton = findViewById(R.id.saveButton);

        // Lấy dữ liệu bài viết từ Firebase
        loadPostData();

        // Lắng nghe sự kiện thay đổi URL ảnh
        editImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String imageUrl = s.toString().trim();
                if (!imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder_image).into(editPostImage);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Lắng nghe sự kiện nhấn nút Save
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void handleBackNavigation() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentEmail = currentUser.getEmail();
            if (currentEmail != null && currentEmail.equals("admin@gmail.com")) {
                // Nếu người dùng là admin, chuyển đến AdminActivity
                Intent intent = new Intent(EditPostActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            } else {
                navigateToHomeActivity();
            }
        } else {
            // Trường hợp không có người dùng đăng nhập
            Toast.makeText(EditPostActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(EditPostActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    // Hàm tải dữ liệu bài viết từ Firebase
    private void loadPostData() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String content = snapshot.child("content").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    // Điền dữ liệu vào các trường
                    editPostTitle.setText(title);
                    editPostContent.setText(content);
                    editImageUrl.setText(imageUrl);

                    // Hiển thị ảnh bài viết
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder_image).into(editPostImage);
                    }
                } else {
                    Toast.makeText(EditPostActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng activity nếu không tìm thấy bài viết
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditPostActivity.this, "Failed to load post: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm lưu thay đổi
    private void saveChanges() {
        String newTitle = editPostTitle.getText().toString().trim();
        String newContent = editPostContent.getText().toString().trim();
        String newImageUrl = editImageUrl.getText().toString().trim();

        // Cập nhật bài viết trong Firebase
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);

        Map<String, Object> postUpdates = new HashMap<>();
        postUpdates.put("title", newTitle);
        postUpdates.put("content", newContent);
        postUpdates.put("imageUrl", newImageUrl);

        postRef.updateChildren(postUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditPostActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditPostActivity.this, "Failed to update post", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
