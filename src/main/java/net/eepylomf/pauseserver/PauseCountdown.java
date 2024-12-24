package net.eepylomf.pauseserver;

import net.minecraft.server.MinecraftServer;

public class PauseCountdown {

    private static PauseCountdown instance;

    // Must be > 0 to allow time for some world setup and cleanup.
    private static final int COUNTDOWN_PLAYERS = 200;
    private static final int COUNTDOWN_STOPPING = 200;
    private static final int COUNTDOWN_STARTING = 200;

    private int pauseCountdown = COUNTDOWN_STARTING;
    private int afkCountdown = 0;

    public static PauseCountdown getInstance() {
        if (instance == null) {
            instance = new PauseCountdown();
        }
        return instance;
    }

    public void setAFKCountdown(int countdown) {
        afkCountdown = countdown;
    }

    public void reset() {
        pauseCountdown = COUNTDOWN_STARTING;
        afkCountdown = 0;
    }

    public boolean ShouldPause() {
        return pauseCountdown == 0 && afkCountdown == 0
            && MinecraftServer.getServer()
                .isServerRunning();
    }

    public void tick() {
        pauseCountdown = Math.max(pauseCountdown - 1, 0);
        afkCountdown = Math.max(afkCountdown - 1, 0);

        if (!MinecraftServer.getServer()
            .isServerRunning()) {
            // Server is stopping, resume normal behaviour for safety.
            pauseCountdown = Math.max(pauseCountdown, COUNTDOWN_STOPPING);
        }
        if (MinecraftServer.getServer()
            .getCurrentPlayerCount() > 0) {
            // Players are online. Don't pause for at least 10 seconds.
            pauseCountdown = Math.max(pauseCountdown, COUNTDOWN_PLAYERS);
        }
    }
}
