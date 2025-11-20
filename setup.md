# Quick Setup Guide 🚀

## For New Team Members

### Prerequisites Checklist
- [ ] Android Studio (latest version)
- [ ] JDK 11 or higher
- [ ] Android SDK (API 24+)
- [ ] Git

### Step-by-Step Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd BookBuddy
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to BookBuddy folder
   - Click "OK"

3. **Wait for Gradle Sync**
   - Android Studio will automatically sync the project
   - This may take a few minutes on first run
   - Watch the bottom status bar for progress

4. **Configure SDK (if needed)**
   - Go to **File → Project Structure**
   - Under **SDK Location**, ensure Android SDK is set
   - Under **Modules → app**, verify:
     - Compile SDK Version: 34
     - Target SDK Version: 34
     - Min SDK Version: 24

5. **Run the App**
   - Connect Android device or start emulator
   - Click the green "Run" button
   - Select your target device
   - App should build and install

### Troubleshooting

**Gradle Sync Issues:**
- Check internet connection
- Clear cache: **File → Invalidate Caches and Restart**
- Clean project: **Build → Clean Project**

**Build Errors:**
- Ensure all dependencies are resolved
- Check Android SDK installation
- Verify JDK version (should be 11+)

**Emulator Issues:**
- Create new AVD with API 24+
- Enable hardware acceleration
- Allocate 4GB+ RAM

### Project Structure Overview

```
BookBuddy/
├── app/src/main/java/com/example/bookbuddy/
│   ├── MainActivity.kt              # App entry point
│   ├── ProfileSetupWizard.kt        # User onboarding
│   ├── BookAdditionInterface.kt     # Book management
│   ├── BookCategorizationSystem.kt   # Book organization
│   ├── Collection.kt                 # Collection features
│   ├── Data.kt                       # Data models
│   ├── Profile.kt                    # User profile
│   └── Registration.kt              # User registration
└── app/src/main/res/                 # Resources
```

### Key Features to Test

1. **Profile Setup Wizard** - Complete onboarding flow
2. **Book Collection** - Add and manage books
3. **Reading Goals** - Set and track goals
4. **Genre Preferences** - Customize reading experience

### Development Tips

- Use **Preview** in Android Studio for Compose UI development
- Enable **Live Edit** for faster iteration
- Use **Logcat** for debugging
- Check **Build Output** for any warnings

### Need Help?

- Check the main README.md for detailed information
- Review the troubleshooting section
- Ask team members for assistance
- Create an issue if you find bugs

---

**Welcome to the BookBuddy team! 📚✨**
