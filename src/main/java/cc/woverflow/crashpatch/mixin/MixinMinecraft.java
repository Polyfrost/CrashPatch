/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/client/MixinMinecraft.java
 *The source file uses the MIT License.
 */

package cc.woverflow.crashpatch.mixin;

import cc.woverflow.crashpatch.gui.GuiCrashMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.*;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.FutureTask;

/**
 * @author Runemoro
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    @Final
    private static Logger logger;
    @Shadow
    volatile boolean running;
    @Shadow
    private boolean hasCrashed;
    @Shadow
    private CrashReport crashReporter;
    @Shadow
    public static byte[] memoryReserve;
    @Shadow
    @Final
    private Queue<FutureTask<?>> scheduledTasks;
    @Shadow
    public EntityRenderer entityRenderer;
    @Shadow
    private long debugCrashKeyPressTime;
    @Shadow
    public GameSettings gameSettings;
    @Shadow
    public GuiIngame ingameGUI;
    @Shadow
    private IReloadableResourceManager mcResourceManager;
    @Shadow
    public FontRenderer fontRendererObj;
    @Shadow
    public TextureManager renderEngine;
    @Shadow
    private LanguageManager mcLanguageManager;
    @Shadow
    private SoundHandler mcSoundHandler;
    @Shadow
    @Final
    private IMetadataSerializer metadataSerializer_;
    @Shadow
    private Framebuffer framebufferMc;
    @Shadow
    public GuiScreen currentScreen;
    @Shadow
    public int displayWidth;
    @Shadow
    public int displayHeight;

    private boolean crashpatch$letDie = false;

    @Shadow
    protected abstract void startGame() throws LWJGLException;

    @Shadow
    public abstract void displayGuiScreen(GuiScreen guiScreenIn);

    @Shadow
    public abstract CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash);

    @Shadow
    protected abstract void runGameLoop();

    @Shadow
    public abstract NetHandlerPlayClient getNetHandler();

    @Shadow
    public abstract void loadWorld(WorldClient worldClientIn);

    @Shadow
    public abstract void freeMemory();

    @Shadow
    public abstract void shutdownMinecraftApplet();

    @Shadow
    public abstract void refreshResources();

    @Shadow
    protected abstract void checkGLError(String message);

    @Shadow
    public abstract void updateDisplay();

    @Shadow public abstract void displayCrashReport(CrashReport crashReportIn);

    @Shadow private Timer timer;
    @Shadow private int leftClickCounter;
    private int crashpatch$clientCrashCount = 0;
    private int crashpatch$serverCrashCount = 0;

    /**
     * @author Runemoro
     * @reason Overwrite Minecraft.run()
     */
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    public void run(CallbackInfo ci) {
        ci.cancel();
        running = true;
        try {
            startGame();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Initializing game");
            crashReport.makeCategory("Initialization");
            crashpatch$displayInitErrorScreen(addGraphicsAndWorldToCrashReport(crashReport));
            return;
        }
        try {
            while (running) {
                if (!hasCrashed || crashReporter == null) {
                    try {
                        runGameLoop();
                    } catch (ReportedException e) {
                        crashpatch$clientCrashCount++;
                        addGraphicsAndWorldToCrashReport(e.getCrashReport());
                        crashpatch$addInfoToCrash(e.getCrashReport());
                        crashpatch$resetGameState();
                        logger.fatal("Reported exception thrown!", e);
                        crashpatch$displayCrashScreen(e.getCrashReport());
                    } catch (Throwable e) {
                        crashpatch$clientCrashCount++;
                        CrashReport report = new CrashReport("Unexpected error", e);
                        addGraphicsAndWorldToCrashReport(report);
                        crashpatch$addInfoToCrash(report);
                        crashpatch$resetGameState();
                        logger.fatal("Unreported exception thrown!", e);
                        crashpatch$displayCrashScreen(report);
                    }
                } else {
                    crashpatch$serverCrashCount++;
                    crashpatch$addInfoToCrash(crashReporter);
                    freeMemory();
                    crashpatch$displayCrashScreen(crashReporter);
                    hasCrashed = false;
                    crashReporter = null;
                }
            }
        } catch (MinecraftError ignored) {
        } finally {
            shutdownMinecraftApplet();
        }
    }

    /**
     * @author Runemoro
     */
    public void crashpatch$displayCrashScreen(CrashReport report) {
        try {
            displayCrashReport(report);

            // Reset hasCrashed, debugCrashKeyPressTime, and crashIntegratedServerNextTick
            hasCrashed = false;
            debugCrashKeyPressTime = -1;
//            crashIntegratedServerNextTick = false;

            // Vanilla does this when switching to main menu but not our custom crash screen
            // nor the out of memory screen (see https://bugs.mojang.com/browse/MC-128953)
            gameSettings.showDebugInfo = false;
            ingameGUI.getChatGUI().clearChatMessages();

            // Display the crash screen
//            crashpatch$runGUILoop(new GuiCrashScreen(report));
            displayGuiScreen(new GuiCrashMenu(report));
        } catch (Throwable t) {
            // The crash screen has crashed. Report it normally instead.
            logger.error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            displayCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    private void crashpatch$addInfoToCrash(CrashReport crashReport) {
        crashReport.getCategory().addCrashSectionCallable("Client Crashes Since Restart", () -> String.valueOf(crashpatch$clientCrashCount));
        crashReport.getCategory().addCrashSectionCallable("Integrated Server Crashes Since Restart", () -> String.valueOf(crashpatch$serverCrashCount));
    }

    /**
     * @author Runemoro
     */
    public void crashpatch$resetGameState() {
        try {
            // Free up memory such that this works properly in case of an OutOfMemoryError
            int originalMemoryReserveSize = -1;
            try { // In case another mod actually deletes the memoryReserve field
                if (memoryReserve != null) {
                    originalMemoryReserveSize = memoryReserve.length;
                    memoryReserve = new byte[0];
                }
            } catch (Throwable ignored) {
            }

            if (getNetHandler() != null) {
                getNetHandler().getNetworkManager().closeChannel(new ChatComponentText("[CrashPatch] Client crashed"));
            }
            loadWorld(null);
            if (entityRenderer.isShaderActive()) {
                entityRenderer.stopUseShader();
            }
            scheduledTasks.clear(); // TODO: Figure out why this isn't necessary for vanilla disconnect

            if (originalMemoryReserveSize != -1) {
                try {
                    memoryReserve = new byte[originalMemoryReserveSize];
                } catch (Throwable ignored) {
                }
            }
            System.gc();
        } catch (Throwable t) {
            logger.error("Failed to reset state after a crash", t);
        }
    }

    /**
     * @author Runemoro
     */
    public void crashpatch$displayInitErrorScreen(CrashReport report) {
        displayCrashReport(report);
        try {
            mcResourceManager = new SimpleReloadableResourceManager(metadataSerializer_);
            renderEngine = new TextureManager(mcResourceManager);
            mcResourceManager.registerReloadListener(renderEngine);

            mcLanguageManager = new LanguageManager(metadataSerializer_, gameSettings.language);
            mcResourceManager.registerReloadListener(mcLanguageManager);

            refreshResources(); // TODO: Why is this necessary?
            fontRendererObj = new FontRenderer(gameSettings, new ResourceLocation("textures/font/ascii.png"), renderEngine, false);
            mcResourceManager.registerReloadListener(fontRendererObj);

            mcSoundHandler = new SoundHandler(mcResourceManager, gameSettings);
            mcResourceManager.registerReloadListener(mcSoundHandler);

            running = true;
            try {
                //noinspection deprecation
                SplashProgress.pause();// Disable the forge splash progress screen
                GlStateManager.disableTexture2D();
                GlStateManager.enableTexture2D();
            } catch (Throwable ignored) {
            }
            crashpatch$runGUILoop(new GuiCrashMenu(report, true));
        } catch (Throwable t) {
            if (!crashpatch$letDie) {
                logger.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            }
            displayCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    /**
     * @author Runemoro
     */
    private void crashpatch$runGUILoop(GuiCrashMenu screen) throws Throwable {
        displayGuiScreen(screen);
        while (running && currentScreen != null) {
            if (Display.isCreated() && Display.isCloseRequested()) {
                System.exit(0);
            }

            timer.updateTimer();

            for (int j = 0; j < this.timer.elapsedTicks; ++j)
            {
                leftClickCounter = 10000;
                currentScreen.handleInput();
                currentScreen.updateScreen();
            }

            GlStateManager.pushMatrix();
            GlStateManager.clear(16640);
            framebufferMc.bindFramebuffer(true);
            GlStateManager.enableTexture2D();

            GlStateManager.viewport(0, 0, ((Minecraft) (Object) this).displayWidth, ((Minecraft) (Object) this).displayHeight);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();

            ScaledResolution scaledResolution = new ScaledResolution(((Minecraft) (Object) this));
            GlStateManager.clear(256);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, scaledResolution.getScaledWidth_double(), scaledResolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);

            GlStateManager.clear(256);
            int width = scaledResolution.getScaledWidth();
            int height = scaledResolution.getScaledHeight();
            int mouseX = Mouse.getX() * width / displayWidth;
            int mouseY = height - Mouse.getY() * height / displayHeight - 1;
            currentScreen.drawScreen(mouseX, mouseY, 0);
            if (screen.getShouldCrash()) {
                crashpatch$letDie = true;
                throw screen.getReport().getCrashCause();
            }

            framebufferMc.unbindFramebuffer();
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            framebufferMc.framebufferRender(displayWidth, displayHeight);
            GlStateManager.popMatrix();

            updateDisplay();
            Thread.yield();
            Display.sync(60);
            checkGLError("CrashPatch GUI Loop");
        }
    }

    @Redirect(method = "displayCrashReport", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;handleExit(I)V"))
    public void redirect(FMLCommonHandler instance, int soafsdg) {
    }

}



