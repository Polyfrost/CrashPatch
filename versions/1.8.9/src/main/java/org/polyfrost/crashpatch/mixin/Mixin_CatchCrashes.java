package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.ProgressRenderError;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.pipeline.RenderTarget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.resource.manager.ReloadableResourceManager;
import net.minecraft.client.resource.manager.SimpleReloadableResourceManager;
import net.minecraft.client.resource.metadata.ResourceMetadataSerializerRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.polyfrost.crashpatch.CrashPatchConstants;
import org.polyfrost.crashpatch.client.CrashPatchConfig;
import org.polyfrost.crashpatch.client.gui.CrashUI;
import org.polyfrost.oneconfig.internal.ui.compose.SkiaCtx;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.FutureTask;

/**
 * @author Runemoro
 */
@Mixin(value = Minecraft.class, priority = -9000)
public abstract class Mixin_CatchCrashes {
    @Shadow @Final private static Logger LOGGER;
    @Shadow public static byte[] MEMORY_RESERVED_FOR_CRASH;
    @Shadow volatile boolean running;
    @Shadow private boolean crashed;
    @Shadow private CrashReport crashReport;
    @Shadow private long f3CTime;
    @Shadow public GameOptions options;
    @Shadow private ReloadableResourceManager resourceManager;
    @Shadow @Final private ResourceMetadataSerializerRegistry resourceMetadataSerializerRegistry;
    @Shadow private TextureManager textureManager;
    @Shadow private LanguageManager languageManager;
    @Shadow public TextRenderer textRenderer;
    @Shadow private RenderTarget renderTarget;
    @Shadow public Screen screen;
    @Shadow public int width;
    @Shadow public int height;
    @Shadow private int attackCooldown;
    @Shadow public GameRenderer gameRenderer;
    @Shadow @Final private Queue<FutureTask<?>> tasks;

    @Shadow protected abstract void init();
    @Shadow protected abstract void runGame();
    @Shadow public abstract void shutdown();
    @Shadow public abstract void setScreen(Screen screen);
    @Shadow public abstract CrashReport populateCrashReport(CrashReport report);
    @Shadow public abstract void gameCrashed(CrashReport report);
    @Shadow public abstract void reloadResources();
    @Shadow public abstract void updateDisplay();
    @Shadow protected abstract void logGlError(String message);
    @Shadow public abstract ClientPlayNetworkHandler getNetworkHandler();
    @Shadow public abstract void setWorld(ClientWorld world);

    @Unique private boolean crashpatch$letDie = false;
    @Unique private int crashpatch$clientCrashCount = 0;
    @Unique private int crashpatch$serverCrashCount = 0;

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void crashpatch$run(CallbackInfo ci) {
        ci.cancel();
        running = true;
        try {
            init();
        } catch (Throwable throwable) {
            CrashReport report = CrashReport.of(throwable, "Initializing game");
            report.addCategory("Initialization");
            crashpatch$displayInitErrorScreen(populateCrashReport(report));
            return;
        }
        try {
            while (running) {
                if (!crashed || crashReport == null) {
                    try {
                        runGame();
                    } catch (CrashException e) {
                        crashpatch$clientCrashCount++;
                        populateCrashReport(e.getReport());
                        crashpatch$addInfoToCrash(e.getReport());
                        crashpatch$resetGameState(false);
                        LOGGER.fatal("Reported exception thrown!", e);
                        crashpatch$displayCrashScreen(e.getReport());
                    } catch (ProgressRenderError e) {
                        throw e;
                    } catch (OutOfMemoryError e) {
                        crashpatch$clientCrashCount++;
                        crashpatch$resetGameState(true);
                        CrashReport report = populateCrashReport(new CrashReport("Out of memory", e));
                        crashpatch$addInfoToCrash(report);
                        LOGGER.fatal("Out of memory!", e);
                        crashpatch$displayCrashScreen(report);
                    } catch (Throwable e) {
                        crashpatch$clientCrashCount++;
                        CrashReport report = populateCrashReport(new CrashReport("Unexpected error", e));
                        crashpatch$addInfoToCrash(report);
                        crashpatch$resetGameState(false);
                        LOGGER.fatal("Unreported exception thrown!", e);
                        crashpatch$displayCrashScreen(report);
                    }
                } else {
                    crashpatch$serverCrashCount++;
                    crashpatch$addInfoToCrash(crashReport);
                    crashpatch$resetGameState(true);
                    crashpatch$displayCrashScreen(crashReport);
                    crashed = false;
                    crashReport = null;
                }
            }
        } catch (ProgressRenderError ignored) {
        } finally {
            shutdown();
        }
    }

    @Redirect(method = "gameCrashed", at = @At(value = "INVOKE", target = "Ljava/lang/System;exit(I)V"))
    private void crashpatch$exitIfDying(int status) {
        if (crashpatch$letDie) {
            System.exit(status);
        }
    }

    @Unique
    private void crashpatch$displayCrashScreen(CrashReport report) {
        try {
            if (!CrashPatchConfig.INSTANCE.getInGameCrashPatch()) {
                crashpatch$letDie = true;
            } else if (crashpatch$clientCrashCount >= CrashPatchConfig.INSTANCE.getCrashLimit() || crashpatch$serverCrashCount >= CrashPatchConfig.INSTANCE.getCrashLimit()) {
                LOGGER.error("Crash limit reached, exiting game");
                crashpatch$letDie = true;
            }
            gameCrashed(report);
            CrashPatchConstants.recoveredFromCrash = true;

            f3CTime = -1;
            options.debugEnabled = false;

            setScreen(new CrashUI(report).create());
        } catch (Throwable t) {
            LOGGER.error("An uncaught exception occurred while displaying the crash screen, making normal report instead", t);
            crashpatch$letDie = true;
            gameCrashed(report);
        }
    }

    @Unique
    private void crashpatch$addInfoToCrash(CrashReport report) {
        report.getSystemDetails().add("Client Crashes Since Restart", () -> String.valueOf(crashpatch$clientCrashCount));
        report.getSystemDetails().add("Integrated Server Crashes Since Restart", () -> String.valueOf(crashpatch$serverCrashCount));
    }

    @Unique
    private void crashpatch$resetGameState(boolean freeingMemory) {
        try {
            int originalMemoryReserveSize = -1;
            try {
                if (MEMORY_RESERVED_FOR_CRASH != null) {
                    originalMemoryReserveSize = MEMORY_RESERVED_FOR_CRASH.length;
                    MEMORY_RESERVED_FOR_CRASH = new byte[0];
                }
            } catch (Throwable ignored) {
            }

            BufferBuilder buffer = Tesselator.getInstance().getBuffer();
            if (((Mixin_AccessBufferBuilder) buffer).isBuilding()) {
                buffer.end();
            }
            renderTarget.unbindWrite();
            GlStateManager.matrixMode(5888);

            boolean leaveWorld = crashpatch$clientCrashCount >= CrashPatchConfig.INSTANCE.getLeaveLimit() || crashpatch$serverCrashCount >= CrashPatchConfig.INSTANCE.getLeaveLimit();
            if (leaveWorld && !freeingMemory) {
                LOGGER.error("Crash limit reached, exiting world");
                CrashUI.Companion.setLeaveWorldCrash(true);
            }

            if (leaveWorld || freeingMemory) {
                if (getNetworkHandler() != null) {
                    getNetworkHandler().getConnection().disconnect(new LiteralText("[CrashPatch] Client crashed"));
                }
                setWorld(null);

                if (gameRenderer.hasShader()) {
                    gameRenderer.closeShader();
                }

                tasks.clear();
            }

            if (originalMemoryReserveSize != -1) {
                try {
                    MEMORY_RESERVED_FOR_CRASH = new byte[originalMemoryReserveSize];
                } catch (Throwable ignored) {
                }
            }
            System.gc();
        } catch (Throwable t) {
            LOGGER.error("Failed to reset state after a crash", t);
        }
    }

    @Unique
    private void crashpatch$displayInitErrorScreen(CrashReport report) {
        try {
            if (!CrashPatchConfig.INSTANCE.getInitCrashPatch()) {
                crashpatch$letDie = true;
            }
            gameCrashed(report);

            resourceManager = new SimpleReloadableResourceManager(resourceMetadataSerializerRegistry);
            textureManager = new TextureManager(resourceManager);
            resourceManager.addListener(textureManager);
            languageManager = new LanguageManager(resourceMetadataSerializerRegistry, options.language);
            resourceManager.addListener(languageManager);
            reloadResources();
            textRenderer = new TextRenderer(options, new Identifier("textures/font/ascii.png"), textureManager, false);
            resourceManager.addListener(textRenderer);
            if (!SkiaCtx.INSTANCE.isReady()) {
                SkiaCtx.INSTANCE.init();
            }

            running = true;
            crashpatch$runGuiLoop(new CrashUI(report, CrashUI.GuiType.INIT));
        } catch (Throwable t) {
            if (!crashpatch$letDie) {
                LOGGER.error("An uncaught exception occurred while displaying the init error screen, making normal report instead", t);
            }
        }
        crashpatch$letDie = true;
        gameCrashed(report);
    }

    @Unique
    private void crashpatch$runGuiLoop(CrashUI crashUI) throws Throwable {
        Screen crashScreen = crashUI.create();
        setScreen(crashScreen);
        while (running && screen == crashScreen) {
            if (Display.isCreated() && Display.isCloseRequested()) {
                System.exit(0);
            }
            attackCooldown = 10000;
            screen.handleInputs();
            screen.tick();

            GlStateManager.pushMatrix();
            GlStateManager.clear(16640);
            renderTarget.bindWrite(true);
            GlStateManager.enableTexture();
            GlStateManager.viewport(0, 0, width, height);

            Window window = new Window((Minecraft) (Object) this);
            GlStateManager.clear(256);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, window.getScaledWidth(), window.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translatef(0.0F, 0.0F, -2000.0F);
            GlStateManager.clear(256);

            int mouseX = Mouse.getX() * window.getWidth() / width;
            int mouseY = window.getHeight() - Mouse.getY() * window.getHeight() / height - 1;
            screen.render(mouseX, mouseY, 0);
            if (crashUI.getShouldCrash()) {
                crashpatch$letDie = true;
                throw Objects.requireNonNull(crashUI.getThrowable());
            }

            renderTarget.unbindWrite();
            GlStateManager.popMatrix();

            SkiaCtx.INSTANCE.draw();
            GlStateManager.pushMatrix();
            renderTarget.draw(width, height);
            GlStateManager.popMatrix();

            updateDisplay();
            Thread.yield();
            Display.sync(60);
            logGlError("CrashPatch GUI Loop");
        }
    }
}
