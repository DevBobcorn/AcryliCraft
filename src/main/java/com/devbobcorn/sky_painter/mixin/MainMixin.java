package com.devbobcorn.sky_painter.mixin;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.main.Main;

@Mixin(Main.class)
public class MainMixin {

    @Shadow
    private static Logger LOGGER;

    @Inject(at = @At("HEAD"), method = "main([Ljava/lang/String;)V")
    private static void mainHead(CallbackInfo callback) {

        LOGGER.info("Before main starts...");

        LOGGER.info("PID: " + ProcessHandle.current().pid());

        try {
            // Some time for injecting RenderDoc manually
            //Thread.sleep(20000L);
        } catch (Exception e) {

        }
    }
}
