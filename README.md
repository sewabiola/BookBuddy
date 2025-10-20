# BookBuddy 📚

A modern Android application for book lovers to manage their reading collections, track reading goals, and discover new books.

## Features

- **Profile Setup Wizard**: Guided setup for new users with reading preferences
- **Book Collection Management**: Add, categorize, and organize your books
- **Reading Goals**: Set and track annual reading goals
- **Genre Preferences**: Customize your reading experience based on favorite genres
- **Modern UI**: Built with Jetpack Compose and Material 3 design

## Prerequisites

Before running this project, make sure you have:

- **Android Studio** (latest version recommended)
- **JDK 11** or higher
- **Android SDK** with API level 24+ (Android 7.0)
- **Git** for version control

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd BookBuddy
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to the BookBuddy folder and select it
4. Wait for Gradle sync to complete

### 3. Configure SDK

1. Go to **File → Project Structure**
2. Ensure **SDK Location** is set correctly
3. Verify that **Compile SDK Version** is set to API 34 or higher
4. Set **Target SDK Version** to API 34 or higher

### 4. Build and Run

1. Connect an Android device or start an emulator
2. Click the **Run** button (green play icon) or press `Shift + F10`
3. Select your target device
4. The app will build and install automatically

## Project Structure

```
BookBuddy/
├── app/
│   ├── src/main/java/com/example/bookbuddy/
│   │   ├── MainActivity.kt              # Main entry point
│   │   ├── ProfileSetupWizard.kt        # User onboarding flow
│   │   ├── BookAdditionInterface.kt     # Book management
│   │   ├── BookCategorizationSystem.kt  # Book organization
│   │   ├── Collection.kt                # Collection management
│   │   ├── Data.kt                      # Data models
│   │   ├── Profile.kt                   # User profile
│   │   └── Registration.kt             # User registration
│   └── src/main/res/                    # Resources (layouts, strings, etc.)
├── gradle/                              # Gradle wrapper
├── build.gradle.kts                     # Project build configuration
└── settings.gradle.kts                  # Project settings
```

## Key Technologies

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Latest Material Design components
- **Gradle** - Build system
- **Android SDK** - Platform APIs

## Development Setup

### Recommended Android Studio Settings

1. **Enable Kotlin support**
2. **Install Jetpack Compose plugin** (usually included in latest Android Studio)
3. **Configure code style** to use Kotlin official style
4. **Enable auto-import** for better development experience

### Build Configuration

The project uses:
- **Compile SDK**: 34
- **Target SDK**: 34
- **Min SDK**: 24
- **Gradle**: 8.0+
- **Kotlin**: 1.8+

## Troubleshooting

### Common Issues

1. **Gradle Sync Failed**
   - Check internet connection
   - Clear Gradle cache: `./gradlew clean`
   - Invalidate caches: **File → Invalidate Caches and Restart**

2. **Build Errors**
   - Ensure all dependencies are properly resolved
   - Check that Android SDK is properly configured
   - Verify JDK version compatibility

3. **Emulator Issues**
   - Create a new AVD with API level 24+
   - Enable hardware acceleration
   - Allocate sufficient RAM (4GB+ recommended)

### Performance Tips

- Use **Release build** for testing performance
- Enable **R8 shrinking** for smaller APK size
- Use **ProGuard** for code obfuscation in production

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

If you encounter any issues:

1. Check the troubleshooting section above
2. Search existing issues in the repository
3. Create a new issue with detailed description
4. Include device information and error logs

---

**Happy Reading! 📖✨**
