# GuriResumeRewrite

An Android application that provides AI-powered resume analysis and advice using Firebase AI and Google's Generative AI models.

## 📱 Overview

GuriResumeRewrite is a modern Android app built with Jetpack Compose that allows users to upload PDF resumes and receive AI-generated advice and analysis. The app leverages Firebase AI and Google's Generative AI to provide intelligent feedback on resume content.

## ✨ Features

- **PDF Upload**: Select and upload PDF resume files from device storage
- **AI Analysis**: Get intelligent advice and feedback on resume content
- **Modern UI**: Built with Material Design 3 and Jetpack Compose
- **Remote Configuration**: Dynamic prompt and model configuration via Firebase Remote Config
- **Real-time Processing**: Stream-based AI response generation
- **Permission Handling**: Proper storage permission management for different Android versions

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModel
- **AI Integration**: Firebase AI with Google Generative AI
- **Remote Config**: Firebase Remote Configuration
- **State Management**: Kotlin Flow
- **Dependency Injection**: Manual DI with Factory pattern
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)

## 📋 Prerequisites

Before running this project, make sure you have:

- Android Studio Hedgehog or later
- Android SDK 36
- Kotlin 1.9+
- A Firebase project with AI features enabled
- Google Services configuration file (`google-services.json`)

## 🚀 Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd GuriResumeRewrite
```

### 2. Firebase Setup

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Firebase AI features in your project
3. Download the `google-services.json` file and place it in the `app/` directory
4. Configure Firebase Remote Config with the following parameters:
   - `prompt`: The AI prompt for resume analysis
   - `model`: The AI model name to use

### 3. Build and Run

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Connect an Android device or start an emulator
4. Click the "Run" button or use `./gradlew installDebug`

## 🏗️ Project Structure

```
app/src/main/java/com/guri/guriresumerewrite/
├── MainActivity.kt                 # Main activity entry point
├── GuriResumeApplication.kt        # Application class
├── ConfigParam.kt                  # Remote config parameter definitions
├── FirebaseRemoteConfig.kt         # Remote config interface
├── FirebaseRemoteConfigImpl.kt     # Remote config implementation
├── JsonUtils.kt                    # JSON utility functions
└── ui/
    ├── PDFPickerScreen.kt          # Main UI screen
    ├── viewmodel/
    │   └── PDFPickerViewModel.kt   # ViewModel for PDF processing
    ├── state/                      # UI state definitions
    ├── event/                      # UI event definitions
    └── theme/                      # Material Design theme
```

## 🔧 Configuration

### Remote Config Parameters

The app uses Firebase Remote Config to dynamically configure:

- **prompt**: The AI prompt used for resume analysis
- **model**: The AI model name for content generation

### Default Values

Default configuration values are stored in `app/src/main/res/xml/remote_config_defaults.xml`.

## 📱 Usage

1. Launch the app
2. Tap "Select PDF File" to choose a resume
3. Grant storage permissions if prompted
4. Tap "Resume Advice" to analyze the selected PDF
5. View the AI-generated advice and feedback

## 🔒 Permissions

The app requires the following permissions:

- **Storage Access**: For reading PDF files (handled automatically on Android 13+)
- **Internet**: For AI model communication

## 🧪 Testing

The project includes both unit tests and instrumentation tests:

- **Unit Tests**: `app/src/test/`
- **Instrumentation Tests**: `app/src/androidTest/`

Run tests using:
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumentation tests
```

## 📦 Dependencies

### Core Dependencies
- `androidx.core:core-ktx` - Kotlin extensions
- `androidx.lifecycle:lifecycle-runtime-ktx` - Lifecycle components
- `androidx.activity:activity-compose` - Compose activity support

### UI Dependencies
- `androidx.compose:compose-bom` - Compose BOM
- `androidx.compose.ui:ui` - Compose UI components
- `androidx.compose.material3:material3` - Material Design 3
- `androidx.compose.ui:ui-tooling-preview` - Compose tooling

### Firebase Dependencies
- `com.google.firebase:firebase-bom` - Firebase BOM
- `com.google.firebase:firebase-ai` - Firebase AI
- `com.google.firebase:firebase-analytics` - Firebase Analytics
- `com.google.firebase:firebase-config` - Firebase Remote Config

### Utility Dependencies
- `com.google.code.gson:gson` - JSON parsing

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/your-repo/issues) page
2. Create a new issue with detailed information
3. Include device information, Android version, and error logs

## 🔄 Version History

- **v1.0** - Initial release with PDF upload and AI analysis features

---

**Note**: This app requires an active internet connection and Firebase project configuration to function properly. 