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

import java.util.HashMap;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.ImageAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.databases.SQLiteHelper;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;
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

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        FB_database = FirebaseDatabase.getInstance();
        databaseReference = FB_database.getReference("Users");

        // Kiểm tra kết nối mạng
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

                            // Hiển thị dữ liệu lên giao diện
                            tvName.setText(name);
                            tvEmail.setText(email);
                            tvPhone.setText(phone);

                            if (!TextUtils.isEmpty(image)) {
                                Picasso.get().load(image).into(imgvAvatar);

                                ImageAdapter.saveImageToInternalStorage(getContext(), "image", image, new ImageAdapter.SaveImageCallback() {
                                    @Override
                                    public void onImageSaved(String filePath) {
                                        // Cập nhật SQLite với đường dẫn file
                                        dbHelper.updateUserInfo(user.getUid(), "image", filePath);
                                        Log.d("ProfileFragment", "Profile image saved at: " + filePath);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("ProfileFragment", "Error saving profile image: " + e.getMessage());
                                    }
                                });
                            } else {
                                Picasso.get().load(R.drawable.error_image).into(imgvAvatar);
                            }

                            if (!TextUtils.isEmpty(coverImg)) {
                                Picasso.get().load(coverImg).into(Coverimgv);

                                ImageAdapter.saveImageToInternalStorage(getContext(), "cover", coverImg, new ImageAdapter.SaveImageCallback() {
                                    @Override
                                    public void onImageSaved(String filePath) {
                                        // Cập nhật SQLite với đường dẫn file ảnh bìa
                                        dbHelper.updateUserInfo(user.getUid(), "cover", filePath);
                                        Log.d("ProfileFragment", "Cover image saved at: " + filePath);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("ProfileFragment", "Error saving cover image: " + e.getMessage());
                                    }
                                });
                            } else {
                                Picasso.get().load(R.drawable.error_image).into(Coverimgv);
                            }

                            // Lưu dữ liệu vào SQLite
                            dbHelper.insertOrUpdateUser(uid, name, email, phone, image, coverImg);
                        }
                    } finally {
                        pd.dismiss(); // Đóng ProgressDialog dù có exception
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    try {
                        // Xử lý lỗi Firebase
                        Log.e("FirebaseError", "Error fetching data from Firebase: " + error.getMessage());
                        Toast.makeText(getActivity(), "Firebase Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                        // Nếu không có kết nối mạng, tải dữ liệu từ SQLite
                        if (!isNetworkAvailable()) {
                            loadFromSQLite();
                        }
                    } finally {
                        pd.dismiss(); // Đóng ProgressDialog trong mọi trường hợp
                    }
                }
            });
        } else {
            // Nếu không có mạng, tải dữ liệu từ SQLite
            loadFromSQLite();
            pd.dismiss(); // Đóng ProgressDialog ngay lập tức vì không cần chờ
        }

        // Thiết lập sự kiện click cho fab (nút chỉnh sửa hồ sơ)
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialogue();
            }
        });

        return view;
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
                                // Lưu ảnh vào bộ nhớ trong và cập nhật vào SQLite
                                saveImageToInternalStorage(getContext(), profileOrCoverPhoto, imageUrl, new ImageAdapter.SaveImageCallback() {
                                    @Override
                                    public void onImageSaved(String filePath) {
                                        // Thêm logic nếu cần
                                        dbHelper.updateUserInfo(uid, profileOrCoverPhoto, filePath);
                                        Log.d("ProfileFragment", "Image saved at: " + filePath);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("ProfileFragment", "Error saving image: " + e.getMessage());
                                    }
                                });

                                // Cập nhật vào SQLite sau khi cập nhật Firebase thành công
                                dbHelper.updateUserInfo(uid, profileOrCoverPhoto, imageUrl);
                                Toast.makeText(getActivity(), "Đã cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Lỗi cập nhật ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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