# Gradle Wrapper

This directory contains the Gradle wrapper files needed to build the project.

## Missing Files

The `gradle-wrapper.jar` file is not included in this repository. To get it:

1. **Option 1**: Download from Gradle's official distribution
   - Visit: https://github.com/gradle/gradle/releases
   - Download the gradle-wrapper.jar for Gradle 8.2
   - Place it in this directory

2. **Option 2**: Use Android Studio
   - Open the project in Android Studio
   - Android Studio will automatically download the wrapper jar

3. **Option 3**: Use Gradle wrapper command
   - Run: `gradle wrapper` (if you have Gradle installed)
   - This will generate the wrapper jar

## Files in this directory:
- `gradle-wrapper.properties`: Configuration for the wrapper
- `gradle-wrapper.jar`: The wrapper JAR file (needs to be downloaded)

## Build the project:
Once the wrapper jar is in place, you can build the project with:
```bash
./gradlew build
``` 