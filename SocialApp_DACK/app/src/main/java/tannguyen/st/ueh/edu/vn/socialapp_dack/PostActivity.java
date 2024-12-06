package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
        imagePreview = findViewById(R.id.imagePreview);

        databaseHelper = new SQLiteHelper(this);
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("posts");

        // Kiểm tra xem người dùng đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String posterUid = currentUser.getUid();
            loadUserInfo(posterUid); // Tải thông tin người dùng
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để đăng bài.", Toast.LENGTH_SHORT).show();
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
                    Picasso.get().load(imageUrl).into(imagePreview);
                    imagePreview.setVisibility(View.VISIBLE);
                } else {
                    imagePreview.setVisibility(View.GONE);
                }
            }
        });

        // Xử lý nút back trong Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> navigateToHomeActivity());
    }

    private void loadUserInfo(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String name = task.getResult().child("name").getValue(String.class);
                userTextView.setText("Posted by: " + (name != null ? name : "Unknown User"));
            } else {
                Log.e("Firebase", "Không thể tải thông tin người dùng.");
            }
        }).addOnFailureListener(e -> Log.e("Firebase", "Lỗi: " + e.getMessage()));
    }

    private void createPost() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        String imageUrl = imageUrlEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String posterUid = currentUser.getUid();

            String id = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();

            // Tạo bài viết mới
            Post post = new Post(id, title, content, timestamp, imageUrl, posterUid);

            // Lưu bài viết vào SQLite (nếu cần)
            databaseHelper.addPost(post);

            // Lưu bài viết lên Firebase Realtime Database
            firebaseDatabase.child(id).setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Đăng bài thành công.", Toast.LENGTH_SHORT).show();
                    navigateToHomeActivity();
                } else {
                    Toast.makeText(this, "Đăng bài thất bại.", Toast.LENGTH_SHORT).show();
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
