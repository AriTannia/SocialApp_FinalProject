package tannguyen.st.ueh.edu.vn.socialapp_dack.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.activities.AdminProfileActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.AdapterAdminUser;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;

public class ManageUsers extends Fragment {

    private RecyclerView recyclerViewUsers;
    private Button btnAddUser;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public ManageUsers() {
        // Required empty public constructor
    }

    public static ManageUsers newInstance(String param1, String param2) {
        ManageUsers fragment = new ManageUsers();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_users, container, false);

        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getActivity()));

        btnAddUser = view.findViewById(R.id.btnAddUser);

        btnAddUser.setOnClickListener(v -> showAddUserDialog());

        // Tải danh sách người dùng
        loadUsers();

        return view;
    }

    private void showAddUserDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle("Thêm Tài Khoản Mới");

        // Tạo giao diện nhập liệu
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        EditText etName = new EditText(getActivity());
        etName.setHint("Tên người dùng");
        layout.addView(etName);

        EditText etEmail = new EditText(getActivity());
        etEmail.setHint("Email");
        layout.addView(etEmail);

        EditText etPassword = new EditText(getActivity());
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
                Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                addUserToFirebase(name, email, password);
            }
        });

        // Nút Hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    public void editUser(ModelUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // Sử dụng getContext() thay vì this
        builder.setTitle("Cập nhật thông tin người dùng");

        LinearLayout layout = new LinearLayout(getContext()); // Sử dụng getContext()
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        EditText etName = new EditText(getContext()); // Sử dụng getContext()
        etName.setText(user.getName());
        layout.addView(etName);

        EditText etEmail = new EditText(getContext()); // Sử dụng getContext()
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
                        Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Tải lại danh sách người dùng
                    });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void deleteUser(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        ref.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Xóa tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    loadUsers(); // Tải lại danh sách người dùng
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi xóa tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addUserToFirebase(String name, String email, String password) {
        String userId = FirebaseDatabase.getInstance().getReference("Users").push().getKey();
        if (userId != null) {
            ModelUser newUser = new ModelUser(name, email, password, "", "", userId, "");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            ref.setValue(newUser)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getActivity(), "Thêm tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Lỗi khi thêm tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                    if (user != null && !user.getEmail().equalsIgnoreCase("admin@gmail.com")) {
                        userList.add(user);
                    }
                }
                // Thiết lập adapter cho RecyclerView
                AdapterAdminUser adapter = new AdapterAdminUser(getActivity(), userList, ManageUsers.this);
                recyclerViewUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Lỗi tải danh sách: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}