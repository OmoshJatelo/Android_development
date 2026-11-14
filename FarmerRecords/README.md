# FarmerRecords 🌾

**FarmerRecords** is an Android application built in Java that demonstrates input validation, structured data logging, UI state management, and Android Activity lifecycle tracking.

---

## 📌 Features

- **Farmer Record Entry:** Log crop harvests by entering farmer name/reference, selecting crop type (Maize, Beans, Tea, Coffee, Potatoes, Tomatoes), and entering quantity harvested (in kg).
- **Robust Validation (`RecordValidator`):** Pure-Java validation rules designed for unit testing:
  - Farmer name is required.
  - Harvest quantity must be a valid, finite, non-negative decimal number (zero allowed for zero-harvest weeks).
  - Handles edge cases such as empty input, invalid formatting, negative values, and non-numeric numbers (`NaN`/`Infinity`).
- **Dynamic List Display:** Rendered using a custom `RecordAdapter` backed by a `ListView`.
- **State Preservation & Lifecycle Tracking:**
  - Uses `RecordsViewModel` to preserve submitted records across orientation changes.
  - Implements `onSaveInstanceState` and `onRestoreInstanceState` to retain unsaved form drafts.
  - On-screen event trace log displays real-time Activity lifecycle transitions (`onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`).

---

## 🏗️ Architecture & Project Structure

The project follows a clean separation of concerns:

```
com.smart.farmerrecords/
├── FarmerRecord.java       # Immutable data model for harvest records
├── RecordAdapter.java      # Custom ArrayAdapter for list row rendering
├── RecordValidator.java    # Pure Java validation logic (decoupled from Android API)
├── RecordsViewModel.java   # Jetpack ViewModel for maintaining screen state
└── MainActivity.java       # Activity handling UI interaction and lifecycle callbacks
```

---

## 🛠️ Tech Stack & Dependencies

- **Language:** Java 11
- **Min SDK:** 24 (Android 7.0)
- **Target / Compile SDK:** 36
- **Architecture & Libraries:**
  - AndroidX AppCompat & Activity KTX
  - AndroidX Lifecycle ViewModel
  - ConstraintLayout & Material Components

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- JDK 11
- Android SDK 36

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/OmoshJatelo/Android_development.git
   ```
2. Open the project in **Android Studio**.
3. Build the project using Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run on an emulator or connected Android device.

---

## 🧪 Testing

Run Java compile and validation checks via Gradle:
```bash
./gradlew :app:compileDebugJavaWithJavac
```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
