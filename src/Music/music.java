package Music;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;


// ---------------- Main App -----------------
class MusicPlayerApp {

    static Scanner sc = new Scanner(System.in);
    static int loggedInUserId = -1;
    static String subscriptionStatus = "free";
    static boolean guiMode = false;

    public static void main(String[] args) {
        showConsoleMenu();
    }

    // New helper method for getting validated integer input
    //  Added this helper method to handle exceptions
    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (sc.hasNextInt()) {
                int choice = sc.nextInt();
                sc.nextLine(); // Consume the newline
                return choice;
            } else {
                System.out.println("❌ Invalid input. Please enter a number.");
                sc.nextLine(); // Consume the invalid input
            }
        }
    }

    private static void showConsoleMenu() {

        while (!guiMode) {
            System.out.println("\n=== Music Player ===");
            System.out.println("1. Login");
            System.out.println("2. Create New Account");
            System.out.println("3. Exit");

            int choice = getIntInput("Enter your choice (1-3): ");

            // Check if the choice is valid
            if (choice < 1 || choice > 3) {
                System.out.println("❌ Invalid choice! Please enter 1, 2, or 3.");
                continue;
            }
            switch (choice) {
                case 1:
                    guiMode = true;
                    SwingUtilities.invokeLater(() -> new LoginPage());
                    break;
                case 2:
                    guiMode = true;
                    SwingUtilities.invokeLater(() -> new CreateAccountPage());
                    break;
                case 3:
                    System.out.println("Exiting program...");
                    sc.close(); // Close the scanner here when exiting
                    System.exit(0);
                    break;
            }
        }
    }

    // Called after successful login
    public static void launchUserMenu(int userId, String subscription, String username) {
        loggedInUserId = userId;
        subscriptionStatus = subscription;

        MusicPlayer musicPlayer = new MusicPlayer();

        // to clear the playlist for a new session for search and play song history
        //  musicPlayer.sessionPlaylist.clear();

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Display All Songs");
            System.out.println("2. Search and Play Music ");
            System.out.println("3. Create New Playlist");
            System.out.println("4. View My Playlists");
            System.out.println("5. Add / Remove songs from playlist");
            System.out.println("6. Play from Playlist (Doubly Linked List)");
            System.out.println("7. Manage Subscription");
            System.out.println("8. Logout");
            System.out.println("9. Exit");
            int ch = getIntInput("Enter choice: ");

            switch (ch) {
                case 1:
                    musicPlayer.stopMusic();  // stops old song before showing song
                    SongOperations.displayAllSongs(loggedInUserId, subscriptionStatus);
                    break;
                case 2:
                    musicPlayer.stopMusic();
                    SongOperations.playSong(loggedInUserId, subscriptionStatus, musicPlayer , sc);
                    break;

                case 3:
                    musicPlayer.stopMusic();
                    SongOperations.createPlaylist(loggedInUserId, subscriptionStatus , sc);
                    break;
                case 4:
                    musicPlayer.stopMusic();
                    SongOperations.viewPlaylists(loggedInUserId , sc);
                    break;

                case 5:
                    musicPlayer.stopMusic();
                    SongOperations.manageUserPlaylists(loggedInUserId, subscriptionStatus, sc);
                    break;


                case 6:
                    musicPlayer.stopMusic();
                    SongOperations.playFromPlaylist(loggedInUserId, subscriptionStatus, musicPlayer, sc);
                    break;

                case 7:
                    musicPlayer.stopMusic();
                    subscriptionStatus = SongOperations.manageSubscription(loggedInUserId , sc);
                    break;
                case 8: // Logout Case
                    musicPlayer.stopMusic();
                    System.out.println("👋 User " + username + " logging out...");
                    loggedInUserId = -1;
                    subscriptionStatus = "free";
                    guiMode=false;
                    showConsoleMenu(); // Return to the initial menu
                    return; // Exit the current method to prevent the loop from continuing
                case 9:
                    musicPlayer.stopMusic();
                    System.out.println("Goodbye!");
                    sc.close(); // Close the scanner here when exiting
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void addSongToPlaylist(int playlistId, int userId, String sub, Scanner sc) {
        try (Connection con = DatabaseConnection.getConnection()) {
            SongOperations.displayAllSongs(userId, sub);

            int songId = getIntInput("Enter song ID to add: ");

            // Check if the song ID exists in the database
            String fetchSong = "SELECT title FROM songs WHERE song_id=?";
            PreparedStatement pst = con.prepareStatement(fetchSong);
            pst.setInt(1, songId);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Song not found.");
                return;
            }

            // Get the song's name for the success message
            String songName = rs.getString("title");

            // Java code handles checking if song is already in the playlist
            String checkExist = "SELECT * FROM playlist_songs WHERE playlist_id=? AND song_id=?";
            PreparedStatement pstCheck = con.prepareStatement(checkExist);
            pstCheck.setInt(1, playlistId);
            pstCheck.setInt(2, songId);
            ResultSet rsCheck = pstCheck.executeQuery();

            if (rsCheck.next()) {
                System.out.println("⚠ Song already exists in this playlist.");
                return;
            }

            // Add song to playlist. The trigger will check for premium songs.
            String insertSong = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?,?)";
            PreparedStatement pstInsert = con.prepareStatement(insertSong);
            pstInsert.setInt(1, playlistId);
            pstInsert.setInt(2, songId);
            pstInsert.executeUpdate();

            System.out.println("✅ Song added successfully: " + songName);

        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().equals("45000")) {
                System.out.println(  e.getMessage());
            } else {
                System.out.println("❌ An unexpected database error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeSongFromPlaylist(int playlistId, Scanner sc) {
        try (Connection con = DatabaseConnection.getConnection()) {
            System.out.print("Enter song name to remove: ");
            String songName = sc.nextLine();

            // Find the song ID by name
            String fetchSong = "SELECT song_id FROM songs WHERE title=?";
            PreparedStatement pst = con.prepareStatement(fetchSong);
            pst.setString(1, songName);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                System.out.println(" Song not found.");
                return;
            }

            int songId = rs.getInt("song_id");

            // Check if the song is actually in the playlist
            String checkExist = "SELECT * FROM playlist_songs WHERE playlist_id=? AND song_id=?";
            PreparedStatement pstCheck = con.prepareStatement(checkExist);
            pstCheck.setInt(1, playlistId);
            pstCheck.setInt(2, songId);
            ResultSet rsCheck = pstCheck.executeQuery();

            if (!rsCheck.next()) {
                System.out.println(" Song is not in this playlist.");
                return;
            }

            // Remove song from playlist
            String deleteSong = "DELETE FROM playlist_songs WHERE playlist_id=? AND song_id=?";
            PreparedStatement pstDelete = con.prepareStatement(deleteSong);
            pstDelete.setInt(1, playlistId);
            pstDelete.setInt(2, songId);
            pstDelete.executeUpdate();

            System.out.println(" Song removed successfully: " + songName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
// ---------------- Login GUI (Unchanged) -----------------
class LoginPage extends JFrame {
    public LoginPage() {
        setTitle("Login");
        setSize(300, 190);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new java.awt.FlowLayout());

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                // ===== VALIDATION START =====

                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            LoginPage.this,
                            "enter a username!"
                    );
                    return;
                }

                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            LoginPage.this,
                            "enter a password!"
                    );
                    return;
                }

                // ===== VALIDATION END =====

                try (Connection con = DatabaseConnection.getConnection()) {
                    String q = "SELECT user_id, subscription_status, username FROM users WHERE username=? AND password=?";
                    PreparedStatement pst = con.prepareStatement(q);
                    pst.setString(1, username);
                    pst.setString(2, password);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        int uid = rs.getInt("user_id");
                        String sub = rs.getString("subscription_status");
                        String uname = rs.getString("username"); // Retrieve username from the ResultSet

                        // print to console also
                        System.out.println(" Login successful for user: " + uname + " (ID: " + uid + ", Subscription: " + sub + ")");

                        JOptionPane.showMessageDialog(LoginPage.this, "Login Successful!");
                        dispose();
                        MusicPlayerApp.launchUserMenu(uid, sub,username);
                    } else {
                        JOptionPane.showMessageDialog(LoginPage.this, "Invalid credentials, try again.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }
}

// ---------------- Create Account GUI (Updated) -----------------
class CreateAccountPage extends JFrame {
    public CreateAccountPage() {
        setTitle("Create New Account");
        setSize(300, 190);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new java.awt.FlowLayout());

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JButton createButton = new JButton("Create Account");

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(createButton);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                // ===== VALIDATION START =====

                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            CreateAccountPage.this,
                            "Username cannot be empty!"
                    );
                    return;
                }

                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            CreateAccountPage.this,
                            "Password cannot be empty!"
                    );
                    return;
                }

                if (username.length() < 3) {
                    JOptionPane.showMessageDialog(
                            CreateAccountPage.this,
                            "Username must have at least 3 characters!"
                    );
                    return;
                }

                if (password.length() < 4) {
                    JOptionPane.showMessageDialog(
                            CreateAccountPage.this,
                            "Password must have at least 4 characters!"
                    );
                    return;
                }

                if (!username.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                    JOptionPane.showMessageDialog(
                            CreateAccountPage.this,
                            "Username must start with a letter and contain only letters, numbers, and _"
                    );
                    return;
                }

                // ===== VALIDATION END =====
                try (Connection con = DatabaseConnection.getConnection()) {
                    // check if username already exists
                    String check = "SELECT username FROM users WHERE username=?";
                    PreparedStatement pst = con.prepareStatement(check);
                    pst.setString(1, username);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(CreateAccountPage.this, "Username already exists!");
                        return;
                    }

                    // insert new account (default subscription free)
                    String ins = "INSERT INTO users (username,password,subscription_status) VALUES (?,?,?)";
                    PreparedStatement pst2 = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS);
                    pst2.setString(1, username);
                    pst2.setString(2, password);
                    pst2.setString(3, "free");
                    pst2.executeUpdate();

                    // after inserting and getting user id
                    // get user id and subscription
                    ResultSet genKeys = pst2.getGeneratedKeys();
                    int uid = -1;
                    String sub = "free"; // default
                    if (genKeys.next()) {
                        uid = genKeys.getInt(1);
                    }

                    // print to console
                    System.out.println("Account created for user: " + username + " (ID: " + uid + ", Subscription: " + sub + ")");

                    // show popup
                    JOptionPane.showMessageDialog(CreateAccountPage.this, "Account created successfully!");
                    dispose();

                    // Launch user menu right away
                    if (uid != -1) {
                        MusicPlayerApp.launchUserMenu(uid, sub,username);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }
}
// A simple data class to hold song information for playfromplalist method
class Song {
    int id;
    String title;
    String artist;
    String path;

    public Song(int id, String title, String artist, String path) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.path = path;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getPath() { return path; }

    // This is the method that will be overridden
    public boolean isPremium() {
        return false;
    }
}
class PremiumSong extends Song {

    public PremiumSong(int id, String title, String artist, String path) {
        super(id, title, artist, path);
    }

    @Override
    public boolean isPremium() {
        return true;
    }
}

// data Structure
class DoublyLinkedList {
    private class Node {
        Song song;
        Node next;
        Node prev;

        Node(Song song) {
            this.song = song;
            this.next = null;
            this.prev = null;
        }
    }

    private Node head;
    private Node tail;
    private Node current;
    private int size;

    public DoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.current = null;
        this.size = 0;
    }

    public void addLast(Song song) {
        Node newNode = new Node(song);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    public Song getFirst() {
        if (head != null) {
            current = head;
            return current.song;
        }
        return null;
    }

    public Song getNext() {
        if (current != null && current.next != null) {
            current = current.next;
            return current.song;
        }
        return null;
    }

    public Song getPrevious() {
        if (current != null && current.prev != null) {
            current = current.prev;
            return current.song;
        }
        return null;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}



