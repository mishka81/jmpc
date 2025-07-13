package uk.humbkr.jmpc.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.bff.javampd.song.MPDSong;
import uk.humbkr.jmpc.service.MpdService;

import java.util.Collection;

public class PlaylistComponent extends VerticalLayout {

    private final MpdService mpdService;

    private Grid<MPDSong> playlistGrid;

    private Button clearPlaylistButton;

    public PlaylistComponent(MpdService mpdService) {
        this.mpdService = mpdService;

        createComponents();
        setupLayout();
        loadPlaylist();
    }

    private void createComponents() {
        // Header
        H3 title = new H3("Current Playlist");

        // Clear playlist button
        clearPlaylistButton = new Button("Clear Playlist", VaadinIcon.TRASH.create());
        clearPlaylistButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearPlaylistButton.addClickListener(e -> clearPlaylist());

        // Playlist grid
        playlistGrid = new Grid<>(MPDSong.class, false);
        playlistGrid.setHeightFull();

        // Configure columns
        playlistGrid.addColumn(song -> {
            String songTitle = song.getTitle();
            return songTitle != null ? songTitle : song.getName();
        }).setHeader("Title").setFlexGrow(2);

        playlistGrid.addColumn(MPDSong::getArtistName)
                .setHeader("Artist")
                .setFlexGrow(1);

        playlistGrid.addColumn(MPDSong::getAlbumName)
                .setHeader("Album")
                .setFlexGrow(1);

        playlistGrid.addColumn(song -> formatDuration(song.getLength()))
                .setHeader("Duration")
                .setFlexGrow(0)
                .setWidth("80px");

        // Action column with remove button
        playlistGrid.addColumn(new ComponentRenderer<>(song -> {
            Button removeButton = new Button(VaadinIcon.MINUS.create());
            removeButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            removeButton.addClickListener(e -> removeSong(song));
            removeButton.setTooltipText("Remove from playlist");
            return removeButton;
        })).setHeader("Actions").setFlexGrow(0).setWidth("80px");

        // Double-click to play
        playlistGrid.addItemDoubleClickListener(e -> playSong(e.getItem()));
    }

    private void setupLayout() {
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        // Header layout
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        H3 title = new H3("Current Playlist");
        headerLayout.add(title, clearPlaylistButton);

        add(headerLayout, playlistGrid);
        setFlexGrow(1, playlistGrid);
    }

    private void loadPlaylist() {
        try {
            if (mpdService.isConnected()) {
                Collection<MPDSong> songs = mpdService.getPlaylist();
                playlistGrid.setItems(songs);
            }
        } catch (Exception e) {
            // Handle error - could show notification
        }
    }

    private void clearPlaylist() {
        try {
            mpdService.clearPlaylist();
            loadPlaylist();
        } catch (Exception e) {
            // Handle error
        }
    }

    private void removeSong(MPDSong song) {
        try {
            mpdService.removeFromPlaylist(song);
            loadPlaylist();
        } catch (Exception e) {
            // Handle error
        }
    }

    private void playSong(MPDSong song) {
        try {
            // Note: This would require additional MPD API to play specific song by position
            // For now, we'll just play the current selection
            mpdService.play();
        } catch (Exception e) {
            // Handle error
        }
    }

    private String formatDuration(int seconds) {
        if (seconds <= 0) return "--:--";

        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }

    public void refreshPlaylist() {
        loadPlaylist();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        loadPlaylist();
    }

}