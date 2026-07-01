package Music;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

// ---------------- Music Player Core -----------------
class MusicPlayer {
    private AdvancedPlayer player;
    private FileInputStream fis;
    private boolean isPaused = false;
    private boolean isPlaying = false;
    private Thread playThread;
    public List<String> musicPaths = new ArrayList<>();
    private int currentTrackIndex = -1;
    private long pausePosition = 0;
    private long startTime;
    //public List<Song> playlist = new ArrayList<>();

    public void playMusic(String path) {
        try {
            if (isPlaying) player.close();
            fis = new FileInputStream(path);
            player = new AdvancedPlayer(fis);
            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackStarted(PlaybackEvent evt) {
                    startTime = System.currentTimeMillis() - pausePosition;
                }
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    isPlaying = false;
                    if (!isPaused) pausePosition = 0;
                }
            });
            isPlaying = true;
            isPaused = false;
            playThread = new Thread(() -> {
                try {
                    int startFrame = pausePosition > 0 ? (int)(pausePosition / 26) : 0;
                    player.play(startFrame, Integer.MAX_VALUE);
                } catch (Exception e) {
                    System.out.println("Error playing mp3: " + e.getMessage());
                }
            });
            playThread.start();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (isPlaying || isPaused) {
            try {
                player.close();   // stop the player
                // immediatelt close the advancedPlayer
            } catch (Exception e) {
                System.out.println("Error stopping music: " + e.getMessage());
            }
            isPlaying = false; // reset state
            isPaused = false;
            pausePosition = 0;
            System.out.println(" Music stopped.");
        }
        //else {
        // System.out.println(" No music is currently playing to stop.");
        //}
    }

    public void pauseMusic() {
        if (isPlaying) {
            isPaused = true;
            pausePosition = System.currentTimeMillis() - startTime;
            player.close();
            isPlaying = false;
            // System.out.println("Paused at " + pausePosition + "ms");
            System.out.println("Music paused.");
        }
        else if (isPaused) {
            System.out.println(" Music is already paused.");
        } else {
            System.out.println(" No music is currently playing to pause.");
        }
    }
    public void resumeMusic() {
        if (isPaused) {
            isPaused = false;
            playMusic(musicPaths.get(currentTrackIndex));
            System.out.println("▶ Music resumed.");
        } else if (isPlaying) {
            System.out.println(" Music is already playing.");
        } else {
            System.out.println("There is no paused music to resume.");
        }
    }

    // Add this list and setter method to your MusicPlayer class
    //   public List<Song> sessionPlaylist = new ArrayList<>();

    //  public void setSessionPlaylist(List<Song> songs) {
    //  this.sessionPlaylist = songs;
    // }
    public void nextTrack() {
        if (currentTrackIndex < musicPaths.size() - 1) {
            //if (isPlaying) player.close();
            if (isPlaying || isPaused)
                player.close();
            pausePosition = 0;
            currentTrackIndex++;
            playMusic(musicPaths.get(currentTrackIndex));
        } else {
            System.out.println(" No next song seems");
        }
    }


    public void prevTrack() {
        if (currentTrackIndex > 0) {
            if (isPlaying)
                player.close();
            pausePosition = 0;
            currentTrackIndex--;
            playMusic(musicPaths.get(currentTrackIndex));
        } else System.out.println("Already first track");
    }

    /*public void nextTrack() {
        if (currentTrackIndex < sessionPlaylist.size() - 1) {
            if (isPlaying || isPaused)
                player.close();
            pausePosition = 0;
            currentTrackIndex++;
            Song nextSong = sessionPlaylist.get(currentTrackIndex);
            playMusic(nextSong.getPath());
            System.out.println("▶ Now Playing: " + nextSong.getTitle() + " - " + nextSong.getArtist());
        } else {
            System.out.println("❌ No next song available.");
        }
    }

    public void prevTrack() {
        if (currentTrackIndex > 0) {
            if (isPlaying || isPaused)
                player.close();
            pausePosition = 0;
            currentTrackIndex--;
            Song prevSong = sessionPlaylist.get(currentTrackIndex);
            playMusic(prevSong.getPath());
            System.out.println("▶ Now Playing: " + prevSong.getTitle() + " - " + prevSong.getArtist());
        } else {
            System.out.println("❌ Already at the first track.");
        }
    } */
    public void setCurrentTrackIndex(int index) {
        if (index >= 0 && index < musicPaths.size()) {
            this.currentTrackIndex = index;
        }
    }
}