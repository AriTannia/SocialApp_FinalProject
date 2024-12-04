package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.ChatActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;
import tannguyen.st.ueh.edu.vn.socialapp_dack.R;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

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
        // Lấy dữ liệu
        String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String name = userList.get(position).getName();
        String email = userList.get(position).getEmail();

        holder.mNameTv.setText(name);
        holder.mEmailTv.setText(email);

        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.error_image)
                    .into(holder.avatarCiv);

        } catch (Exception e) {

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở ChatActivity và truyền UID của người dùng
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUID); // Gửi UID của người nhận
                context.startActivity(intent);    // Chuyển sang ChatActivity
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        ImageView avatarCiv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarCiv = itemView.findViewById(R.id.AvatarCiv);
            mNameTv = itemView.findViewById(R.id.nameTv_ru);
            mEmailTv = itemView.findViewById(R.id.emailTv_ru);
        }
    }
}
