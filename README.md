# 🎵 Music Player (Java Console Application)

A console-based **Music Player** application built in **Java**. The project uses a **MySQL database** (hosted via **XAMPP / phpMyAdmin**) for all persistent data — users, songs, and playlists — and supports real-time audio playback directly from the console.

---

## 📌 Project Overview

This application allows users to create an account, log in, browse songs, create and manage playlists, and play music (with pause/resume/next/previous controls) — all backed by a live MySQL database rather than static/hardcoded data. It also includes a **Free vs Premium subscription system**, where certain songs are restricted to premium users and enforced at the database level using a trigger.

- **Language:** Java
- **Package:** `Music`
- **Database:** MySQL (via XAMPP / phpMyAdmin)
- **Database file included:** `music_player_db.sql`

---

## ✨ Features

- 🔐 **User Authentication** — Login and Create Account (Java Swing GUI)
- 🎶 **Song Browsing** — View all available songs with premium tags
- ▶️ **Audio Playback Controls** — Play, Pause, Resume, Next, Previous (via console)
- 📁 **Playlist Management** — Create, view, add songs to, and remove songs from playlists
- 🔗 **Play from Playlist** — Uses a custom **Doubly Linked List** data structure for smooth next/previous track navigation
- 👑 **Subscription System** — Free and Premium user tiers; premium songs are blocked for free users (enforced by a MySQL trigger)
- 🗄️ **Live Database-Driven** — All data (users, songs, playlists) is stored and fetched from MySQL, not hardcoded

---

## 🛠️ Tech Stack

| Component          | Technology Used                                      |
|--------------------|-------------------------------------------------------|
| Programming Language | Java                                                |
| Database           | MySQL (via XAMPP / phpMyAdmin)                        |
| DB Connectivity    | JDBC + MySQL Connector Library (`mysql-connector-j`)   |
| Audio Playback     | JLayer Library (`jl1.0.1.jar`) — MP3 playback in console |
| GUI (Login/Signup) | Java Swing                                            |
| Data Structure     | Custom Doubly Linked List (for playlist navigation)    |

---

## 📂 Project Structure

```
MusicPlayerProject/ (root folder)
│
├── src/
│   └── Music/ (package)
│       ├── DatabaseConnection.java   → Handles JDBC connection to MySQL (XAMPP)
│       ├── music.java                 → Main app entry point, Swing GUI (Login/Create Account), menu logic
│       ├── MusicPlayer.java           → Core audio playback engine (play/pause/resume/next/previous)
│       └── SongOperations.java        → All song & playlist database operations (CRUD)
│
├── music_player_db.sql            → Exported MySQL database (schema + sample data)
├── jl1.0.1.jar                     → JLayer library for MP3 playback (added to build path)
└── mysql-connector-j-8.1.0.jar     → MySQL JDBC driver for database connectivity (added to build path)
```

> All Java source files live inside the `Music` package, which sits inside the `src` folder. The two library JARs (`jl1.0.1.jar`, `mysql-connector-j-8.1.0.jar`) and the database export (`music_player_db.sql`) are kept **outside** `src`, at the project root, and are added to the project's build path separately.

---

## 🗄️ Database Schema

The database (`music_player_db`) contains 4 main tables:

| Table            | Description                                          |
|-------------------|-------------------------------------------------------|
| `users`           | Stores username, password, and subscription status (`free`/`premium`) |
| `songs`           | Stores song title, artist, file path, and premium flag |
| `playlists`       | Stores user-created playlists                        |
| `playlist_songs`  | Maps songs to playlists (many-to-many relationship)   |

**Key database features:**
- Foreign key constraints with `ON DELETE CASCADE` between `users` → `playlists` → `playlist_songs`
- A **MySQL trigger** (`block_premium_songs_for_free_users`) that prevents free users from adding premium songs to a playlist directly at the database level

---

## ⚙️ Setup Instructions

1. **Install XAMPP** and start the **Apache** and **MySQL** modules.
2. Open **phpMyAdmin** (`http://localhost/phpmyadmin`) and import the provided `music_player_db.sql` file to create the database and tables.
3. Update the database credentials in `DatabaseConnection.java` if needed (default: `root` with no password).
4. Add the following JAR libraries to your project's build path:
   - `mysql-connector-j-8.1.0.jar` (JDBC driver)
   - `jl1.0.1.jar` (JLayer, for MP3 playback)
5. Update the song file paths in the `songs` table (or the database) to match the actual location of your `.mp3` files on your system.
6. Compile and run `music.java` to start the application.

---

## 👨‍🎓 Note

This project was built to practice:
- Core Java (OOP, inheritance, multithreading)
- JDBC-based database connectivity with MySQL
- Working with external libraries (JLayer for audio, MySQL Connector for DB)
- Real-world CRUD operations and database triggers
- GUI development using Java Swing
