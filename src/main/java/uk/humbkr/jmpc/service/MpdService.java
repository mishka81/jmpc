package uk.humbkr.jmpc.service;

import lombok.extern.slf4j.Slf4j;
import org.bff.javampd.MPDException;
import org.bff.javampd.player.Player;
import org.bff.javampd.playlist.MPDPlaylistSong;
import org.bff.javampd.server.MPD;
import org.bff.javampd.server.MPDConnectionException;
import org.bff.javampd.song.MPDSong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MpdService {

    private MPD mpd;

    @Value("${mpd.host:localhost}")
    private String mpdHost;

    @Value("${mpd.port:6600}")
    private int mpdPort;

    public void updateConnectionInfo(String host, int port) {
        this.mpdHost = host;
        this.mpdPort = port;
    }

    public void connect() throws MPDConnectionException {
        try {
            log.info("Connecting to MPD server at {}:{}", mpdHost, mpdPort);
            mpd = MPD.builder()
                    .server(InetAddress.getByName(mpdHost).getHostAddress())
                    .port(mpdPort)
                    .build();
            log.info("Successfully connected to MPD server");
        } catch (Exception e) {
            log.error("Failed to connect to MPD server at {}:{}", mpdHost, mpdPort, e);
            throw new MPDConnectionException("Failed to connect to MPD server", e);
        }
    }

    public boolean isConnected() {
        return mpd != null && mpd.isConnected();
    }

    public void disconnect() {
        if (mpd != null) {
            log.info("Disconnecting from MPD server");
            mpd.close();
        }
    }

    // Player controls
    public void play() throws MPDException {
        ensureConnected();
        mpd.getPlayer().play();
    }

    public void pause() throws MPDException {
        ensureConnected();
        mpd.getPlayer().pause();
    }

    public void stop() throws MPDException {
        ensureConnected();
        mpd.getPlayer().stop();
    }

    public void next() throws MPDException {
        ensureConnected();
        mpd.getPlayer().playNext();
    }

    public void previous() throws MPDException {
        ensureConnected();
        mpd.getPlayer().playPrevious();
    }

    public int getVolume() throws MPDException {
        ensureConnected();
        return mpd.getPlayer().getVolume();
    }

    // Volume control
    public void setVolume(int volume) throws MPDException {
        ensureConnected();
        mpd.getPlayer().setVolume(Math.max(0, Math.min(100, volume)));
    }

    // Status information
    public Player.Status getPlayerStatus() throws MPDException {
        ensureConnected();
        return mpd.getPlayer().getStatus();
    }

    public Optional<MPDPlaylistSong> getCurrentSong() throws MPDException {
        ensureConnected();
        return mpd.getPlayer().getCurrentSong();
    }

    public long getElapsedTime() throws MPDException {
        ensureConnected();
        return mpd.getPlayer().getElapsedTime();
    }

    public long getTotalTime() throws MPDException {
        ensureConnected();
        return mpd.getPlayer().getTotalTime();
    }

    // Playlist operations
    public Collection<MPDSong> getPlaylist() throws MPDException {
        ensureConnected();
//        return mpd.getPlaylist().getSongList();
        return List.of();
    }

    public void addToPlaylist(MPDSong song) throws MPDException {
        ensureConnected();
        mpd.getPlaylist().addSong(song);
    }

    public void removeFromPlaylist(MPDSong song) throws MPDException {
        ensureConnected();
//        mpd.getPlaylist().removeSong(song);
    }

    public void clearPlaylist() throws MPDException {
        ensureConnected();
        mpd.getPlaylist().clearPlaylist();
    }

    // Music database
    public Collection<MPDSong> getAllSongs() throws MPDException {
        ensureConnected();
//        return mpd.getMusicDatabase().listAllSongs();
        return List.of();
    }

    public Collection<MPDSong> searchByArtist(String artist) throws MPDException {
        ensureConnected();
//        return mpd.getMusicDatabase().searchByArtist(artist);
        return List.of();
    }

    public Collection<MPDSong> searchByAlbum(String album) throws MPDException {
        ensureConnected();
//        return mpd.getMusicDatabase().searchByAlbum(album);
        return List.of();
    }

    public Collection<MPDSong> searchByTitle(String title) throws MPDException {
        ensureConnected();
//        return mpd.getMusicDatabase().searchByTitle(title);
        return List.of();
    }

    private void ensureConnected() throws MPDConnectionException {
        if (!isConnected()) {
            connect();
        }
    }

}