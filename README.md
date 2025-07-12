 # SimpleNote - Android Note-Taking App

A modern, user-friendly note-taking application for Android with secure user authentication and intuitive design.
This android application was created for Mobile Development course in Sharif University of Technology. 
Figma design:
https://www.figma.com/design/qTmbl7cAJiUBxypcbrI4ON/Note-Taking-App-UI---Design-System?node-id=1945-2807&p=f&t=rPBSHCpb6D8MuNYz-0

## 📱 Features

### 🔐 User Authentication
- **User Registration**: Create new accounts with email and password
- **Secure Login**: Authentication with encrypted password storage
- **Password Management**: Change password functionality with validation
- **User Isolation**: Each user can only access their own notes

### 📝 Note Management
- **Create Notes**: Add new notes with title and content
- **Edit Notes**: Modify existing notes with real-time saving
- **Delete Notes**: Remove notes with confirmation dialog
- **Search Notes**: Real-time search through note titles and content
- **Grid Layout**: Beautiful 2-column card-based note display

### 🎨 User Interface
- **Modern Design**: Clean, intuitive interface with card-based layout
- **Search Functionality**: Instant search with 300ms debounce
- **Empty State**: Helpful illustrations when no notes exist
- **Responsive Layout**: Optimized for different screen sizes
- **Bottom Navigation**: Easy access to home and settings

### 🔒 Security Features
- **Encrypted Storage**: User credentials stored securely
- **User-Specific Data**: Complete data isolation between users
- **Secure Database**: All operations filtered by authenticated user

## 🛠️ Technical Stack

### Architecture
- **Language**: Kotlin
- **Architecture Pattern**: MVVM with Repository Pattern
- **Database**: Room (SQLite)
- **UI**: Android View System with Data Binding
- **Navigation**: Android Navigation Component

### Dependencies
- **Room Database**: Local data persistence
- **Coroutines**: Asynchronous programming
- **Navigation Component**: Fragment navigation
- **Material Design**: Modern UI components
- **Encrypted SharedPreferences**: Secure credential storage

## 📦 Project Structure

```
app/src/main/
├── java/com/example/simplenote/
│   ├── activities/
│   │   ├── LoginActivity.kt
│   │   ├── RegisterActivity.kt
│   │   ├── MainActivity.kt
│   │   └── OnboardingActivity.kt
│   ├── fragments/
│   │   ├── FirstFragment.kt (Main Notes List)
│   │   ├── NoteEditorFragment.kt
│   │   ├── SettingsFragment.kt
│   │   └── ChangePasswordFragment.kt
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── Note.kt (Entity)
│   │   ├── NoteDao.kt
│   │   └── NoteRepository.kt
│   ├── adapters/
│   │   └── NoteAdapter.kt
│   └── utils/
│       ├── UserManager.kt
│       └── UiUtils.kt
└── res/
    ├── layout/ (All UI layouts)
    ├── drawable/ (Icons and graphics)
    ├── values/ (Colors, strings, themes)
    └── navigation/ (Navigation graph)
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 21 or higher
- Kotlin 1.5.0 or later

### Installation
1. Clone the repository
```bash
git clone https://github.com/yourusername/SimpleNote.git
```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app on an emulator or physical device

### Building the Project
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
```

## 🗄️ Database Schema

### Notes Table
```sql
CREATE TABLE notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    lastEdited INTEGER NOT NULL,
    username TEXT NOT NULL
);
```

### Database Migrations
- **Version 1 → 2**: Added user authentication
- **Version 2 → 3**: Added username column to notes for user isolation

## 🎯 Key Features Implementation

### User Authentication Flow
1. **Onboarding**: Welcome screen with app introduction
2. **Registration**: Create account with email/password validation
3. **Login**: Secure authentication with encrypted storage
4. **Session Management**: Persistent login state

### Note Management Flow
1. **List View**: Grid display of user's notes with search
2. **Create/Edit**: Rich text editor with auto-save
3. **Delete**: Confirmation dialog before deletion
4. **Search**: Real-time filtering of notes

### Security Implementation
- **Password Encryption**: Secure hashing for user passwords
- **Data Isolation**: Database queries filtered by username
- **Secure Storage**: EncryptedSharedPreferences for sensitive data

## 🎨 UI/UX Features

### Design Elements
- **Color Scheme**: Purple primary (#5B45FE) with clean backgrounds
- **Typography**: Modern, readable fonts with proper hierarchy
- **Icons**: Consistent iconography throughout the app
- **Animations**: Smooth transitions and interactions

### User Experience
- **Intuitive Navigation**: Bottom navigation with clear labels
- **Search Experience**: Instant results with debounced input
- **Empty States**: Helpful guidance when no content exists
- **Confirmation Dialogs**: Prevent accidental data loss

## 📱 Screenshots

### Main Features
- **Note List**: Grid view with search functionality
- **Note Editor**: Clean editing interface with auto-save
- **Settings**: User preferences and account management
- **Authentication**: Login and registration screens

## 🔧 Configuration

### Database Configuration
- **Database Name**: `note_database`
- **Version**: 3
- **Migration Strategy**: Automatic with fallback

### Security Configuration
- **Encryption**: AES-256 for SharedPreferences
- **Key Management**: Android Keystore integration
- **Session Timeout**: Configurable in UserManager

## 🚀 Future Enhancements

### Planned Features
- **Cloud Sync**: Backup notes to cloud storage
- **Rich Text**: Support for formatting and images
- **Categories**: Organize notes with tags/categories
- **Export**: Export notes to various formats
- **Dark Mode**: Theme customization options

### Technical Improvements
- **Offline Support**: Enhanced offline capabilities
- **Performance**: Lazy loading and caching
- **Testing**: Comprehensive unit and UI tests
- **Accessibility**: Improved accessibility features

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**SimpleNote** - Making note-taking simple, secure, and beautiful. 
