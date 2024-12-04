package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import tannguyen.st.ueh.edu.vn.socialapp_dack.Model.ModelUser;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;

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

    // Need To Change from other (5:40 - 03/12/2024)
    private void registerUser(String name, String email, String password) {
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateProfile(user, name, email, password);
                        }
                    } else {
                        handleRegistrationFailure(task); // (2)
                    }
                })
                .addOnFailureListener(e -> handleFailure(e)); // (5)
    }

    // Cập nhật thông tin người dùng ==> Dùng để lưu vào db sau khi đăng ký
    private void updateProfile(FirebaseUser user, String name, String email, String password) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        saveUserToDatabase(user, name, email, password);
                    } else {
                        handleProfileUpdateFailure(profileTask); // (3)
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user, String name, String email, String password) {
        // Lấy User id từ Firebase User
        String uid = user.getUid();
        ModelUser newUser = new ModelUser(name, email, password, "", "", uid, ""); // Add uid here
        ModelUser newUser = new ModelUser(name, email, password, "", "", uid, ""); // Add uid here

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        userRef.setValue(newUser)
                .addOnCompleteListener(databaseTask -> {
                    if (databaseTask.isSuccessful()) {
                        handleSuccessfulRegistration(); // (1)
                    } else {
                        handleDatabaseSaveFailure(databaseTask); // (4)
                    }
                });
    }

    // Xử lý khi đăng ký thành công (1)
    private void handleSuccessfulRegistration() {
        Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Xử lý khi đăng ký thất bại - thuộc else của onSuccess (2)
    private void handleRegistrationFailure(Task<AuthResult> task) {
        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    // Xử lý khi cập nhật profile bị lỗi (3)
    private void handleProfileUpdateFailure(Task<Void> profileTask) {
        Toast.makeText(RegisterActivity.this, "Lỗi cập nhật thông tin người dùng: " + profileTask.getException().getMessage(), Toast.LENGTH_LONG).show();
    }

    // Xử lý khi lưu vào db bị lỗi (4)
    private void handleDatabaseSaveFailure(Task<Void> databaseTask) {
        Toast.makeText(RegisterActivity.this, "Lỗi lưu dữ liệu vào Realtime Database: " + databaseTask.getException().getMessage(), Toast.LENGTH_LONG).show();
    }

    // Xử lý khi đăng ký bị lỗi (thuộc onFailture) (5)
    private void handleFailure(Exception e) {
        progressDialog.dismiss();
        Toast.makeText(RegisterActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }


}