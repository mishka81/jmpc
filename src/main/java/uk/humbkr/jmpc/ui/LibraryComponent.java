package uk.humbkr.jmpc.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.bff.javampd.song.MPDSong;
import uk.humbkr.jmpc.service.MpdService;

import java.util.Collection;
import java.util.stream.Collectors;

public class LibraryComponent extends VerticalLayout {

    private final MpdService mpdService;

    private Grid<MPDSong> libraryGrid;

    private TextField searchField;

    private Collection<MPDSong> allSongs;

    public LibraryComponent(MpdService mpdService) {
        this.mpdService = mpdService;

        createComponents();
        setupLayout();
        loadLibrary();
    }

    private void createComponents() {
        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Search music library...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterLibrary(e.getValue()));

        // Library grid
        libraryGrid = new Grid<>(MPDSong.class, false);
        libraryGrid.setHeightFull();

        // Configure columns
        libraryGrid.addColumn(song -> {
            String title = song.getTitle();
            return title != null ? title : song.getName();
        }).setHeader("Title").setFlexGrow(2);

        libraryGrid.addColumn(MPDSong::getArtistName)
                .setHeader("Artist")
                .setFlexGrow(1);

        libraryGrid.addColumn(MPDSong::getAlbumName)
                .setHeader("Album")
                .setFlexGrow(1);

        libraryGrid.addColumn(song -> formatDuration(song.getLength()))
                .setHeader("Duration")
                .setFlexGrow(0)
                .setWidth("80px");

        // Action column with add button
        libraryGrid.addColumn(new ComponentRenderer<>(song -> {
            Button addButton = new Button(VaadinIcon.PLUS.create());
            addButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            addButton.addClickListener(e -> addToPlaylist(song));
            addButton.setTooltipText("Add to playlist");
            return addButton;
        })).setHeader("Actions").setFlexGrow(0).setWidth("80px");

        // Double-click to add to playlist
        libraryGrid.addItemDoubleClickListener(e -> addToPlaylist(e.getItem()));
    }

    private void setupLayout() {
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        // Header
        H3 title = new H3("Music Library");

        // Header layout with search
        VerticalLayout headerLayout = new VerticalLayout();
        headerLayout.setPadding(false);
        headerLayout.setSpacing(true);
        headerLayout.add(title, searchField);

        add(headerLayout, libraryGrid);
        setFlexGrow(1, libraryGrid);
    }

    private void loadLibrary() {
        try {
            if (mpdService.isConnected()) {
                allSongs = mpdService.getAllSongs();
                libraryGrid.setItems(allSongs);
            }
        } catch (Exception e) {
            // Handle error - could show notification
        }
    }

    private void filterLibrary(String searchTerm) {
        if (allSongs == null) return;

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            libraryGrid.setItems(allSongs);
            return;
        }

        String lowerCaseFilter = searchTerm.toLowerCase().trim();

        Collection<MPDSong> filteredSongs = allSongs.stream()
                .filter(song -> matchesFilter(song, lowerCaseFilter))
                .collect(Collectors.toList());

        libraryGrid.setItems(filteredSongs);
    }

    private boolean matchesFilter(MPDSong song, String filter) {
        // Check title
        String title = song.getTitle();
        if (title != null && title.toLowerCase().contains(filter)) {
            return true;
        }

        // Check filename if no title
        if (title == null && song.getName() != null && song.getName().toLowerCase().contains(filter)) {
            return true;
        }

        // Check artist
        String artist = song.getArtistName();
        if (artist != null && artist.toLowerCase().contains(filter)) {
            return true;
        }

        // Check album
        String album = song.getAlbumName();
        if (album != null && album.toLowerCase().contains(filter)) {
            return true;
        }

        return false;
    }

    private void addToPlaylist(MPDSong song) {
        try {
            mpdService.addToPlaylist(song);
            // Could show a brief notification that song was added
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

    public void refreshLibrary() {
        loadLibrary();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        loadLibrary();
    }

}