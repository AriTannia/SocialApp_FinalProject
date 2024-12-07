package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.activities.ChatActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;
import tannguyen.st.ueh.edu.vn.socialapp_dack.R;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUser> userList;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate (hiển thị) layout(row_users.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String name = userList.get(position).getName();
        String email = userList.get(position).getEmail();

        holder.mNameTv.setText(name);
        holder.mEmailTv.setText(email);

        if (isOnline()) {
            // Online - Load images using Picasso
            try {
                Picasso.get()
                        .load(userImage)
                        .placeholder(R.drawable.error_image)
                        .into(holder.avatarCiv);
            } catch (Exception e) {
                Log.e("PicassoError", "Error loading image online", e);
            }
        } else {
            // Offline - Use setImageURI
            if (!TextUtils.isEmpty(userImage)) {
                holder.avatarCiv.setImageURI(Uri.parse(userImage));
            } else {
                holder.avatarCiv.setImageResource(R.drawable.error_image);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            // Mở ChatActivity và truyền UID của người dùng
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("hisUid", hisUID);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        ImageView avatarCiv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarCiv = itemView.findViewById(R.id.AvatarCiv);
            mNameTv = itemView.findViewById(R.id.nameTv_ru);
            mEmailTv = itemView.findViewById(R.id.emailTv_ru);
        }
    }

    /**
     * Checks if the device is connected to a network (online).
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            return network != null; // Return true if the network exists.
        }
        return false;
    }
}
