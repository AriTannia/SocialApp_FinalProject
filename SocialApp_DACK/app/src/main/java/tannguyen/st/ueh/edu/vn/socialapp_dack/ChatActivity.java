package tannguyen.st.ueh.edu.vn.socialapp_dack;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.MessageAdapter;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.MessageModel;

public class ChatActivity extends AppCompatActivity {

    // Views from XML
    Toolbar toolbar;
    CircularImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;
    RecyclerView chatRecyclerView;

    // Firebase references
    FirebaseAuth mAuth;
    DatabaseReference userRef, chatRef;

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

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        myUid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(ChatActivity.this, messageList);
        chatRecyclerView.setAdapter(adapter);

        // Set up Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get receiver UID from Intent
        hisUid = getIntent().getStringExtra("hisUid");

        // Load receiver's information
        loadReceiverInfo(hisUid);

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
    }

    private void loadReceiverInfo(String hisUid) {
        userRef.child(hisUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String image = snapshot.child("image").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class); // Optional: online/offline

                    nameTv.setText(name);
                    userStatusTv.setText(status != null ? status : "offline");
                    if (image != null) {
                        Picasso.get().load(image).placeholder(R.drawable.ic_default).into(profileIv);
                    } else {
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

    private void sendMessage(String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("sender", myUid);
        chatMessage.put("receiver", hisUid);
        chatMessage.put("message", message);
        chatMessage.put("timestamp", timestamp);

        chatRef.push().setValue(chatMessage)
                .addOnSuccessListener(aVoid -> messageEt.setText("")) // Clear the input field after sending
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel message = ds.getValue(MessageModel.class);

                    if ((message.getSender().equals(myUid) && message.getReceiver().equals(hisUid)) ||
                            (message.getSender().equals(hisUid) && message.getReceiver().equals(myUid))) {
                        messageList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Không thể tải tin nhắn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
