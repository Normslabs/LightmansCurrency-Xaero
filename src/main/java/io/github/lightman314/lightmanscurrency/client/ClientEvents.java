package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ChestCoinCollectButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.wallet.VisibilityToggleButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.NotificationButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.TeamManagerButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.EjectionMenuButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.wallet.WalletButton;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketOpenWallet;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.CPacketSetVisible;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@EventBusSubscriber(modid = LightmansCurrency.MODID, value = Dist.CLIENT)
public class ClientEvents {

	public static final ResourceLocation WALLET_SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/container/wallet_slot.png");
	
	public static final KeyMapping KEY_WALLET = new KeyMapping(LCText.KEY_WALLET.getKey(), GLFW.GLFW_KEY_V, KeyMapping.CATEGORY_INVENTORY);
	public static final KeyMapping KEY_PORTABLE_TERMINAL = new KeyMapping(LCText.KEY_PORTABLE_TERMINAL.getKey(), GLFW.GLFW_KEY_BACKSLASH, KeyMapping.CATEGORY_INVENTORY);
	public static final KeyMapping KEY_PORTABLE_ATM = new KeyMapping(LCText.KEY_PORTABLE_ATM.getKey(), GLFW.GLFW_KEY_EQUAL, KeyMapping.CATEGORY_INVENTORY);
	
	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.screen instanceof WalletScreen && minecraft.player != null)
		{
			if(event.getAction() == GLFW.GLFW_PRESS && event.getKey() == KEY_WALLET.getKey().getValue())
			{
				minecraft.player.clientSideCloseContainer();
			}
		}
		else if(minecraft.player != null && minecraft.screen == null)
		{
			LocalPlayer player = minecraft.player;
			if(KEY_WALLET.isDown())
			{
				
				new CPacketOpenWallet(-1).send();

				ItemStack wallet = CoinAPI.API.getEquippedWallet(player);
				if(!wallet.isEmpty())
				{
					minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.25f + player.level().random.nextFloat() * 0.5f, 0.75f));

					if(!WalletItem.isEmpty(wallet))
						minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.COINS_CLINKING.get(), 1f, 0.4f));
				}
			}
			//Open portable terminal/atm from curios slot
			/*if(LightmansCurrency.isCuriosLoaded() && event.getAction() == GLFW.GLFW_PRESS)
			{
				if(event.getKey() == KEY_PORTABLE_TERMINAL.getKey().getValue() && LCCurios.hasPortableTerminal(minecraft.player))
					new CPacketOpenNetworkTerminal(true).send();
				else if(event.getKey() == KEY_PORTABLE_ATM.getKey().getValue() && LCCurios.hasPortableATM(minecraft.player))
					CPacketOpenATM.sendToServer();
			}//*/
		}
		
	}
	
	//Add the wallet button to the gui
	@SubscribeEvent
	public static void onInventoryGuiInit(ScreenEvent.Init.Post event)
	{

		Screen screen = event.getScreen();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
		{
			
			AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>)screen;

			//Add notification button
			event.addListener(new NotificationButton(gui));
			event.addListener(new TeamManagerButton(gui));
			event.addListener(new EjectionMenuButton(gui));

			Minecraft mc = Minecraft.getInstance();

			//Add Wallet-Related buttons if Curios doesn't exist or is somehow broken
			event.addListener(new WalletButton(gui, b -> new CPacketOpenWallet(-1).send()));

			event.addListener(new VisibilityToggleButton(gui, ClientEvents::toggleVisibility));

		}
		else if(screen instanceof ContainerScreen chestScreen)
		{
			//Add Chest Quick-Collect Button
			event.addListener(new ChestCoinCollectButton(chestScreen));
		}

	}
	
	private static void toggleVisibility(EasyButton button) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		WalletHandler handler = WalletHandler.get(player);
		boolean nowVisible = !handler.visible();
		handler.setVisible(nowVisible);
		new CPacketSetVisible(nowVisible).send();
	}
	
	//Renders empty gui slot
	@SubscribeEvent
	public static void renderInventoryScreen(ContainerScreenEvent.Render.Background event)
	{
		
		Minecraft mc = Minecraft.getInstance();
		
		AbstractContainerScreen<?> screen = event.getContainerScreen();
		
		if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
		{
			if(screen instanceof CreativeModeInventoryScreen creativeScreen && !creativeScreen.isInventoryOpen())
				return;

			EasyGuiGraphics gui = EasyGuiGraphics.create(event);
			ScreenPosition slotPosition = getWalletSlotPosition(screen instanceof CreativeModeInventoryScreen).offsetScreen(screen);
			gui.resetColor();
			//Render slot background
			gui.blit(WALLET_SLOT_TEXTURE, slotPosition.x, slotPosition.y, 0, 0, 18, 18);
		}
	}
	
	//Renders button tooltips
	@SubscribeEvent
	public static void renderInventoryTooltips(ScreenEvent.Render.Post event)
	{
		
		if(event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen)
		{
			AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)event.getScreen();
			
			if(!screen.getMenu().getCarried().isEmpty()) //Don't renderBG tooltips if the held item isn't empty
				return;
			
			if(screen instanceof CreativeModeInventoryScreen creativeScreen && !creativeScreen.isInventoryOpen())
				return;

			EasyGuiGraphics gui = EasyGuiGraphics.create(event);

			//Render notification & team manager button tooltips
			NotificationButton.tryRenderTooltip(gui);
			TeamManagerButton.tryRenderTooltip(gui);
			EjectionMenuButton.tryRenderTooltip(gui);
			
		}
		else if(event.getScreen() instanceof ContainerScreen)
		{
			ChestCoinCollectButton.tryRenderTooltip(EasyGuiGraphics.create(event), event.getMouseX(), event.getMouseY());
		}

	}
	
	public static ScreenPosition getWalletSlotPosition(boolean isCreative) { return isCreative ? LCConfig.CLIENT.walletSlotCreative.get() : LCConfig.CLIENT.walletSlot.get(); }

	@SubscribeEvent
	public static void playerJoinsServer(ClientPlayerNetworkEvent.LoggingIn event) { ConfigFile.loadClientFiles(ConfigFile.LoadPhase.GAME_START); }

	@SubscribeEvent
	public static void playerLeavesServer(ClientPlayerNetworkEvent.LoggingOut event) {
		SyncedConfigFile.onClientLeavesServer();
	}

}
