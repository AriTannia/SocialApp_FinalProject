package tannguyen.st.ueh.edu.vn.socialapp_dack.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageDownloader {
    public static void downloadImage(Context context, String url, String fileName, DownloadCallback callback) {
        new Thread(() -> {
            try {
                if (context == null) {
                    callback.onError(new NullPointerException("Context is null"));
                    return;
                }
                // Tải ảnh bằng Picasso
                Bitmap bitmap = Picasso.get().load(url).get();

                // Tạo file trong internal storage
                File file = new File(context.getFilesDir(), fileName);
                FileOutputStream fos = new FileOutputStream(file);

                // Lưu ảnh vào file
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                fos.flush();
                fos.close();

                // Gọi callback khi hoàn thành
                callback.onSuccess(file.getAbsolutePath());
            } catch (IOException e) {
                // Gọi callback khi có lỗi
                callback.onError(e);
            }
        }).start();
    }

    // Interface để xử lý callback
    public interface DownloadCallback {
        void onSuccess(String filePath);

        void onError(Exception e);
    }
}
