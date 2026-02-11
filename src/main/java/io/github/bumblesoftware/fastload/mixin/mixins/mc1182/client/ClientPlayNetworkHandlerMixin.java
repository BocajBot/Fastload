package io.github.bumblesoftware.fastload.mixin.mixins.mc1182.client;

import io.github.bumblesoftware.fastload.client.FLClientEvents.Contexts.PlayerJoinEventContext;
import io.github.bumblesoftware.fastload.client.FLClientEvents.Contexts.SetScreenEventContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static io.github.bumblesoftware.fastload.client.FLClientEvents.Events.PLAYER_JOIN_EVENT;
import static io.github.bumblesoftware.fastload.client.FLClientEvents.Events.SET_SCREEN_EVENT;
import static io.github.bumblesoftware.fastload.client.FLClientEvents.Locations.*;

/**
 * Sets setPlayerJoined to true when the player joins the game
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Unique
    private boolean fastload$inOnGameJoin;

    @Inject(method = "onGameJoin", at = @At("HEAD"), require = 0)
    private void setGameJoinStateOnStart(GameJoinS2CPacket packet, CallbackInfo ci) {
        fastload$inOnGameJoin = true;
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"), require = 0)
    private void onGamedJoinEvent(GameJoinS2CPacket packet, CallbackInfo ci) {
        fastload$inOnGameJoin = false;
        if (PLAYER_JOIN_EVENT.isNotEmpty())
            PLAYER_JOIN_EVENT.execute(new PlayerJoinEventContext(packet));
    }

    @Redirect(method = "startWorldLoading", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreenAndRender(Lnet/minecraft/client/gui/screen/Screen;)V"), require = 0)
    private void modifyDownloadingTerrainScreen(MinecraftClient client, Screen screen) {
        if (fastload$inOnGameJoin && SET_SCREEN_EVENT.isNotEmpty(DTS_GAME_JOIN_REDIRECT))
            SET_SCREEN_EVENT.execute(List.of(DTS_GAME_JOIN_REDIRECT), new SetScreenEventContext(screen, null));
        else client.setScreenAndRender(screen);
    }

    @Redirect(method = "startWorldLoading", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/LevelLoadingScreen;init(Lnet/minecraft/client/world/ClientChunkLoadProgress;Lnet/minecraft/client/gui/screen/world/LevelLoadingScreen$WorldEntryReason;)V"), require = 0)
    private void modifyExistingDownloadingTerrainScreen(
            LevelLoadingScreen levelLoadingScreen,
            ClientChunkLoadProgress chunkLoadProgress,
            LevelLoadingScreen.WorldEntryReason worldEntryReason
    ) {
        if (fastload$inOnGameJoin && SET_SCREEN_EVENT.isNotEmpty(DTS_GAME_JOIN_REDIRECT))
            SET_SCREEN_EVENT.execute(List.of(DTS_GAME_JOIN_REDIRECT), new SetScreenEventContext(levelLoadingScreen, null));
        else levelLoadingScreen.init(chunkLoadProgress, worldEntryReason);
    }

    @Redirect(method = "onPlayerRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), require = 0)
    private void instantLoad(MinecraftClient client, Screen screen) {
        if (SET_SCREEN_EVENT.isNotEmpty(RESPAWN_DTS_REDIRECT))
            SET_SCREEN_EVENT.execute(List.of(RESPAWN_DTS_REDIRECT), new SetScreenEventContext(screen, null));
        else client.setScreen(screen);

    }
}
