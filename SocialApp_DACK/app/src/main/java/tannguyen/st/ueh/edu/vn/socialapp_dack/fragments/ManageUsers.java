package tannguyen.st.ueh.edu.vn.socialapp_dack.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
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
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.ImageAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.databases.SQLiteHelper;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;

public class ManageUsers extends Fragment {

    private RecyclerView recyclerViewUsers;
    private Button btnAddUser;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private SQLiteHelper dbHelper;

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
        if (isNetworkAvailable()) {
            loadUsersFromFirebase();
        } else {
            loadUsersFromSQLite();
        }

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
                        loadUsersFromFirebase(); // Tải lại danh sách người dùng
                    });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void deleteUser(String userId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("comments");
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("Chats");

        // Tra cứu email và password từ userId
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userEmail = snapshot.child("email").getValue(String.class);
                    String userPassword = snapshot.child("password").getValue(String.class);

                    if (userEmail != null && userPassword != null) {
                        // Đăng nhập tạm thời với email và password
                        auth.signInWithEmailAndPassword(userEmail, userPassword)
                                .addOnSuccessListener(authResult -> {
                                    FirebaseUser currentUser = auth.getCurrentUser();
                                    if (currentUser != null && currentUser.getUid().equals(userId)) {
                                        // Xóa tài khoản khỏi Authentication
                                        currentUser.delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Sau khi xóa tài khoản Authentication, xóa dữ liệu trong Realtime Database
                                                    deleteUserDataFromDatabase(userId, usersRef, postsRef, commentsRef, chatsRef);

                                                    // Đăng nhập lại tài khoản admin
                                                    reLoginAsAdmin();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getContext(), "Lỗi khi xóa tài khoản Authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    reLoginAsAdmin(); // Dù lỗi, vẫn đăng nhập lại admin
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "Người dùng không hợp lệ!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi đăng nhập tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy email hoặc password của người dùng!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy người dùng với userId này!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi truy vấn dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm phụ để xóa dữ liệu trong Realtime Database
    private void deleteUserDataFromDatabase(String userId, DatabaseReference usersRef, DatabaseReference postsRef, DatabaseReference commentsRef, DatabaseReference chatsRef) {
        // Xóa User từ "Users" nhánh
        usersRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Xóa tất cả các bài đăng của User
                    postsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ds.getRef().removeValue(); // Xóa từng bài đăng
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Lỗi khi xóa bài đăng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Xóa tất cả bình luận của User
                    commentsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ds.getRef().removeValue(); // Xóa từng bình luận
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Lỗi khi xóa bình luận: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Xóa tất cả tin nhắn của User
                    chatsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ds.getRef().removeValue(); // Xóa từng tin nhắn
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Lỗi khi xóa tin nhắn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    Toast.makeText(getContext(), "Đã xóa tài khoản và dữ liệu liên quan thành công!", Toast.LENGTH_SHORT).show();
                    loadUsersFromFirebase(); // Tải lại danh sách người dùng
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi xóa dữ liệu Realtime Database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm đăng nhập lại với tài khoản admin
    private void reLoginAsAdmin() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String adminEmail = "admin@gmail.com"; // Thay bằng email admin thực tế
        String adminPassword = "admin1"; // Thay bằng password admin thực tế

        auth.signInWithEmailAndPassword(adminEmail, adminPassword)
                .addOnSuccessListener(authResult -> Toast.makeText(getContext(), "Đã đăng nhập lại tài khoản admin thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi đăng nhập lại tài khoản admin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addUserToFirebase(String name, String email, String password) {
        String userId = FirebaseDatabase.getInstance().getReference("Users").push().getKey();
        if (userId != null) {
            ModelUser newUser = new ModelUser(name, email, password, "", "", userId, "");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            ref.setValue(newUser)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getActivity(), "Thêm tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        loadUsersFromFirebase();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Lỗi khi thêm tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUsersFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SQLiteHelper dbHelper = new SQLiteHelper(requireContext());
                List<ModelUser> userList = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);

                    if (user != null && !user.getEmail().equalsIgnoreCase("admin@gmail.com")) {
                        Log.d("Firebase-User", "Loaded user: " + user.getName() + ", Email: " + user.getEmail());

                        userList.add(user);

                        // Lưu ảnh đại diện
                        if (!TextUtils.isEmpty(user.getImage())) {
                            Log.d("Firebase-Image", "Processing profile image: " + user.getImage());
                            ImageAdapter.saveImageToInternalStorage(getContext(), user.getUid(), "image", user.getImage(), new ImageAdapter.SaveImageCallback() {
                                @Override
                                public void onImageSaved(String filePath) {
                                    Log.d("Image-Save", "Saved profile image path: " + filePath);
                                    dbHelper.insertOrUpdateUser(user.getUid(), user.getName(), user.getEmail(), user.getPhone(), filePath, user.getcover());
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("Image-Save", "Error saving profile image", e);
                                }
                            });
                        }

                        // Lưu ảnh bìa
                        if (!TextUtils.isEmpty(user.getcover())) {
                            Log.d("Firebase-Cover", "Processing cover image: " + user.getcover());
                            ImageAdapter.saveImageToInternalStorage(getContext(), user.getUid(), "cover", user.getcover(), new ImageAdapter.SaveImageCallback() {
                                @Override
                                public void onImageSaved(String filePath) {
                                    Log.d("Image-Save", "Saved cover image path: " + filePath);
                                    dbHelper.insertOrUpdateUser(user.getUid(), user.getName(), user.getEmail(), user.getPhone(), user.getImage(), filePath);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("Image-Save", "Error saving cover image", e);
                                }
                            });
                        }
                    }
                }

                AdapterAdminUser adapter = new AdapterAdminUser(getActivity(), userList, ManageUsers.this);
                recyclerViewUsers.setAdapter(adapter);
                Log.d("User-List", "Number of users loaded: " + userList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Lỗi tải danh sách: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase-Error", "Error loading user list: ", error.toException());
            }
        });
    }


    @SuppressLint("Range")
    private void loadUsersFromSQLite() {
        SQLiteHelper dbHelper = new SQLiteHelper(getContext());
        Cursor cursor = dbHelper.getAllUsers();
        List<ModelUser> userList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String uid = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_UID));
                String name = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_NAME));
                String email = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_PHONE));
                String imagePath = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_IMAGE));
                String coverPath = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_COVER));

                ModelUser user = new ModelUser(name, email, null, phone, imagePath, uid, coverPath);
                userList.add(user);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Hiển thị danh sách người dùng
        AdapterAdminUser adapter = new AdapterAdminUser(getActivity(), userList, ManageUsers.this);
        recyclerViewUsers.setAdapter(adapter);
    }

    // Kiểm tra trạng thái mạng
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            return network != null;
        }
        return false;
    }
}