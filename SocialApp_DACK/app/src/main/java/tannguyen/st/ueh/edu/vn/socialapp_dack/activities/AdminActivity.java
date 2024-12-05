package tannguyen.st.ueh.edu.vn.socialapp_dack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.AdapterAdminUser;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private Button btnAddUser; // Nút thêm tài khoản
    private Button btnLogout;
    private BottomNavigationView bottomNavigation; // Thanh điều hướng dưới cùng


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Ánh xạ RecyclerView
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        btnAddUser = findViewById(R.id.btnAddUser); // Ánh xạ nút thêm tài khoản
        btnLogout = findViewById(R.id.btnLogout);  // Nút đăng xuất
        bottomNavigation = findViewById(R.id.bottom_navigation);

        btnAddUser.setOnClickListener(v -> showAddUserDialog()); // Gọi dialog thêm tài khoản
        btnLogout.setOnClickListener(v -> logoutUser()); // Gọi hàm đăng xuất

        // Xử lý sự kiện khi chọn mục trong BottomNavigationView
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_manage_accounts) {
                // Quản lý tài khoản - màn hình hiện tại
                Toast.makeText(this, "Bạn đang ở màn hình Quản lý tài khoản!", Toast.LENGTH_SHORT).show();
                return true;

            } else if (item.getItemId() == R.id.nav_admin_profile) {
                // Chuyển sang màn hình Thông tin cá nhân
                Intent intent = new Intent(AdminActivity.this, AdminProfileActivity.class);
                startActivity(intent);
                return true;

            } else {
                return false;
            }
        });


        // Tải danh sách người dùng
        loadUsers();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut(); // Đăng xuất khỏi Firebase Auth
        Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Đóng AdminActivity
    }


    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Tài Khoản Mới");

        // Tạo giao diện nhập liệu
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        EditText etName = new EditText(this);
        etName.setHint("Tên người dùng");
        layout.addView(etName);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        layout.addView(etEmail);

        EditText etPassword = new EditText(this);
        etPassword.setHint("Mật khẩu");
        etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPassword);

        builder.setView(layout);

        // Nút Thêm
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                addUserToFirebase(name, email, password);
            }
        });

        // Nút Hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void addUserToFirebase(String name, String email, String password) {
        String userId = FirebaseDatabase.getInstance().getReference("Users").push().getKey(); // Tạo ID ngẫu nhiên
        if (userId != null) {
            ModelUser newUser = new ModelUser(name, email, password, "", "", userId, "");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            ref.setValue(newUser)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thêm tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Tải lại danh sách
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi thêm tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUsers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ModelUser> userList = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);
                    if (user != null) {
                        if (!user.getEmail().equalsIgnoreCase("admin@gmail.com")) {
                            userList.add(user);
                        }
                    }
                }
                // Thiết lập adapter cho RecyclerView
                AdapterAdminUser adapter = new AdapterAdminUser(AdminActivity.this, userList, AdminActivity.this);
                recyclerViewUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Lỗi tải danh sách: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void editUser(ModelUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật thông tin người dùng");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        EditText etName = new EditText(this);
        etName.setText(user.getName());
        layout.addView(etName);

        EditText etEmail = new EditText(this);
        etEmail.setText(user.getEmail());
        layout.addView(etEmail);

        builder.setView(layout);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String updatedName = etName.getText().toString().trim();
            String updatedEmail = etEmail.getText().toString().trim();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            ref.child("name").setValue(updatedName);
            ref.child("email").setValue(updatedEmail)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void deleteUser(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        ref.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Xóa tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi xóa tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
