package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private List<Comment> commentList;
    private String currentUserId;
    private OnCommentInteractionListener listener;

    public CommentAdapter(Context context, List<Comment> commentList, String currentUserId, OnCommentInteractionListener listener) {
        this.context = context;
        this.commentList = commentList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.commentUserName.setText(comment.getUserName());
        holder.commentContent.setText(comment.getContent());
        holder.commentTimestamp.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(comment.getTimestamp()));

        fetchUserInfo(comment.getUserId(), holder);

        // Hiển thị nút xóa nếu đây là bình luận của người dùng hiện tại
        if (comment.getUserId().equals(currentUserId)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteComment(comment);
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Hiển thị nút "Sửa" nếu bình luận thuộc về người dùng hiện tại
        if (comment.getUserId().equals(currentUserId)) {
            holder.editButton.setVisibility(View.VISIBLE);
        } else {
            holder.editButton.setVisibility(View.GONE);
        }

        // Xử lý nút "Sửa"
        holder.editButton.setOnClickListener(v -> {
            // Chuyển sang chế độ chỉnh sửa
            holder.commentContent.setVisibility(View.GONE); // Ẩn nội dung bình luận hiện tại
            holder.editTextComment.setVisibility(View.VISIBLE); // Hiển thị ô nhập bình luận mới
            holder.editTextComment.setText(comment.getContent()); // Đặt nội dung ban đầu vào ô nhập
            holder.saveButton.setVisibility(View.VISIBLE); // Hiển thị nút "Lưu"
            holder.editButton.setVisibility(View.GONE); // Ẩn nút "Sửa"
        });

        // Xử lý nút "Lưu"
        holder.saveButton.setOnClickListener(v -> {
            String newContent = holder.editTextComment.getText().toString();
            if (newContent.isEmpty()) {
                Toast.makeText(context, "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật Firebase
            DatabaseReference commentRef = FirebaseDatabase.getInstance()
                    .getReference("comments")
                    .child(comment.getPostId())
                    .child(comment.getId());

            commentRef.child("content").setValue(newContent).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Comment updated successfully", Toast.LENGTH_SHORT).show();

                    // Cập nhật giao diện
                    holder.commentContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung bình luận
                    holder.commentContent.setText(newContent); // Cập nhật nội dung mới
                    holder.editTextComment.setVisibility(View.GONE); // Ẩn ô nhập bình luận
                    holder.saveButton.setVisibility(View.GONE); // Ẩn nút "Lưu"
                    holder.editButton.setVisibility(View.VISIBLE); // Hiển thị nút "Sửa"
                } else {
                    Toast.makeText(context, "Failed to update comment", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    // Hàm lấy thông tin người dùng từ Firebase và cập nhật vào View
    private void fetchUserInfo(String userId, CommentViewHolder holder) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String name = snapshot.child("name").getValue(String.class);
                String image = snapshot.child("image").getValue(String.class);

                // Hiển thị tên người dùng
                holder.commentUserName.setText(name != null ? name : "Unknown User");

                // Hiển thị avatar người dùng
                if (image != null && !image.isEmpty()) {
                    Picasso.get()
                            .load(image)
                            .placeholder(R.drawable.error_image) // Placeholder image
                            .error(R.drawable.error_image) // Error image
                            .fit()
                            .centerCrop()
                            .into(holder.commentAvatarImageView);
                } else {
                    holder.commentAvatarImageView.setImageResource(R.drawable.error_image);
                }
            } else {
                // Nếu không tìm thấy thông tin người dùng, hiển thị "Unknown User"
                holder.commentUserName.setText("Unknown User");
                holder.commentAvatarImageView.setImageResource(R.drawable.error_image);
            }
        }).addOnFailureListener(e -> {
            // Xử lý lỗi khi lấy thông tin người dùng
            holder.commentUserName.setText("Unknown User");
            holder.commentAvatarImageView.setImageResource(R.drawable.error_image);
        });
    }

    public interface OnCommentInteractionListener {
        void onDeleteComment(Comment comment);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentUserName, commentContent, commentTimestamp;
        ImageButton deleteButton;
        EditText editTextComment;
        Button editButton, saveButton;
        ImageView commentAvatarImageView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUserName = itemView.findViewById(R.id.commentUserName);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentTimestamp = itemView.findViewById(R.id.commentTimestamp);
            deleteButton = itemView.findViewById(R.id.buttonDeleteComment);
            editTextComment = itemView.findViewById(R.id.editTextEditComment);
            editButton = itemView.findViewById(R.id.buttonEditComment);
            saveButton = itemView.findViewById(R.id.buttonSaveComment);
            commentAvatarImageView = itemView.findViewById(R.id.commentUserAvatar); // Avatar ImageView
        }
    }
}
