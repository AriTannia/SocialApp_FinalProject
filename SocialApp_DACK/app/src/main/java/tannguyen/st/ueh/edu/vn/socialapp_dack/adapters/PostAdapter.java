package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import tannguyen.st.ueh.edu.vn.socialapp_dack.CommentActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.EditPostActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.PostDetailActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private String currentUserId;

    public PostAdapter(Context context, List<Post> postList, String currentUserId) {
        this.context = context;
        this.postList = postList;
        this.currentUserId = currentUserId;
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        if (post != null) {
            // Set data for title, content, timestamp, and poster name
            holder.titleTextView.setText(post.getTitle());
            holder.contentTextView.setText(post.getContent());
            String formattedTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(post.getTimestamp());
            holder.timestampTextView.setText(formattedTimestamp);
            holder.posterNameTextView.setText(post.getPosterName()); // Assuming `getPosterName()` exists in your Post model

            // Set up button listeners for like, comment, save actions
            holder.likeButton.setOnClickListener(v -> {
                // Handle "Like" action
                Toast.makeText(context, "Liked: " + post.getTitle(), Toast.LENGTH_SHORT).show();
            });

            holder.commentButton.setOnClickListener(v -> {
                // Handle "Comment" action
                Toast.makeText(context, "Commenting on: " + post.getTitle(), Toast.LENGTH_SHORT).show();

                // Kiểm tra xem post.getId() có null không
                String postId = post.getId();
                if (postId != null && !postId.isEmpty()) {
                    // Chuyển đến Activity bình luận, truyền ID của bài viết
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("postId", postId);  // Truyền ID của bài viết
                    context.startActivity(intent);  // Mở CommentActivity
                } else {
                    // Nếu không có postId, hiển thị lỗi hoặc thực hiện hành động khác
                    Toast.makeText(context, "Post ID is invalid", Toast.LENGTH_SHORT).show();
                }
            });


            holder.saveButton.setOnClickListener(v -> {
                // Handle "Save" action
                Toast.makeText(context, "Saved: " + post.getTitle(), Toast.LENGTH_SHORT).show();
            });
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", post.getId());  // Truyền ID bài viết
                context.startActivity(intent);
            });


            // Xử lý click vào bài viết để xem chi tiết
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", post.getId());
                context.startActivity(intent);
            });
//            // Kiểm tra nếu bài đăng này thuộc về người dùng hiện tại
            if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
                // Nếu userId của bài đăng và userId hiện tại là giống nhau, hiển thị nút sửa/xóa
                holder.buttonEdit.setVisibility(View.VISIBLE);
                holder.buttonDelete.setVisibility(View.VISIBLE);
            }
            else {
                // Nếu không phải, ẩn các nút sửa/xóa
                holder.buttonEdit.setVisibility(View.GONE);
                holder.buttonDelete.setVisibility(View.GONE);
            }
//            // Handle other button click events here
            holder.buttonEdit.setOnClickListener(v -> {
                // Chuyển tới EditPostActivity để sửa bài đăng
                Intent intent = new Intent(context, EditPostActivity.class);
                intent.putExtra("postId", post.getId());
                context.startActivity(intent);
            });

            holder.buttonDelete.setOnClickListener(v -> {
                // Xóa bài đăng
                deletePost(post);
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
    // Xóa bài đăng
    private void deletePost(Post post) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(post.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa thành công, cập nhật danh sách
                    postList.remove(post);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Xóa thất bại
                    Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                });
    }

    // ViewHolder class for RecyclerView
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, timestampTextView, posterNameTextView;
        ImageButton likeButton, commentButton, saveButton,buttonEdit,buttonDelete;



        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.postTitle);
            contentTextView = itemView.findViewById(R.id.postContent);
            timestampTextView = itemView.findViewById(R.id.postTimestamp);
            posterNameTextView = itemView.findViewById(R.id.postPosterName); // New TextView for the poster name
            likeButton = itemView.findViewById(R.id.buttonLike); // Like button
            commentButton = itemView.findViewById(R.id.buttonComment); // Comment button
            saveButton = itemView.findViewById(R.id.buttonSave); // Save button
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete  = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
