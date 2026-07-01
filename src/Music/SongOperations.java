package Music;

import java.sql.*;
import java.util.*;

// ---------------- Song Operations -----------------

class SongOperations {
    private static int getIntInput(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (sc.hasNextInt()) {
                int choice = sc.nextInt();
                sc.nextLine();
                return choice;
            } else {
                System.out.println("❌ Invalid input. Please enter a number.");
                sc.nextLine();
            }
        }
    }

    public static void displayAllSongs(int userId, String sub) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String q = "SELECT song_id, title, artist_name, is_premium FROM songs";
            PreparedStatement pst = con.prepareStatement(q);
            ResultSet rs = pst.executeQuery();
            System.out.println("\n=== Available Songs ===");
            while (rs.next()) {
                int id = rs.getInt("song_id");
                String title = rs.getString("title");
                String artist = rs.getString("artist_name");
                boolean prem = rs.getBoolean("is_premium");
                if (prem) {
                    System.out.println(id + ". " + title + " - " + artist + " (👑 Premium)");
                } else {
                    System.out.println(id + ". " + title + " - " + artist);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Case 2: Play Song
    public static void playSong(int userId, String sub, MusicPlayer musicPlayer, Scanner sc) {

        // 1. Show all songs
        displayAllSongs(userId, sub);
        Song selectedSong = null;

        int songId = getIntInput(sc, "Enter the Song ID to play: ");

        try (Connection con = DatabaseConnection.getConnection()) {
            String q = "SELECT song_id, title, artist_name, song_path, is_premium FROM songs WHERE song_id=?";
            PreparedStatement pst = con.prepareStatement(q);
            pst.setInt(1, songId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String title = rs.getString("title");
                String artist = rs.getString("artist_name");
                String path = rs.getString("song_path"); // path to .mp3 in DB
                boolean prem = rs.getBoolean("is_premium");

                // Check subscription
                if (prem && sub.equalsIgnoreCase("free")) {
                    System.out.println("❌ " + title + " is a premium song. Upgrade your subscription!");
                    return;
                }

                //  musicPlayer.musicPaths.clear();
                //  musicPlayer.musicPaths.add(path);
                //  musicPlayer.setCurrentTrackIndex(0);

                // Add song to player queue
                musicPlayer.musicPaths.add(path);
                musicPlayer.setCurrentTrackIndex(musicPlayer.musicPaths.size() - 1);

                System.out.println("▶ Now Playing: " + title + " - " + artist);
                musicPlayer.playMusic(path);

                // 3. Controls
                while (true) {
                    System.out.println("\nControls: (p)ause | (r)esume | (n)ext | (b)previous | (q)uit to menu");
                    String cmd = sc.nextLine().toLowerCase();
                    switch (cmd) {
                        case "p":
                            musicPlayer.pauseMusic();
                            break;

                        case "r":
                            musicPlayer.resumeMusic();
                            break;

                        case "n":
                            musicPlayer.nextTrack();
                            break;

                        case "b":
                            musicPlayer.prevTrack();
                            //  System.out.println("▶ Now Playing: " + title + " - " + artist);
                            break;

                        case "q":
                            musicPlayer.stopMusic();
                            return;

                        default:
                            System.out.println("❌ Invalid command. Enter p, r, n, b, or q.");
                    }

                }

            } else {
                System.out.println("Song not found!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- Create New Playlist -----------------
    public static void createPlaylist(int userId, String sub, Scanner sc) {
        try (Connection con = DatabaseConnection.getConnection()) {

            while (true) {
                System.out.print("\nEnter a name for your new playlist: ");
                String playlistName = sc.nextLine().trim();

                if (playlistName.isEmpty()) {
                    System.out.println("Playlist name cannot be empty.");
                    continue;
                }

                // 1 Check if the user already has a playlist with this name
                String checkQuery = "SELECT playlist_id FROM playlists WHERE created_by_user_id=? AND playlist_name=?";
                PreparedStatement chkPst = con.prepareStatement(checkQuery);
                chkPst.setInt(1, userId);
                chkPst.setString(2, playlistName);
                ResultSet rs = chkPst.executeQuery();

                if (rs.next()) {
                    System.out.println(" You already have a playlist named '" + playlistName + "'. Please choose a different name.");
                    continue; // ask again
                }

                // 2️ Insert new playlist into DB
                String insertQuery = "INSERT INTO playlists (playlist_name, created_by_user_id) VALUES (?, ?)";
                PreparedStatement pst = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                pst.setString(1, playlistName);
                pst.setInt(2, userId);
                pst.executeUpdate();

                // 3️ Get generated playlist ID (optional)
                ResultSet genKeys = pst.getGeneratedKeys();
                int playlistId = -1;
                if (genKeys.next()) {
                    playlistId = genKeys.getInt(1);
                }

                System.out.println(" Playlist '" + playlistName + "' created successfully! (ID: " + playlistId + ")");
                break; // exit the loop after successful creation
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void viewPlaylists(int userId, Scanner sc) {
        try (Connection con = DatabaseConnection.getConnection()) {

            // 1️⃣ Check if user has any playlists
            String playlistQuery = "SELECT playlist_id, playlist_name FROM playlists WHERE created_by_user_id=?";
            PreparedStatement pst = con.prepareStatement(playlistQuery);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            List<Integer> playlistIds = new ArrayList<>();
            List<String> playlistNames = new ArrayList<>();

            while (rs.next()) {
                playlistIds.add(rs.getInt("playlist_id"));
                playlistNames.add(rs.getString("playlist_name"));
            }

            if (playlistIds.isEmpty()) {
                System.out.println("You don't have any playlists yet.");
                return;
            }

            // 2️⃣ Display all playlists of the user
            System.out.println("\n=== Your Playlists ===");
            for (int i = 0; i < playlistIds.size(); i++) {
                System.out.println(playlistIds.get(i) + ". " + playlistNames.get(i));
            }

            // 3️⃣ Ask user which playlist to view
            int chosenId = getIntInput(sc, "Enter the Playlist ID to view songs: ");

            if (!playlistIds.contains(chosenId)) {
                System.out.println(" Invalid Playlist ID.");
                return;
            }

            // 4️⃣ Fetch songs of the selected playlist
            String songQuery = "SELECT s.song_id, s.title, s.artist_name, s.is_premium " +
                    "FROM songs s JOIN playlist_songs ps ON s.song_id = ps.song_id " +
                    "WHERE ps.playlist_id=?";
            PreparedStatement pstSongs = con.prepareStatement(songQuery);
            pstSongs.setInt(1, chosenId);
            ResultSet rsSongs = pstSongs.executeQuery();

            List<String> songs = new ArrayList<>();
            while (rsSongs.next()) {
                String title = rsSongs.getString("title");
                String artist = rsSongs.getString("artist_name");
                boolean premium = rsSongs.getBoolean("is_premium");
                if (premium)
                    title += " (👑 Premium)";
                songs.add(title + " - " + artist);
            }

            if (songs.isEmpty()) {
                System.out.println(" This playlist is empty!");
            } else {
                System.out.println("\n--- Songs in Playlist ---");
                for (int i = 0; i < songs.size(); i++) {
                    System.out.println((i + 1) + ". " + songs.get(i));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void manageUserPlaylists(int userId, String sub, Scanner sc) {
        try (Connection con = DatabaseConnection.getConnection()) {
            boolean exitMainMenu = false;

            while (!exitMainMenu) {
                // 1️⃣ Display all playlists for the user
                String playlistQuery = "SELECT playlist_id, playlist_name FROM playlists WHERE created_by_user_id=?";
                PreparedStatement pst = con.prepareStatement(playlistQuery);
                pst.setInt(1, userId);
                ResultSet rs = pst.executeQuery();

                List<Integer> playlistIds = new ArrayList<>();
                List<String> playlistNames = new ArrayList<>();

                while (rs.next()) {
                    playlistIds.add(rs.getInt("playlist_id"));
                    playlistNames.add(rs.getString("playlist_name"));
                }

                if (playlistIds.isEmpty()) {
                    System.out.println("You don't have any playlists yet.");
                    return;
                }

                System.out.println("\n=== Your Playlists ===");
                for (int i = 0; i < playlistIds.size(); i++) {
                    System.out.println(playlistIds.get(i) + ". " + playlistNames.get(i));
                }
                System.out.println("0. Exit to Main Menu");

                int pid = getIntInput(sc, "Enter Playlist ID to ADD / REMOVE song (or 0 to exit): ");

                if (pid == 0) {
                    exitMainMenu = true; // exit completely
                    break;
                }

                if (!playlistIds.contains(pid)) {
                    System.out.println("Invalid Playlist ID.");
                    continue; // ask for playlist again
                }

                // 2️⃣ Manage selected playlist
                boolean exitPlaylistMenu = false;
                while (!exitPlaylistMenu) {
                    // Display songs in selected playlist
                    String fetchSongs = "SELECT s.song_id, s.title, s.artist_name, s.is_premium " +
                            "FROM songs s JOIN playlist_songs ps ON s.song_id = ps.song_id " +
                            "WHERE ps.playlist_id=?";
                    PreparedStatement pstSongs = con.prepareStatement(fetchSongs);
                    pstSongs.setInt(1, pid);
                    ResultSet rsSongs = pstSongs.executeQuery();

                    List<Integer> currentSongIds = new ArrayList<>();
                    System.out.println("\n=== Songs in Playlist: " + playlistNames.get(playlistIds.indexOf(pid)) + " ===");
                    while (rsSongs.next()) {
                        int sid = rsSongs.getInt("song_id");
                        currentSongIds.add(sid);
                        String title = rsSongs.getString("title");
                        String artist = rsSongs.getString("artist_name");
                        boolean isPremium = rsSongs.getBoolean("is_premium");

                        if (isPremium) {
                            System.out.println(sid + ". " + title + " - " + artist + " [Premium]");
                        } else {
                            System.out.println(sid + ". " + title + " - " + artist);
                        }
                    }

                    if (currentSongIds.isEmpty()) {
                        System.out.println("There are no songs in this playlist yet.");
                    }


                    // Playlist options
                    System.out.println("\n1. Add Song");
                    System.out.println("2. Remove Song");
                    System.out.println("3. Go Back to Playlist Selection");
                    System.out.println("0. Exit to Main Menu");

                    int action = getIntInput(sc, "Enter your choice: ");

                    switch (action) {
                        case 1:
                            MusicPlayerApp.addSongToPlaylist(pid, userId, sub, sc); // add song with subscription check
                            break;
                        case 2:
                            MusicPlayerApp.removeSongFromPlaylist(pid, sc); // remove song
                            break;
                        case 3:
                            exitPlaylistMenu = true; // back to playlist selection
                            break;
                        case 0:
                            exitPlaylistMenu = true;
                            exitMainMenu = true; // exit completely
                            break;
                        default:
                            System.out.println("Invalid choice!");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playFromPlaylist(int userId, String sub, MusicPlayer musicPlayer, Scanner sc) {
        try (Connection con = DatabaseConnection.getConnection()) {
            System.out.println("\n=== Play from Playlist ===");

            // 1. Display all playlists of the user
            String playlistQuery = "SELECT playlist_id, playlist_name FROM playlists WHERE created_by_user_id=?";
            PreparedStatement pst = con.prepareStatement(playlistQuery);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            List<Integer> playlistIds = new ArrayList<>();
            Map<Integer, String> playlistNames = new HashMap<>();

            if (!rs.isBeforeFirst()) {
                System.out.println("❌ You don't have any playlists to play from yet.");
                return;
            }

            System.out.println("\n--- Your Playlists ---");
            while (rs.next()) {
                int pId = rs.getInt("playlist_id");
                playlistIds.add(pId);
                playlistNames.put(pId, rs.getString("playlist_name"));
                System.out.println(pId + ". " + playlistNames.get(pId));
            }

            // 2. Ask user to select a playlist
            int chosenId = getIntInput(sc, "Enter the Playlist ID to play: ");

            if (!playlistIds.contains(chosenId)) {
                System.out.println("❌ Invalid Playlist ID.");
                return;
            }

            // 3. Fetch songs and load into Doubly Linked List
            String songQuery = "SELECT s.song_id, s.title, s.artist_name, s.song_path, s.is_premium " +
                    "FROM songs s JOIN playlist_songs ps ON s.song_id = ps.song_id " +
                    "WHERE ps.playlist_id=?";
            PreparedStatement pstSongs = con.prepareStatement(songQuery);
            pstSongs.setInt(1, chosenId);
            ResultSet rsSongs = pstSongs.executeQuery();

            DoublyLinkedList playlistDLL = new DoublyLinkedList();
            musicPlayer.musicPaths.clear();

            while (rsSongs.next()) {
                int songId = rsSongs.getInt("song_id");
                String title = rsSongs.getString("title");
                String artist = rsSongs.getString("artist_name");
                String path = rsSongs.getString("song_path");
                boolean isPremium = rsSongs.getBoolean("is_premium");

                // New logic: Instantiate a Song or PremiumSong object based on database data
                Song song;
                if (isPremium) {
                    song = new PremiumSong(songId, title, artist, path);
                } else {
                    song = new Song(songId, title, artist, path);
                }

                // Use the object's isPremium() method for the check
                if (song.isPremium() && sub.equalsIgnoreCase("free")) {
                    System.out.println("⚠ Skipping premium song: '" + title + "' (Upgrade to premium to play)");
                    continue;
                }

                playlistDLL.addLast(song);
                musicPlayer.musicPaths.add(path);
            }

            if (playlistDLL.isEmpty()) {
                System.out.println("⚠ The selected playlist is empty!");
                return;
            }

            // 4. Start playback and show controls
            Song firstSong = playlistDLL.getFirst();
            musicPlayer.setCurrentTrackIndex(0);
            System.out.println("\n▶ Now Playing from playlist: " + playlistNames.get(chosenId));
            System.out.println("   Now Playing: " + firstSong.getTitle() + " - " + firstSong.getArtist());
            musicPlayer.playMusic(firstSong.getPath());

            while (true) {
                System.out.println("\nControls: (p)ause | (r)esume | (n)ext | (b)previous | (q)uit to menu");
                String cmd = sc.nextLine().toLowerCase();

                if (cmd.equals("p")) {
                    musicPlayer.pauseMusic();
                } else if (cmd.equals("r")) {
                    musicPlayer.resumeMusic();
                } else if (cmd.equals("n")) {
                    Song nextSong = playlistDLL.getNext();
                    if (nextSong != null) {
                        musicPlayer.nextTrack();
                        System.out.println("▶ Now Playing: " + nextSong.getTitle() + " - " + nextSong.getArtist());
                    } else {
                        System.out.println("End of playlist.");
                    }
                } else if (cmd.equals("b")) {
                    Song prevSong = playlistDLL.getPrevious();
                    if (prevSong != null) {
                        musicPlayer.prevTrack();
                        System.out.println("▶ Now Playing: " + prevSong.getTitle() + " - " + prevSong.getArtist());
                    } else {
                        System.out.println("Already at the beginning of the playlist.");
                    }
                } else if (cmd.equals("q")) {
                    musicPlayer.stopMusic();
                    break;
                } else {
                    System.out.println("❌ Invalid command. Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String manageSubscription(int userId, Scanner sc) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String currentSub = "free";
            String checkQ = "SELECT subscription_status FROM users WHERE user_id=?";
            PreparedStatement chk = con.prepareStatement(checkQ);
            chk.setInt(1, userId);
            ResultSet rs = chk.executeQuery();
            if (rs.next()) {
                currentSub = rs.getString("subscription_status");
            }

            System.out.println("\n=== Subscription Management ===");
            System.out.println("Your current account status is: " + currentSub.toUpperCase());

            if (currentSub.equalsIgnoreCase("premium")) {
                System.out.println("You are already a premium member.");
                return currentSub;
            }

            System.out.println("1. Upgrade to Premium (199₹/month)");
            System.out.println("2. Go Back");
            int choice = getIntInput(sc, "Enter your choice: ");

            if (choice == 1) {
                String updateQ = "UPDATE users SET subscription_status = 'premium' WHERE user_id = ?";
                PreparedStatement updatePst = con.prepareStatement(updateQ);
                updatePst.setInt(1, userId);
                int rowsAffected = updatePst.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println(" Subscription successfully upgraded to Premium!");
                    return "premium";
                } else {
                    System.out.println("Failed to upgrade subscription. Please try again.");
                    return currentSub;
                }
            } else {
                System.out.println("Returning to main menu...");
                return currentSub;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "free";
        }
    }
}
