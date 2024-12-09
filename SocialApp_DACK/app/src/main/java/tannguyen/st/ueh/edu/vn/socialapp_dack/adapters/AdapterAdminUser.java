package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.activities.AdminActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.fragments.ManageUsers;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;

public class AdapterAdminUser extends RecyclerView.Adapter<AdapterAdminUser.AdminUserViewHolder> {

    private Context context;
    private List<ModelUser> userList;
    private ManageUsers manageUsersFragment; // Thay vì AdminActivity, truyền fragment

    // Sửa constructor để nhận ManageUsers (Fragment)
    public AdapterAdminUser(Context context, List<ModelUser> userList, ManageUsers manageUsersFragment) {
        this.context = context;
        this.userList = userList;
        this.manageUsersFragment = manageUsersFragment;
    }

    @NonNull
    @Override
    public AdminUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_admin_user, parent, false);
        return new AdminUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserViewHolder holder, int position) {
        ModelUser user = userList.get(position);

        holder.nameTv.setText(user.getName());
        holder.emailTv.setText(user.getEmail());

        if (isOnline()) {
            // Online - Load image from URL using Picasso
            try {
                Picasso.get()
                        .load(user.getImage())
                        .placeholder(R.drawable.error_image)
                        .into(holder.avatarCiv);
            } catch (Exception e) {
                Log.e("PicassoError", "Error loading image online", e);
            }
        } else {
            // Offline - Use local URI or placeholder
            if (!TextUtils.isEmpty(user.getImage())) {
                holder.avatarCiv.setImageURI(Uri.parse(user.getImage()));
            } else {
                holder.avatarCiv.setImageResource(R.drawable.error_image);
            }
        }

        // Xóa người dùng
        holder.deleteBtn.setOnClickListener(v -> {
            if (manageUsersFragment != null) {
                manageUsersFragment.deleteUser(user.getUid());
            }
        });

        // Sửa thông tin người dùng
        holder.editBtn.setOnClickListener(v -> {
            if (manageUsersFragment != null) {
                manageUsersFragment.editUser(user);
            }
        });
    }

    // Kiểm tra trạng thái mạng
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            return network != null;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class AdminUserViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv, emailTv;
        ImageView avatarCiv;
        Button editBtn, deleteBtn;

        public AdminUserViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.tvAdminName);
            emailTv = itemView.findViewById(R.id.tvAdminEmail);
            avatarCiv = itemView.findViewById(R.id.AvatarCiv); // Ánh xạ ImageView
            editBtn = itemView.findViewById(R.id.btnAdminEdit);
            deleteBtn = itemView.findViewById(R.id.btnAdminDelete);
        }
    }
}

