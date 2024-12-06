package tannguyen.st.ueh.edu.vn.socialapp_dack.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tannguyen.st.ueh.edu.vn.socialapp_dack.R;
import tannguyen.st.ueh.edu.vn.socialapp_dack.adapters.AdapterUsers;
import tannguyen.st.ueh.edu.vn.socialapp_dack.models.ModelUser;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdapterUsers adapterUsers;
    private List<ModelUser> userList;
    private ValueEventListener listener;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        userList = new ArrayList<>();

        getAllUsers();
        return view;
    }

    private void getAllUsers() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear(); // Xóa danh sách cũ trước khi tải lại

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class); // Ánh xạ dữ liệu từ Firebase sang lớp User

                    if (user != null && fUser != null) {
                        // Kiểm tra xem người dùng hiện tại có khớp với user trong danh sách không
                        if (!user.getUid().equals(fUser.getUid())) {
                            userList.add(user);
                        }
                    }
                }

                updateAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ghi log hoặc hiển thị lỗi nếu có
                Log.e("FirebaseError", "Lỗi: " + error.getMessage());
            }
        });
    }

    public void searchUsers(String query) {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Xóa listener cũ trước khi thêm mới
        if (listener != null) {
            ref.removeEventListener(listener);
        }

        // Tạo mới listener
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear(); // Xóa danh sách cũ

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);

                    if (user != null && fUser != null && !user.getUid().equals(fUser.getUid())) {
                        if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(user);
                        }
                    }
                }
                // Cập nhật adapter
                updateAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi: " + error.getMessage());
            }
        };

        // Thêm listener mới
        ref.addValueEventListener(listener);
    }

    private void updateAdapter() {
        // Kiểm tra null trước khi gán adapter
        Log.d("LÀO GÌ CŨNG TÔN", String.valueOf(adapterUsers));
        if (adapterUsers == null) {
            adapterUsers = new AdapterUsers(getActivity(), userList);
            recyclerView.setAdapter(adapterUsers); // Gắn adapter vào RecyclerView
        } else {
            adapterUsers.notifyDataSetChanged(); // Cập nhật dữ liệu
        }
    }

}