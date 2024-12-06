package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostDetailActivity extends AppCompatActivity {

    private TextView postTitleTextView, postContentTextView, postAuthorTextView;
    private RecyclerView recyclerViewComments;
    private EditText editTextComment;
    private Button buttonSendComment;

    private String currentPostId;
    private DatabaseReference postRef, commentsRef;
    private List<Comment> commentList;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Liên kết các view trong layout
        postTitleTextView = findViewById(R.id.textViewPostTitle);
        postContentTextView = findViewById(R.id.textViewPostContent);
        postAuthorTextView = findViewById(R.id.textViewPostAuthor);
        recyclerViewComments = findViewById(R.id.recyclerViewPostComments);
        editTextComment = findViewById(R.id.editTextComment);
        buttonSendComment = findViewById(R.id.buttonSendComment);

        // Lấy ID bài viết từ Intent
        currentPostId = getIntent().getStringExtra("POST_ID");
        if (currentPostId == null) {
            Toast.makeText(this, "Post ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase references
        postRef = FirebaseDatabase.getInstance().getReference("posts").child(currentPostId);
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(currentPostId);

        // Danh sách bình luận
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, FirebaseAuth.getInstance().getUid(), this::deleteComment);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        // Tải thông tin bài đăng và bình luận
        loadPostDetails();
        loadComments();

        // Xử lý khi nhấn nút gửi bình luận
        buttonSendComment.setOnClickListener(v -> postComment());
    }

    private void loadPostDetails() {
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    postTitleTextView.setText(post.getTitle());
                    postContentTextView.setText(post.getContent());
                    postAuthorTextView.setText("Posted by: " + post.getPosterName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to load post details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments() {
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
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String content = editTextComment.getText().toString();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

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

            commentsRef.child(commentId).setValue(comment).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    editTextComment.setText("");
                } else {
                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteComment(Comment comment) {
        commentsRef.child(comment.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
