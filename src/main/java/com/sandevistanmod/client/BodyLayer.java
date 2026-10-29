package com.sandevistanmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sandevistanmod.SandevistanMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

/** Bracos de ferro (blocos de ferro cobrindo o braco todo) e o Soquete de Chipware atras da orelha direita. */
public class BodyLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation IRON = ResourceLocation.withDefaultNamespace("textures/block/iron_block.png");
    private static final ResourceLocation SOCKET =
            ResourceLocation.fromNamespaceAndPath(SandevistanMod.MODID, "textures/entity/chipware_socket.png");

    public BodyLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (player.isInvisible() || player.isSpectator()) return;
        int code = ClientState.get(player.getUUID());
        boolean slim = player.getSkin().model() == PlayerSkin.Model.SLIM;
        if ((code & (1 << 13)) != 0) {
            if ((code & (1 << 10)) != 0) arm(poseStack, buffer, packedLight, getParentModel().rightArm, true, slim);
            if ((code & (1 << 11)) != 0) arm(poseStack, buffer, packedLight, getParentModel().leftArm, false, slim);
        }
        if ((code & (1 << 12)) != 0) socket(poseStack, buffer, packedLight);
    }

    /** Caixa um pouco maior que o braco (com a manga), com a textura do bloco de ferro em cada face. */
    private void arm(PoseStack ps, MultiBufferSource buffer, int light, ModelPart part, boolean right, boolean slim) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(IRON));
        ps.pushPose();
        part.translateAndRotate(ps);
        float e = 0.6f;
        float w = slim ? 3f : 4f;
        float x0 = right ? (slim ? -2f : -3f) : -1f;
        float x1 = x0 + w;
        box(ps.last(), vc, (x0 - e) / 16f, (-2f - e) / 16f, (-2f - e) / 16f, (x1 + e) / 16f, (10f + e) / 16f,
                (2f + e) / 16f, light);
        ps.popPose();
    }

    private static void box(PoseStack.Pose pose, VertexConsumer vc, float x0, float y0, float z0, float x1, float y1,
                            float z1, int light) {
        // frente (-z) e costas (+z)
        quad(pose, vc, light, 0, 0, -1, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);
        quad(pose, vc, light, 0, 0, 1, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        // lados
        quad(pose, vc, light, -1, 0, 0, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        quad(pose, vc, light, 1, 0, 0, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
        // topo (y0) e base (y1)
        quad(pose, vc, light, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(pose, vc, light, 0, 1, 0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer vc, int light, float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        v(pose, vc, light, nx, ny, nz, ax, ay, az, 0f, 1f);
        v(pose, vc, light, nx, ny, nz, bx, by, bz, 1f, 1f);
        v(pose, vc, light, nx, ny, nz, cx, cy, cz, 1f, 0f);
        v(pose, vc, light, nx, ny, nz, dx, dy, dz, 0f, 0f);
    }

    private static void v(PoseStack.Pose pose, VertexConsumer vc, int light, float nx, float ny, float nz, float x,
                          float y, float z, float u, float vv) {
        vc.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, vv)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    /** Soquete na lateral direita da cabeca (o lado -x do modelo), atras da orelha, acima da camada do chapeu. */
    private void socket(PoseStack ps, MultiBufferSource buffer, int light) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(SOCKET));
        ps.pushPose();
        getParentModel().head.translateAndRotate(ps);
        PoseStack.Pose pose = ps.last();
        float x = -4.55f / 16f;
        float yTop = -5.2f / 16f;
        float yBot = -1.4f / 16f;
        float zFront = 0.2f / 16f;
        float zBack = 4.0f / 16f;
        v(pose, vc, light, -1f, 0f, 0f, x, yTop, zFront, 0f, 0f);
        v(pose, vc, light, -1f, 0f, 0f, x, yBot, zFront, 0f, 1f);
        v(pose, vc, light, -1f, 0f, 0f, x, yBot, zBack, 1f, 1f);
        v(pose, vc, light, -1f, 0f, 0f, x, yTop, zBack, 1f, 0f);
        ps.popPose();
    }
}
