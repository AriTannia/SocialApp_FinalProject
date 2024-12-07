package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostActivity extends AppCompatActivity {

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

        databaseHelper = new SQLiteHelper(this);
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("posts");

        // Kiểm tra xem người dùng đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
        } else {
            return;
        }

        // Xử lý khi người dùng nhấn nút đăng bài
        findViewById(R.id.buttonPost).setOnClickListener(v -> createPost());

    }

    private void createPost() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            String id = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();


            databaseHelper.addPost(post);

            firebaseDatabase.child(id).setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                } else {
                }
            });
        }
    }
}
