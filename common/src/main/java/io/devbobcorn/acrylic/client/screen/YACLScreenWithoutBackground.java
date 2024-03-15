package io.devbobcorn.acrylic.client.screen;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;

public class YACLScreenWithoutBackground extends YACLScreen {

    public YACLScreenWithoutBackground(YetAnotherConfigLib config, Screen parent) {
        super(config, parent);
    }

    @SuppressWarnings("null")
    @Override
    protected void init() {
        super.init();

        removeWidget(tabNavigationBar);
    }

    @SuppressWarnings("null")
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {

        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);

        for (Renderable renderable : this.renderables) {
            renderable.render(matrices, mouseX, mouseY, delta);
        }
    }

    public static YACLScreenWithoutBackground generateForConfig(YetAnotherConfigLib config, Screen parent) {
        return new YACLScreenWithoutBackground(config, parent);
    }

}