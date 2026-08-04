package net.pixeldreamstudios.spw.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public final class SpwListScreen extends Screen {

    private final Screen parent;
    private final List<String> initial;
    private final Consumer<List<String>> onSave;
    private SpwBlacklistList list;

    public SpwListScreen(Screen parent, Component title, List<String> initial,
                         Consumer<List<String>> onSave) {
        super(title);
        this.parent = parent;
        this.initial = initial;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        list = new SpwBlacklistList(minecraft, width, height - 100, 40, 24, initial);
        addRenderableWidget(list);

        addRenderableWidget(Button.builder(Component.literal("+"),
                button -> list.add("")).bounds(width / 2 - 155, height - 52, 310, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            onSave.accept(list.collect());
            onClose();
        }).bounds(width / 2 - 155, height - 28, 150, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),
                button -> onClose()).bounds(width / 2 + 5, height - 28, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
