package tannguyen.st.ueh.edu.vn.socialapp_dack.adapters;

import android.content.Context;
import android.text.TextUtils;

import tannguyen.st.ueh.edu.vn.socialapp_dack.utils.ImageDownloader;

public class ImageAdapter {

    public interface SaveImageCallback {
        void onImageSaved(String filePath); // Giao diện callback trả về file path
        void onError(Exception e); // Giao diện callback trả về lỗi
    }

    public static void saveImageToInternalStorage(Context context, String uid, String profileOrCoverPhoto, String imageUrl, SaveImageCallback callback) {
        if (TextUtils.isEmpty(imageUrl) || "null".equals(imageUrl)) {
            callback.onError(new IllegalArgumentException("URL ảnh không hợp lệ."));
            return;
        }

        // Tạo tên file dựa trên uid
        String fileName = profileOrCoverPhoto.equals("image") ? uid + "_profile_image.jpg" : uid + "_cover_image.jpg";

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
