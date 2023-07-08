package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine;

import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class SlotMachineRenderBlock {

    public abstract void render(@Nonnull EasyGuiGraphics gui, int x, int y);
    public final int weight;
    protected SlotMachineRenderBlock(int weight) { this.weight = weight; }

    public static SlotMachineRenderBlock empty() { return Empty.INSTANCE; }
    public static SlotMachineRenderBlock forItem(int weight, ItemStack item) { return new ItemBlock(weight, item); }

    private static class ItemBlock extends SlotMachineRenderBlock
    {
        private final ItemStack item;
        protected ItemBlock(int weight, ItemStack item) { super(weight); this.item = item.copy(); }
        @Override
        public void render(@Nonnull EasyGuiGraphics gui, int x, int y) { gui.renderItem(this.item, x, y); }
    }

    private static class Empty extends SlotMachineRenderBlock
    {
        protected static final SlotMachineRenderBlock INSTANCE = new Empty();
        private Empty() { super(0); }
        @Override
        public void render(@Nonnull EasyGuiGraphics gui, int x, int y) { gui.blit(IconAndButtonUtil.ICON_TEXTURE, x, y, 16, 32, 16, 16); }
    }

}
