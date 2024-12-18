package tannguyen.st.ueh.edu.vn.socialapp_dack.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.AdapterUsers;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.ImageAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.databases.SQLiteHelper;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdapterUsers adapterUsers;
    private List<ModelUser> userList;
    private SQLiteHelper dbHelper;
    private ValueEventListener listener;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        userList = new ArrayList<>();
        dbHelper = new SQLiteHelper(requireContext()); // Khởi tạo SQLiteHelper

        // Kiểm tra mạng và tải dữ liệu
        if (isNetworkAvailable()) {
            getAllUsersFromFirebase(); // Tải dữ liệu từ Firebase nếu có mạng
        } else {
            loadUsersFromSQLite(); // Tải dữ liệu từ SQLite nếu không có mạng
        }

        return view;
    }

    private void processUserFromFirebase(ModelUser user) {
        if (user == null) return;

        // Lưu ảnh profile
        if (!TextUtils.isEmpty(user.getImage())) {
            ImageAdapter.saveImageToInternalStorage(getContext(), user.getUid(), "image", user.getImage(), new ImageAdapter.SaveImageCallback() {
                @Override
                public void onImageSaved(String profilePath) {
                    // Tiếp tục lưu ảnh bìa sau khi ảnh profile được lưu
                    saveCoverImage(user, profilePath);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Firebase-SQLite", "Error saving profile image: ", e);
                    // Dùng null cho profilePath nếu xảy ra lỗi
                    saveCoverImage(user, null);
                }
            });
        } else {
            // Nếu không có ảnh profile, tiếp tục xử lý ảnh bìa
            saveCoverImage(user, null);
        }
    }

    private void saveCoverImage(ModelUser user, String profilePath) {
        if (!TextUtils.isEmpty(user.getcover())) {
            ImageAdapter.saveImageToInternalStorage(getContext(), user.getUid(), "cover", user.getcover(), new ImageAdapter.SaveImageCallback() {
                @Override
                public void onImageSaved(String coverPath) {
                    // Lưu thông tin user vào SQLite khi cả hai ảnh đã được xử lý
                    saveUserToSQLite(user, profilePath, coverPath);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Firebase-SQLite", "Error saving cover image: ", e);
                    // Dùng null cho coverPath nếu xảy ra lỗi
                    saveUserToSQLite(user, profilePath, null);
                }
            });
        } else {
            // Nếu không có ảnh bìa, lưu thông tin user ngay lập tức
            saveUserToSQLite(user, profilePath, null);
        }
    }

    private void saveUserToSQLite(ModelUser user, String profilePath, String coverPath) {
        dbHelper.insertOrUpdateUser(user.getUid(), user.getName(), user.getEmail(), user.getPhone(), profilePath, coverPath);
        Log.d("Firebase-SQLite", "User saved to SQLite: UID=" + user.getUid() + ", ProfilePath=" + profilePath + ", CoverPath=" + coverPath);
    }

    private void getAllUsersFromFirebase() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear(); // Xóa danh sách user trước khi thêm mới

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);

                    if (user != null && fUser != null && !user.getUid().equals(fUser.getUid())) {
                        processUserFromFirebase(user); // Xử lý từng user
                        userList.add(user); // Cập nhật danh sách người dùng
                    }
                }

                updateAdapter(); // Cập nhật giao diện sau khi hoàn tất
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi: " + error.getMessage());
                Toast.makeText(getActivity(), "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                loadUsersFromSQLite(); // Tải dữ liệu từ SQLite khi Firebase bị lỗi
            }
        });
    }


    private void loadUsersFromSQLite() {
        Log.d("DataLoad", "Loading users from SQLite...");
        userList.clear();

        // Lấy UID của tài khoản đang đăng nhập
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        Cursor cursor = dbHelper.getAllUsers();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String uid = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_UID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_NAME));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_EMAIL));
                @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_PHONE));
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_IMAGE));
                @SuppressLint("Range") String coverPath = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_COVER));

                // Kiểm tra nếu tài khoản đang đăng nhập không được hiển thị
                if (currentUserUid != null && currentUserUid.equals(uid)) {
                    continue; // Bỏ qua tài khoản đang đăng nhập
                }

                // Tạo ModelUser và thêm vào danh sách
                ModelUser user = new ModelUser(name, email, null, phone, imagePath, uid, coverPath);
                userList.add(user);
            } while (cursor.moveToNext());

            Log.d("SQLiteData", "Loaded " + userList.size() + " users from SQLite.");
            cursor.close();
        }

        // Cập nhật adapter sau khi lọc dữ liệu
        updateAdapter();
    }

    private boolean isNetworkAvailable() {
        if (getActivity() == null) return false;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) return false;

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void searchUsers(String query) {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Xóa listener cũ trước khi thêm mới
        if (listener != null) {
            ref.removeEventListener(listener);
        }

        // Tạo mới listener
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear(); // Xóa danh sách cũ

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);

                    if (user != null && fUser != null && !user.getUid().equals(fUser.getUid())) {
                        if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(user);
                        }
                    }
                }
                // Cập nhật adapter
                updateAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi: " + error.getMessage());
            }
        };

        // Thêm listener mới
        ref.addValueEventListener(listener);
    }

    private void updateAdapter() {
        // Kiểm tra null trước khi gán adapter
        Log.d("LÀO GÌ CŨNG TÔN", String.valueOf(adapterUsers));
        if (adapterUsers == null) {
            adapterUsers = new AdapterUsers(getActivity(), userList);
            recyclerView.setAdapter(adapterUsers); // Gắn adapter vào RecyclerView
        } else {
            adapterUsers.notifyDataSetChanged(); // Cập nhật dữ liệu
        }
    }
}