package net.pixeldreamstudios.spw.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.pixeldreamstudios.spw.config.SpwConfig;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SpwConfigScreen extends Screen {

    private final Screen parent;

    private boolean spreadDamageNumbers;
    private boolean showModeTags;
    private boolean showDamageBreakdown;
    private final Map<String, String> particles = new LinkedHashMap<>();

    private SpwParticleList list;

    public SpwConfigScreen(Screen parent) {
        super(Component.translatable("config.spell_powered_weapons.title"));
        this.parent = parent;
        this.spreadDamageNumbers = SpwConfig.spreadDamageNumbersOnMobs();
        this.showModeTags = SpwConfig.showModeTags();
        this.showDamageBreakdown = SpwConfig.showDamageBreakdown();
        this.particles.putAll(SpwConfig.schoolParticles());
    }

    @Override
    protected void init() {
        Button toggle = Button.builder(spreadToggleLabel(), button -> {
            spreadDamageNumbers = !spreadDamageNumbers;
            button.setMessage(spreadToggleLabel());
        }).bounds(width / 2 - 155, 28, 310, 20).build();
        addRenderableWidget(toggle);

        addRenderableWidget(Button.builder(modeTagsLabel(), button -> {
            showModeTags = !showModeTags;
            button.setMessage(modeTagsLabel());
        }).bounds(width / 2 - 155, 52, 152, 20).build());

        addRenderableWidget(Button.builder(breakdownLabel(), button -> {
            showDamageBreakdown = !showDamageBreakdown;
            button.setMessage(breakdownLabel());
        }).bounds(width / 2 + 3, 52, 152, 20).build());

        list = new SpwParticleList(minecraft, width, height - 172, 84, 25, particles);
        addRenderableWidget(list);

        int navY = height - 76;
        addRenderableWidget(Button.builder(
                Component.translatable("config.spell_powered_weapons.blacklist"),
                button -> minecraft.setScreen(new SpwListScreen(this,
                        Component.translatable("config.spell_powered_weapons.blacklist"),
                        SpwConfig.projectileBlacklist(),
                        ids -> { SpwConfig.setProjectileBlacklist(ids); SpwConfig.save(); })))
                .bounds(width / 2 - 155, navY, 310, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("config.spell_powered_weapons.split_schools"),
                button -> minecraft.setScreen(new SpwListScreen(this,
                        Component.translatable("config.spell_powered_weapons.split_schools"),
                        SpwConfig.splitAndAdditiveSchools(),
                        ids -> { SpwConfig.setSplitAndAdditiveSchools(ids); SpwConfig.save(); })))
                .bounds(width / 2 - 155, navY + 24, 152, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("config.spell_powered_weapons.additive_schools"),
                button -> minecraft.setScreen(new SpwListScreen(this,
                        Component.translatable("config.spell_powered_weapons.additive_schools"),
                        SpwConfig.additiveOnlySchools(),
                        ids -> { SpwConfig.setAdditiveOnlySchools(ids); SpwConfig.save(); })))
                .bounds(width / 2 + 3, navY + 24, 152, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            save();
            onClose();
        }).bounds(width / 2 - 155, height - 28, 150, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),
                button -> onClose()).bounds(width / 2 + 5, height - 28, 150, 20).build());
    }

    private Component spreadToggleLabel() {
        return Component.translatable("config.spell_powered_weapons.spread_damage_numbers")
                .append(": ")
                .append(Component.translatable(spreadDamageNumbers
                        ? "options.on" : "options.off"));
    }

    private Component modeTagsLabel() {
        return toggleLabel("config.spell_powered_weapons.mode_tags", showModeTags);
    }

    private Component breakdownLabel() {
        return toggleLabel("config.spell_powered_weapons.damage_breakdown", showDamageBreakdown);
    }

    private Component toggleLabel(String key, boolean on) {
        return Component.translatable(key)
                .append(": ")
                .append(Component.translatable(on ? "options.on" : "options.off"));
    }

    private void save() {
        SpwConfig.setSpreadDamageNumbersOnMobs(spreadDamageNumbers);
        SpwConfig.setShowModeTags(showModeTags);
        SpwConfig.setShowDamageBreakdown(showDamageBreakdown);
        list.applyTo(particles);
        for (Map.Entry<String, String> entry : particles.entrySet()) {
            SpwConfig.setParticleFor(entry.getKey(), entry.getValue());
        }
        SpwConfig.save();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY,
                       float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
