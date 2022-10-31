package com.github.alexthe668.domesticationinnovation.client.render;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class DIRenderTypes extends RenderType {
    protected static final RenderStateShard.TexturingStateShard IFRAME_TEXTURING = new RenderStateShard.TexturingStateShard("entity_glint_texturing", () -> {
        setupIframeShading(3, 7L);
    }, () -> {
        RenderSystem.resetTextureMatrix();
    });

    protected static final RenderStateShard.TexturingStateShard SHADOW_HAND_TEXTURING = new RenderStateShard.TexturingStateShard("entity_glint_texturing", () -> {
        setupShadowHandShading(0.5F, 2L);
    }, () -> {
        RenderSystem.resetTextureMatrix();
    });

    protected static final RenderStateShard.TexturingStateShard PSYCHIC_WALL_TEXTURING = new RenderStateShard.TexturingStateShard("entity_glint_texturing", () -> {
        setupPsychicWallTexturing(0.5F, 40L);
    }, () -> {
        RenderSystem.resetTextureMatrix();
    });


    public static final RenderType IFRAME_GLINT = create("iframe_glint", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(DomesticationMod.MODID + ":textures/immunity_frame_overlay.png"), true, false)).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(IFRAME_TEXTURING).setOverlayState(OVERLAY).createCompositeState(true));
    public static final RenderType VOID_CLOUD = create("void_cloud", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_END_PORTAL_SHADER).setTextureState(RenderStateShard.MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false, false).add(new ResourceLocation(DomesticationMod.MODID + ":textures/void_cloud.png"), false, false).build()).setLightmapState(LIGHTMAP).createCompositeState(false));
    public static final RenderType SHADOW_HAND_ENTITY = create("shadow_hand_entity", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(DomesticationMod.MODID + ":textures/shadow_hand.png"), false, false)).setWriteMaskState(COLOR_DEPTH_WRITE).setTexturingState(SHADOW_HAND_TEXTURING).setLightmapState(LIGHTMAP).setCullState(RenderStateShard.NO_CULL).createCompositeState(false));
    public static final RenderType PSYCHIC_WALL = create("psychic_wall2", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(DomesticationMod.MODID + ":textures/psychic_wall_overlay.png"), false, false)).setTexturingState(PSYCHIC_WALL_TEXTURING).setLightmapState(LIGHTMAP).setCullState(RenderStateShard.NO_CULL).setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST).createCompositeState(true));
    public static final RenderType PSYCHIC_WALL_BORDER = create("psychic_wall2", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(DomesticationMod.MODID + ":textures/psychic_wall_border.png"), true, false)).setLightmapState(LIGHTMAP).setCullState(RenderStateShard.NO_CULL).setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST).createCompositeState(true));
    public static final RenderType HEALING_AURA = create("healing_aura", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(DomesticationMod.MODID + ":textures/healing_aura.png"), true, false)).setLightmapState(LIGHTMAP).setCullState(RenderStateShard.NO_CULL).setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY).setWriteMaskState(COLOR_DEPTH_WRITE).setDepthTestState(LEQUAL_DEPTH_TEST).createCompositeState(true));

    public static final RenderType TRANSLUCENT_NO_CULL = translucentNoCull();

    public DIRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    public static RenderType getZombieOverlay(ResourceLocation texture, int x, int y) {
        return create("zombie_overlay", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(DomesticationMod.MODID + ":textures/zombie_overlay.png"), false, false)).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setTexturingState(new ZombieTexturing("zombie", x, y)).setOverlayState(OVERLAY).createCompositeState(true));
    }

    private static RenderType translucentNoCull(){
        RenderType type = create("translucent_no_cull", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_TRANSLUCENT_SHADER).setCullState(NO_CULL).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(TRANSLUCENT_TARGET).createCompositeState(true));
        return type;
    }

    private static void setupShadowHandShading(float in, long time) {
        long i = Util.getMillis() * time;
        float f = (float) (i % 110000L) / 110000.0F;
        float f1 = (float) (i % 30000L) / 30000.0F;
        Matrix4f matrix4f = Matrix4f.createTranslateMatrix(0.0F, -f1, 0.0F);
        matrix4f.multiply(Matrix4f.createScaleMatrix(in, in, in));
        RenderSystem.setTextureMatrix(matrix4f);
    }

    private static void setupIframeShading(float in, long time) {
        long i = Util.getMillis() * time;
        float f1 = (float) (i % 30000L) / 30000.0F;
        Matrix4f matrix4f = Matrix4f.createTranslateMatrix(0.0F, f1, 0.0F);
        matrix4f.multiply(Vector3f.ZP.rotationDegrees(45.0F));
        matrix4f.multiply(Matrix4f.createScaleMatrix(in, in, in));
        RenderSystem.setTextureMatrix(matrix4f);
    }

    private static void setupPsychicWallTexturing(float in, long time) {
        long i = Util.getMillis() * 2;
        float f1 = (float) (i % 30000L) / 30000.0F;
        float f2 = (float) Math.sin(i / 20000.0F) + 1.0F;
        float f3 = (float) Math.cos(i / 20000.0F) + 1.0F;
        Matrix4f matrix4f = Matrix4f.createTranslateMatrix(1 + f2, 1 + f3, 0.0F);
        matrix4f.multiply(Matrix4f.createScaleMatrix(in, in, in));
        RenderSystem.setTextureMatrix(matrix4f);
    }

    private static class ZombieTexturing extends TexturingStateShard {

        public ZombieTexturing(String name, int x, int y) {
            super(name, () -> setupZombieTexturing(x, y), () -> RenderSystem.resetTextureMatrix());
        }

        private static void setupZombieTexturing(int x, int y) {
            Matrix4f matrix4f = Matrix4f.createTranslateMatrix(0.0F, 0.0F, 0.0F);
            matrix4f.multiply(Matrix4f.createScaleMatrix(x / 64F, y / 64F, 1));
            RenderSystem.setTextureMatrix(matrix4f);
        }
    }
}
