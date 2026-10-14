# Deployment & Link Sharing Guide: Smart Load Shedding

This guide explains how to deploy and share the **Smart Load Shedding** Android application so anyone can access and run it via a link.

---

## 1. Interactive In-Browser Demo Link (Appetize.io)

Using **Appetize.io**, anyone can interact with the live Android app directly inside their web browser (Chrome, Edge, Safari) without needing an Android device.

### Steps:
1. Build your APK:
   - In Android Studio: **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**
   - Or run in terminal: `./gradlew assembleDebug`
   - Output file: `app/build/outputs/apk/debug/app-debug.apk`
2. Go to [Appetize.io](https://appetize.io) and create an account.
3. Click **Upload App** and upload `app-debug.apk`.
4. Choose device (e.g., Pixel 7) and Android OS version (13.0 or 14.0).
5. Copy the generated public link (e.g., `https://appetize.io/app/xxxxxx`).
6. Share this link with reviewers, evaluators, or users.

> **Tip:** When testing on Appetize.io, click **"⚡ Explore Demo Mode (Instant Access)"** on the welcome screen to bypass registration and immediately experience all features!

---

## 2. Direct Download Link (GitHub Releases)

To provide a permanent download link for Android phone users:

1. Push your changes and create a git release tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. The included GitHub Actions workflow (`.github/workflows/build-apk.yml`) will automatically build the APK and attach it to your release!
3. Alternatively, manually go to your GitHub repository > **Releases** > **Draft a new release**, upload `app-debug.apk`, and publish.
4. Your permanent link:
   ```text
   https://github.com/nshongane-eng/Peak_Perfomers_NPRT63/releases/latest
   ```

---

## 3. Connecting to a Live Backend API

The app includes:
- **Built-in Offline & Standalone Fallbacks**: Outage schedules, community tips, checklists, loss logging, Room database caching, and financial calculations function completely out-of-the-box.
- **Configurable Live API**:
  - The default base URL is defined in `RetrofitClient.java`.
  - To connect to a live backend (e.g. hosted on Render, Railway, or Heroku):
    1. Open the app > go to **Settings** (via sidebar).
    2. Tap **🌐 API Server URL**.
    3. Enter your deployed server URL (e.g. `https://your-api.onrender.com/`) and tap **Save**.
    4. Or update `DEFAULT_BASE_URL` in `app/src/main/java/com/peakperformers/smartloadshedding/network/RetrofitClient.java`.
