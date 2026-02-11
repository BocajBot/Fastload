package io.github.bumblesoftware.fastload.abstraction;

import com.mojang.serialization.Codec;
import io.github.bumblesoftware.fastload.api.abstraction.core.config.RetrieveValueFunction;
import io.github.bumblesoftware.fastload.api.abstraction.core.config.StoreValueFunction;
import io.github.bumblesoftware.fastload.client.BuildingTerrainScreen;
import io.github.bumblesoftware.fastload.compat.modmenu.FLConfigScreenButtons;
import io.github.bumblesoftware.fastload.config.DefaultConfig;
import io.github.bumblesoftware.fastload.config.FLConfig;
import io.github.bumblesoftware.fastload.util.Action;
import io.github.bumblesoftware.fastload.util.Bound;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public class Client1182 implements AbstractClientCalls {
    @Override
    public String[] getSupportedVersions() {
        return new String[]{"1.21.11"};
    }

    @Override
    public MinecraftClient getClientInstance() {
        return MinecraftClient.getInstance();
    }

    @Override
    public ClientWorld getClientWorld() {
        return getClientInstance().world;
    }

    @Override
    public <T> Screen newConfigScreen(
            final Screen parent,
            GameOptions gameOptions,
            final Text title,
            Function<Object[], T[]> options,
            Action config
    ) {
        return new GameOptionsScreen(parent, gameOptions, title) {
            @Override
            protected void addOptions() {
                this.body.addAll((SimpleOption<?>[]) options.apply(new SimpleOption[]{}));
            }

            @Override
            public void removed() {
                config.commit();
                super.removed();
            }
        };
    }

    @Override
    public Screen newFastloadConfigScreen(final Screen parent) {
        return newConfigScreen(
                parent,
                getClientInstance().options,
                Text.translatable("fastload.screen.config"),
                objects -> newFLConfigScreenButtons().getAllOptions(objects),
                FLConfig::writeToDisk
        );
    }

    @Override
    public Screen newBuildingTerrainScreen(final int loadingAreaGoal) {
        return new BuildingTerrainScreen(loadingAreaGoal);
    }

    @Override
    public Screen getCurrentScreen() {
        return getClientInstance().currentScreen;
    }

    @Override
    public Text newTranslatableText(final String content) {
        return Text.translatable(content);
    }

    @Override
    public Text newLiteralText(final String content) {
        return Text.literal(content);
    }

    @Override
    public <T> FLConfigScreenButtons<T> newFLConfigScreenButtons() {
        return (FLConfigScreenButtons<T>) new FLConfigScreenButtons<SimpleOption<?>>();
    }

    @Override
    public SimpleOption<Boolean> newCyclingButton(
            final String namespace,
            final String identifier,
            final RetrieveValueFunction retrieveValueFunction,
            final StoreValueFunction storeValueFunction
    ) {
        return SimpleOption.ofBoolean(
                namespace + identifier,
                SimpleOption.constantTooltip(newTranslatableText(namespace + identifier + ".tooltip")),
                Boolean.parseBoolean(retrieveValueFunction.getValue(identifier)),
                aBoolean -> storeValueFunction.setValue(identifier, Boolean.toString(aBoolean))
        );
    }

    @Override
    public SimpleOption<Integer> newSlider(
            final String namespace,
            final String identifier,
            final RetrieveValueFunction retrieveValueFunction,
            final StoreValueFunction storeValueFunction,
            final Bound minMaxValues,
            final int width
    ) {
        int max = minMaxValues.max();
        int min = minMaxValues.min();
        return new SimpleOption<>(
                namespace + identifier,
                SimpleOption.constantTooltip(newTranslatableText(namespace + identifier + ".tooltip")),
                (optionText, value) -> {
                    if (value.equals(min)) {
                        return GameOptions.getGenericValueText(optionText, Text.translatable(namespace + identifier + ".min"));
                    } else if (value.equals(max)) {
                        return GameOptions.getGenericValueText(optionText, Text.translatable(namespace + identifier + ".max"));
                    } else {
                        return GameOptions.getGenericValueText(optionText, value);
                    }
                },
                new SimpleOption.ValidatingIntSliderCallbacks(min, max),
                Codec.DOUBLE.xmap(value -> max, value -> (double) value - max),
                Integer.parseInt(retrieveValueFunction.getValue(identifier)),
                value -> storeValueFunction.setValue(identifier, Integer.toString(value))
        );
    }

    @Override
    public void setScreen(final Screen screen) {
        getClientInstance().setScreen(screen);
    }

    @Override
    public void renderScreenBackgroundTexture(
            final Screen screen,
            final int offset,
            final DrawContext drawContext
    ) {
        // Avoid Screen#renderBackground blur path; some modpacks already blur once per frame.
        screen.renderInGameBackground(drawContext);
    }

    @Override
    public void drawCenteredText(
            final DrawContext drawContext,
            final TextRenderer textRenderer,
            final Text text,
            final int centerX,
            final int y,
            final int color
    ) {
        drawContext.drawCenteredTextWithShadow(textRenderer, text, centerX, y, color);
    }

    @Override
    public void drawCenteredText(
            final DrawContext drawContext,
            final TextRenderer textRenderer,
            final String text,
            final int centerX,
            final int y,
            final int color
    ) {
        drawContext.drawCenteredTextWithShadow(textRenderer, text, centerX, y, color);
    }

    @Override
    public int getLoadedChunkCount() {
        return getClientWorld().getChunkManager().getLoadedChunkCount();
    }

    @Override
    public int getCompletedChunkCount() {
        return getClientInstance().worldRenderer.getCompletedChunkCount();
    }

    @Override
    public int getViewDistance() {
        if (getClientInstance().options == null) {
            return DefaultConfig.LOCAL_CHUNK_RADIUS_BOUND.max();
        } else {
            return getClientInstance().options.getViewDistance().getValue();
        }
    }

    @Override
    public boolean isWindowFocused() {
        return getClientInstance().isWindowFocused();
    }

    @Override
    public boolean isSingleplayer() {
        return getClientInstance().isInSingleplayer();
    }

    @Override
    public boolean forCurrentScreen(final ScreenProvider screenProvider) {
        return screenProvider.getCurrent(getCurrentScreen());
    }

    @Override
    public boolean isBuildingTerrainScreen(final Screen screen) {
        return screen instanceof BuildingTerrainScreen;
    }

    @Override
    public boolean isGameMenuScreen(final Screen screen) {
        return screen instanceof GameMenuScreen;
    }

    @Override
    public boolean isDownloadingTerrainScreen(final Screen screen) {
        return screen instanceof LevelLoadingScreen;
    }
}
