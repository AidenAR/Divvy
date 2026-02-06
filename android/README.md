# Divvy Android App

## Run
1. Open `divvy/android` in Android Studio.
2. Sync Gradle.
3. Run the `app` configuration.
4. CLI build: from repo root run `./gradlew -p divvy/android :app:assembleDebug`.

## Structure
- `app/src/main/java/com/example/divvy/backend` for repositories and data access
- `app/src/main/java/com/example/divvy/ui` for screens and navigation
- `app/src/main/java/com/example/divvy/models` for domain models

## Notes
- Supabase connectivity is scaffolded but not wired to any client keys.
