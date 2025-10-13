package org.polyfrost.crashpatch.mixin;
//#if MC<1.13
//#if FORGE
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.FMLCommonHandler;
//#endif

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.polyfrost.crashpatch.client.CrashPatchConfig;
import org.polyfrost.crashpatch.client.RenderState;
import org.polyfrost.crashpatch.client.crashes.GameStateManager;
import org.polyfrost.crashpatch.client.gui.CrashUI;
import org.polyfrost.crashpatch.hooks.MinecraftHook;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.FutureTask;

@SuppressWarnings("AccessStaticViaInstance")
@Mixin(value = Minecraft.class, priority = -9000)
public abstract class MixinMinecraft implements MinecraftHook {

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
    private long debugCrashKeyPressTime;
    @Shadow
    public GameSettings gameSettings;
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

    @Unique
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
    public abstract void shutdownMinecraftApplet();

    @Shadow
    public abstract void refreshResources();

    @Shadow
    protected abstract void checkGLError(String message);

    @Shadow
    public abstract void updateDisplay();

    @Shadow public abstract void displayCrashReport(CrashReport crashReportIn);
    @Shadow private int leftClickCounter;

    @Shadow public abstract NetHandlerPlayClient getNetHandler();

    @Shadow public abstract void loadWorld(WorldClient worldClientIn);

    @Shadow public EntityRenderer entityRenderer;
    @Shadow @Final private Queue<FutureTask<?>> scheduledTasks;
    @Unique
    private int crashpatch$clientCrashCount = 0;
    @Unique
    private int crashpatch$serverCrashCount = 0;
    @Unique
    private boolean crashpatch$recoveredFromCrash = false;

    @Override
    public boolean hasRecoveredFromCrash() {
        return crashpatch$recoveredFromCrash;
    }

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
                if (!this.hasCrashed || this.crashReporter == null) {
                    try {
                        runGameLoop();
                    } catch (ReportedException e) {
                        crashpatch$clientCrashCount++;
                        addGraphicsAndWorldToCrashReport(e.getCrashReport());
                        crashpatch$addInfoToCrash(e.getCrashReport());
                        crashpatch$resetGameState();
                        this.logger.fatal("Reported exception thrown!", e);
                        crashpatch$displayCrashScreen(e.getCrashReport());
                    } catch (Throwable e) {
                        crashpatch$clientCrashCount++;
                        CrashReport report = new CrashReport("Unexpected error", e);
                        addGraphicsAndWorldToCrashReport(report);
                        crashpatch$addInfoToCrash(report);
                        crashpatch$resetGameState();
                        this.logger.fatal("Unreported exception thrown!", e);
                        crashpatch$displayCrashScreen(report);
                    }
                } else {
                    crashpatch$serverCrashCount++;
                    crashpatch$addInfoToCrash(this.crashReporter);
                    freeMemory();
                    crashpatch$displayCrashScreen(this.crashReporter);
                    this.hasCrashed = false;
                    this.crashReporter = null;
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
        if (!CrashPatchConfig.INSTANCE.getInGameCrashPatch()) {
            crashpatch$letDie = true;
        }

        if ((crashpatch$clientCrashCount >= CrashPatchConfig.INSTANCE.getCrashLimit() || crashpatch$serverCrashCount >= CrashPatchConfig.INSTANCE.getCrashLimit())) {
            this.logger.error("Crash limit reached, exiting game");
            crashpatch$letDie = true;
        }
        displayCrashReport(report);
        crashpatch$recoveredFromCrash = true;
        try {

            // Reset hasCrashed, debugCrashKeyPressTime, and crashIntegratedServerNextTick
            this.hasCrashed = false;
            this.debugCrashKeyPressTime = -1;

            // Vanilla does this when switching to main menu but not our custom crash screen
            // nor the out of memory screen (see https://bugs.mojang.com/browse/MC-128953)
            this.gameSettings.showDebugInfo = false;

            // Display the crash screen
//            crashpatch$runGUILoop(new GuiCrashScreen(report));
            displayGuiScreen(new CrashUI(report).create());
        } catch (Throwable t) {
            // The crash screen has crashed. Report it normally instead.
            this.logger.error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            displayCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    private void crashpatch$addInfoToCrash(CrashReport crashReport) {
        crashReport.getCategory().addCrashSectionCallable("Client Crashes Since Restart", () -> String.valueOf(crashpatch$clientCrashCount));
        crashReport.getCategory().addCrashSectionCallable("Integrated Server Crashes Since Restart", () -> String.valueOf(crashpatch$serverCrashCount));
    }

    public void crashpatch$resetGameState() {
        crashpatch$resetGameState(false);
    }

    /**
     * @author Runemoro
     */
    public void crashpatch$resetGameState(boolean freeingMemory) {
        try {
            // Free up memory such that this works properly in case of an OutOfMemoryError
            int originalMemoryReserveSize = -1;
            try { // In case another mod actually deletes the memoryReserve field
                if (this.memoryReserve != null) {
                    originalMemoryReserveSize = this.memoryReserve.length;
                    this.memoryReserve = new byte[0];
                }
            } catch (Throwable ignored) {
            }

            GameStateManager.resetStates();

            boolean shouldCrash = crashpatch$clientCrashCount >= CrashPatchConfig.INSTANCE.getLeaveLimit() || crashpatch$serverCrashCount >= CrashPatchConfig.INSTANCE.getLeaveLimit();

            if (shouldCrash && !freeingMemory) {
                this.logger.error("Crash limit reached, exiting world");
                CrashUI.Companion.setLeaveWorldCrash(true);
            }

            if (shouldCrash || freeingMemory
                    //#if MC > 1.12
                    //$$ || true
                    //#endif
            ) {
                if (getNetHandler() != null) {
                    getNetHandler().getNetworkManager().closeChannel(new ChatComponentText("[CrashPatch] Client crashed"));
                }
                loadWorld(null);

                if (this.entityRenderer.isShaderActive()) {
                    this.entityRenderer.stopUseShader();
                }

                this.scheduledTasks.clear(); // TODO: Figure out why this isn't necessary for vanilla disconnect
            }

            RenderState.reset();

            if (originalMemoryReserveSize != -1) {
                try {
                    this.memoryReserve = new byte[originalMemoryReserveSize];
                } catch (Throwable ignored) {
                }
            }
            System.gc();
        } catch (Throwable t) {
            this.logger.error("Failed to reset state after a crash", t);
            try {
                GameStateManager.INSTANCE.resetStates();
                RenderState.reset();
            } catch (Throwable ignored) {}
        }
    }

    /**
     * @reason Disconnect from the current world and free memory, using a memory reserve
     * to make sure that an OutOfMemory doesn't happen while doing this.
     * <p>
     * Bugs Fixed:
     * - https://bugs.mojang.com/browse/MC-128953
     * - Memory reserve not recreated after out-of memory
     * @author Runemoro
     */
    @Overwrite
    public void freeMemory() {
        crashpatch$resetGameState(true);
    }

    /**
     * @author Runemoro
     */
    public void crashpatch$displayInitErrorScreen(CrashReport report) {
        if (!CrashPatchConfig.INSTANCE.getInitCrashPatch()) {
            crashpatch$letDie = true;
        }
        displayCrashReport(report);
        try {
            this.mcResourceManager = new SimpleReloadableResourceManager(this.metadataSerializer_);
            this.renderEngine = new TextureManager(this.mcResourceManager);
            this.mcResourceManager.registerReloadListener(this.renderEngine);

            this.mcLanguageManager = new LanguageManager(this.metadataSerializer_, this.gameSettings.language);
            this.mcResourceManager.registerReloadListener(this.mcLanguageManager);

            refreshResources(); // TODO: Why is this necessary?
            this.fontRendererObj = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii.png"), this.renderEngine, false);
            this.mcResourceManager.registerReloadListener(this.fontRendererObj);

            this.mcSoundHandler = new SoundHandler(this.mcResourceManager, this.gameSettings);
            this.mcResourceManager.registerReloadListener(this.mcSoundHandler);

            //try { // this is necessary for some GUI stuff. if it works, cool, if not, it's not a big deal
            //    //EventManager.INSTANCE.register(Notifications.INSTANCE);
            //    GuiUtils.getDeltaTime(); // make sure static initialization is called
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
            //todo do we need a polyui equivalent

            this.running = true;
            try {
                //#if FORGE
                //noinspection deprecation
                SplashProgress.pause();// Disable the forge splash progress screen
                //#endif
                GlStateManager.disableTexture2D();
                GlStateManager.enableTexture2D();
            } catch (Throwable ignored) {
            }
            crashpatch$runGUILoop(new CrashUI(report, CrashUI.GuiType.INIT));
        } catch (Throwable t) {
            if (!crashpatch$letDie) {
                this.logger.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
                crashpatch$letDie = true;
            }
            displayCrashReport(report);
        }
    }

    /**
     * @author Runemoro
     */
    private void crashpatch$runGUILoop(CrashUI crashUI) throws Throwable {
        GuiScreen screen = crashUI.create();
        displayGuiScreen(screen);
        while (this.running && this.currentScreen != null) {
            if (Display.isCreated() && Display.isCloseRequested()) {
                System.exit(0);
            }
            //EventManager.INSTANCE.post(new RenderEvent.Start()); todo
            this.leftClickCounter = 10000;
            this.currentScreen.handleInput();
            this.currentScreen.updateScreen();

            GlStateManager.pushMatrix();
            GlStateManager.clear(16640);
            this.framebufferMc.bindFramebuffer(true);
            GlStateManager.enableTexture2D();

            GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);

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
            int mouseX = Mouse.getX() * width / this.displayWidth;
            int mouseY = height - Mouse.getY() * height / this.displayHeight - 1;
            Gui.drawRect(0, 0, width, height, Color.WHITE.getRGB()); // DO NOT REMOVE THIS! FOR SOME REASON NANOVG DOESN'T RENDER WITHOUT IT
            this.currentScreen.drawScreen(mouseX, mouseY, 0);
            if (crashUI.getShouldCrash()) {
                crashpatch$letDie = true;
                throw Objects.requireNonNull(crashUI.getThrowable());
            }

            this.framebufferMc.unbindFramebuffer();
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);
            GlStateManager.popMatrix();

            //EventManager.INSTANCE.post(new RenderEvent(Stage.END, 0)); todo

            updateDisplay();
            Thread.yield();
            Display.sync(60);
            checkGLError("CrashPatch GUI Loop");
        }
    }

    //#if FORGE
    @Redirect(method = "displayCrashReport", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;handleExit(I)V"))
    public void redirect(FMLCommonHandler instance, int code) {
        if (crashpatch$letDie) {
            instance.handleExit(code);
        }
    }
    //#endif


}

//#endif