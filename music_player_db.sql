-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 01, 2026 at 04:49 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `music_player_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `playlists`
--

CREATE TABLE `playlists` (
  `playlist_id` int(11) NOT NULL,
  `playlist_name` varchar(255) NOT NULL,
  `created_by_user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `playlists`
--

INSERT INTO `playlists` (`playlist_id`, `playlist_name`, `created_by_user_id`) VALUES
(9, 'sport', 12);

-- --------------------------------------------------------

--
-- Table structure for table `playlist_songs`
--

CREATE TABLE `playlist_songs` (
  `playlist_id` int(11) NOT NULL,
  `song_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `playlist_songs`
--

INSERT INTO `playlist_songs` (`playlist_id`, `song_id`) VALUES
(9, 6);

--
-- Triggers `playlist_songs`
--
DELIMITER $$
CREATE TRIGGER `block_premium_songs_for_free_users` BEFORE INSERT ON `playlist_songs` FOR EACH ROW BEGIN
    DECLARE user_subscription_status VARCHAR(50);
    DECLARE is_song_premium TINYINT(1);
    
    SELECT subscription_status INTO user_subscription_status
    FROM users
    WHERE user_id = (SELECT created_by_user_id FROM playlists WHERE playlist_id = NEW.playlist_id);
    
    SELECT is_premium INTO is_song_premium
    FROM songs
    WHERE song_id = NEW.song_id;
    
    IF user_subscription_status = 'free' AND is_song_premium = 1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'You must be a premium member to add this song.';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `songs`
--

CREATE TABLE `songs` (
  `song_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `artist_name` varchar(255) NOT NULL,
  `is_premium` tinyint(1) NOT NULL DEFAULT 0,
  `song_path` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `songs`
--

INSERT INTO `songs` (`song_id`, `title`, `artist_name`, `is_premium`, `song_path`) VALUES
(1, 'Dhun Laagi', 'Aaditya Gadhvi', 0, 'D:\\songs\\Dhun Laagi.mp3'),
(2, 'Champion', 'Pritam', 0, 'D:\\songs\\Chandu Champion.mp3'),
(3, 'Sapphire', 'Ed Sheeran', 1, 'D:\\songs\\Sapphire.mp3'),
(4, 'Veera Raja Veera', 'Carnatic Song', 1, 'D:\\songs\\Veera Raja Veera.mp3'),
(5, 'Patadas de Ahogado', 'Pambo', 0, 'D:\\songs\\Patadas de Ahogado.mp3'),
(6, 'If you Believe', 'pop', 0, 'D:\\songs\\If You Believe.mp3'),
(7, 'Titli', 'artist', 0, 'D:\\songs\\Titli.mp3'),
(8, 'Sitar Drone', 'Instrumental', 0, 'D:\\songs\\Sitar Drone.mp3'),
(9, 'Ilahi', 'Arijit Singh', 0, 'D:\\songs\\Illahi.mp3'),
(10, 'Kaapi Thillana', 'Carnatic Music', 0, 'D:\\songs\\Kaapi Thillana.mp3'),
(11, 'Vhalam', 'Sachin Jigar', 0, 'D:\\songs\\Vhalam Aavo Ne.mp3');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `subscription_status` varchar(50) NOT NULL DEFAULT 'free'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `subscription_status`) VALUES
(1, 'suraj', 'password123', 'premium'),
(2, 'ravi', 'password4569', 'free'),
(3, 'Aakash', 'pass1', 'free'),
(4, 'Veda', 'pass1235', 'free'),
(5, 'madhav', 'pas', 'free'),
(6, 'Asha', 'paw123', 'free'),
(7, 'Vedant', 'pass89', 'free'),
(8, 'Siddhi', 'sid89', 'premium'),
(9, 'Rajvi', 'password1133', 'free'),
(10, 'Madhvi', 'pass', 'free'),
(11, 'Vagisha', 'pass', 'free'),
(12, 'Sid', 'pass12', 'free'),
(13, 'mahi', '123456', 'free');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `playlists`
--
ALTER TABLE `playlists`
  ADD PRIMARY KEY (`playlist_id`),
  ADD KEY `created_by_user_id` (`created_by_user_id`);

--
-- Indexes for table `playlist_songs`
--
ALTER TABLE `playlist_songs`
  ADD PRIMARY KEY (`playlist_id`,`song_id`),
  ADD KEY `song_id` (`song_id`);

--
-- Indexes for table `songs`
--
ALTER TABLE `songs`
  ADD PRIMARY KEY (`song_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `playlists`
--
ALTER TABLE `playlists`
  MODIFY `playlist_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `songs`
--
ALTER TABLE `songs`
  MODIFY `song_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `playlists`
--
ALTER TABLE `playlists`
  ADD CONSTRAINT `playlists_ibfk_1` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `playlist_songs`
--
ALTER TABLE `playlist_songs`
  ADD CONSTRAINT `playlist_songs_ibfk_1` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`playlist_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `playlist_songs_ibfk_2` FOREIGN KEY (`song_id`) REFERENCES `songs` (`song_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
