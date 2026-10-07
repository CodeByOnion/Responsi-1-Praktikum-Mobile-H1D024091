# Animaxyz 🎬

**Animaxyz** adalah aplikasi mobile berbasis Android modern untuk menjelajahi katalog anime, melakukan pencarian anime secara real-time, menyaring berdasarkan genre, serta melihat informasi detail anime. Aplikasi ini dibangun dengan standar arsitektur **MVVM (Model-View-ViewModel)** menggunakan **Jetpack Compose** dan **Material 3**.

---

## 📌 Daftar Isi
- [Tentang Aplikasi](#-tentang-aplikasi)
- [Fitur Utama](#-fitur-utama)
- [Teknologi & Library](#-teknologi--library)
- [Struktur Arsitektur MVVM](#-struktur-arsitektur-mvvm)
  - [1. Model (Data Layer)](#1-model-data-layer)
  - [2. ViewModel (State & Business Logic Layer)](#2-viewmodel-state--business-logic-layer)
  - [3. View (Presentation Layer)](#3-view-presentation-layer)
  - [Diagram Alur Data (Unidirectional Data Flow)](#diagram-alur-data-unidirectional-data-flow)
- [Penjelasan Penggunaan API](#-penjelasan-penggunaan-api)
  - [Spesifikasi API](#spesifikasi-api)
  - [Endpoint yang Digunakan](#endpoint-yang-digunakan)
  - [Mekanisme Pemanggilan & Optimasi](#mekanisme-pemanggilan--optimasi)
  - [Error Handling](#error-handling)
- [Struktur Direktori Proyek](#-struktur-direktori-proyek)

---

## 📱 Tentang Aplikasi

Animaxyz dirancang untuk memberikan pengalaman pengguna yang cepat, responsif, dan interaktif dalam menjelajahi ribuan judul anime. Aplikasi ini mengimplementasikan prinsip UI deklaratif Jetpack Compose serta memisahkan logika bisnis dari tampilan secara bersih melalui arsitektur MVVM.

### Fitur Utama:
* **Katalog Anime Populer**: Menampilkan daftar anime terpopuler secara default saat pertama kali dibuka.
* **Pencarian Real-Time (Debounced)**: Fitur pencarian judul anime secara instan dengan jeda debounce 500ms untuk menghemat kuota dan meminimalisir request ke server.
* **Filter Berdasarkan Genre**: Filter chip horizontal dinamis yang dimuat langsung dari endpoint genre API.
* **Halaman Detail Lengkap**: Informasi detail mencakup poster resolusi tinggi, skor/rating, status penayangan, jumlah episode, daftar tag genre, dan sinopsis cerita.
* **Manajemen State Komprehensif**: Penanganan state UI yang jelas (Loading, Success, Error) dilengkapi tombol *Retry* jika koneksi terputus.

---

## 🛠 Teknologi & Library

* **Bahasa**: [Kotlin](https://kotlinlang.org/)
* **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3
* **Arsitektur**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
* **Networking**:
  * [Retrofit 2](https://square.github.io/retrofit/) – HTTP client REST API
  * [Gson Converter](https://github.com/google/gson) – Serialisasi dan deserialisasi JSON
* **Async / Concurrency**: Kotlin Coroutines & Kotlin StateFlow / Flow
* **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/) – Pemuatan gambar asinkron dengan caching
* **Navigasi**: Navigation Compose (Jetpack Navigation)

---

## 🏛 Struktur Arsitektur MVVM

Aplikasi ini menerapkan pola arsitektur **MVVM (Model-View-ViewModel)** untuk memastikan kode bersifat modular, mudah diuji (*testable*), dan mudah dirawat (*maintainable*).

```
   ┌───────────────────────────────────────────────┐
   │             VIEW (Jetpack Compose)            │
   │  HomeScreen / DetailScreen / MainActivity     │
   └──────────────▲─────────────────┬──────────────┘
                  │ State           │ User Events
                  │ (StateFlow)     │ (Fungsi aksi)
   ┌──────────────┴─────────────────▼──────────────┐
   │                   VIEWMODEL                   │
   │                AnimeViewModel                 │
   └──────────────▲─────────────────┬──────────────┘
                  │ Data Model      │ Coroutines
                  │ (List, Object)  │ request
   ┌──────────────┴─────────────────▼──────────────┐
   │              MODEL / REPOSITORY               │
   │   AnimeRepository  ◄──►  AnimeApiService      │
   │   (Data Layer)     ◄──►  ApiClient (Retrofit) │
   └───────────────────────────────────────────────┘
```

### 1. Model (Data Layer)
Bertanggung jawab atas pengelolaan data dan komunikasi dengan sumber data eksternal (API):
* **Data Models (`data/model/`)**:
  * `Anime`: Entitas anime yang memuat ID, judul, skor, episode, status, sinopsis, gambar, dan genre.
  * `Genre`: Entitas data genre.
  * `AnimeListResponse`, `AnimeDetailResponse`, `GenreListResponse`: DTO (Data Transfer Object) untuk memetakan respons JSON dari server.
* **Data Sources & Network (`data/network/`)**:
  * `AnimeApiService`: Interface Retrofit yang mendefinisikan kontrak endpoint HTTP.
  * `ApiClient`: Singleton objek pembuat instance Retrofit dengan converter Gson.
* **Repository (`data/repository/`)**:
  * `AnimeRepository`: Bertindak sebagai *single source of truth*. Mengabstraksikan pemanggilan API dari ViewModel serta memvalidasi data respons sebelum diteruskan ke layer di atasnya.

### 2. ViewModel (State & Business Logic Layer)
Bertindak sebagai jembatan antara Model dan View:
* **`AnimeViewModel`**:
  * Mengelola state UI menggunakan `StateFlow` (`homeUiState`, `detailUiState`, `searchQuery`, `selectedGenreId`, `genres`).
  * Menggunakan `viewModelScope` untuk menjalankan *coroutine* secara aman sesuai siklus hidup (*lifecycle*).
  * Mengimplementasikan operator Kotlin Flow seperti `debounce(500ms)`, `combine`, `distinctUntilChanged`, dan `collectLatest` untuk mengoptimalkan alur pencarian dan filter genre.
* **UI State (`ui/viewmodel/UiState.kt`)**:
  * `HomeUiState` (Sealed Interface): `Loading`, `Success(List<Anime>)`, `Error(message)`.
  * `DetailUiState` (Sealed Interface): `Loading`, `Success(Anime)`, `Error(message)`.

### 3. View (Presentation Layer)
Bertanggung jawab murni atas visualisasi UI dan menangkap interaksi pengguna:
* **`HomeScreen`**: Menampilkan kolom pencarian, deretan filter chip genre, daftar kartu anime, indikator loading, dan pesan error jika request gagal.
* **`DetailScreen`**: Menampilkan data detail anime terpilih secara komprehensif dengan dukungan scroll.
* **`MainActivity`**: Menangani routing navigasi aplikasi menggunakan `NavHost` (`"home"` dan `"detail/{animeId}"`).
* **Karakteristik**: View bersifat pasif; hanya mengamati (*observe*) `StateFlow` dari ViewModel dan tidak memiliki logika bisnis langsung.

---

## 🌐 Penjelasan Penggunaan API

Animaxyz menggunakan REST API publik berbasis Jikan/Tenrai API untuk menyajikan data anime secara dinamis.

### Spesifikasi API
* **Base URL**: `https://api.tenrai.org/v1/` (didefinisikan di `com.pemmob.animaxyz.util.Constants`)
* **Format Data**: JSON
* **Klien HTTP**: Retrofit 2 didukung OkHttp & Gson Converter

### Endpoint yang Digunakan

| HTTP Method | Endpoint | Deskripsi | Query / Path Parameters |
|:---|:---|:---|:---|
| `GET` | `/anime` | Mengambil daftar anime berdasarkan pencarian, genre, dan urutan popularitas | `q` (string keyword), `genres` (int ID genre), `page` (int), `limit` (int), `order_by` (string), `sort` (string), `sfw` (boolean) |
| `GET` | `/anime/{id}` | Mengambil data rincian lengkap dari satu anime tertentu | `id` (int path: ID anime/MAL ID) |
| `GET` | `/genres/anime` | Mengambil daftar seluruh genre anime yang tersedia untuk opsi filter | - |

### Mekanisme Pemanggilan & Optimasi
1. **Asynchronous Execution**: Pemanggilan endpoint dideklarasikan dengan kata kunci `suspend fun` pada `AnimeApiService`, dijalankan di thread background secara non-blocking melalui Kotlin Coroutines.
2. **Search Debounce**: Saat pengguna mengetik di kolom pencarian, `AnimeViewModel` menerapkan jeda waktu `debounce(500ms)`. API hanya akan dipanggil setelah pengguna berhenti mengetik selama 500 milidetik, mencegah pemborosan kuota dan kelebihan beban request (*rate limit*).
3. **Reactive Query & Filter Combination**: Operator `combine` menyatukan perubahan kata kunci pencarian dan ID genre yang dipilih secara otomatis tanpa perlu pemanggilan manual berulang.

### Error Handling
Aplikasi menangani berbagai skenario kegagalan jaringan secara spesifik melalui fungsi `mapErrorMessage()` di ViewModel:
* **HTTP 429 (Too Many Requests)**: Ditampilkan pesan `"Terlalu banyak permintaan, coba sebentar lagi."`
* **HTTP 404 (Not Found)**: Ditampilkan pesan `"Anime tidak ditemukan."`
* **IOException (No Internet Connection)**: Ditampilkan pesan `"Tidak ada koneksi internet. Periksa jaringan lalu coba lagi."`
* **Exception Umum**: Ditampilkan pesan ramah pengguna `"Terjadi kesalahan. Silakan coba lagi."` disertai tombol coba lagi (*Retry*).

---

## 📂 Struktur Direktori Proyek

```text
Animaxyz/
├── app/
│   ├── src/main/java/com/pemmob/animaxyz/
│   │   ├── data/
│   │   │   ├── model/                  # Data classes & Response DTO
│   │   │   │   ├── Anime.kt
│   │   │   │   ├── AnimeDetailResponse.kt
│   │   │   │   ├── AnimeListResponse.kt
│   │   │   │   ├── Genre.kt
│   │   │   │   └── GenreListResponse.kt
│   │   │   ├── network/                # Konfigurasi Retrofit & Service Interface
│   │   │   │   ├── ApiClient.kt
│   │   │   │   └── AnimeApiService.kt
│   │   │   └── repository/             # Data repository (Single Source of Truth)
│   │   │       └── AnimeRepository.kt
│   │   ├── ui/
│   │   │   ├── screen/                 # Komponen Tampilan (Compose Screens)
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   └── DetailScreen.kt
│   │   │   ├── theme/                  # Konfigurasi Tema, Warna & Tipografi
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt
│   │   │   └── viewmodel/              # ViewModel & UI State Model
│   │   │       ├── AnimeViewModel.kt
│   │   │       └── UiState.kt
│   │   ├── util/                       # Konstanta dan Utilitas
│   │   │   └── Constants.kt
│   │   └── MainActivity.kt             # Entry point & NavHost Controller
│   └── build.gradle.kts                # Konfigurasi dependensi modul aplikasi
└── README.md
```
