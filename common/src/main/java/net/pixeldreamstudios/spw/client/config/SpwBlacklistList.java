package net.pixeldreamstudios.spw.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SpwBlacklistList
        extends ContainerObjectSelectionList<SpwBlacklistList.Entry> {

    public SpwBlacklistList(Minecraft minecraft, int width, int height, int top, int itemHeight,
                            List<String> ids) {
        super(minecraft, width, height, top, itemHeight);
        for (String id : ids) {
            addEntry(new Entry(id));
        }
    }

    public void add(String id) {
        addEntry(new Entry(id));
    }

    public List<String> collect() {
        List<String> ids = new ArrayList<>();
        for (Entry entry : children()) {
            String value = entry.field.getValue().trim();
            if (!value.isEmpty()) {
                ids.add(value);
            }
        }
        return ids;
    }

    @Override
    public int getRowWidth() {
        return 310;
    }

    @Override
    protected int getScrollbarPosition() {
        return width / 2 + 160;
    }

    public final class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final EditBox field;
        private final Button remove;

        Entry(String id) {
            this.field = new EditBox(minecraft.font, 0, 0, 270, 18,
                    Component.literal("entity id"));
            this.field.setMaxLength(256);
            this.field.setValue(id);
            this.remove = Button.builder(Component.literal("X"),
                    button -> removeEntry(this)).size(20, 18).build();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int rowWidth,
                           int rowHeight, int mouseX, int mouseY, boolean hovered,
                           float partialTick) {
            field.setX(left);
            field.setY(top);
            field.render(graphics, mouseX, mouseY, partialTick);
            remove.setX(left + rowWidth - 20);
            remove.setY(top);
            remove.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(field, remove);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(field, remove);
        }
    }
}
