package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.Objects;

import tannguyen.st.ueh.edu.vn.socialapp_dack.activities.CommentActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.activities.EditPostActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.activities.PostDetailActivity;
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        if (post != null) {
            // Set data for title, content, and timestamp
            holder.titleTextView.setText(post.getTitle());
            holder.contentTextView.setText(post.getContent());
            String formattedTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(post.getTimestamp());
            holder.timestampTextView.setText(formattedTimestamp);
            holder.posterNameTextView.setText(post.getPosterName()); // Display poster's name

            // Load post image
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(post.getImageUrl())
                        .placeholder(R.drawable.placeholder_image) // Placeholder image
                        .error(R.drawable.error_image) // Error image
                        .fit()
                        .centerCrop()
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_image);
            }

            // Load poster information (name and avatar) from Firebase
            fetchPosterInfo(post.getUserId(), holder);

            // Set up button listeners for like, comment, save actions
            holder.likeButton.setOnClickListener(v -> {
                // Kiểm tra trạng thái hiện tại của nút (trắng hoặc đỏ)
                Drawable currentDrawable = holder.likeButton.getDrawable();

                if (Objects.equals(currentDrawable.getConstantState(), context.getResources().getDrawable(R.drawable.ic_like).getConstantState())) {
                    // Nếu là icon trắng, đổi sang icon đỏ
                    holder.likeButton.setImageResource(R.drawable.ic_like_red);
                    Toast.makeText(context, "Liked: " + post.getTitle(), Toast.LENGTH_SHORT).show();
                } else {
                    // Nếu là icon đỏ, đổi lại icon trắng
                    holder.likeButton.setImageResource(R.drawable.ic_like);
                    Toast.makeText(context, "Unliked: " + post.getTitle(), Toast.LENGTH_SHORT).show();
                }

            });

            holder.commentButton.setOnClickListener(v -> {
                // Khi nhấn nút comment, mở CommentActivity và truyền POST_ID vào
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("POST_ID", post.getId());  // Truyền POST_ID vào Intent
                context.startActivity(intent);  // Khởi động Activity
            });


            holder.saveButton.setOnClickListener(v -> {
                Toast.makeText(context, "Saved: " + post.getTitle(), Toast.LENGTH_SHORT).show();
            });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", post.getId());
                context.startActivity(intent);
            });

            // Show or hide edit and delete buttons if the post belongs to the current user
            if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
                if (holder.buttonEdit != null) {
                    holder.buttonEdit.setVisibility(View.VISIBLE);
                }
                if (holder.buttonDelete != null) {
                    holder.buttonDelete.setVisibility(View.VISIBLE);
                }
            } else {
                if (holder.buttonEdit != null) {
                    holder.buttonEdit.setVisibility(View.GONE);
                }
                if (holder.buttonDelete != null) {
                    holder.buttonDelete.setVisibility(View.GONE);
                }
            }

            // Edit post action
            if (holder.buttonEdit != null) {
                holder.buttonEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(context, EditPostActivity.class);
                    intent.putExtra("postId", post.getId());
                    context.startActivity(intent);
                });
            }

            // Delete post action
            if (holder.buttonDelete != null) {
                holder.buttonDelete.setOnClickListener(v -> {
                    deletePost(post);
                });
            }
        }
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    // Delete post from Firebase and update the RecyclerView
    private void deletePost(Post post) {
        FirebaseDatabase.getInstance().getReference("posts").child(post.getId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    postList.remove(post);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                });
    }

    // Fetch poster's name and avatar from Firebase and update the ViewHolder
    private void fetchPosterInfo(String uid, PostViewHolder holder) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String name = snapshot.child("name").getValue(String.class);
                String image = snapshot.child("image").getValue(String.class);

                // Set poster name
                holder.posterNameTextView.setText(name != null ? name : "Unknown User");

                // Set poster avatar
                if (image != null && !image.isEmpty()) {
                    Picasso.get()
                            .load(image)
                            .placeholder(R.drawable.error_image) // Placeholder image
                            .error(R.drawable.error_image) // Error image
                            .fit()
                            .centerCrop()
                            .into(holder.posterAvatarImageView);
                } else {
                    holder.posterAvatarImageView.setImageResource(R.drawable.error_image);
                }
            } else {
                Log.e("Firebase", "Cannot load user info with UID: " + uid);
                holder.posterNameTextView.setText("Unknown User");
                holder.posterAvatarImageView.setImageResource(R.drawable.error_image);
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error fetching user info: " + e.getMessage());
            holder.posterNameTextView.setText("Unknown User");
            holder.posterAvatarImageView.setImageResource(R.drawable.error_image);
        });
    }

    // ViewHolder class for RecyclerView
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, timestampTextView, posterNameTextView;
        ImageView imageView, posterAvatarImageView; // Avatar ImageView
        ImageButton likeButton, commentButton, saveButton, buttonEdit, buttonDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.postTitle);
            contentTextView = itemView.findViewById(R.id.postContent);
            timestampTextView = itemView.findViewById(R.id.postTimestamp);
            posterNameTextView = itemView.findViewById(R.id.postPosterName);
            imageView = itemView.findViewById(R.id.postImage);
            posterAvatarImageView = itemView.findViewById(R.id.postProfileImage); // Avatar ImageView
            likeButton = itemView.findViewById(R.id.buttonLike);
            commentButton = itemView.findViewById(R.id.buttonComment);
            saveButton = itemView.findViewById(R.id.buttonSave);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
