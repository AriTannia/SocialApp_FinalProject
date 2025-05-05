# ✨ Dự án Ứng dụng Di động SADACK ✨

## Giới thiệu

**SADACK** (Sharing and Discussing, Always Caring & Kind) là một ứng dụng mạng xã hội di động nhằm tạo ra một cộng đồng thân thiện, nơi người dùng có thể chia sẻ cảm xúc, kết nối và nhận được sự hỗ trợ khi cần. SADACK tập trung vào việc tăng cường sức khoẻ tinh thần và tương tác cá nhân trong bối cảnh các vấn đề tâm lý ngày càng gia tăng.

## ❤️ Tính cấp thiết

Sức khoẻ tinh thần là vấn đề ngày càng được quan tâm. SADACK ra đời nhằm cung cấp một nền tảng kỹ thuật để hỗ trợ người dùng chia sẻ, kiểm soát cảm xúc và xây dựng mối quan hệ với những người đồng cảm.

## 🛠️ Công nghệ sử dụng

* **Ngôn ngữ:** Java
* **SDK:** Android SDK
* **IDE:** Android Studio
* **Backend & Cloud:** Firebase (Authentication, Realtime Database, Cloud Storage)
* **CSDL cục bộ:** SQLite

## 🚀 Tính năng chính

### ✨ Dành cho người dùng

**Xác thực:**

* Đăng nhập (Email/Mật khẩu, Google Sign-In)
* Đăng ký tài khoản
* Khôi phục mật khẩu qua email

**Quản lý hồ sơ:**

* Xem & chỉnh sửa tên, email, ảnh đại diện, ảnh bìa

**Bài viết:**

* Xem danh sách bài viết (Trang chủ)
* Tạo/Sửa/Xóa bài viết cá nhân
* Xem chi tiết bài viết

**Tương tác:**

* Like, bình luận, sửa/xóa bình luận

**Nhắn tin:**

* Chat trực tiếp, danh sách & chi tiết tin nhắn

### 📄 Dành cho Quản trị viên (Admin)

* Quản lý người dùng: Xem danh sách, thêm, chỉnh sửa, xóa tài khoản
* Quản lý hồ sơ Admin: Thay đổi thông tin, ảnh đại diện, ảnh bìa

## 🗄️ Cơ sở dữ liệu

* **Firebase Realtime Database:** Dữ liệu chính, đồng bộ theo thời gian thật
* **SQLite:** Lưu trữ dữ liệu offline

**Bảng dữ liệu chính:**

* User (ID, Name, Email, Phone, Password, Image, Cover)
* Post (ID, Title, Content, TimeStamp, ImageUrl, UserId)
* Message (ID, Sender, Receiver, Message, TimeStamp, IsSeen)
* Comment (ID, PostID, UserID, UserName, Content, TimeStamp)

## 👥 Nhóm phát triển

| Thành viên            | Nhiệm vụ chính                                                                             |
| --------------------- | ------------------------------------------------------------------------------------------ |
| Nguyễn Hoàng Minh Tấn | Coding: Đăng nhập/Đăng ký/Profile, chat list. Báo cáo: Ch. I, II (2.1.1-2.1.6), V (5.1.1)  |
| Đoàn Thanh Lâm        | Coding: Chat chi tiết. Báo cáo: II (2.1.7-2.1.8, 2.2, 2.3), IV (4.1, 4.2), V (5.1.1)       |
| Dương Lâm Gia Kiệt    | Coding: Firebase/SQLite - Profile, chat, post. Báo cáo: III (3.1-3.6), IV (4.3), V (5.1.1) |
| Phạm Trung            | Coding: Trang chủ, post (tiêu đề, nội dung), comment. Báo cáo: III, V (5.1.1)              |
| Nguyễn Tuấn Dũng      | Coding: Admin, post (hình ảnh). Báo cáo: VI, V (5.3.1.8, 5.3.2.1, 5.3.2.2)                 |

## 📚 Tài liệu tham khảo

* DataReportal. *Digital 2023: Global Overview Report*
* World Health Organization (WHO). *Mental health statistics*
