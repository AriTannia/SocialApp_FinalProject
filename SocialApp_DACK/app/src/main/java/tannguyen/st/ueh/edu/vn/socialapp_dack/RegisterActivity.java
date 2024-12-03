package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.intellij.lang.annotations.Pattern;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private TextView tvLogin;
    private Button Registerbtn;
    private EditText Nameedt, Emailedt, Psdedt;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ
        Nameedt = findViewById(R.id.mNameedt);
        Emailedt = findViewById(R.id.mEmailedt);
        Psdedt = findViewById(R.id.mPsdedt);
        tvLogin = findViewById(R.id.tv_login);
        Registerbtn = findViewById(R.id.mRegisterbtn);

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        // Xử lý click vào "ĐĂNG NHẬP"
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class); // Chuyển về MainActivity
            startActivity(intent);
            finish();
        });

        Registerbtn.setOnClickListener(v -> {
            String name = Nameedt.getText().toString().trim();
            String email = Emailedt.getText().toString().trim();
            String password = Psdedt.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Emailedt.setError("Email không hợp lệ");
                Emailedt.requestFocus();
            } else if (password.length() <= 5) {
                Psdedt.setError("Mật khẩu phải có độ dài lớn hơn 5");
                Psdedt.requestFocus();
            } else {
                registerUser(name, email, password);
            }
        });
    }

    private void registerUser(String name, String email, String password) {
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.show();

        // Thực hiện tạo người dùng với Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        // Đăng ký thành công
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Cập nhật thông tin người dùng (nếu cần)
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            // Cập nhật profile người dùng
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Lưu thông tin người dùng vào Firebase Realtime Database
                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                                            User newUser = new User(name, email, password); // Tạo đối tượng User

                                            // Lưu thông tin người dùng vào Realtime Database
                                            userRef.setValue(newUser)
                                                    .addOnCompleteListener(databaseTask -> {
                                                        if (databaseTask.isSuccessful()) {
                                                            // Đăng ký thành công và lưu thông tin vào Realtime Database
                                                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Lỗi lưu dữ liệu vào Realtime Database: " + databaseTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Lỗi cập nhật thông tin người dùng: " + profileTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        // Đăng ký thất bại
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}