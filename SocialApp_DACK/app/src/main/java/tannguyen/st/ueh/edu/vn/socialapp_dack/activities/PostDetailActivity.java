package tannguyen.st.ueh.edu.vn.socialapp_dack.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.CommentAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Comment;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostDetailActivity extends AppCompatActivity {

    private TextView postTitleTextView, postContentTextView, postAuthorTextView, postAuthorName;
    private ImageView postImageView, postAuthorAvatar;
    private RecyclerView recyclerViewComments;
    private EditText editTextComment;
    private Button buttonSendComment;

    private String currentPostId;
    private DatabaseReference postRef, commentsRef, usersRef;
    private List<Comment> commentList;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Ánh xạ Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hiển thị nút back
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow); // Đặt icon cho nút Back (nếu có)
        }

        // Liên kết các view trong layout
        postImageView = findViewById(R.id.postImageView);
        postAuthorAvatar = findViewById(R.id.postAuthorAvatar);
        postAuthorName = findViewById(R.id.postAuthorName);
        postTitleTextView = findViewById(R.id.textViewPostTitle);
        postContentTextView = findViewById(R.id.textViewPostContent);
        postAuthorTextView = findViewById(R.id.textViewPostAuthor);
        recyclerViewComments = findViewById(R.id.recyclerViewPostComments);
        editTextComment = findViewById(R.id.editTextComment);
        buttonSendComment = findViewById(R.id.buttonSendComment);

        // Lấy ID bài viết từ Intent
        currentPostId = getIntent().getStringExtra("postId");
        if (currentPostId == null) {
            Toast.makeText(this, "Post ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase references
        postRef = FirebaseDatabase.getInstance().getReference("posts").child(currentPostId);
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(currentPostId);
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Danh sách bình luận
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(
                this,
                commentList,
                FirebaseAuth.getInstance().getUid(),
                new CommentAdapter.OnCommentInteractionListener() {
                    @Override
                    public void onDeleteComment(Comment comment) {
                        deleteComment(comment);
                    }

                    @Override
                    public void onEditComment(Comment comment) {
                        editComment(comment);
                    }
                }
        );
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        // Tải thông tin bài đăng và bình luận
        loadPostDetails();
        loadComments();

        // Xử lý khi nhấn nút gửi bình luận
        buttonSendComment.setOnClickListener(v -> postComment());

        // Xử lý sự kiện nút Back
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    private void editComment(Comment comment) {
        // Hiển thị hộp thoại để chỉnh sửa bình luận
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Comment");

        // Tạo EditText để nhập nội dung mới
        final EditText input = new EditText(this);
        input.setText(comment.getContent()); // Hiển thị nội dung hiện tại
        input.setSelection(input.getText().length()); // Đặt con trỏ ở cuối văn bản
        builder.setView(input);

        // Nút "Lưu"
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newContent = input.getText().toString().trim();
            if (TextUtils.isEmpty(newContent)) {
                Toast.makeText(this, "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật nội dung bình luận trong Firebase
            commentsRef.child(comment.getId()).child("content").setValue(newContent)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Comment updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to update comment", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Nút "Hủy"
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Hiển thị hộp thoại
        builder.show();
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

                    // Hiển thị thời gian đăng bài
                    long timestamp = post.getTimestamp(); // Lấy timestamp từ Post
                    String formattedTime = formatTimestamp(timestamp); // Định dạng timestamp
                    TextView postTimeTextView = findViewById(R.id.postTime);
                    postTimeTextView.setText(formattedTime);

                    // Hiển thị ảnh bài viết nếu có
                    if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                        Picasso.get().load(post.getImageUrl()).placeholder(R.drawable.placeholder_image).into(postImageView);
                    }

                    // Lấy avatar từ bảng Users
                    String userId = post.getUserId();
                    if (userId != null && !userId.isEmpty()) {
                        loadUserAvatar(userId);
                        loadUserName(userId);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to load post details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserName(String userId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class); // Lấy giá trị `name`
                    if (name != null) {
                        TextView postAuthorName = findViewById(R.id.postAuthorName);
                        postAuthorName.setText(name); // Hiển thị tên vào TextView
                    } else {
                        postAuthorName.setText("Unknown Author");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadUserAvatar(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String avatarUrl = snapshot.child("image").getValue(String.class);
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Picasso.get().load(avatarUrl).placeholder(R.drawable.error_image).into(postAuthorAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to load user avatar", Toast.LENGTH_SHORT).show();
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
