# AA Studio — Android starter

Repository AA Studio berisi antarmuka web dan GitHub Actions untuk membangun APK Android debug.

## Fitur
- Menu Editor dengan kanvas pratinjau dan elemen yang dapat digeser.
- Web Source dengan URL, pratinjau iframe, dan buka browser eksternal.
- URL Sociabuzz Alert: simpan lokal, pratinjau, pemeriksaan format URL, dan buka eksternal.
- URL MediaShare: simpan lokal, pratinjau, pemeriksaan format URL, dan buka eksternal.
- Workflow GitHub Actions untuk membangun APK debug dan mengunggahnya sebagai artifact.

## Build APK
1. Buka tab **Actions** di repository.
2. Pilih workflow **Build AA Studio APK**.
3. Tekan **Run workflow** pada branch `main`, atau lakukan commit untuk memicu build otomatis.
4. Setelah selesai, buka run yang sukses dan bagian **Artifacts**.
5. Unduh `AA-Studio-debug-apk`, ekstrak ZIP, lalu pasang APK di Android.

## Catatan penting
- Ini starter project; APK perlu berhasil dibangun dan diuji sebelum dianggap siap produksi.
- URL Sociabuzz/MediaShare harus berasal dari dashboard atau dokumentasi resmi layanan. Jangan masukkan kata sandi akun.
- Pratinjau iframe/WebView bisa gagal jika situs memblokir embed, meminta login, atau membatasi autoplay. Gunakan tombol buka eksternal jika perlu.
- Pemeriksaan URL hanya memeriksa format, bukan membuktikan koneksi/API atau status akun.
- **Izin “Tampilkan di atas aplikasi lain” dan overlay sistem native belum diimplementasikan.** Halaman web tidak dapat memberikan izin tersebut sendiri. Overlay sistem sungguhan memerlukan kode Android native dan alur persetujuan `SYSTEM_ALERT_WINDOW`.
- Audio alert mengikuti halaman/widget eksternal dan kebijakan Android WebView; aplikasi ini tidak menggandakan suara secara otomatis.

