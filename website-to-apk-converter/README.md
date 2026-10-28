# Website to APK Converter

Proyek ini mengubah website online menjadi aplikasi Android dengan nama yang dapat diatur pengguna.

## Paket sumber lengkap
Paket proyek berisi:
- `converter-android/`: aplikasi Android untuk memasukkan URL, nama aplikasi, package ID, dan mengunduh APK hasil build.
- `builder-server/`: backend Python yang menjalankan Gradle + Android SDK untuk membuat APK.
- `generated-app-template/`: template WebView dengan floating overlay yang bisa digeser.

## Cara kerja
Aplikasi Android converter mengirim konfigurasi ke build server. Server membangun APK dengan Android SDK dan Gradle, lalu converter mengunduh file APK ke perangkat. APK tidak dapat dibuat dari URL saja tanpa toolchain build Android.

## Kebutuhan build server
- Python 3.10+
- Java 17
- Android SDK platform 35 dan Build Tools
- Gradle 8.9+ di PATH
- `ANDROID_SDK_ROOT` atau `ANDROID_HOME`

Jalankan server dari folder `builder-server`:
```sh
pip install -r requirements.txt
uvicorn server:app --host 0.0.0.0 --port 8080
```

Untuk pemakaian publik, gunakan HTTPS, autentikasi, rate limiting, dan isolasi build. Jangan mengekspos server tanpa pengamanan.

## Build aplikasi converter
Buka folder `converter-android` di Android Studio, lakukan Gradle Sync, lalu pilih Build APK(s).

## Overlay
Template hasil APK meminta pengguna mengaktifkan izin Android “Display over other apps”. Izin ini tidak dapat diaktifkan diam-diam.

## Status
Ini adalah proyek sumber awal, belum diklaim sebagai APK siap instal atau build yang telah diuji. Lihat README pada paket ZIP sumber lengkap untuk instruksi rinci.
