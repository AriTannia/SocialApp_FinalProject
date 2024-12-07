package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<Comment> commentList;
    private final String currentUserId;
    private final OnCommentInteractionListener listener;

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

        // Hiển thị nội dung bình luận và thời gian
        holder.commentContent.setText(comment.getContent());
        holder.commentTimestamp.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(comment.getTimestamp()));

        // Lấy thông tin người dùng từ Firebase (tên và avatar)
        fetchUserInfo(comment.getUserId(), holder);

        // Hiển thị nút xóa nếu đây là bình luận của người dùng hiện tại
        if (comment.getUserId().equals(currentUserId)) {
            holder.buttonOptionsMenu.setVisibility(View.VISIBLE);
            holder.buttonOptionsMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.buttonOptionsMenu);
                popupMenu.inflate(R.menu.comment_options_menu);

                // Xử lý sự kiện nhấn vào các mục trong menu
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        if (listener != null) listener.onEditComment(comment); // Gọi callback sửa bình luận
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        if (listener != null) listener.onDeleteComment(comment); // Gọi callback xóa bình luận
                        return true;
                    } else {
                        return false;
                    }
                });

                popupMenu.show();
            });
        } else {
            holder.buttonOptionsMenu.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private void fetchUserInfo(String userId, CommentViewHolder holder) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String name = snapshot.child("name").getValue(String.class);
                String avatarUrl = snapshot.child("image").getValue(String.class);

                holder.commentUserName.setText(name != null ? name : "Unknown User");

                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Picasso.get()
                            .load(avatarUrl)
                            .placeholder(R.drawable.error_image)
                            .error(R.drawable.error_image)
                            .fit()
                            .centerCrop()
                            .into(holder.commentAvatarImageView);
                } else {
                    holder.commentAvatarImageView.setImageResource(R.drawable.error_image);
                }
            } else {
                holder.commentUserName.setText("Unknown User");
                holder.commentAvatarImageView.setImageResource(R.drawable.error_image);
            }
        });
    }

    public interface OnCommentInteractionListener {
        void onDeleteComment(Comment comment);
        void onEditComment(Comment comment);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentUserName, commentContent, commentTimestamp;
        EditText editTextComment;
        ImageButton buttonOptionsMenu;

        ImageView commentAvatarImageView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUserName = itemView.findViewById(R.id.commentUserName);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentTimestamp = itemView.findViewById(R.id.commentTimestamp);
            editTextComment = itemView.findViewById(R.id.editTextEditComment);
            buttonOptionsMenu = itemView.findViewById(R.id.buttonOptionsMenu);

            commentAvatarImageView = itemView.findViewById(R.id.commentUserAvatar);
        }
    }
}
