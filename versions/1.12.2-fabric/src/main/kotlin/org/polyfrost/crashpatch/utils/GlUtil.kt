package org.polyfrost.crashpatch.utils

import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.render.DiffuseLighting
import org.lwjgl.opengl.*

// I hate Legacy Yarn mappings..
// This should be better once we switch to Ornithe/Feather mappings, but
// if you want to see the original MCP code, go to:
// https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/utils/GlUtil.java
object GlUtil {

    fun resetState() {

        // Clear matrix stack
        GlStateManager.matrixMode(GL11.GL_MODELVIEW)
        GlStateManager.loadIdentity()
        GlStateManager.matrixMode(GL11.GL_PROJECTION)
        GlStateManager.loadIdentity()
        GlStateManager.matrixMode(GL11.GL_TEXTURE)
        GlStateManager.loadIdentity()
        GlStateManager.matrixMode(GL11.GL_COLOR)
        GlStateManager.loadIdentity()


        // Clear attribute stacks TODO: Broken, a stack underflow breaks LWJGL
        // try {
        //     do GL11.glPopAttrib(); while (GlStateManager.glGetError() == 0);
        // } catch (Throwable ignored) {}
        //
        // try {
        //     do GL11.glPopClientAttrib(); while (GlStateManager.glGetError() == 0);
        // } catch (Throwable ignored) {}

        // Reset texture
        GlStateManager.bindTexture(0)
        GlStateManager.disableTexture()


        // Reset GL lighting
        GlStateManager.disableLighting()
        GlStateManager.method_12282(GL11.GL_LIGHT_MODEL_AMBIENT, DiffuseLighting.method_845(0.2f, 0.2f, 0.2f, 1.0f))
        for (i in 0..7) {
            GlStateManager.disableLight(i)
            GlStateManager.method_12281(
                GL11.GL_LIGHT0 + i,
                GL11.GL_AMBIENT,
                DiffuseLighting.method_845(0.0f, 0.0f, 0.0f, 1.0f)
            )
            GlStateManager.method_12281(
                GL11.GL_LIGHT0 + i,
                GL11.GL_POSITION,
                DiffuseLighting.method_845(0.0f, 0.0f, 1.0f, 0.0f)
            )

            if (i == 0) {
                GlStateManager.method_12281(
                    GL11.GL_LIGHT0 + i,
                    GL11.GL_DIFFUSE,
                    DiffuseLighting.method_845(1.0f, 1.0f, 1.0f, 1.0f)
                )
                GlStateManager.method_12281(
                    GL11.GL_LIGHT0 + i,
                    GL11.GL_SPECULAR,
                    DiffuseLighting.method_845(1.0f, 1.0f, 1.0f, 1.0f)
                )
            } else {
                GlStateManager.method_12281(
                    GL11.GL_LIGHT0 + i,
                    GL11.GL_DIFFUSE,
                    DiffuseLighting.method_845(0.0f, 0.0f, 0.0f, 1.0f)
                )
                GlStateManager.method_12281(
                    GL11.GL_LIGHT0 + i,
                    GL11.GL_SPECULAR,
                    DiffuseLighting.method_845(0.0f, 0.0f, 0.0f, 1.0f)
                )
            }
        }
        GlStateManager.disableColorMaterial()
        GlStateManager.colorMaterial(1032, 5634)


        // Reset depth
        GlStateManager.disableDepthTest()
        GlStateManager.depthFunc(513)
        GlStateManager.depthMask(true)


        // Reset blend mode
        GlStateManager.disableBlend()
        GlStateManager.method_12287(GlStateManager.class_2870.ONE, GlStateManager.class_2866.ZERO)
        GlStateManager.method_12288(
            GlStateManager.class_2870.ONE,
            GlStateManager.class_2866.ZERO,
            GlStateManager.class_2870.ONE,
            GlStateManager.class_2866.ZERO
        )
        GlStateManager.method_12305(GL14.GL_FUNC_ADD)


        // Reset fog
        GlStateManager.disableFog()
        GlStateManager.method_12285(GlStateManager.class_2867.LINEAR)
        GlStateManager.fogDensity(1.0f)
        GlStateManager.fogStart(0.0f)
        GlStateManager.fogEnd(1.0f)
        GlStateManager.method_12298(GL11.GL_FOG_COLOR, DiffuseLighting.method_845(0.0f, 0.0f, 0.0f, 0.0f))
        if (GLContext.getCapabilities().GL_NV_fog_distance) GlStateManager.method_12300(GL11.GL_FOG_MODE, 34140)


        // Reset polygon offset
        GlStateManager.polygonOffset(0.0f, 0.0f)
        GlStateManager.disablePolyOffset()


        // Reset color logic
        GlStateManager.disableColorLogic()
        GlStateManager.logicOp(5379)


        // Reset texgen TODO: is this correct?
        GlStateManager.disableTexCoord(GlStateManager.TexCoord.S)
        GlStateManager.disableTexCoord(GlStateManager.TexCoord.T)
        GlStateManager.disableTexCoord(GlStateManager.TexCoord.R)
        GlStateManager.disableTexCoord(GlStateManager.TexCoord.Q)
        GlStateManager.genTex(GlStateManager.TexCoord.S, 9216)
        GlStateManager.genTex(GlStateManager.TexCoord.T, 9216)
        GlStateManager.genTex(GlStateManager.TexCoord.R, 9216)
        GlStateManager.genTex(GlStateManager.TexCoord.Q, 9216)
        GlStateManager.genTex(GlStateManager.TexCoord.S, 9474, DiffuseLighting.method_845(1.0f, 0.0f, 0.0f, 0.0f))
        GlStateManager.genTex(GlStateManager.TexCoord.T, 9474, DiffuseLighting.method_845(0.0f, 1.0f, 0.0f, 0.0f))
        GlStateManager.genTex(GlStateManager.TexCoord.R, 9474, DiffuseLighting.method_845(0.0f, 0.0f, 1.0f, 0.0f))
        GlStateManager.genTex(GlStateManager.TexCoord.Q, 9474, DiffuseLighting.method_845(0.0f, 0.0f, 0.0f, 1.0f))
        GlStateManager.genTex(GlStateManager.TexCoord.S, 9217, DiffuseLighting.method_845(1.0f, 0.0f, 0.0f, 0.0f))
        GlStateManager.genTex(GlStateManager.TexCoord.T, 9217, DiffuseLighting.method_845(0.0f, 1.0f, 0.0f, 0.0f))
        GlStateManager.genTex(GlStateManager.TexCoord.R, 9217, DiffuseLighting.method_845(0.0f, 0.0f, 1.0f, 0.0f))
        GlStateManager.genTex(GlStateManager.TexCoord.Q, 9217, DiffuseLighting.method_845(0.0f, 0.0f, 0.0f, 1.0f))


        // Disable lightmap
        GlStateManager.activeTexture(GLX.lightmapTextureUnit)
        GlStateManager.disableTexture()

        GlStateManager.activeTexture(GLX.textureUnit)


        // Reset texture parameters
        GlStateManager.method_12294(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GlStateManager.method_12294(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR)
        GlStateManager.method_12294(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GlStateManager.method_12294(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        GlStateManager.method_12294(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1000)
        GlStateManager.method_12294(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 1000)
        GlStateManager.method_12294(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, -1000)
        GlStateManager.method_12293(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0f)

        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
        GlStateManager.method_12297(
            GL11.GL_TEXTURE_ENV,
            GL11.GL_TEXTURE_ENV_COLOR,
            DiffuseLighting.method_845(0.0f, 0.0f, 0.0f, 0.0f)
        )
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL15.GL_SRC0_RGB, GL11.GL_TEXTURE)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL15.GL_SRC1_RGB, GL13.GL_PREVIOUS)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL15.GL_SRC2_RGB, GL13.GL_CONSTANT)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL15.GL_SRC0_ALPHA, GL11.GL_TEXTURE)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL15.GL_SRC1_ALPHA, GL13.GL_PREVIOUS)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL15.GL_SRC2_ALPHA, GL13.GL_CONSTANT)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA)
        GlStateManager.method_12274(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA)
        GlStateManager.method_12273(GL11.GL_TEXTURE_ENV, GL13.GL_RGB_SCALE, 1.0f)
        GlStateManager.method_12273(GL11.GL_TEXTURE_ENV, GL11.GL_ALPHA_SCALE, 1.0f)

        GlStateManager.disableNormalize()
        GlStateManager.shadeModel(7425)
        GlStateManager.disableRescaleNormal()
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.clearDepth(1.0)
        GlStateManager.method_12304(1.0f)
        GlStateManager.method_12272(0.0f, 0.0f, 1.0f)
        GlStateManager.method_12306(GL11.GL_FRONT, GL11.GL_FILL)
        GlStateManager.method_12306(GL11.GL_BACK, GL11.GL_FILL)

        GlStateManager.enableTexture()
        GlStateManager.shadeModel(7425)
        GlStateManager.clearDepth(1.0)
        GlStateManager.enableDepthTest()
        GlStateManager.depthFunc(515)
        GlStateManager.enableAlphaTest()
        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.method_12284(GlStateManager.class_2865.BACK)
        GlStateManager.matrixMode(5889)
        GlStateManager.loadIdentity()
        GlStateManager.matrixMode(5888)
    }
}