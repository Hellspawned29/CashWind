# Cashwind Android (Native Kotlin)

This is the native Android app for Cashwind using Retrofit + OkHttp.

## Open in Android Studio
1. File → Open → select the `android/` folder.
2. When prompted, allow Android Gradle Plugin sync. Android Studio will create/upgrade the Gradle Wrapper.
3. Choose a device/emulator and Run.

## Configure API
- Set your base URL in `app/src/main/java/com/cashwind/app/network/RetrofitProvider.kt` (`BASE_URL`).
- Replace `ApiService` endpoints to match Prosper/Cashwind backend.

## Module coordinates
- Application ID: `com.cashwind.app`
- Min SDK: 24, Target SDK: 34

## Key libraries
- Retrofit 2 + Moshi converter
- OkHttp + logging-interceptor
- AndroidX ViewModel, Activity KTX, Material Components

## Next steps
- Add auth (e.g., token interceptor)
- Define real API models + DTOs (Moshi or Kotlinx Serialization)
- Implement feature screens and navigation
