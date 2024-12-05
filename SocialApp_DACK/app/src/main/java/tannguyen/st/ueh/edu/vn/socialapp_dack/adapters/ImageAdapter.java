package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import tannguyen.st.ueh.edu.vn.socialapp_dack.activities.HomeActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.activities.MainActivity;
import tannguyen.st.ueh.edu.vn.socialapp_dack.utils.ImageDownloader;

public class ImageAdapter {
    public static void saveImageToInternalStorage(Context context, String profileOrCoverPhoto, String imageUrl) {

        // Kiểm tra URL trước khi xử lý
        if (TextUtils.isEmpty(imageUrl) || "null".equals(imageUrl)) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "URL ảnh không hợp lệ. Không thể lưu ảnh.", Toast.LENGTH_SHORT).show()
                );
            } else if (context instanceof HomeActivity) {
                ((HomeActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "URL ảnh không hợp lệ. Không thể lưu ảnh.", Toast.LENGTH_SHORT).show()
                );
            }
            return; // Thoát sớm vì URL không hợp lệ
        }

        String fileName = profileOrCoverPhoto.equals("image") ? "profile_image.jpg" : "cover_image.jpg";

        ImageDownloader.downloadImage(context, imageUrl, fileName, new ImageDownloader.DownloadCallback() {
            @Override
            public void onSuccess(String filePath) {
                // Đảm bảo chạy trên UI thread khi hiển thị Toast
                if (context instanceof MainActivity) {
                    ((MainActivity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Ảnh đã được lưu: " + filePath, Toast.LENGTH_SHORT).show();
                        System.out.println("Image saved at: " + filePath); // In ra log
                    });
                } else if (context instanceof HomeActivity) {
                    ((HomeActivity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Ảnh đã được lưu: " + filePath, Toast.LENGTH_SHORT).show();
                        System.out.println("Image saved at: " + filePath); // In ra log
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // Đảm bảo chạy trên UI thread khi hiển thị Toast
                if (context instanceof MainActivity) {
                    ((MainActivity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else if (context instanceof HomeActivity) {
                    ((HomeActivity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

}
