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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
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
                if(!TextUtils.isEmpty(query.trim())) {
                    openUsersFragment(query);
                } else {
                    openUsersFragment(""); // Nếu không có từ khóa, lấy tất cả người dùng
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText.trim())) {
                    openUsersFragment(newText);
                } else {
                    openUsersFragment(""); // Nếu không có từ khóa, lấy tất cả người dùng
                }
                return false;
            }
        });

        return true;
    }

    private void openUsersFragment(String query) {
        // Tạo hoặc lấy fragment hiện tại
        UsersFragment fragment = (UsersFragment) getSupportFragmentManager().findFragmentByTag("USERS_FRAGMENT");
        if (fragment == null) {
            fragment = new UsersFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, "USERS_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
        }
        fragment.searchUsers(query); // Gọi phương thức tìm kiếm trong fragment
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_personal) {
            Toast.makeText(this, "Thông tin cá nhân được chọn", Toast.LENGTH_SHORT).show();

            // Thay thế Fragment hiện tại bằng ProfileFragment
            ProfileFragment profileFragment = new ProfileFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit();
            return true;

        } else if (id == R.id.action_other_users) {
            Toast.makeText(this, "Người dùng khác được chọn", Toast.LENGTH_SHORT).show();

            UsersFragment usersFragment = new UsersFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, usersFragment)
                    .addToBackStack(null)
                    .commit();
            return true;

        } else if (id == R.id.action_logout) {
            // Xử lý đăng xuất
            signOut();
            return true;

        } else if (id == R.id.nav_chat) { // Mục menu "Chat"
            Intent intent = new Intent(HomeActivity.this, ChatActivity.class); // Chuyển sang ChatActivity
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                mAuth.signOut();
                Toast.makeText(HomeActivity.this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class); // Chuyển về MainActivity
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(HomeActivity.this, "Đăng xuất thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}