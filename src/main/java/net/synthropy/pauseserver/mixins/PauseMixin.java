package net.synthropy.pauseserver.mixins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.synthropy.pauseserver.PauseCountdown;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class PauseMixin {

    private int ticksPaused = 0;

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
        PauseCountdown.getInstance()
            .tick();
        if (PauseCountdown.getInstance()
            .ShouldPause()) {
            if (ticksPaused == 0) {
                System.out.println("No players online, pausing server.");
            }
            PausedTick();
            ci.cancel();
            ticksPaused++;
        } else if (ticksPaused > 0) {
            System.out.println("Unpausing.");
            ticksPaused = 0;
        }
    }
}
