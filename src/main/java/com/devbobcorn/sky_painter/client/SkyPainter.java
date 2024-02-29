package com.devbobcorn.sky_painter.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.devbobcorn.sky_painter.ExampleMod;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ICloudRenderHandler;
import net.minecraftforge.client.ISkyRenderHandler;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(value = Dist.CLIENT, modid = ExampleMod.MODID, bus = Bus.FORGE)
public class SkyPainter {
    private static Logger LOGGER = LogManager.getLogger("Sky Painter");
    private static Minecraft mc = null;

    private static ICloudRenderHandler cloudRender = new MicaCloudRender();
    private static ISkyRenderHandler skyRender = new AcrylicSkyRender();
    
    @SubscribeEvent
    public static void onRenderCloud(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        if (mc == null) {
            mc = Minecraft.getInstance();
        }

        var r = 249F / 255F;
        var g = 240F / 255F;
        var b = 241F / 255F;

        //RenderSystem.clearColor(0, 0, 0, 0);
        var mainRT = mc.getMainRenderTarget();
        mainRT.setClearColor(r, g, b, 0);
        mainRT.clear(Minecraft.ON_OSX);
        // Bind write back to main target for upcoming rendering (solid block)
        mainRT.bindWrite(false);
        
        if (mc.level != null) {
            RenderSystem.setShaderFogColor(r, g, b);

            if (mc.level.effects().getCloudRenderHandler() != cloudRender) {
                mc.level.effects().setCloudRenderHandler(cloudRender);
                LOGGER.info("Cloud Renderer Set");
            }

            if (mc.level.effects().getSkyRenderHandler() != skyRender) {
                mc.level.effects().setSkyRenderHandler(skyRender);
                LOGGER.info("Sky Renderer Set");
            }
        }
    }

}
