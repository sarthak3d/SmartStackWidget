# Smart Stack Widget

An Android app for API level 35 (Android 15) and above, focused on creating a customizable widget stack host, taking design and usability inspiration from iOS Smart Stack.

## Features

### Core Widget Stack Functionality
- **Base Widget**: Minimal, empty, resizable widget serving as a stack container
- **Widget Stacking**: Support for stacking multiple widgets atop the base widget
- **Swipeable Navigation**: ViewPager2/RecyclerView embedded via RemoteViews for widget switching
- **Smart Rotation**: Automatic widget rotation based on time of day and usage patterns
- **Persistent State**: Widget stack order and configuration saved across reboots

### Android 15 Enhancements
- **API Level 35**: Targets latest Android 15 features
- **Adaptive Sizing**: Leverages new widget host behavior
- **Improved Performance**: Enhanced RemoteViews performance
- **Jetpack Libraries**: Uses latest Glance and AppWidgetsManager

### User Experience
- **Gesture Support**: Tap, swipe up/down to switch widgets manually
- **Drag & Drop**: Reordering support (where platform constraints allow)
- **Graceful Fallback**: Works on older supported APIs (minSdk 31)
- **Accessibility**: Compliant with Android design guidelines

### Extensibility
- **Third-party Integration**: Designed to allow external widget addition
- **Service APIs**: Metadata and service APIs for external widget integration
- **Modular Design**: Clean architecture for easy extension

## Project Structure

```
SmartStackWidget/
├── app/
│   └── build.gradle                 # App-level build configuration
├── build.gradle                     # Project-level build configuration
├── settings.gradle                  # Gradle settings
└── src/main/
    ├── AndroidManifest.xml          # App manifest with permissions and components
    ├── java/com/smartstack/widget/
    │   ├── MainActivity.kt          # Main app activity with Compose UI
    │   ├── StackWidgetProvider.kt   # Main widget stack provider
    │   ├── StackWidgetConfigureActivity.kt  # Widget configuration UI
    │   ├── data/
    │   │   └── WidgetStackManager.kt # Widget data and state management
    │   ├── service/
    │   │   ├── StackWidgetService.kt # RemoteViews service for widget content
    │   │   └── WidgetUpdateService.kt # Background widget updates
    │   ├── receiver/
    │   │   └── BootReceiver.kt      # Boot completion handling
    │   ├── sample/                  # Sample widget implementations
    │   │   ├── ClockWidgetProvider.kt
    │   │   ├── WeatherWidgetProvider.kt
    │   │   └── CalendarWidgetProvider.kt
    │   └── ui/theme/                # Material 3 theme components
    │       ├── Color.kt
    │       ├── Theme.kt
    │       └── Type.kt
    └── res/
        ├── drawable/                 # Widget backgrounds and icons
        ├── layout/                   # Widget layouts
        ├── values/                   # Strings, colors, themes
        └── xml/                      # Widget info and backup rules
```

## Key Components

### StackWidgetProvider
The main widget provider that creates the stackable widget container. Handles:
- Widget lifecycle management
- RemoteViews setup for widget content
- Gesture handling (swipe left/right)
- Smart rotation scheduling
- Cross-reboot state restoration

### WidgetStackManager
Manages widget data and state persistence using DataStore:
- Widget stack ordering and persistence
- Smart rotation based on time patterns
- Widget configuration storage
- Cross-reboot state restoration

### StackWidgetService
Provides RemoteViews for the widget stack:
- Dynamic widget content generation
- Sample widget implementations (clock, weather, calendar)
- Click handling for widget interactions

### Sample Widgets
Demonstration widgets showing different content types:
- **ClockWidget**: Displays current time
- **WeatherWidget**: Shows weather information
- **CalendarWidget**: Displays calendar data

## Usage

### Adding Widgets to Home Screen
1. Open the Smart Stack Widget app
2. Tap "Add Stack Widget to Home Screen"
3. Follow the system widget picker to add the widget
4. Configure the widget stack through the configuration activity

### Configuring Widget Stack
1. Long-press the widget on home screen
2. Select "Configure" or tap the settings icon
3. Choose which widgets to include in the stack
4. Configure smart rotation settings
5. Save configuration

### Widget Interaction
- **Tap**: Opens the current widget's associated app
- **Swipe Left/Right**: Navigate between stacked widgets
- **Smart Rotation**: Automatic widget changes based on time

## Technical Implementation

### Widget Stack Architecture
The widget stack uses a combination of:
- **RemoteViews**: For widget content display
- **ListView**: For swipeable widget navigation
- **DataStore**: For persistent state management
- **Coroutines**: For asynchronous operations

### Smart Rotation Logic
Widget rotation is based on time of day:
- **6-9 AM**: Clock/Weather widgets (morning routine)
- **10 AM-5 PM**: Calendar/Tasks widgets (work hours)
- **6-9 PM**: Entertainment widgets (evening)
- **10 PM-5 AM**: Minimal info widgets (night)

### Android 15 Features
- **Adaptive Sizing**: Leverages new widget sizing capabilities
- **Enhanced RemoteViews**: Improved performance and flexibility
- **Modern Permissions**: Uses latest permission model
- **Material 3**: Modern design system integration

## Building and Running

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK API 35 (Android 15)
- Minimum SDK API 31 (Android 12)

### Build Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build the project
5. Install on Android 15+ device

### Testing
- Test on Android 15+ device or emulator
- Add widget to home screen
- Configure widget stack
- Test swipe gestures and smart rotation

## Design Rationale

### iOS Smart Stack Inspiration
The app takes inspiration from iOS Smart Stack while adapting to Android's widget system:

**Similarities:**
- Stackable widget concept
- Smart rotation based on time/usage
- Swipeable navigation
- Persistent state

**Android Adaptations:**
- Uses RemoteViews instead of SwiftUI
- Leverages Android's widget framework
- Implements Android-specific gesture handling
- Adapts to Android's permission model

### Platform Constraints
- **RemoteViews Limitations**: More restrictive than native views
- **Background Processing**: Limited by Android's battery optimization
- **Widget Updates**: Controlled by system update intervals
- **Gesture Handling**: Limited to basic touch events

## Future Enhancements

### Planned Features
- **Third-party Widget Integration**: API for external widgets
- **Advanced Gestures**: More sophisticated touch handling
- **Custom Widget Creation**: User-defined widget types
- **Analytics**: Usage pattern analysis for better smart rotation

### Technical Improvements
- **Glance Integration**: Full Glance widget support when stable
- **WorkManager**: Better background task scheduling
- **Room Database**: More sophisticated data persistence
- **Compose for Widgets**: Modern UI framework integration

## Contributing

This project demonstrates advanced Android widget development techniques. Contributions are welcome for:
- Bug fixes and improvements
- Additional sample widgets
- Performance optimizations
- Documentation enhancements

## License

This project is provided as a demonstration of Android widget development techniques. Feel free to use and modify for educational purposes. 