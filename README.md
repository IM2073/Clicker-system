# Mobile Programming — Clicker System

A simple mobile clicker system with an Android app and Java Servlet backend.

## Project Structure

```
Mobile-Programming/
├── app/                  ← Android Studio project
└── backend-src/          ← Java Servlet backend
    ├── SelectServlet.java
    ├── DisplayServlet.java
    ├── setup.sql
    └── WEB-INF/
        ├── web.xml
        ├── classes/      ← compiled .class files (gitignored)
        └── lib/
            └── mysql-connector-j-9.6.0.jar
```

---

## Backend Setup

### 1. Create the database

```bash
mysql -u myuser -p < backend-src/setup.sql
```

### 2. Deploy to Tomcat (symlink — once only)

```bash
ln -s /Users/goodwilllion/IM2073/Mobile-Programming/backend-src \
      /Users/goodwilllion/Web-Programming/tomcat/webapps/clicker
```

### 3. Compile the servlets

```bash
cd /Users/goodwilllion/IM2073/Mobile-Programming/backend-src/WEB-INF/classes

javac -cp "/Users/goodwilllion/Web-Programming/tomcat/lib/servlet-api.jar:../lib/mysql-connector-j-9.6.0.jar" \
      -d /Users/goodwilllion/IM2073/Mobile-Programming/backend-src/WEB-INF/classes/ \
      /Users/goodwilllion/IM2073/Mobile-Programming/backend-src/*.java
```

### 4. Start Tomcat (if not already running)

```bash
/Users/goodwilllion/Web-Programming/tomcat/bin/startup.sh
```

### 5. Test in browser

| URL | Expected |
|-----|----------|
| `http://localhost:9999/clicker/select?choice=a` | `OK` |
| `http://localhost:9999/clicker/display` | HTML results table |

---

## Android App

### Files changed

| File | Path |
|------|------|
| `activity_main.xml` | `app/src/main/res/layout/activity_main.xml` |
| `MainActivity.java` | `app/src/main/java/com/example/clickerapp/MainActivity.java` |
| `AndroidManifest.xml` | `app/src/main/AndroidManifest.xml` |

### How to run

1. Open `Mobile-Programming/` in **Android Studio**
2. Wait for Gradle sync to finish
3. Make sure Tomcat is running (`startup.sh`)
4. Click **Run** (green play button) to launch on the emulator
5. Tap buttons A / B / C / D — each sends a request to the backend
6. Check results at `http://localhost:9999/clicker/display`

### Why 10.0.2.2 and not localhost?

The Android emulator runs in its own virtual machine. `localhost` inside the emulator refers to the emulator itself, not your Mac. `10.0.2.2` is the special IP that routes back to your Mac's localhost.
