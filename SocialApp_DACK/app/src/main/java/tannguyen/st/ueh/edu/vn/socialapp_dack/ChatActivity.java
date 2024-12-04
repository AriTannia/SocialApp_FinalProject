package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularimageview.CircularImageView;

public class ChatActivity extends AppCompatActivity {

    // Views from XML
    Toolbar toolbar;
    CircularImageView profileIv; // Ảnh đại diện (CircularImageView từ thư viện mikhaellopez)
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(""); // Xóa tiêu đề mặc định của Toolbar

        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);

        // Thiết lập thông tin của người nhận
        setReceiverInfo("Doan Thanh Lam", "online");

        // Logic gửi tin nhắn
        sendBtn.setOnClickListener(view -> {
            String message = messageEt.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageEt.setText(""); // Xóa nội dung sau khi gửi
            }
        });
    }

    /**
     * Thiết lập thông tin người nhận (tên và trạng thái)
     *
     * @param name   Tên người nhận
     * @param status Trạng thái người nhận (online/offline)
     */
    private void setReceiverInfo(String name, String status) {
        nameTv.setText(name);
        userStatusTv.setText(status);
    }

    /**
     * Logic xử lý gửi tin nhắn
     *
     * @param message Nội dung tin nhắn
     */
    private void sendMessage(String message) {
        // (Placeholder) Thêm logic gửi tin nhắn tại đây
        // Có thể cập nhật RecyclerView hoặc gửi đến server

        // Hiển thị log cho mục đích kiểm tra
        System.out.println("Tin nhắn đã gửi: " + message);
    }
}
