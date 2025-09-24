package org.polyfrost.crashpatch.utils

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL14

// Replaced in 1.12.2-fabric source set, removed in 1.16 and later
object GlUtil {

    fun resetState() {
        GlStateManager.bindTexture(0)
        GlStateManager.disableTexture2D()

        // Reset depth
        GlStateManager.disableDepth()
        GlStateManager.depthFunc(513)
        GlStateManager.depthMask(true)

        // Reset blend mode
        GlStateManager.disableBlend()
        GlStateManager.blendFunc(1, 0)
        GlStateManager.tryBlendFuncSeparate(1, 0, 1, 0)
        GL14.glBlendEquation(GL14.GL_FUNC_ADD)

        // Reset polygon offset
        GlStateManager.doPolygonOffset(0.0f, 0.0f)
        GlStateManager.disablePolygonOffset()

        // Reset color logic
        GlStateManager.disableColorLogic()
        GlStateManager.colorLogicOp(5379)

        // Disable lightmap
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)

        // Reset texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1000)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 1000)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, -1000)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0f)

        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.clearDepth(1.0)
        GL11.glLineWidth(1.0f)
        GL11.glNormal3f(0.0f, 0.0f, 1.0f)
        GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL)
        GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL)
        GlStateManager.enableTexture2D()
        GlStateManager.clearDepth(1.0)
        GlStateManager.enableDepth()
        GlStateManager.depthFunc(515)
        GlStateManager.enableCull()
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }
}