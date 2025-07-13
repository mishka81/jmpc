FROM alpine:latest

# Install MPD and required dependencies
RUN apk add --no-cache \
    mpd \
    lame \
    flac \
    vorbis-tools \
    faac \
    ffmpeg \
    curl \
    && rm -rf /var/cache/apk/*

# Create required directories with proper permissions
RUN mkdir -p \
    /var/lib/mpd/music \
    /var/lib/mpd/playlists \
    /var/log/mpd \
    /run/mpd && \
    chown -R mpd:audio /var/lib/mpd /var/log/mpd /run/mpd

# Copy MPD configuration
COPY ./data/mpd.conf /etc/mpd.conf
RUN chown mpd:audio /etc/mpd.conf

# Create a simple entrypoint script
RUN echo '#!/bin/sh' > /entrypoint.sh && \
    echo 'echo "Starting MPD server..."' >> /entrypoint.sh && \
    echo 'echo "MPD will be available on port 6600"' >> /entrypoint.sh && \
    echo 'echo "HTTP stream will be available on port 8000"' >> /entrypoint.sh && \
    echo 'exec mpd --no-daemon --stdout /etc/mpd.conf' >> /entrypoint.sh && \
    chmod +x /entrypoint.sh

# Switch to MPD user
USER mpd

# Expose MPD port and HTTP streaming port
EXPOSE 6600 8000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD nc -z localhost 6600 || exit 1

# Set working directory
WORKDIR /var/lib/mpd

# Start MPD
ENTRYPOINT ["/entrypoint.sh"]