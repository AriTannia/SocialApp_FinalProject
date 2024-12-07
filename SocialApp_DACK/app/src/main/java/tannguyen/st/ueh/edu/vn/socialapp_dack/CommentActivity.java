package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.app.AlertDialog;
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
        editTextComment = findViewById(R.id.editTextEditComment);
        findViewById(R.id.buttonSendComment).setOnClickListener(this::postComment);

        // Lấy ID bài viết hiện tại từ Intent
        currentPostId = getIntent().getStringExtra("POST_ID");  // Lấy postId từ Intent
        if (currentPostId == null || currentPostId.isEmpty()) {
            // Nếu không có POST_ID, thông báo và đóng Activity
            Toast.makeText(this, "Post ID is missing!", Toast.LENGTH_SHORT).show();
            finish();  // Đóng CommentActivity nếu không có POST_ID
            return;
        }

        // Khởi tạo Firebase
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(currentPostId);

        // Khởi tạo adapter và danh sách bình luận
        commentList = new ArrayList<>();
        adapter = new CommentAdapter(
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
        recyclerViewComments.setAdapter(adapter);

        // Lấy danh sách bình luận từ Firebase
        loadComments();
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
            String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail();
            Comment comment = new Comment(
                    commentId,
                    currentPostId,
                    currentUser.getUid(),
                    userName,
                    content,
                    System.currentTimeMillis()
            );

            // Lưu bình luận vào Firebase
            commentsRef.child(commentId).setValue(comment).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Nếu thành công, xóa nội dung nhập vào và thông báo
                    editTextComment.setText("");
                    Toast.makeText(CommentActivity.this, "Comment posted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteComment(Comment comment) {
        // Kiểm tra xem người xóa có phải là người tạo bình luận không
        if (comment.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            // Xóa bình luận
            commentsRef.child(comment.getId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "You can only delete your own comments", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadComments() {
        // Lấy dữ liệu bình luận từ Firebase và sắp xếp theo timestamp giảm dần
        commentsRef.orderByChild("timestamp").limitToLast(50)  // Bạn có thể điều chỉnh số lượng bình luận tải về (50 ở đây là ví dụ)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Kiểm tra xem có dữ liệu mới không
                        if (snapshot.exists()) {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CommentActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
