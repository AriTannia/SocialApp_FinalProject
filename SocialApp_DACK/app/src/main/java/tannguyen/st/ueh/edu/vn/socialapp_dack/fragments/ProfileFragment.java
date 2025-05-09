package tannguyen.st.ueh.edu.vn.socialapp_dack.fragments;

import static tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.ImageAdapter.saveImageToInternalStorage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.ImageAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.PostAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.databases.SQLiteHelper;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;
import tannguyen.st.ueh.edu.vn.socialapp_dack.utils.ImageDownloader;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone;
    private ImageView imgvAvatar, Coverimgv;
    private FloatingActionButton fab;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase FB_database;
    private DatabaseReference databaseReference;
    private SQLiteHelper dbHelper;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference postsRef;
    private FirebaseUser currentUser;

    ProgressDialog pd;

    Uri img_uri;
    String profileOrCoverPhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ View
        tvName = view.findViewById(R.id.profile_name);
        tvEmail = view.findViewById(R.id.profile_email);
        tvPhone = view.findViewById(R.id.profile_phone);
        imgvAvatar = view.findViewById(R.id.profile_image);
        Coverimgv = view.findViewById(R.id.Cover_iv);
        fab = view.findViewById(R.id.fab);
        dbHelper = new SQLiteHelper(getContext());

        recyclerViewPosts = view.findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize post list and adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, user.getUid());
        recyclerViewPosts.setAdapter(postAdapter);

        // Firebase reference for posts
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        // Kiểm tra kết nối mạng và tải dữ liệu
        checkNetworkAndLoadUserProfile();

        // Thiết lập sự kiện click cho fab (nút chỉnh sửa hồ sơ)
        fab.setOnClickListener(v -> showEditProfileDialogue());

        return view;
    }

    private void checkNetworkAndLoadUserProfile() {
        if (isNetworkAvailable()) {
            pd.show(); // Hiển thị ProgressDialog

            // Truy vấn Firebase
            Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        // Dữ liệu Firebase đã được tải thành công
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String uid = "" + ds.child("uid").getValue();
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String image = "" + ds.child("image").getValue();
                            String coverImg = "" + ds.child("cover").getValue();

                            // Hiển thị thông tin lên giao diện
                            tvName.setText(name);
                            tvEmail.setText(email);
                            tvPhone.setText(phone);

                            if (!TextUtils.isEmpty(image)) {
                                // Lưu ảnh đại diện vào bộ nhớ trong
                                ImageAdapter.saveImageToInternalStorage(getContext(), uid, "image", image, new ImageAdapter.SaveImageCallback() {
                                    @Override
                                    public void onImageSaved(String filePath) {
                                        dbHelper.updateUserInfo(uid, "image", filePath);
                                        Log.d("ProfileFragment", "Profile image saved at: " + filePath);
                                        Log.d("Picasso", "Image loaded successfully");

                                        // Cập nhật giao diện ảnh
                                        requireActivity().runOnUiThread(() -> Picasso.get().load(image).into(imgvAvatar));
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("ProfileFragment", "Error saving profile image: ", e);
                                        requireActivity().runOnUiThread(() -> Picasso.get().load(R.drawable.error_image).into(imgvAvatar));
                                        Log.e("Picasso", "Error loading image: " + e.getMessage());
                                    }
                                });
                            } else {
                                requireActivity().runOnUiThread(() -> Picasso.get().load(R.drawable.error_image).into(imgvAvatar));
                            }

                            if (!TextUtils.isEmpty(coverImg)) {
                                // Lưu ảnh bìa vào bộ nhớ trong
                                ImageAdapter.saveImageToInternalStorage(getContext(), uid, "cover", coverImg, new ImageAdapter.SaveImageCallback() {
                                    @Override
                                    public void onImageSaved(String filePath) {
                                        dbHelper.updateUserInfo(uid, "cover", filePath);
                                        Log.d("ProfileFragment", "Cover image saved at: " + filePath);

                                        // Cập nhật giao diện ảnh bìa
                                        requireActivity().runOnUiThread(() -> Picasso.get().load(coverImg).into(Coverimgv));
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("ProfileFragment", "Error saving cover image: ", e);
                                        requireActivity().runOnUiThread(() -> Picasso.get().load(R.drawable.error_image).into(Coverimgv));
                                    }
                                });
                            } else {
                                requireActivity().runOnUiThread(() -> Picasso.get().load(R.drawable.error_image).into(Coverimgv));
                            }

                            // Lưu thông tin SQLite
                            dbHelper.insertOrUpdateUser(uid, name, email, phone, image, coverImg);
                        }
                    } finally {
                        pd.dismiss(); // Đóng ProgressDialog dù có exception
                    }

                    // Tải bài viết của người dùng từ Firebase
                    loadPostsFromFirebase();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    pd.dismiss();
                    Log.e("FirebaseError", "Error fetching data: " + error.getMessage());
                    Toast.makeText(getActivity(), "Firebase Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    loadPostsFromSQLite();
                }
            });
        } else {
            // Nếu không có mạng, tải dữ liệu từ SQLite
            loadFromSQLite();
            loadPostsFromSQLite();
            pd.dismiss(); // Đóng ProgressDialog ngay lập tức vì không cần chờ
        }
    }

    private void loadPostsFromFirebase() {
        postsRef.orderByChild("userId").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(getContext(), "Không thể tải bài viết.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostsFromSQLite() {
        Cursor cursor = dbHelper.getPostsByUserId(user.getUid());
        if (cursor != null && cursor.moveToFirst()) {
            postList.clear();
            do {
                @SuppressLint("Range") Post post = new Post(
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_POST_ID)),
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_CONTENT)),
                        cursor.getLong(cursor.getColumnIndex(SQLiteHelper.COLUMN_TIMESTAMP)),
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_IMAGE_URL)),
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_POSTER_NAME))
                );
                postList.add(post);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.w("SQLiteData", "No posts found for user: " + user.getUid());
            Toast.makeText(getContext(), "Không có bài viết nào!", Toast.LENGTH_SHORT).show();
        }
        postAdapter.notifyDataSetChanged();
    }

    // Hàm kiểm tra kết nối mạng
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Hàm tải dữ liệu từ SQLite
    private void loadFromSQLite() {
        // Khi không có mạng và tải từ SQLite, hãy chắc chắn dừng ProgressDialog
        pd.dismiss();

        Cursor cursor = dbHelper.getUserByEmail(user.getEmail());
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_NAME));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_EMAIL));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_PHONE));
            @SuppressLint("Range") String image = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_IMAGE));
            @SuppressLint("Range") String coverImg = cursor.getString(cursor.getColumnIndex(SQLiteHelper.COLUMN_COVER));

            tvName.setText(name);
            tvEmail.setText(email);
            tvPhone.setText(phone);

            if (!TextUtils.isEmpty(image)) {
                imgvAvatar.setImageURI(Uri.parse(image));
            } else {
                imgvAvatar.setImageResource(R.drawable.error_image);
            }

            if (!TextUtils.isEmpty(coverImg)) {
                Coverimgv.setImageURI(Uri.parse(coverImg));
            } else {
                Coverimgv.setImageResource(R.drawable.error_image);
            }
        } else {
            Log.w("SQLiteData", "No data found in SQLite for email: " + user.getEmail());
            Toast.makeText(getContext(), "Không có dữ liệu cục bộ!", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void loadImage(ImageView imageView, String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.get().load(imageUrl).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.error_image);
        }
    }

    private void showEditProfileDialogue() {
        // Các tùy chọn
        String[] options = {"Chỉnh sửa ảnh đại diện", "Chỉnh sửa ảnh bìa", "Chỉnh sửa tên", "Chỉnh sửa số điện thoại"};
        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Đặt tiêu đề
        builder.setTitle("Chọn hành động");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Xử lý các mục trong hộp thoại khi được chọn
                if (which == 0) {
                    // Người dùng chọn "Chỉnh sửa ảnh đại diện"
                    pd.setMessage("Đang cập nhật ảnh đại diện");
                    profileOrCoverPhoto = "image";
                    showImgPicDialogue();
                }
                else if (which == 1) {
                    // Người dùng chọn "Chỉnh sửa ảnh bìa"
                    pd.setMessage("Đang cập nhật ảnh bìa");
                    profileOrCoverPhoto = "cover";
                    showImgPicDialogue();
                }
                else if (which == 2) {
                    // Người dùng chọn "Chỉnh sửa tên"
                    pd.setMessage("Đang cập nhật tên");
                    showNamePhoneUpdateDialogue("name");
                }
                else if (which == 3) {
                    // Người dùng chọn "Chỉnh sửa số điện thoại"
                    pd.setMessage("Đang cập nhật số điện thoại");
                    showNamePhoneUpdateDialogue("phone");
                }

            }
        });

        builder.create().show();
    }

    private void showNamePhoneUpdateDialogue(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Đang cập nhật " + key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Nhập " + key);

        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    // Cập nhật Firebase
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Đã cập nhật... " + key, Toast.LENGTH_SHORT).show();

                                    // Cập nhật thông tin vào SQLite sau khi thành công
                                    dbHelper.updateUserInfo(user.getUid(), key, value);  // Cập nhật SQLite
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getActivity(), "Hãy Nhập " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    private void showImgPicDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Nhập đường dẫn ảnh");

        // Tạo EditText để người dùng nhập link
        final EditText input = new EditText(getActivity());
        input.setHint("https://example.com/your-image.jpg");
        builder.setView(input);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String imageUrl = input.getText().toString().trim();
            if (!TextUtils.isEmpty(imageUrl)) {
                uploadProfileCoverPhoto(imageUrl);
            } else {
                Toast.makeText(getActivity(), "Hãy nhập đường dẫn ảnh!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void uploadProfileCoverPhoto(String imageUrl) {
        // Hiển thị ProgressDialog
        pd.setMessage("Đang cập nhật ảnh...");
        pd.show();

        // Tạo dữ liệu để cập nhật vào Firebase Realtime Database
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(profileOrCoverPhoto, imageUrl); // Chọn "image" hoặc "cover" tùy thuộc vào ảnh đại diện hay ảnh bìa

        // Lấy User id từ Firebase User
        String uid = user.getUid();

        // Lấy thông tin người dùng hiện tại từ Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Cập nhật ảnh vào Firebase
                    userRef.updateChildren(updates)
                            .addOnSuccessListener(unused -> {
                                pd.dismiss();
                                // Lưu ảnh vào bộ nhớ trong và cập nhật SQLite
                                ImageAdapter.saveImageToInternalStorage(getContext(), uid, profileOrCoverPhoto, imageUrl, new ImageAdapter.SaveImageCallback() {
                                    @Override
                                    public void onImageSaved(String filePath) {
                                        dbHelper.updateUserInfo(uid, profileOrCoverPhoto, filePath);
                                        Log.d("ProfileFragment", "Image saved at: " + filePath);

                                        requireActivity().runOnUiThread(() -> {
                                            Toast.makeText(getActivity(), "Đã cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("ProfileFragment", "Error saving image: ", e);

                                        requireActivity().runOnUiThread(() -> {
                                            Toast.makeText(getActivity(), "Lỗi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                pd.dismiss();
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getActivity(), "Lỗi cập nhật ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            });
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                pd.dismiss();
                Toast.makeText(getActivity(), "Lỗi khi lấy dữ liệu người dùng: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}