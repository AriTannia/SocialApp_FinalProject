package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import tannguyen.st.ueh.edu.vn.socialapp_dack.utils.ImageDownloader;

public class ImageAdapter {

    public interface SaveImageCallback {
        void onImageSaved(String filePath); // Trả về đường dẫn khi ảnh được lưu
        void onError(Exception e); // Xử lý lỗi
    }

    public static void saveImageToInternalStorage(Context context, String profileOrCoverPhoto, String imageUrl, SaveImageCallback callback) {

        // Kiểm tra URL trước khi xử lý
        if (TextUtils.isEmpty(imageUrl) || "null".equals(imageUrl)) {
            callback.onError(new IllegalArgumentException("URL ảnh không hợp lệ."));
            return;
        }

        String fileName = profileOrCoverPhoto.equals("image") ? "profile_image.jpg" : "cover_image.jpg";

        ImageDownloader.downloadImage(context, imageUrl, fileName, new ImageDownloader.DownloadCallback() {
            @Override
            public void onSuccess(String filePath) {
                // Trả về đường dẫn qua callback
                callback.onImageSaved(filePath);
            }

            @Override
            public void onError(Exception e) {
                // Trả về lỗi qua callback
                callback.onError(e);
            }
        });
    }
}
