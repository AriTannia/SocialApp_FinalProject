package tannguyen.st.ueh.edu.vn.socialapp_dack.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.PostAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.databases.SQLiteHelper;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference postsRef;
    private SQLiteHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize post list and adapter
        postList = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : null;
        postAdapter = new PostAdapter(getContext(), postList,userId);
        recyclerView.setAdapter(postAdapter);

        // Firebase reference
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        dbHelper = new SQLiteHelper(getContext());

        // Load posts
        if (isNetworkAvailable()) {
            loadPosts();
        } else {
            loadPostsOffline();
        }

        return view;
    }

    private void loadPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                        dbHelper.insertOrUpdatePost(post); // Lưu vào SQLite
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Không thể tải bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostsOffline() {
        Cursor cursor = dbHelper.getAllPosts(); // Lấy tất cả các bài viết từ SQLite
        if (cursor != null && cursor.moveToFirst()) {
            postList.clear();
            do {
                @SuppressLint("Range") Post post = new Post(
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_POST_ID)),
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_CONTENT)),
                        cursor.getLong(cursor.getColumnIndex(SQLiteHelper.COLUMN_TIMESTAMP)), // Sửa thành long
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_IMAGE_URL)), // Thêm imageUrl
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_USER_ID)), // Thêm userId
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_POSTER_NAME)) // Thêm posterName
                );
                postList.add(post);
            } while (cursor.moveToNext());
            cursor.close();
        }
        postAdapter.notifyDataSetChanged();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
