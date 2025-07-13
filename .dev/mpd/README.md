# Custom MPD Docker Setup

## Quick Start

1. **Build and start MPD container:**
   ```bash
   docker-compose build mpd
   docker-compose up -d mpd
   ```

2. **Add music files:**
   - Place your music files in `.dev/mpd/data/music/`
   - MPD will automatically scan and add them to the database

3. **Run your JMPC application:**
   ```bash
   mvn spring-boot:run
   ```

## Services

### Custom MPD Server (Alpine-based)
- **Base Image:** Alpine Linux 3.19
- **Port:** 6600 (MPD protocol) 
- **HTTP Stream:** 8000 (HTTP audio streaming for testing)
- **Music Directory:** `./.dev/mpd/data/music`
- **Playlists:** `./.dev/mpd/data/playlists`
- **Features:** HTTP-only output (no sound card required)

### Music Server (Optional)
Enable with: `docker-compose --profile music-server up -d`
- **Port:** 8080 (HTTP file browser)
- Browse music files at http://localhost:8080

## Commands

```bash
# Build the custom MPD image
docker-compose build mpd

# Start MPD only
docker-compose up -d mpd

# Start with music file server
docker-compose --profile music-server up -d

# View logs
docker-compose logs -f mpd

# Stop services
docker-compose down

# Rebuild and restart
docker-compose build mpd && docker-compose up -d mpd
```

## Configuration

- **Dockerfile:** `Dockerfile.mpd` (Alpine-based custom MPD)
- **MPD Config:** `.dev/mpd/data/mpd.conf`
- **Music Directory:** `.dev/mpd/data/music/`
- **Playlists:** `.dev/mpd/data/playlists/`

## Troubleshooting

**MPD won't start:**
- Check if port 6600 is already in use: `lsof -i :6600`
- View container logs: `docker-compose logs mpd`

**No music found:**
- Ensure music files are in `dev-data/music/`
- Check file permissions
- Restart MPD: `docker-compose restart mpd`