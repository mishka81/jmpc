package uk.humbkr.jmpc.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;
import uk.humbkr.jmpc.service.MpdService;

@Route("")
public class MainView extends AppLayout {

    private final MpdService mpdService;

    private PlayerControlsComponent playerControls;

    private PlaylistComponent playlist;

    private LibraryComponent library;

    @Autowired
    public MainView(MpdService mpdService) {
        this.mpdService = mpdService;

        createHeader();
        createDrawer();
        createContent();
    }

    private void createHeader() {
        H1 logo = new H1("JMPC");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM
        );

        DrawerToggle toggle = new DrawerToggle();

        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        // Add navigation items here later

        addToDrawer(drawerContent);
    }

    private void createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);

        // Player controls at the bottom
        playerControls = new PlayerControlsComponent(mpdService);

        // Main content area with playlist and library
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);

        // Playlist on the left
        playlist = new PlaylistComponent(mpdService);
        playlist.setWidth("50%");

        // Library on the right
        library = new LibraryComponent(mpdService);
        library.setWidth("50%");

        mainContent.add(playlist, library);

        content.add(mainContent);
        content.add(playerControls);
        content.setFlexGrow(1, mainContent);
        content.setFlexGrow(0, playerControls);

        setContent(content);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Initialize MPD connection
        try {
            mpdService.connect();
        } catch (Exception e) {
            // Handle connection error
        }
    }

}