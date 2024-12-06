package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText, imageUrlEditText;
    private TextView userTextView;
    private ImageView imagePreview;
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
        imagePreview = findViewById(R.id.imagePreview);  // ImageView để hiển thị ảnh

        databaseHelper = new SQLiteHelper(this);
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("posts");

        // Kiểm tra xem người dùng đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String posterName = currentUser.getDisplayName();
            if (posterName == null || posterName.isEmpty()) {
                posterName = currentUser.getEmail();
            }
            userTextView.setText("Posted by: " + posterName);
        } else {
            Toast.makeText(this, "You need to be logged in to post.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Xử lý khi người dùng nhấn nút đăng bài
        findViewById(R.id.buttonPost).setOnClickListener(v -> createPost());

        // Xử lý khi người dùng nhập URL ảnh
        imageUrlEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String imageUrl = imageUrlEditText.getText().toString();
                if (!TextUtils.isEmpty(imageUrl)) {
                    // Sử dụng Picasso để tải ảnh từ URL
                    Picasso.get().load(imageUrl).into(imagePreview);
                    imagePreview.setVisibility(View.VISIBLE); // Hiển thị ImageView
                } else {
                    imagePreview.setVisibility(View.GONE); // Ẩn ImageView nếu không có URL
                }
            }
        });

        // Xử lý nút back trong Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> navigateToHomeActivity());
    }

    private void createPost() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        String imageUrl = imageUrlEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String posterName = currentUser.getDisplayName();
            if (posterName == null || posterName.isEmpty()) {
                posterName = currentUser.getEmail();
            }

            String id = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();
            String posterAvatar = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "https://your-default-avatar-url.com";

            Post post = new Post(id, title, content, timestamp, posterName, imageUrl, posterAvatar);

            databaseHelper.addPost(post);

            firebaseDatabase.child(id).setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show();
                    navigateToHomeActivity();
                } else {
                    Toast.makeText(this, "Failed to post", Toast.LENGTH_SHORT).show();
                }
            });
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
        navigateToHomeActivity();
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
