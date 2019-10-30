# Mindustry Crystal edition

_Crystal edition Github_

## _Running_

1. Move to release page
2. Download the file for your platform.
3. Just run the file.(java needed.)

### Information

- Discord : _https://discord.gg/g8R9XTK_

### Translating support

1. move to core/asset/bundle/
2. open bundle_(language code).properties
3. Compare and work with bundle.properties.

`_Translating is similar to the original._`

#### Windows

_Running:_ `gradlew desktop:run`  
_Building:_ `gradlew desktop:dist`

#### Linux/Mac OS

_Running:_ `./gradlew desktop:run`  
_Building:_ `./gradlew desktop:dist`

#### Server

Server builds are bundled with each released build (in Releases). If you'd rather compile on your own, replace 'desktop' with 'server', e.g. `gradlew server:dist`.

#### Android

1. Install the Android SDK [here.](https://developer.android.com/studio#downloads) Make sure you're downloading the "Command line tools only", as Android Studio is not required.
2. Create a file named `local.properties` inside the Mindustry directory, with its contents looking like this: `sdk.dir=<Path to Android SDK you just downloaded, without these bracket>`. For example, if you're on Windows and installed the tools to C:\\tools, your local.properties would contain `sdk.dir=C:\\tools` (*note the double backslashes are required instead of single ones!*).
3. Run `gradlew android:assembleDebug` (or `./gradlew` if on linux/mac). This will create an unsigned APK in `android/build/outputs/apk`.
4. (Optional) To debug the application on a connected phone, do `gradlew android:installDebug android:run`. It is **highly recommended** to use IntelliJ for this instead, however.

##### Troubleshooting

If the terminal returns `Permission denied` or `Command not found` on Mac/Linux, run `chmod +x ./gradlew` before running `./gradlew`. *This is a one-time procedure.*

---

Gradle may take up to several minutes to download files. Be patient. <br>
After building, the output .JAR file should be in `/desktop/build/libs/Mindustry.jar` for desktop builds, and in `/server/build/libs/server-release.jar` for server builds.
