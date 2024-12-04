package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.MessageModel;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messageList;
    private String myUid;

    public MessageAdapter(Context context, List<MessageModel> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

        holder.messageTv.setText(message.getMessage());

        // Align messages and show "Seen/Delivered" based on sender or receiver
        if (message.getSender().equals(myUid)) {
            // Message sent by current user
            holder.messageTv.setBackgroundResource(R.drawable.bg_message_sender);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageTv.getLayoutParams();
            params.gravity = Gravity.END;
            holder.messageTv.setLayoutParams(params);

            // Display "Seen" or "Delivered"
            if (message.isSeen()) {
                holder.statusTv.setText("Seen");
            } else {
                holder.statusTv.setText("Delivered");
            }
        } else {
            // Message received by current user
            holder.messageTv.setBackgroundResource(R.drawable.bg_message_receiver);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageTv.getLayoutParams();
            params.gravity = Gravity.START;
            holder.messageTv.setLayoutParams(params);

            // Hide status for received messages
            holder.statusTv.setVisibility(View.GONE);
        }
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
            statusTv = itemView.findViewById(R.id.statusTv); // Added for "Seen/Delivered" status
        }
    }
}
