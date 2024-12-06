package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.CommentAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Comment;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private EditText editTextComment;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private DatabaseReference commentsRef;
    private String currentPostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // Khởi tạo các thành phần giao diện
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        editTextComment = findViewById(R.id.editTextComment);
        findViewById(R.id.buttonSendComment).setOnClickListener(this::postComment);

        // Lấy ID bài viết hiện tại từ Intent
        currentPostId = getIntent().getStringExtra("postId");
        if (currentPostId == null) {
            // Nếu không có POST_ID, thông báo và đóng Activity
            Toast.makeText(this, "Post ID is missing!", Toast.LENGTH_SHORT).show();
            finish();  // Đóng CommentActivity nếu không có POST_ID
            return;
        }

        // Khởi tạo Firebase
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(currentPostId);

        // Khởi tạo adapter và danh sách bình luận
        commentList = new ArrayList<>();
        adapter = new CommentAdapter(this, commentList, FirebaseAuth.getInstance().getUid(), this::deleteComment);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(adapter);

        // Lấy danh sách bình luận từ Firebase
        loadComments();
    }

    private void postComment(View view) {
        // Lấy nội dung bình luận
        String content = editTextComment.getText().toString();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin người dùng
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String commentId = UUID.randomUUID().toString();
            Comment comment = new Comment(
                    commentId,
                    currentPostId,
                    currentUser.getUid(),
                    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                    content,
                    System.currentTimeMillis()
            );

            // Lưu bình luận vào Firebase
            commentsRef.child(commentId).setValue(comment).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Nếu thành công, xóa nội dung nhập vào và thông báo
                    editTextComment.setText("");
                } else {
                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteComment(Comment comment) {
        // Xóa bình luận
        commentsRef.child(comment.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments() {
        // Lấy dữ liệu bình luận từ Firebase
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                // Cập nhật danh sách bình luận
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}