package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.AdminActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;

public class AdapterAdminUser extends RecyclerView.Adapter<AdapterAdminUser.AdminUserViewHolder> {

    private Context context;
    private List<ModelUser> userList;
    private AdminActivity adminActivity;

    public AdapterAdminUser(Context context, List<ModelUser> userList, AdminActivity adminActivity) {
        this.context = context;
        this.userList = userList;
        this.adminActivity = adminActivity;
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

        // Hiển thị ảnh đại diện
        try {
            Picasso.get()
                    .load(user.getImage()) // Lấy URL ảnh từ ModelUser
                    .placeholder(R.drawable.error_image) // Ảnh mặc định nếu không tải được
                    .error(R.drawable.error_image) // Ảnh hiển thị khi có lỗi
                    .into(holder.avatarCiv); // Đặt ảnh vào ImageView
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Xóa người dùng
        holder.deleteBtn.setOnClickListener(v -> {
            adminActivity.deleteUser(user.getUid());
        });

        // Sửa thông tin người dùng
        holder.editBtn.setOnClickListener(v -> {
            adminActivity.editUser(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class AdminUserViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv, emailTv;
        ImageView avatarCiv; // Thêm ImageView để hiển thị avatar
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
