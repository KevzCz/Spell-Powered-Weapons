package net.pixeldreamstudios.spw.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SpwParticleList
        extends ContainerObjectSelectionList<SpwParticleList.Entry> {

    public SpwParticleList(Minecraft minecraft, int width, int height, int top, int itemHeight,
                           Map<String, String> particles) {
        super(minecraft, width, height, top, itemHeight);
        for (Map.Entry<String, String> school : particles.entrySet()) {
            addEntry(new Entry(school.getKey(), school.getValue()));
        }
    }

    public void applyTo(Map<String, String> target) {
        for (Entry entry : children()) {
            target.put(entry.schoolId, entry.field.getValue().trim());
        }
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
        private final String schoolId;
        private final EditBox field;

        Entry(String schoolId, String particleId) {
            this.schoolId = schoolId;
            this.field = new EditBox(minecraft.font, 0, 0, 150, 18,
                    Component.literal(schoolId));
            this.field.setMaxLength(256);
            this.field.setValue(particleId);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int rowWidth,
                           int rowHeight, int mouseX, int mouseY, boolean hovered,
                           float partialTick) {
            graphics.drawString(minecraft.font, schoolId, left, top + 5, 0xFFFFFF);
            field.setX(left + rowWidth - 150);
            field.setY(top);
            field.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(field);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(field);
        }
    }
}
