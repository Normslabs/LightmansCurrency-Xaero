package io.github.lightman314.lightmanscurrency.commands;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

public class CommandLCAdmin {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> lcAdminCommand
			= Commands.literal("lcadmin")
				.requires((commandSource) -> commandSource.hasPermission(2))
				.then(Commands.literal("help")
						.executes(CommandLCAdmin::help))
				.then(Commands.literal("toggleadmin")
						.requires((commandSource) -> commandSource.getEntity() instanceof ServerPlayer)
						.executes(CommandLCAdmin::toggleAdmin))
				.then(Commands.literal("universaldata")
						.then(Commands.literal("list")
							.executes(CommandLCAdmin::listUniversalData))
						.then(Commands.literal("search")
								.then(Commands.argument("searchText", MessageArgument.message())
										.executes(CommandLCAdmin::searchUniversalData)))
						.then(Commands.literal("delete")
								.then(Commands.argument("dataID", MessageArgument.message())
										.executes(CommandLCAdmin::deleteUniversalData))));
		
		dispatcher.register(lcAdminCommand);
		
	}
	
	static int help(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		
		//help
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.help.help", "/lcadmin help -> "), false);
		//toggleadmin
		if(source.getEntity() instanceof ServerPlayer)
			source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.toggleadmin.help", "/lcadmin toggleadmin -> "), false);
		//universaldata list
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.help", "/lcadmin universaldata list -> "), false);
		//universaldata delete <traderID>
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.delete.help", "/lcadmin universaldata delete <traderID> "), false);
		
		return 1;
	}
	
	static int toggleAdmin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		ServerPlayer sourcePlayer = source.getPlayerOrException();
		
		TradingOffice.toggleAdminPlayer(sourcePlayer);
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.toggleadmin",new TranslatableComponent("command.lightmanscurrency.lcadmin.toggleadmin." + (TradingOffice.isAdminPlayer(sourcePlayer) ? "enabled" : "disabled"))), true);
		
		return 1;
	}
	
	static int listUniversalData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		List<UniversalTraderData> allTraders = TradingOffice.getTraders();
		
		if(allTraders.size() > 0)
		{
			
			source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			
			for(int i = 0; i < allTraders.size(); i++)
			{
				UniversalTraderData thisTrader = allTraders.get(i);
				//Spacer
				if(i > 0) //No spacer on the first output
					source.sendSuccess(new TextComponent(""), true);
				
				sendTraderDataFeedback(thisTrader, source);
				
			}
		}
		else
		{
			source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.none"), true);
		}
		
		return 1;
	}
	
	static int searchUniversalData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException{
		
		CommandSourceStack source = commandContext.getSource();
		
		String searchText = MessageArgument.getMessage(commandContext, "searchText").getString();
		
		List<UniversalTraderData> allTraders = TradingOffice.getTraders(searchText);
		if(allTraders.size() > 0)
		{
			
			source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.title"), true);
			for(int i = 0; i < allTraders.size(); i++)
			{
				UniversalTraderData thisTrader = allTraders.get(i);
				//Spacer
				if(i > 0) //No spacer on the first output
					source.sendSuccess(new TextComponent(""), true);
				
				sendTraderDataFeedback(thisTrader, source);
				
			}
		}
		else
		{
			source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.search.none"), true);
		}
		
		return 1;
	}
	
	private static void sendTraderDataFeedback(UniversalTraderData thisTrader, CommandSourceStack source)
	{
		//Trader ID
		String traderID = thisTrader.getTraderID().toString();
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.traderid", new TextComponent(traderID).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, traderID)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.traderid.copytooltip"))))), true);
		//Type
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.type", thisTrader.getTraderType()), true);
		//Owner / Owner ID
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.owner", thisTrader.getOwnerName(), thisTrader.getOwnerID().toString()), true);
		//Dimension
		String dimension = thisTrader.getWorld().location().toString();
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.dimension", dimension), true);
		//Position
		BlockPos pos = thisTrader.getPos();
		String position = pos.getX() + " " + pos.getY() + " " + pos.getZ();
		String teleportPosition = pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.position", new TextComponent(position).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + dimension + " run tp @s " + teleportPosition)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.position.teleporttooltip"))))), true);
		//Custom Name (if applicable)
		if(thisTrader.hasCustomName())
			source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.list.name", thisTrader.getName()), true);
	}
	
	static int deleteUniversalData(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
	{
		
		CommandSourceStack source = commandContext.getSource();
		
		String traderID = MessageArgument.getMessage(commandContext, "dataID").getString();
		if(traderID == "")
		{
			source.sendFailure(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.delete.noid"));
			return 0;
		}
		List<UniversalTraderData> allTraders = TradingOffice.getTraders();
		for(int i = 0; i < allTraders.size(); i++)
		{
			if(allTraders.get(i).getTraderID().toString().equals(traderID))
			{
				//Remove the trader
				TradingOffice.removeTrader(allTraders.get(i).getTraderID());
				//Send success message
				source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.delete.success", traderID), true);
				return 1;
			}
		}
		//If no trader with that id found, send a not found message
		source.sendSuccess(new TranslatableComponent("command.lightmanscurrency.lcadmin.universaldata.delete.notfound"), true);
		return 0;
	}
	
}