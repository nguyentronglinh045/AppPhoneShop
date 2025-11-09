# AppPhoneShop

Ứng dụng Android cho cửa hàng điện thoại, giúp quản lý sản phẩm, đơn hàng và khách hàng.

## Mô tả

AppPhoneShop là một ứng dụng di động Android được phát triển bằng Java và Gradle, sử dụng Firebase để lưu trữ dữ liệu. Ứng dụng cung cấp giao diện thân thiện cho việc quản lý cửa hàng điện thoại, bao gồm các tính năng như đăng nhập, xem sản phẩm, đặt hàng và quản lý kho.

## Yêu cầu hệ thống

- **Android Studio**: Phiên bản Arctic Fox hoặc mới hơn
- **JDK**: Phiên bản 11 hoặc 17
- **Android SDK**: API level 21 trở lên
- **Windows PowerShell**: Để chạy các script tự động

## Cài đặt

1. Clone repository:

   ```bash
   git clone https://github.com/nguyentronglinh045/AppPhoneShop.git
   cd AppPhoneShop
   ```

2. Mở dự án trong Android Studio hoặc sử dụng các script tự động.

## Chạy dự án

Dự án cung cấp các script PowerShell trong thư mục `scripts/` để chạy nhanh chóng:

### 1. Script thông minh (Khuyến nghị)

```powershell
.\scripts\app.ps1
```

- Tự động phát hiện trạng thái emulator
- Nếu emulator chưa chạy: Khởi động đầy đủ (build, install, run)
- Nếu emulator đã chạy: Chỉ build và reload nhanh
- Các tùy chọn:
  - `-Force`: Buộc chạy đầy đủ quy trình
  - `-NoLogs`: Không hiển thị logcat
  - `-KillStale`: Dọn dẹp các tiến trình adb/logcat cũ

### 2. Chạy đầy đủ (Khởi động từ đầu)

```powershell
.\scripts\run-app.ps1
```

- Khởi động emulator
- Build APK
- Cài đặt và chạy ứng dụng
- Hiển thị logcat
- Các tùy chọn:
  - `-NoLogs`: Không hiển thị logcat
  - `-KillStale`: Dọn dẹp tiến trình cũ

### 3. Build và reload nhanh (Khi emulator đã chạy)

```powershell
.\scripts\build-and-reload.ps1
```

- Chỉ build và reload ứng dụng
- Yêu cầu emulator phải đang chạy

## Cấu trúc dự án

```
AppPhoneShop/
├── app/                    # Module chính
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/       # Source code Java
│   │   │   ├── res/        # Resources (layout, drawable, etc.)
│   │   │   └── AndroidManifest.xml
│   │   └── androidTest/    # Unit tests
│   ├── build.gradle        # Build config cho app
│   └── google-services.json # Firebase config
├── scripts/                # PowerShell scripts
│   ├── app.ps1            # Script thông minh
│   ├── run-app.ps1        # Chạy đầy đủ
│   └── build-and-reload.ps1 # Reload nhanh
├── build.gradle           # Root build config
└── settings.gradle        # Project settings
```

## Công nghệ sử dụng

- **Ngôn ngữ**: Java
- **Build tool**: Gradle
- **Database**: Firebase Firestore
- **UI**: XML Layouts với View Binding
- **Architecture**: MVVM (Model-View-ViewModel)

## Phát triển

1. Mở dự án trong Android Studio
2. Đồng bộ Gradle files
3. Chạy trên emulator hoặc thiết bị thật
4. Sử dụng Firebase console để quản lý dữ liệu

## Giấy phép

Dự án này được phát triển cho mục đích học tập và không sử dụng cho mục đích thương mại.
