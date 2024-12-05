package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
            });

            holder.saveButton.setOnClickListener(v -> {
                // Handle "Save" action
                Toast.makeText(context, "Saved: " + post.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // ViewHolder class for RecyclerView
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, timestampTextView, posterNameTextView;
        ImageButton likeButton, commentButton, saveButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.postTitle);
            contentTextView = itemView.findViewById(R.id.postContent);
            timestampTextView = itemView.findViewById(R.id.postTimestamp);
            posterNameTextView = itemView.findViewById(R.id.postPosterName); // New TextView for the poster name
            likeButton = itemView.findViewById(R.id.buttonLike); // Like button
            commentButton = itemView.findViewById(R.id.buttonComment); // Comment button
            saveButton = itemView.findViewById(R.id.buttonSave); // Save button
        }
    }
}
