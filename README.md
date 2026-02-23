# Social Network Backend
## GIỚI THIỆU
- Đây là hệ thống backend cho một nền tảng mạng xã hội mini, cung cấp RESTful API phục vụ cho các chức năng xác thực người dùng, quản lí bạn bè, tương giác như like,comment,share và nhắn tin realtime.
- Dự án được xây dựng với mục tiêu thực hành các bài toán thực tế bằng những công nghệ phổ biến trong doanh nghiệp.

## CÔNG NGHỆ SỬ DỤNG
- Java 17
- Spring Boot
- Spring Security
- JWT Authentication (HTTP-only Cookie)
- WebSocket (STOMP)
- Spring Data JPA (Hibernate)
- MySQL, Postgre
- Maven

## Chức năng chính
- Đăng ký / Đăng nhập người dùng
- Xác thực bằng JWT (lưu trong HTTP-only Cookie)
- Phân quyền truy cập API giữa OWNER,ADMIN,MEMBER trong group
- Thực hiện các tương tác người dùng như Kết bạn, thả cảm xúc, bình luận, chia sẻ, tạo bài đăng
- Nhắn tin realtime bằng WebSocket
- Tìm kiếm nâng cao: Full-text + fuzzy search (gợi ý realtime) bằng PostgreSQL với TSVECTOR/GIN (full-text search) + pg_trgm (similarity/fuzzy matching) + unaccent (chuẩn hóa tiếng Việt).
- Tích hợp Redis (chạy bằng Docker) theo mô hình Cache Aside để tối ưu truy vấn hồ sơ người dùng và giảm tải cơ sở dữ liệu.

## Kiến trúc hệ thống
Dự án áp dụng mô hình phân tầng (Layered Architecture):

Controller → Service → Repository → Database

Cấu trúc thư mục:
src
-  Authorization
-  Config
-  Controller
-  DTO
-  Entity
-  Enums
-  Exception
-  Mapper
-  Repository
-  Security
-  Service
