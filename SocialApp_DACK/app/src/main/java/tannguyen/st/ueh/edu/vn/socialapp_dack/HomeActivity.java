package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.PostAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;

public class HomeActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference postsRef; // Firebase reference to posts node
    private List<Post> postList; // List to hold posts data
    private PostAdapter postAdapter; // Adapter to display posts
    private FirebaseDatabase mDatabase;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Set up insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Home_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Set up Toolbar and Bottom Navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setSupportActionBar(toolbar);

        // Initialize FirebaseAuth and GoogleSignInClient
        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        // Bottom Navigation Item Selection Logic
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId(); // Get the selected menu item ID

            if (itemId == R.id.action_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.action_add) {
                // Transition to PostActivity to create a new post
                Intent intent = new Intent(HomeActivity.this, PostActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_other_users) {
                selectedFragment = new UsersFragment();
            } else if (itemId == R.id.action_personal) {
                selectedFragment = new ProfileFragment();
            }

            // Replace current fragment with the selected one
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
            return true;
        });

        // Initialize Firebase Database and Posts Reference
        mDatabase = FirebaseDatabase.getInstance();
        postsRef = mDatabase.getReference("posts");

        postList = new ArrayList<>(); // Initialize the list of posts

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : null;  // Lấy userId


        postAdapter = new PostAdapter(this, postList, userId); // Initialize the adapter with the list


        // Load posts from Firebase
        loadPosts();
        Fragment defaultFragment = new HomeFragment();
        replaceFragment(defaultFragment);
    }

    // Method to replace the fragment
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Add to back stack to allow navigation back to previous fragment
        transaction.commit();
    }

    // Method to sign out the user
    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                mAuth.signOut();
                Toast.makeText(HomeActivity.this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class); // Transition to MainActivity
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(HomeActivity.this, "Đăng xuất thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to load posts from Firebase
    private void loadPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear(); // Clear previous data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    postList.add(post); // Add post to the list
                }
                postAdapter.notifyDataSetChanged(); // Notify adapter to update the view
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Không thể tải bài viết.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            // Perform sign out when user clicks logout
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    openUsersFragment(query); // Search with query
                } else {
                    openUsersFragment(""); // If query is empty, show all users
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    openUsersFragment(newText); // Update search results as user types
                } else {
                    openUsersFragment(""); // If query is empty, show all users
                }
                return false;
            }
        });

        return true;
    }

    private void openUsersFragment(String query) {
        UsersFragment fragment = new UsersFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        fragment.searchUsers(query);
    }
}
