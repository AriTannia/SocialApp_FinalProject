package tannguyen.st.ueh.edu.vn.socialapp_dack.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone;
    private ImageView imgvAvatar, Coverimgv;
    private FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    private ProgressDialog pd;
    private String profileOrCoverPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        // Ánh xạ các thành phần giao diện
        tvName = findViewById(R.id.profile_name);
        tvEmail = findViewById(R.id.profile_email);
        tvPhone = findViewById(R.id.profile_phone);
        imgvAvatar = findViewById(R.id.profile_image);
        Coverimgv = findViewById(R.id.Cover_iv);
        fab = findViewById(R.id.fab);

        pd = new ProgressDialog(this);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Tải thông tin admin
        loadAdminInfo();

        // Sự kiện chỉnh sửa thông tin
        fab.setOnClickListener(v -> showEditProfileDialog());

        // Setup Toolbar
        Toolbar toolbar1 = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar1);

        // Setup BottomNavigationView
        setupBottomNavigation();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true; // Trả về true để hiển thị menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            mAuth.signOut();
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_manage_accounts) {
                Intent intentManageAccounts = new Intent(AdminProfileActivity.this, AdminActivity.class);
                startActivity(intentManageAccounts);
                finish();
                return true;

            } else if (itemId == R.id.nav_admin_profile) {
                Toast.makeText(AdminProfileActivity.this, "Bạn đang ở màn hình thông tin cá nhân!", Toast.LENGTH_SHORT).show();
                return true;

            } else {
                return false;
            }
        });

        bottomNavigation.setSelectedItemId(R.id.nav_admin_profile);
    }

    private void loadAdminInfo() {
        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = "" + snapshot.child("name").getValue();
                    String email = "" + snapshot.child("email").getValue();
                    String phone = "" + snapshot.child("phone").getValue();
                    String image = "" + snapshot.child("image").getValue();
                    String coverImg = "" + snapshot.child("cover").getValue();

                    tvName.setText(name);
                    tvEmail.setText(email);
                    tvPhone.setText(phone);

                    if (!TextUtils.isEmpty(image)) {
                        Picasso.get().load(image).into(imgvAvatar);
                    }

                    if (!TextUtils.isEmpty(coverImg)) {
                        Picasso.get().load(coverImg).into(Coverimgv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminProfileActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog() {
        String[] options = {"Chỉnh sửa ảnh đại diện", "Chỉnh sửa ảnh bìa", "Chỉnh sửa tên", "Chỉnh sửa số điện thoại"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn hành động");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                profileOrCoverPhoto = "image";
                showImageUpdateDialog();
            } else if (which == 1) {
                profileOrCoverPhoto = "cover";
                showImageUpdateDialog();
            } else if (which == 2) {
                showTextUpdateDialog("name");
            } else if (which == 3) {
                showTextUpdateDialog("phone");
            }
        });
        builder.create().show();
    }

    private void showImageUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập URL ảnh");

        EditText input = new EditText(this);
        input.setHint("https://example.com/your-image.jpg");
        builder.setView(input);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String imageUrl = input.getText().toString().trim();
            if (!TextUtils.isEmpty(imageUrl)) {
                updateImageToFirebase(imageUrl);
            } else {
                Toast.makeText(this, "Hãy nhập URL ảnh!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateImageToFirebase(String imageUrl) {
        pd.setMessage("Đang cập nhật...");
        pd.show();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(profileOrCoverPhoto, imageUrl);

        databaseReference.child(user.getUid()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showTextUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật " + key);

        EditText input = new EditText(this);
        input.setHint("Nhập " + key);
        builder.setView(input);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!TextUtils.isEmpty(value)) {
                updateTextToFirebase(key, value);
            } else {
                Toast.makeText(this, "Hãy nhập " + key, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateTextToFirebase(String key, String value) {
        pd.setMessage("Đang cập nhật...");
        pd.show();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(key, value);

        databaseReference.child(user.getUid()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
