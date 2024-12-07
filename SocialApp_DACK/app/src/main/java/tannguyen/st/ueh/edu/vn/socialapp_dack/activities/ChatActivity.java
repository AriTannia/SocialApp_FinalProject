package tannguyen.st.ueh.edu.vn.socialapp_dack.activities;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.MessageAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.databases.SQLiteHelper;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.MessageModel;

public class ChatActivity extends AppCompatActivity {

    // Views
    Toolbar toolbar;
    CircularImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;
    RecyclerView chatRecyclerView;

    // Firebase
    FirebaseAuth mAuth;
    DatabaseReference userRef, chatRef;
    private SQLiteHelper sqliteHelper; // Thêm biến cho SQLiteHelper

    String hisUid; // Receiver's UID
    String myUid;  // Sender's UID

    // Adapter and message list
    MessageAdapter adapter;
    List<MessageModel> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Views
        toolbar = findViewById(R.id.toolbar);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        myUid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        sqliteHelper = new SQLiteHelper(this);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(ChatActivity.this, messageList, chatRef, myUid); // Pass chatRef and myUid
        chatRecyclerView.setAdapter(adapter);

        // Set up Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get receiver UID from Intent
        hisUid = getIntent().getStringExtra("hisUid");

        // Load receiver's information
        loadReceiverInfo(hisUid);
        loadReceiverStatus(hisUid);

        // Set send button click listener
        sendBtn.setOnClickListener(v -> {
            String message = messageEt.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
            } else {
                Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn trống", Toast.LENGTH_SHORT).show();
            }
        });

        // Load chat messages
        loadMessages();
        markMessagesAsSeen(hisUid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu); // Gắn menu đã tạo
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.clear_all) {
            confirmDeleteAllMessages(); // Gọi hàm xác nhận xóa tất cả
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteAllMessages() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa tất cả tin nhắn");
        builder.setMessage("Bạn có chắc muốn xóa tất cả tin nhắn không?");
        builder.setPositiveButton("Xóa", (dialog, which) -> deleteAllMessages());
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deleteAllMessages() {
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel message = ds.getValue(MessageModel.class);
                    // Kiểm tra nếu tin nhắn giữa myUid và hisUid
                    if ((message.getSender().equals(myUid) && message.getReceiver().equals(hisUid)) ||
                            (message.getSender().equals(hisUid) && message.getReceiver().equals(myUid))) {
                        ds.getRef().removeValue(); // Xóa tin nhắn
                    }
                }
                Toast.makeText(ChatActivity.this, "Đã xóa tất cả tin nhắn", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Lỗi khi xóa tin nhắn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void loadReceiverInfo(String hisUid) {
        userRef.child(hisUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String image = snapshot.child("image").getValue(String.class);

                    nameTv.setText(name != null ? name : "No Name"); // Nếu không có tên, hiển thị mặc định
                    if (!TextUtils.isEmpty(image)) {
                        // Nếu có ảnh, tải ảnh bằng Picasso
                        Picasso.get().load(image).placeholder(R.drawable.error_image).into(profileIv);
                    } else {
                        // Nếu không có ảnh, đặt ảnh mặc định
                        profileIv.setImageResource(R.drawable.ic_default);
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Người dùng không tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Lỗi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadReceiverStatus(String hisUid) {
        DatabaseReference ref = userRef.child(hisUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    userStatusTv.setText(status != null && status.equals("online") ? "online" : "offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Không thể tải trạng thái người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message) {
        String messageId = chatRef.push().getKey();
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("messageId", messageId);
        chatMessage.put("sender", myUid);
        chatMessage.put("receiver", hisUid);
        chatMessage.put("message", message);
        chatMessage.put("timestamp", timestamp);
        chatMessage.put("isSeen", false);

        chatRef.child(messageId).setValue(chatMessage)
                .addOnSuccessListener(aVoid -> {
                    messageEt.setText(""); // Clear input field
                    // Lưu tin nhắn vào SQLite
                    MessageModel messageModel = new MessageModel(messageId, myUid, hisUid, message, timestamp, false);
                    sqliteHelper.insertMessage(messageModel);
                })
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        if (isNetworkAvailable()) {
            // Nếu có mạng, tải dữ liệu từ Firebase
            loadMessagesFromFirebase();
        } else {
            // Nếu không có mạng, tải dữ liệu từ SQLite
            loadMessagesFromSQLite();
        }
    }

    private void loadMessagesFromFirebase() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel message = ds.getValue(MessageModel.class);
                    if (message != null) {
                        messageList.add(message);
                        // Lưu tin nhắn vào SQLite
                        sqliteHelper.insertMessage(message);
                    }
                }
                adapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessagesFromSQLite() {
        Cursor cursor = sqliteHelper.getMessages(myUid, hisUid);
        messageList.clear();

        if (cursor != null) {
            try {
                // Kiểm tra danh sách các cột trong cursor
                for (String columnName : cursor.getColumnNames()) {
                    Log.d("SQLiteHelper", "Found column: " + columnName);
                }

                int messageIdIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_MESSAGE_ID);
                int senderIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_SENDER);
                int receiverIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_RECEIVER);
                int messageIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_MESSAGE);
                int timestampIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_MESSAGE_TIMESTAMP);
                int isSeenIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_IS_SEEN);

                if (messageIdIndex == -1 || senderIndex == -1 || receiverIndex == -1 ||
                        messageIndex == -1 || timestampIndex == -1 || isSeenIndex == -1) {
                    throw new IllegalArgumentException("Một hoặc nhiều cột không tồn tại trong bảng Messages");
                }

                while (cursor.moveToNext()) {
                    String messageId = cursor.getString(messageIdIndex);
                    String sender = cursor.getString(senderIndex);
                    String receiver = cursor.getString(receiverIndex);
                    String message = cursor.getString(messageIndex);
                    String timestamp = cursor.getString(timestampIndex);
                    boolean isSeen = cursor.getInt(isSeenIndex) == 1;

                    MessageModel messageModel = new MessageModel(messageId, sender, receiver, message, timestamp, isSeen);
                    messageList.add(messageModel);
                }
            } catch (Exception e) {
                Log.e("SQLiteHelper", "Lỗi khi tải tin nhắn từ SQLite: " + e.getMessage());
            } finally {
                cursor.close();
            }
        }

        adapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }


    private void markMessagesAsSeen(String hisUid) {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel message = ds.getValue(MessageModel.class);
                    if (message.getReceiver().equals(myUid) && message.getSender().equals(hisUid)) {
                        HashMap<String, Object> seenMap = new HashMap<>();
                        seenMap.put("isSeen", true);
                        ds.getRef().updateChildren(seenMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Không thể cập nhật trạng thái 'Đã xem'", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
