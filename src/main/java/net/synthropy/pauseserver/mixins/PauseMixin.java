package net.synthropy.pauseserver.mixins;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class PauseMixin {

    // Must be > 0 to allow time for some world setup and cleanup after people leave.
    // I don't know exactly how many ticks we need, which is concerning.
    private static final int TICKS_BEFORE_PAUSE = 200;
    private static final int PAUSED_TICKS_PER_SECOND = 1;

    private static int ticksWithoutPlayers = 0;
    private static int ticksPaused = 0;

    private void CheckPlayers() {
        if (MinecraftServer.getServer()
            .getCurrentPlayerCount() == 0) {
            ticksWithoutPlayers++;
        } else {
            ticksWithoutPlayers = 0;
        }
    }

    private boolean ShouldPause() {
        return ticksWithoutPlayers >= TICKS_BEFORE_PAUSE;
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
            if (ticksPaused % (20 / PAUSED_TICKS_PER_SECOND) > 0) {
                MinecraftServer.getServer()
                    .func_147137_ag()
                    .networkTick();
                ci.cancel();
            }
            ticksPaused++;
        } else if (ticksPaused > 0) {
            System.out.println("Unpausing.");
            ticksPaused = 0;
        }
    }
}
