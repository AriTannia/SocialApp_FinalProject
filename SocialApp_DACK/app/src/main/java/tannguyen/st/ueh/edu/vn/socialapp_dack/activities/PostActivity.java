package tannguyen.st.ueh.edu.vn.socialapp_dack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import androidx.appcompat.widget.Toolbar;

import java.util.UUID;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.databases.SQLiteHelper;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText, imageUrlEditText;
    private TextView userTextView;
    private ImageView imagePreview;
    private SQLiteHelper databaseHelper;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
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
        imagePreview = findViewById(R.id.imagePreview);

        // Khởi tạo SQLiteHelper và FirebaseAuth
        databaseHelper = new SQLiteHelper(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("posts");

        // Kiểm tra người dùng hiện tại
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String posterName = currentUser.getDisplayName();
            if (TextUtils.isEmpty(posterName)) {
                posterName = currentUser.getEmail(); // Dự phòng email nếu thiếu tên
            }
            userTextView.setText("Posted by: " + posterName);
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để đăng bài.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý khi nhấn nút "Đăng bài"
        findViewById(R.id.buttonPost).setOnClickListener(v -> createPost());

        // Xử lý hiển thị ảnh preview từ URL
        imageUrlEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String imageUrl = imageUrlEditText.getText().toString();
                if (!TextUtils.isEmpty(imageUrl)) {
                    Picasso.get().load(imageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(imagePreview);
                    imagePreview.setVisibility(View.VISIBLE);
                } else {
                    imagePreview.setVisibility(View.GONE);
                }
            }
        });

        // Xử lý Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void createPost() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String imageUrl = imageUrlEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            String userId = currentUser.getUid();
            String posterName = currentUser.getDisplayName();
            if (TextUtils.isEmpty(posterName)) {
                posterName = currentUser.getEmail();
            }

            String id = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();

            // Tạo bài viết mới
            Post post = new Post(id, title, content, timestamp, imageUrl, userId, posterName);

            // Lưu bài viết vào Firebase
            firebaseDatabase.child(id).setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Đăng bài thành công.", Toast.LENGTH_SHORT).show();

                    // Lưu bài viết vào SQLite
                    databaseHelper.insertOrUpdatePost(post);

                    navigateToHomeActivity();
                } else {
                    Toast.makeText(this, "Đăng bài thất bại.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Không thể xác thực người dùng.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentEmail = currentUser.getEmail();
            if (currentEmail != null && currentEmail.equals("admin@gmail.com")) {
                // Nếu người dùng là admin, chuyển đến AdminActivity
                Intent intent = new Intent(PostActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Nếu không phải admin, chuyển về HomeActivity
                navigateToHomeActivity();
            }
        } else {
            // Trường hợp không có người dùng đăng nhập (nếu cần thiết)
            Toast.makeText(PostActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateToHomeActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
