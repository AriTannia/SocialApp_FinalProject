package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.MessageModel;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messageList;
    private String myUid;
    private DatabaseReference chatRef;

    public MessageAdapter(Context context, List<MessageModel> messageList, DatabaseReference chatRef, String myUid) {
        this.context = context;
        this.messageList = messageList;
        this.chatRef = chatRef;
        this.myUid = myUid;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messageList.get(position);

        // Set nội dung tin nhắn
        holder.messageTv.setText(message.getMessage());

        // Kiểm tra tin nhắn được gửi bởi ai (người gửi hiện tại hay người nhận)
        if (message.getSender().equals(myUid)) {
            // Tin nhắn được gửi: căn phải
            holder.messageTv.setBackgroundResource(R.drawable.bg_message_sender);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageTv.getLayoutParams();
            params.gravity = Gravity.END;
            holder.messageTv.setLayoutParams(params);

            // Hiển thị trạng thái "Seen" hoặc "Delivered"
            if (message.isSeen()) {
                holder.statusTv.setText("Seen");
            } else {
                holder.statusTv.setText("Delivered");
            }
            holder.statusTv.setVisibility(View.VISIBLE);
        } else {
            // Tin nhắn nhận: căn trái
            holder.messageTv.setBackgroundResource(R.drawable.bg_message_receiver);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageTv.getLayoutParams();
            params.gravity = Gravity.START;
            holder.messageTv.setLayoutParams(params);
            holder.statusTv.setVisibility(View.GONE); // Ẩn trạng thái với tin nhắn nhận
        }

        // Thêm sự kiện long click vào itemView để hiển thị PopupMenu
        holder.itemView.setOnLongClickListener(v -> {
            if (message.getSender().equals(myUid)) {
                // Chỉ hiển thị menu cho tin nhắn do người dùng hiện tại gửi
                showPopupMenu(holder, message);
            }
            return true;
        });
    }


    private void showPopupMenu(MessageViewHolder holder, MessageModel message) {
        PopupMenu popupMenu = new PopupMenu(context, holder.itemView);
        popupMenu.inflate(R.menu.message_options); // Gắn file menu `message_options.xml`
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.edit_message) {
                showEditDialog(message); // Gọi hàm sửa tin nhắn
                return true;
            } else if (itemId == R.id.delete_message) {
                deleteMessage(message); // Gọi hàm xóa tin nhắn
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }


    private void showEditDialog(MessageModel message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sửa tin nhắn");

        // Add EditText to the dialog
        EditText input = new EditText(context);
        input.setText(message.getMessage());
        builder.setView(input);

        // Set dialog buttons
        builder.setPositiveButton("Sửa", (dialog, which) -> {
            String updatedMessage = input.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedMessage)) {
                chatRef.child(message.getMessageId()).child("message").setValue(updatedMessage)
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Tin nhắn đã được sửa", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Không thể sửa tin nhắn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "Tin nhắn không thể để trống", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void deleteMessage(MessageModel message) {
        chatRef.child(message.getMessageId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Tin nhắn đã được xóa", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Không thể xóa tin nhắn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTv, statusTv;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.messageTv);
            statusTv = itemView.findViewById(R.id.statusTv); // For "Seen/Delivered" status
        }
    }
}
