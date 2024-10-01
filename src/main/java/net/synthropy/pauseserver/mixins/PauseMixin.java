package net.synthropy.pauseserver.mixins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class PauseMixin {

    // Must be > 0 to allow time for some world setup and cleanup after people leave.
    private static final int COUNTDOWN_PLAYERS = 200;
    private static final int COUNTDOWN_STOPPING = 200;
    private static final int PAUSED_TICKS_PER_SECOND = 0;

    private static int pauseCountdown = 200;
    private static int ticksPaused = 0;

    private void CheckPlayers() {
        pauseCountdown = Math.max(pauseCountdown - 1, 0);

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

    private boolean ShouldPause() {
        return pauseCountdown == 0 && MinecraftServer.getServer()
            .isServerRunning();
    }

    private void PausedTick() {
        net.minecraftforge.common.chunkio.ChunkIOExecutor.tick();
        MinecraftServer server = MinecraftServer.getServer();
        server.func_147137_ag()
            .networkTick();
        if (server.isDedicatedServer()) {
            ((DedicatedServer) server).executePendingCommands();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {
        CheckPlayers();
        if (ShouldPause()) {
            if (ticksPaused == 0) {
                System.out.println("No players online, pausing server.");
                if (PAUSED_TICKS_PER_SECOND > 0) {
                    System.out.println("(Slowed to " + Math.round(100 * PAUSED_TICKS_PER_SECOND / 20) + "%)");
                }
            }
            if (PAUSED_TICKS_PER_SECOND == 0 || ticksPaused % (20 / PAUSED_TICKS_PER_SECOND) > 0) {
                PausedTick();
                ci.cancel();
            }
            ticksPaused++;
        } else if (ticksPaused > 0) {
            System.out.println("Unpausing.");
            ticksPaused = 0;
        }
    }
}
