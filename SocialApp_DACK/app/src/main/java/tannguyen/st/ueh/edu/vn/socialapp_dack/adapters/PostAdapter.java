package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import android.widget.BaseAdapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import tannguyen.st.ueh.edu.vn.socialapp_dack.models.Post;
import tannguyen.st.ueh.edu.vn.socialapp_dack.R;

public class PostAdapter extends BaseAdapter {

    private Context context;
    private List<Post> postList;

    public PostAdapter(@NonNull Context context, @NonNull List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public Object getItem(int position) {
        return postList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Nếu convertView bằng null, tạo một View mới từ layout
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        }

        // Lấy đối tượng Post tại vị trí hiện tại
        Post post = postList.get(position);

        // Ánh xạ các view trong item_post.xml
        TextView titleTextView = convertView.findViewById(R.id.postTitle);
        TextView contentTextView = convertView.findViewById(R.id.postContent);
        TextView timestampTextView = convertView.findViewById(R.id.postTimestamp);

        // Gán giá trị cho các TextView
        if (post != null) {
            titleTextView.setText(post.getTitle());
            contentTextView.setText(post.getContent());

            // Chuyển timestamp thành định dạng thời gian dễ đọc
            String formattedTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(post.getTimestamp());
            timestampTextView.setText(formattedTimestamp);
        }

        return convertView;
    }
}
