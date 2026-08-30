# WaveNow — Kotlin Android Chat App

WhatsApp-style messenger connected to your DarkX chat server (Node.js + Socket.io + MongoDB).
Theme automatically switches between **light** and **dark blue** based on the phone's system setting.

## What's included

- `LoginActivity` / `RegisterActivity` — auth screens, save the JWT token on success
- `MainActivity` — chat list (WhatsApp home screen), search-to-start-chat, pull-to-refresh, live updates
- `ChatActivity` — message screen, loads history via REST, sends/receives instantly via Socket.io
- `TokenManager` — stores the token; `RetrofitClient` auto-attaches it to every REST call
- `SocketManager` — single socket connection authenticated with the same token
- Full light/dark-blue theming (`values/colors.xml` + `values-night/colors.xml`)

## 1. Connect it to your server

Open `app/src/main/java/com/darkx/wavenow/utils/Constants.kt` and replace:
```kotlin
const val BASE_URL = "https://your-app-name.onrender.com/"
```
with your actual Render URL (from the `darkx-chat-server` project). That's the only thing you must edit before building — everything else (token handling, socket auth, REST calls) is already wired end-to-end.

## 2. About building this in Termux — read this first

I want to be straight with you: **plain Termux cannot build a full Android app out of the box.** Compiling an `.apk` needs the Android SDK, build-tools, and a JDK — these aren't available as normal Termux packages because Google doesn't distribute the SDK for Android/ARM. What Termux *can* do natively is edit code, run Kotlin scripts, use git, etc. — not run Gradle against the real Android Gradle Plugin.

There are three realistic paths, in order of how well they actually work on a phone:

**Option A — Build in the cloud, edit locally in Termux (recommended)**
1. Use Termux only to edit code and push to GitHub (`pkg install git`, `git push`).
2. Add a free GitHub Actions workflow that runs `./gradlew assembleDebug` on GitHub's servers and uploads the `.apk` as a build artifact you download to your phone.
3. This is the approach most Termux-based Android devs actually use — it's fast, free, and doesn't fight your phone's limited resources.

I can write you the GitHub Actions YAML file for this right now if you want it — just say so.

**Option B — Termux + proot-distro (heavier, possible but fragile)**
Install a full Linux distro inside Termux (`pkg install proot-distro`, then `proot-distro install ubuntu`), then inside it install OpenJDK 17, download Android SDK command-line tools manually (`sdkmanager`), and run Gradle from there. This works but is slow, uses a lot of storage (3–5GB+), and Gradle syncs can take a very long time on phone hardware.

**Option C — Use an actual IDE**
Android Studio (on a PC) or an online IDE (Gitpod / GitHub Codespaces opened from your phone browser) will build this project with zero extra setup — it's a completely standard Gradle project.

## 3. Standard build (once you have SDK/Gradle access via A, B, or C)

```bash
cd WaveNow
./gradlew assembleDebug
```
The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## 4. What's NOT built yet (for later)

- Media (image/video) sending — server model supports `type`, but no upload UI yet
- Push notifications (Firebase Cloud Messaging) — needed so messages arrive even when the app is closed
- Group chat UI (server supports groups; app currently only opens 1-on-1 chats from search)
- Message delivery/read ticks beyond the basic "sent" state
- Contact list screen (currently: type a username in search to start a chat)

Tell me which of these you want next, or say the word and I'll write the GitHub Actions workflow for Option A so you can start building from Termux today.
