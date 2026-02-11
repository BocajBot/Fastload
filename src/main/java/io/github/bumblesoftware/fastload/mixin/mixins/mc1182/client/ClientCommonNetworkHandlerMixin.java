package io.github.bumblesoftware.fastload.mixin.mixins.mc1182.client;

import io.github.bumblesoftware.fastload.util.obj_holders.MutableObjectHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import static io.github.bumblesoftware.fastload.client.FLClientEvents.Locations.RP_SEND_RUNNABLE;
import static io.github.bumblesoftware.fastload.common.FLCommonEvents.Events.RUNNABLE_EVENT;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {
    @Redirect(
            method = "onResourcePackSend",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
            ),
            require = 0
    )
    private void deferResourcePackPromptScreen(MinecraftClient client, Screen screen) {
        if (RUNNABLE_EVENT.isNotEmpty(RP_SEND_RUNNABLE)) {
            RUNNABLE_EVENT.execute(
                    List.of(RP_SEND_RUNNABLE),
                    new MutableObjectHolder<>(() -> client.setScreen(screen))
            );
        } else {
            client.setScreen(screen);
        }
    }
}
