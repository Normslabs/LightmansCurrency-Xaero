package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.CPacketOpenVanilla;
import io.github.lightman314.lightmanscurrency.menus.InventoryWalletMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.WalletButton;

public class InventoryWalletScreen extends EffectRenderingInventoryScreen<InventoryWalletMenu>{

	float oldMouseX;
	float oldMouseY;
	
	public InventoryWalletScreen(InventoryWalletMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
	}
	
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.blit(matrix, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		//Hide the crafting table
		this.blit(matrix, this.leftPos + 97, this.topPos + 17, 97, 53, 74, 30);
		this.blit(matrix, this.leftPos + 97, this.topPos + 47, 97, 53, 74, 6);
		//Render the wallet slot
		this.blit(matrix, this.leftPos + InventoryWalletMenu.WALLET_SLOT_X - 1, this.topPos + InventoryWalletMenu.WALLET_SLOT_Y - 1, 7, 7, 18, 18);
		InventoryScreen.renderEntityInInventory(this.leftPos + 51, this.topPos + 75, 30, (float)(this.leftPos + 51) - this.oldMouseX, (float)(this.topPos + 75 - 50) - this.oldMouseY, this.minecraft.player);
	}
	
	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) { }
	
	@Override
	protected void init()
	{
		super.init();
		
		this.addRenderableWidget(new WalletButton(this, 26, 8, this::PressInventoryButton));
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
		
		this.oldMouseX = (float)mouseX;
		this.oldMouseY = (float)mouseY;
	}
	
	private void PressInventoryButton(Button button)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketOpenVanilla());
	}
	
}