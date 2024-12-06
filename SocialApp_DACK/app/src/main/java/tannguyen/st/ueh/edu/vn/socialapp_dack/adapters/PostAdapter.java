package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.content.Intent;
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

import tannguyen.st.ueh.edu.vn.socialapp_dack.CommentActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
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
            // Set data for title, content, and timestamp
            holder.titleTextView.setText(post.getTitle());
            holder.contentTextView.setText(post.getContent());
            String formattedTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(post.getTimestamp());
            holder.timestampTextView.setText(formattedTimestamp);

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
            fetchPosterInfo(post.getuserId(), holder);

            // Set up button listeners for like, comment, save actions
            holder.likeButton.setOnClickListener(v -> {
                Toast.makeText(context, "Liked: " + post.getTitle(), Toast.LENGTH_SHORT).show();
            });

            holder.commentButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("POST_ID", post.getId());
                context.startActivity(intent);
            });

            holder.saveButton.setOnClickListener(v -> {
                Toast.makeText(context, "Saved: " + post.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

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
                Log.e("Firebase", "Không thể tải thông tin người dùng với UID: " + uid);
                holder.posterNameTextView.setText("Unknown User");
                holder.posterAvatarImageView.setImageResource(R.drawable.error_image);
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            holder.posterNameTextView.setText("Unknown User");
            holder.posterAvatarImageView.setImageResource(R.drawable.error_image);
        });
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, timestampTextView, posterNameTextView;
        ImageView imageView, posterAvatarImageView; // ImageView for poster avatar
        ImageButton likeButton, commentButton, saveButton;

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
        }
    }
}
