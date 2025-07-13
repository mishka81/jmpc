package uk.humbkr.jmpc.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.bff.javampd.player.Player;
import org.bff.javampd.playlist.MPDPlaylistSong;
import org.bff.javampd.song.MPDSong;
import org.vaadin.addons.componentfactory.PaperSlider;
import uk.humbkr.jmpc.service.MpdService;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerControlsComponent extends VerticalLayout {

    private final MpdService mpdService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Button playPauseButton;

    private Button stopButton;

    private Button previousButton;

    private Button nextButton;

    private PaperSlider volumeSlider;

    private ProgressBar progressBar;

    private Span songInfoLabel;

    private Span timeLabel;

    private Future<?> pollingRegistration;

    public PlayerControlsComponent(MpdService mpdService) {
        this.mpdService = mpdService;

        createControls();
        setupLayout();
        setupEventHandlers();
    }

    private void createControls() {
        // Song info
        songInfoLabel = new Span("No song playing");
        songInfoLabel.getStyle().set("font-weight", "bold");

        // Progress bar
        progressBar = new ProgressBar();
        progressBar.setWidthFull();
        progressBar.setMin(0);
        progressBar.setMax(100);

        // Time display
        timeLabel = new Span("00:00 / 00:00");

        // Control buttons
        previousButton = new Button(VaadinIcon.STEP_BACKWARD.create());
        previousButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        playPauseButton = new Button(VaadinIcon.PLAY.create());
        playPauseButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY);

        stopButton = new Button(VaadinIcon.STOP.create());
        stopButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        nextButton = new Button(VaadinIcon.STEP_FORWARD.create());
        nextButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        // Volume control
        volumeSlider = new PaperSlider();
        volumeSlider.setMin(0);
        volumeSlider.setMax(100);
        volumeSlider.setValue(50);
        volumeSlider.setWidth("150px");
    }

    private void setupLayout() {
        setPadding(true);
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);

        // Song info and progress
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);
        infoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        infoLayout.add(songInfoLabel, progressBar, timeLabel);
        infoLayout.setWidthFull();

        // Control buttons
        HorizontalLayout controlsLayout = new HorizontalLayout();
        controlsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        controlsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        controlsLayout.add(previousButton, playPauseButton, stopButton, nextButton);

        // Volume control
        HorizontalLayout volumeLayout = new HorizontalLayout();
        volumeLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        volumeLayout.add(VaadinIcon.VOLUME_DOWN.create(), volumeSlider, VaadinIcon.VOLUME_UP.create());

        // Complete layout
        HorizontalLayout completeControls = new HorizontalLayout();
        completeControls.setWidthFull();
        completeControls.setAlignItems(FlexComponent.Alignment.CENTER);
        completeControls.add(controlsLayout, volumeLayout);
        completeControls.setFlexGrow(1, controlsLayout);
        completeControls.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        add(infoLayout, completeControls);
        setWidthFull();
    }

    private void setupEventHandlers() {
        playPauseButton.addClickListener(e -> togglePlayPause());
        stopButton.addClickListener(e -> stop());
        previousButton.addClickListener(e -> previous());
        nextButton.addClickListener(e -> next());

        volumeSlider.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                setVolume(e.getValue());
            }
        });
    }

    private void togglePlayPause() {
        try {
            Player.Status status = mpdService.getPlayerStatus();
            if (status == Player.Status.STATUS_PLAYING) {
                mpdService.pause();
            } else {
                mpdService.play();
            }
            updateUI();
        } catch (Exception e) {
            // Handle error
        }
    }

    private void stop() {
        try {
            mpdService.stop();
            updateUI();
        } catch (Exception e) {
            // Handle error
        }
    }

    private void previous() {
        try {
            mpdService.previous();
            updateUI();
        } catch (Exception e) {
            // Handle error
        }
    }

    private void next() {
        try {
            mpdService.next();
            updateUI();
        } catch (Exception e) {
            // Handle error
        }
    }

    private void setVolume(int volume) {
        try {
            mpdService.setVolume(volume);
        } catch (Exception e) {
            // Handle error
        }
    }

    private void updateUI() {
        try {
            // Update play/pause button
            Player.Status status = mpdService.getPlayerStatus();
            if (status == Player.Status.STATUS_PLAYING) {
                playPauseButton.setIcon(VaadinIcon.PAUSE.create());
            } else {
                playPauseButton.setIcon(VaadinIcon.PLAY.create());
            }

            // Update song info
            Optional<MPDPlaylistSong> currentSong = mpdService.getCurrentSong();
            if (currentSong.isPresent()) {
                MPDSong song = currentSong.get();
                String artist = song.getArtistName() != null ? song.getArtistName() : "Unknown Artist";
                String title = song.getTitle() != null ? song.getTitle() : song.getName();
                songInfoLabel.setText(artist + " - " + title);
            } else {
                songInfoLabel.setText("No song playing");
            }

            // Update progress
            long elapsed = mpdService.getElapsedTime();
            long total = mpdService.getTotalTime();

            if (total > 0) {
                double progress = (double) elapsed / total * 100;
                progressBar.setValue(progress);

                String elapsedStr = formatTime(elapsed);
                String totalStr = formatTime(total);
                timeLabel.setText(elapsedStr + " / " + totalStr);
            } else {
                progressBar.setValue(0);
                timeLabel.setText("00:00 / 00:00");
            }

            // Update volume
            int volume = mpdService.getVolume();
            volumeSlider.setValue(volume);

        } catch (Exception e) {
            // Handle error
        }
    }

    private String formatTime(long seconds) {
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Start periodic UI updates
        UI ui = attachEvent.getUI();
        pollingRegistration = scheduler.scheduleAtFixedRate(() -> {
            ui.access(this::updateUI);
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        if (pollingRegistration != null) {
            pollingRegistration.cancel(true);
        }
        scheduler.shutdown();
    }

}