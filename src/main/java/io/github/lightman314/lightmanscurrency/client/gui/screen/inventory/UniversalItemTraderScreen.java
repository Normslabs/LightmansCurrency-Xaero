package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.ItemTraderUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageExecuteTrade;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.menus.UniversalItemTraderMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class UniversalItemTraderScreen extends AbstractContainerScreen<UniversalItemTraderMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	Button buttonShowStorage;
	Button buttonCollectMoney;
	Button buttonBack;
	
	protected List<ItemTradeButton> tradeButtons = new ArrayList<>();
	
	public UniversalItemTraderScreen(UniversalItemTraderMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.imageHeight = 133 + ItemTraderUtil.getTradeDisplayHeight(this.menu.getData());
		this.imageWidth = ItemTraderUtil.getWidth(this.menu.getData());
	}
	
	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
	{
		
		ItemTraderScreen.drawTraderBackground(poseStack, this, this.menu, this.minecraft, this.imageWidth, this.imageHeight, this.menu.getData());
		
	}
	
	@Override
	protected void renderLabels(PoseStack matrix, int mouseX, int mouseY)
	{
		
		ItemTraderScreen.drawTraderForeground(matrix, this.font, this.menu.getData(), this.imageHeight,
				this.menu.getData().getTitle(),
				this.playerInventoryTitle,
				new TranslatableComponent("tooltip.lightmanscurrency.credit", MoneyUtil.getStringOfValue(this.menu.GetCoinValue())));
		
	}
	
	@Override
	protected void init()
	{
		super.init();
		
		int tradeOffset = ItemTraderUtil.getTradeDisplayOffset(this.menu.getData());
		int inventoryOffset = ItemTraderUtil.getInventoryDisplayOffset(this.menu.getData());
		
		this.buttonBack = this.addRenderableWidget(new IconButton(this.leftPos -20 + inventoryOffset, this.topPos + this.imageHeight - 20, this::PressBackButton, this.font, IconData.of(GUI_TEXTURE, 176 + 32, 0)));
		
		this.buttonShowStorage = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos, this::PressStorageButton, this.font, IconData.of(GUI_TEXTURE, 176, 0)));
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE);
		
		this.buttonCollectMoney = this.addRenderableWidget(new IconButton(this.leftPos - 20 + tradeOffset, this.topPos + 20, this::PressCollectionButton, this.font, IconData.of(GUI_TEXTURE, 176 + 16, 0)));
		this.buttonCollectMoney.active = false;
		this.buttonCollectMoney.visible = this.menu.hasPermission(Permissions.COLLECT_COINS) && !this.menu.getData().getCoreSettings().hasBankAccount();
		
		initTradeButtons();
		
	}
	
	protected void initTradeButtons()
	{
		int tradeCount = this.menu.getTradeCount();
		for(int i = 0; i < tradeCount; i++)
		{
			this.tradeButtons.add(this.addRenderableWidget(new ItemTradeButton(this.leftPos + ItemTraderUtil.getButtonPosX(this.menu.getData(), i), this.topPos + ItemTraderUtil.getButtonPosY(this.menu.getData(), i), this::PressTradeButton, i, this, this.font, () -> this.menu.getData(), this.menu::GetCoinValue, this.menu::GetItemInventory)));
		}
	}
	
	@Override
	public void containerTick()
	{
		this.menu.tick();
		
		this.buttonShowStorage.visible = this.menu.hasPermission(Permissions.OPEN_STORAGE);
		
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			this.buttonCollectMoney.visible = !this.menu.getData().getCoreSettings().hasBankAccount();
			this.buttonCollectMoney.active = this.menu.getData().getStoredMoney().getRawValue() > 0;
			if(!this.buttonCollectMoney.active)
				this.buttonCollectMoney.visible = !this.menu.getData().getCoreSettings().isCreative();
		}
		else
			this.buttonCollectMoney.visible = false;
		
		
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX,  mouseY);
		
		if(this.buttonShowStorage != null && this.buttonShowStorage.isMouseOver(mouseX,mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.openstorage"), mouseX, mouseY);
		}
		else if(this.buttonCollectMoney != null && this.buttonCollectMoney.active && this.buttonCollectMoney.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.collectcoins", this.menu.getData().getStoredMoney().getString()), mouseX, mouseY);
		}
		else if(this.buttonBack != null && this.buttonBack.active && this.buttonBack.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslatableComponent("tooltip.lightmanscurrency.trader.universaltrader.back"), mouseX, mouseY);
		}
		for(int i = 0; i < this.tradeButtons.size(); i++)
		{
			this.tradeButtons.get(i).tryRenderTooltip(matrixStack, this, this.menu.getData(), false, mouseX, mouseY);
		}
		
	}
	
	private void PressStorageButton(Button button)
	{
		//Open the container screen
		if(this.menu.hasPermission(Permissions.OPEN_STORAGE))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to open the Trader's Storage.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.menu.getData().getTraderID()));
		}
	}
	
	private void PressCollectionButton(Button button)
	{
		//Open the container screen
		if(this.menu.hasPermission(Permissions.COLLECT_COINS))
		{
			//CurrencyMod.LOGGER.info("Owner attempted to collect the stored money.");
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
		}
	}
	
	private void PressTradeButton(Button button)
	{
		
		int tradeIndex = 0;
		if(tradeButtons.contains(button))
			tradeIndex = tradeButtons.indexOf(button);
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageExecuteTrade(tradeIndex));
		
	}
	
	private void PressBackButton(Button button)
	{
		this.minecraft.setScreen(new TradingTerminalScreen());
	}
	
}
