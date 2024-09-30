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
        // if (tickCount >= 200) {
        // if (ShouldPause()) {
        // System.out.println("Server is paused!");
        // } else {
        // System.out.println("Server tick is happening!");
        // }
        // tickCount = 0;
        // }
        if (ShouldPause()) {
            ticksPaused++;
            if (ticksPaused % 200 == 0) {
                System.out.println("Server is paused!");
            }
            MinecraftServer.getServer()
                .func_147137_ag()
                .networkTick();
            ci.cancel();
        } else {
            ticksPaused = 0;
        }
    }
}
