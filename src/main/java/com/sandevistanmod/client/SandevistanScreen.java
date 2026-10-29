package com.sandevistanmod.client;

import com.sandevistanmod.data.SandevistanData;
import com.sandevistanmod.data.Tx;
import com.sandevistanmod.net.ActionPayload;
import com.sandevistanmod.registry.ModRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Aba J: os implantes do jogador (so visual), a Carga Cibernetica, os chips e os botoes do HUD. */
public class SandevistanScreen extends Screen {
    private Button armBtn;

    public SandevistanScreen() {
        super(Tx.t("implants.title"));
    }

    private int cx() {
        return this.width / 2;
    }

    private int y0() {
        return this.height / 2 - 96;
    }

    private int lx() {
        return cx() - 100;
    }

    @Override
    protected void init() {
        int bx = cx() + 16;
        int y = y0();
        armBtn = addRenderableWidget(Button.builder(armLabel(), b -> PacketDistributor.sendToServer(new ActionPayload(4)))
                .bounds(bx, y + 100, 176, 20).build());
        addRenderableWidget(Button.builder(cornerLabel(), b -> {
                    ClientLayout.nextCorner();
                    b.setMessage(cornerLabel());
                })
                .bounds(bx, y + 122, 176, 20).build());
        addRenderableWidget(Button.builder(musicLabel(), b -> {
                    ClientLayout.nextMusic();
                    b.setMessage(musicLabel());
                })
                .bounds(bx, y + 144, 176, 20).build());
        addRenderableWidget(Button.builder(Tx.t("btn.close"), b -> onClose())
                .bounds(bx, y + 166, 176, 20).build());
    }

    private static Component armLabel() {
        return ClientData.DATA.armsVisible ? Tx.t("arms.visible") : Tx.t("arms.hidden");
    }

    private static Component musicLabel() {
        return Tx.t("btn.music", Tx.t("music." + ClientLayout.music));
    }

    private static Component cornerLabel() {
        return Tx.t("btn.corner", Tx.t("corner." + ClientLayout.corner));
    }

    @Override
    public void tick() {
        if (armBtn != null) {
            armBtn.visible = ClientData.DATA.arms() > 0;
            armBtn.setMessage(armLabel());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeys.SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---- posicoes dos encaixes ----
    private int slotX(int slot) {
        return switch (slot) {
            case SandevistanData.SLOT_BACK, SandevistanData.SLOT_ARM_R -> lx() - 80;
            default -> lx() + 62;
        };
    }

    private int slotY(int slot) {
        return switch (slot) {
            case SandevistanData.SLOT_BACK, SandevistanData.SLOT_NEURAL -> y0() + 26;
            default -> y0() + 62;
        };
    }

    private int chipX() {
        return cx() + 16;
    }

    private int chipY(int i) {
        return y0() + 54 + i * 22;
    }

    private static boolean in(double mx, double my, int x, int y) {
        return mx >= x && mx < x + 18 && my >= y && my < y + 18;
    }

    private static ItemStack slotStack(int slot) {
        return new ItemStack(switch (slot) {
            case SandevistanData.SLOT_BACK -> ModRegistries.SANDEVISTAN_ITEM.get();
            case SandevistanData.SLOT_ARM_R, SandevistanData.SLOT_ARM_L -> ModRegistries.BRACO_FERRO.get();
            default -> ModRegistries.LINK_NEURAL.get();
        });
    }

    private static ItemStack chipStack(SandevistanData d, int i) {
        if (d.chip[i] == 0) return ItemStack.EMPTY;
        ItemStack s = new ItemStack(ModRegistries.CHIPS.get(d.chip[i] - 1).get());
        s.setDamageValue(d.chipDmg[i]);
        return s;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = 0; i < 2; i++) {
                if (ClientData.DATA.chip[i] != 0 && in(mx, my, chipX(), chipY(i))) {
                    PacketDistributor.sendToServer(new ActionPayload(2 + i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private static final String[] SLOT_KEYS = {"slot.back", "slot.armr", "slot.arml", "slot.neural"};

    @Override
    public void renderBackground(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gg, mouseX, mouseY, partialTick);
        SandevistanData d = ClientData.DATA;
        int cx = cx();
        int y0 = y0();
        gg.fill(cx - 200, y0, cx + 200, y0 + 192, 0xC0101410);
        gg.fill(cx - 200, y0, cx + 200, y0 + 2, 0xFF39FF14);
        gg.fill(cx + 5, y0 + 8, cx + 6, y0 + 184, 0x40FFFFFF);
        gg.drawCenteredString(this.font, Tx.t("implants.title"), lx(), y0 + 7, 0x39FF14);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(gg, lx() - 45, y0 + 18, lx() + 45, y0 + 112, 40,
                    0.0625f, mouseX, mouseY, mc.player);
        }
        for (int s = 0; s < SandevistanData.SLOT_COUNT; s++) {
            int x = slotX(s);
            int y = slotY(s);
            boolean on = d.has(s);
            gg.fill(x - 1, y - 1, x + 19, y + 19, on ? 0xFF39FF14 : 0xFF666666);
            gg.fill(x, y, x + 18, y + 18, 0xFF1A1E22);
            if (on) gg.renderItem(slotStack(s), x + 1, y + 1);
            gg.drawCenteredString(this.font, Tx.t(SLOT_KEYS[s]), x + 9, y + 22, on ? 0xCCCCCC : 0x888888);
        }

        // Carga Cibernetica
        float cap = d.capacity();
        int bx = cx + 16;
        gg.drawCenteredString(this.font, Tx.t("load.title", Math.round(cap)), cx + 104, y0 + 7,
                cap >= 50f ? 0xFFAA33 : 0x88FFAA);
        ClientOverlay.bar(gg, bx, y0 + 20, 176, 9, cap / 100f, ClientOverlay.loadColor(cap));
        gg.fill(bx + 88, y0 + 18, bx + 89, y0 + 31, 0xFFFFFFFF);
        gg.drawWordWrap(this.font, Tx.t("load.hint"), bx, y0 + 34, 176, 0x888888);

        // chips
        for (int i = 0; i < 2; i++) {
            int x = chipX();
            int y = chipY(i);
            boolean has = d.chip[i] != 0;
            gg.fill(x - 1, y - 1, x + 19, y + 19, has ? 0xFF55DDFF : 0xFF666666);
            gg.fill(x, y, x + 18, y + 18, 0xFF1A1E22);
            if (has) {
                ItemStack st = chipStack(d, i);
                gg.renderItem(st, x + 1, y + 1);
                gg.renderItemDecorations(this.font, st, x + 1, y + 1);
                gg.drawString(this.font, st.getHoverName(), x + 24, y + 1, 0xFFFFFF);
                if (d.chipLoad[i] > 0f) {
                    gg.drawString(this.font, Tx.t("chip.dl", (int) Math.ceil(d.chipLoad[i])), x + 24, y + 10, 0x55DDFF);
                } else if (d.chipDmg[i] >= SandevistanData.chipMax(d.chip[i])) {
                    gg.drawString(this.font, Tx.t("chip.brokenshort"), x + 24, y + 10, 0xFF5555);
                } else {
                    gg.drawString(this.font, Tx.t("chip.working"), x + 24, y + 10, 0x55FF55);
                }
            } else {
                gg.drawString(this.font, d.neural ? Tx.t("chip.none") : Tx.t("chip.noneural"), x + 24, y + 5, 0x888888);
            }
        }
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.render(gg, mouseX, mouseY, partialTick);
        SandevistanData d = ClientData.DATA;
        for (int s = 0; s < SandevistanData.SLOT_COUNT; s++) {
            if (d.has(s) && in(mouseX, mouseY, slotX(s), slotY(s))) {
                gg.renderTooltip(this.font, slotStack(s), mouseX, mouseY);
                return;
            }
        }
        for (int i = 0; i < 2; i++) {
            if (d.chip[i] != 0 && in(mouseX, mouseY, chipX(), chipY(i))) {
                gg.renderTooltip(this.font, chipStack(d, i), mouseX, mouseY);
                return;
            }
        }
    }
}
