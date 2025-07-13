package uk.humbkr.jmpc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import uk.humbkr.jmpc.service.MpdService;

@Configuration
@ConditionalOnProperty(name = "mpd.embedded.enabled", havingValue = "true")
@Slf4j
public class MpdContainerConfig {

    private GenericContainer<?> mpdContainer;
    
    @Autowired
    private MpdService mpdService;

    @PostConstruct
    public void startMpdContainer() {
        mpdContainer = new GenericContainer<>(DockerImageName.parse("vimagick/mpd:latest"))
                .withExposedPorts(6600)
                .withCommand("--no-daemon", "--stdout")
                .waitingFor(Wait.forLogMessage(".*daemon: startup.*", 1));

        mpdContainer.start();
        
        // Update MPD service with container connection info
        String host = mpdContainer.getHost();
        int port = mpdContainer.getMappedPort(6600);
        mpdService.updateConnectionInfo(host, port);
        
        log.info("MPD container started at " + host + ":" + port);
    }

    @PreDestroy
    public void stopMpdContainer() {
        if (mpdContainer != null) {
            mpdContainer.stop();
        }
    }

    @Bean
    @ConditionalOnProperty(name = "mpd.embedded.enabled", havingValue = "true")
    public String mpdContainerInfo() {
        if (mpdContainer != null && mpdContainer.isRunning()) {
            return "MPD running at " + mpdContainer.getHost() + ":" + mpdContainer.getMappedPort(6600);
        }
        return "MPD container not running";
    }
}