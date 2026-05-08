# MyTracker 

MyTracker is a professional-grade Android application built with **Jetpack Compose** that allows users to track their progress in Manga and Anime. It leverages the **Jikan API** (MyAnimeList) for data and **Firebase Firestore** for real-time cloud synchronization.

## 🚀 Features

- **Real-time Sync**: Uses Firebase Firestore to keep your list updated across devices.
- **Global Search**: Search for any Manga/Anime using the Jikan API integration.
- **Detailed Progress**: Track chapters/episodes with automated status updates (Reading -> Completed).
- **Genre Etiquettes**: View genre tags (Shonen, Action, etc.) for every entry.
- **Clean Architecture**: Implements the **Repository Pattern** and **MVVM** for scalable and maintainable code.
- **Modern UI**: Built entirely with Jetpack Compose and Material 3 design principles.
- **Navigation**: Full navigation support with dedicated list and detail screens.

## 🛠 Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Repository Pattern
- **Database**: Firebase Firestore
- **Networking**: Retrofit + GSON
- **Image Loading**: Coil
- **Navigation**: Jetpack Compose Navigation
- **API**: Jikan API (v4)

## 🏗 Project Structure

```text
com.amaya.mytracker/
├── data/
│   ├── JikanApiService.kt    # API definitions
│   └── TrackerRepository.kt  # Single source of truth for data
├── ui/
│   ├── MainActivity.kt       # Navigation and UI Screens
│   └── TrackerViewModel.kt   # UI State management
└── model/
    └── TrackItem.kt          # Domain models
```

## 🚦 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/MyTracker.git
   ```
2. **Setup Firebase**:
   - Create a project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.amaya.mytracker`.
   - Download `google-services.json` and place it in the `app/` directory.
   - Enable **Firestore Database** in test mode.
3. **Build & Run**:
   - Open the project in Android Studio (Ladybug or newer).
   - Sync Gradle and run on an emulator or physical device.

## 📈 Future Roadmap

- [ ] Implementation of Dependency Injection with **Hilt**.
- [ ] Offline support using **Room Database** (Local caching).
- [ ] User Authentication with Firebase Auth.
- [ ] Dark Mode and Dynamic Theming support.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
