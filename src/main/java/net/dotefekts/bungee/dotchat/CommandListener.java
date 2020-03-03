package net.dotefekts.bungee.dotchat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dotefekts.bungee.dotutils.UtilityFunctions;
import net.dotefekts.bungee.dotutils.commandhelper.CommandEvent;
import net.dotefekts.bungee.dotutils.commandhelper.CommandHandler;
import net.dotefekts.bungee.dotutils.commandhelper.CommandHandlers;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class CommandListener implements Listener {
	private static final String PARTY_MESSAGE_PREFIX = ChatChannel.MARKER_PREFIX + "party" + ChatChannel.MARKER_SUFFIX;
	
	private Plugin plugin;
	private ChatManager chatManager;
	
	public CommandListener(Plugin plugin, ChatManager chatManager) {
		this.plugin = plugin;
		this.chatManager = chatManager;
	}
	
	@CommandHandler(command = "ch", permission = "dotchat.switch", format = "s<channel>", serverCommand = false)
	public void switchChannel(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);
		
		ChatChannel channel;
		if(event.getArgs()[0].equalsIgnoreCase("party"))
			channel = manager.getPartyChannel();
		else
			channel = chatManager.getChannel(event.getArgs()[0]);
		
		if(channel == null || !chatManager.getPlayerManager(player).switchChannel(channel)) {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You have not joined that channel.");
		}
	}

	@CommandHandler(command = "tch", permission = "dotchat.switch", format = "s<channel>", serverCommand = false)
	public void switchChatChannel(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);
		
		ChatChannel channel;
		if(event.getArgs()[0].equalsIgnoreCase("party"))
			channel = manager.getPartyChannel();
		else
			channel = chatManager.getChannel(event.getArgs()[0]);
		
		if(channel == null || !chatManager.getPlayerManager(player).switchChatChannel(channel)) {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You have not joined that channel.");
		}
	}
	
	@CommandHandler(command = "t", permission = "dotchat.switch", format = "s<channel> ...", serverCommand = false)
	public void sendMessage(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);
		
		ChatChannel channel;
		if(event.getArgs()[0].equalsIgnoreCase("party"))
			channel = manager.getPartyChannel();
		else
			channel = chatManager.getChannel(event.getArgs()[0]);
		
		
		if(channel == null || !chatManager.getPlayerManager(player).inChannel(channel)) {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You have not joined that channel.");
		} else if(event.getArgs().length <= 1) {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You didn't type anything.");
		} else if(event.getArgs().length > 1) {
			String message = String.join(" ", Arrays.copyOfRange(event.getArgs(), 1, event.getArgs().length));
			ChatEvent newEvent = plugin.getProxy().getPluginManager().callEvent(new ChatEvent(player, player.getServer(), ChatChannel.MARKER_PREFIX + channel.getName() + ChatChannel.MARKER_SUFFIX + message));
			if(!newEvent.isCancelled())
				player.chat(newEvent.getMessage());
		}
	}
	
	@CommandHandler(command = "join", permission = "dotchat.join", format = "s<channel>", serverCommand = false)
	public void joinChannel(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		ChatChannel channel = chatManager.getChannel(event.getArgs()[0]);
		
		if(channel == null || !chatManager.getPlayerManager(player).joinChannel(channel, false)) {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "That channel does not exist or you do not have permission to join it.");
		}
	}
	
	@CommandHandlers(value = { 
		@CommandHandler(command = "leave", permission = "dotchat.leave", format = "s[channel]", serverCommand = false),
		@CommandHandler(command = "close", permission = "dotchat.leave", format = "s[channel]", serverCommand = false) 
	})
	public void leaveChannel(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);
		
		ChatChannel channel;
		
		if(event.getArgs().length == 1) {
			if(event.getArgs()[0].equalsIgnoreCase("party")) {
				channel = manager.getPartyChannel();
			} else if(event.getArgs()[0].startsWith("p:")) {
				String senderName = event.getArgs()[0].split(";")[0].substring(2);
				String recieverName = event.getArgs()[0].split(";")[1];
				
				channel = manager.getPmChanel(senderName, recieverName);
			} else {
				channel = chatManager.getChannel(event.getArgs()[0]);
			}
			
			if(channel == null) {
				channel = manager.getPmChanel(player.getName(), event.getArgs()[0]);
			}
		} else {
			channel = manager.getActiveChannel();
		}
		 
		
		if(!manager.inChannel(channel)) {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You have not joined that channel.");
		} else if(!manager.leaveChannel(channel)) {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You cannot leave " + (event.getArgs().length == 1 ? "that" : "this") + " channel.");
		} else if(channel.getName() == "party") {
			for(ProxiedPlayer member : ((PartyChannel) channel).getPlayers())
				UtilityFunctions.sendLegacyMessage(member, PARTY_MESSAGE_PREFIX + ChatColor.YELLOW + player.getDisplayName() + " left the party.");
			UtilityFunctions.sendLegacyMessage(player, ChatColor.YELLOW + "You have left the party.");
		}
	}

	@CommandHandlers(value = { 
		@CommandHandler(command = "msg", permission = "dotchat.private", format = "p<User> ...", serverCommand = false),
		@CommandHandler(command = "pm", permission = "dotchat.private", format = "p<User> ...", serverCommand = false),
		@CommandHandler(command = "m", permission = "dotchat.private", format = "p<User> ...", serverCommand = false)
	})
	public void privateMessage(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		
		if(event.getArgs().length > 1) {
			ProxiedPlayer recievingPlayer = plugin.getProxy().getPlayer(event.getArgs()[0]);
			if(player != recievingPlayer && chatManager.getPlayerManager(recievingPlayer).pmsEnabled()) {
				String message = String.join(" ", Arrays.copyOfRange(event.getArgs(), 1, event.getArgs().length));
				player.chat(ChatChannel.MARKER_PREFIX + "p:" + player.getName() + ";" + recievingPlayer.getName() + ChatChannel.MARKER_SUFFIX + message);
			} else {
				UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You cannot message that player.");
			}
		} else {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You didn't type anything.");
		}
	}
	
	@CommandHandler(command = "pmoff", permission = "dotchat.private.disable", format = "n", serverCommand = false)
	public void pmOff(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		chatManager.getPlayerManager(player).disablePms();
		UtilityFunctions.sendLegacyMessage(player, ChatColor.YELLOW + "Private messages have been disabled.");
	}
	
	@CommandHandler(command = "pmon", permission = "dotchat.private.disable", format = "n", serverCommand = false)
	public void pmOn(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		chatManager.getPlayerManager(player).enablePms();
		UtilityFunctions.sendLegacyMessage(player, ChatColor.YELLOW + "Private messages have been enabled.");
	}
	
	@CommandHandler(command = "party create", permission = "dotchat.party.create", format = "...", serverCommand = false)
	public void createParty(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);
		
		if(manager.getPartyChannel() == null) {
			PartyChannel channel = manager.createPartyChannel();
			
			if(event.getArgs().length > 0) {
				for(String name : event.getArgs()) {
					ProxiedPlayer invPlayer = plugin.getProxy().getPlayer(name);
					if(invPlayer != null && invPlayer != player && chatManager.getPlayerManager(invPlayer).partyInviteEnabled()) {
						channel.addInvitedPlayer(invPlayer.getUniqueId());
						UtilityFunctions.sendLegacyMessage(invPlayer, ChatColor.YELLOW + "You've been invited to " + player.getDisplayName() + "'s chat party. Type " + ChatColor.GOLD + "/party join " + player.getDisplayName() + ChatColor.YELLOW + " to accept.");
					}
				}
				
				UtilityFunctions.sendLegacyMessage(player, PARTY_MESSAGE_PREFIX + ChatColor.YELLOW + "Party created and players invited.");
			} else {
				UtilityFunctions.sendLegacyMessage(player, PARTY_MESSAGE_PREFIX + ChatColor.YELLOW + "Party has been created.");
			}
		} else {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You are already in a party.");
		}
	}
	
	@CommandHandler(command = "party invite", permission = "dotchat.party.invite", format = "...", serverCommand = false)
	public void partyInvite(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);
		
		PartyChannel channel = manager.getPartyChannel();
		if(channel != null) {
			if(event.getArgs().length > 0) {
				for(String name : event.getArgs()) {
					ProxiedPlayer invPlayer = plugin.getProxy().getPlayer(name);
					if(invPlayer != null && invPlayer != player && chatManager.getPlayerManager(invPlayer).partyInviteEnabled()) {
						channel.addInvitedPlayer(invPlayer.getUniqueId());
						UtilityFunctions.sendLegacyMessage(invPlayer, ChatColor.YELLOW + "You've been invited to " + player.getDisplayName() + "'s chat party. Type " + ChatColor.GOLD + "/party join " + player.getDisplayName() + ChatColor.YELLOW + " to accept.");
					}
				}
				
				UtilityFunctions.sendLegacyMessage(player, PARTY_MESSAGE_PREFIX + ChatColor.YELLOW + "Players invited to party.");
			} else {
				UtilityFunctions.sendLegacyMessage(player, PARTY_MESSAGE_PREFIX + ChatColor.RED + "You must specify players to invite.");
			}
		} else {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You are not in a party.");
		}
	}
	
	@CommandHandler(command = "party inviteoff", permission = "dotchat.party.disable", format = "n", serverCommand = false)
	public void partyInviteOff(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		chatManager.getPlayerManager(player).disablePartyInvite();
		UtilityFunctions.sendLegacyMessage(player, ChatColor.YELLOW + "Party invites have been disabled.");
	}
	
	@CommandHandler(command = "party inviteon", permission = "dotchat.party.disable", format = "n", serverCommand = false)
	public void partyInviteOn(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		chatManager.getPlayerManager(player).enablePartyInvite();
		UtilityFunctions.sendLegacyMessage(player, ChatColor.YELLOW + "Party invites have been enabled.");
	}
	
	@CommandHandler(command = "party join", permission = "dotchat.party.join", format = "p<User>", serverCommand = false)
	public void partyJoin(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);
		
		if(manager.getPartyChannel() == null) {
			ProxiedPlayer partyPlayer = plugin.getProxy().getPlayer(event.getArgs()[0]);
			
			if(partyPlayer != null) {
				PartyChannel partyChannel = chatManager.getPlayerManager(partyPlayer).getPartyChannel();
				if(partyChannel == null || !manager.joinPartyChannel(partyChannel, false)) {
					UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You have not been invited to that players party.");
				} else {
					for(ProxiedPlayer member : manager.getPartyChannel().getPlayers())
						if(member != player)
							UtilityFunctions.sendLegacyMessage(member, PARTY_MESSAGE_PREFIX + ChatColor.YELLOW + player.getDisplayName() + " joined the party.");
					UtilityFunctions.sendLegacyMessage(player, PARTY_MESSAGE_PREFIX + ChatColor.YELLOW + "You have joined " + partyPlayer.getDisplayName() + "'s party.");
				}
			} else {
				UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "Player is not online.");
			}
		} else {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You are already in a party.");
		}
	}
	
	@CommandHandler(command = "party leave", permission = "dotchat.leave", format = "n", serverCommand = false)
	public void partyLeave(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);

		PartyChannel partyChannel = manager.getPartyChannel();
		if(partyChannel != null) {
			manager.leaveChannel(partyChannel);
			for(ProxiedPlayer member : partyChannel.getPlayers())
				UtilityFunctions.sendLegacyMessage(member, PARTY_MESSAGE_PREFIX + ChatColor.YELLOW + player.getDisplayName() + " left the party.");
			UtilityFunctions.sendLegacyMessage(player, ChatColor.YELLOW + "You have left the party.");
		} else {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You are not in a party.");
		}
	}
	
	@CommandHandler(command = "party members", permission = "dotchat.party.members", format = "n", serverCommand = false)
	@CommandHandler(command = "party list", permission = "dotchat.party.members", format = "n", serverCommand = false)
	public void partyList(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(player);
		
		if(manager.getPartyChannel() != null) {
			String playerList = "";
			for(ProxiedPlayer member : manager.getPartyChannel().getPlayers())
				playerList = playerList + member.getDisplayName() + ", ";
			
			UtilityFunctions.sendLegacyMessage(player, ChatColor.YELLOW + "Party members are: " + playerList.substring(0, playerList.length() - 2) + ".");
		} else {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You are not in a party.");
		}
	}
	
	@CommandHandler(command = "party info", permission = "dotchat.party.info", format = "p<User>", serverCommand = false)
	public void partyInfo(CommandEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		PlayerChatManager manager = chatManager.getPlayerManager(plugin.getProxy().getPlayer(event.getArgs()[0]));
		
		if(manager.getPartyChannel() != null) {
			String playerList = "";
			for(ProxiedPlayer member : manager.getPartyChannel().getPlayers())
				playerList = playerList + member.getDisplayName() + ", ";
			
			UtilityFunctions.sendLegacyMessage(player, ChatColor.YELLOW + "Players party members are: " + playerList.substring(0, playerList.length() - 2) + ".");
		} else {
			UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "Player is not in a party.");
		}
	}

	
	@EventHandler
	public void tabCompleteSwitch(TabCompleteEvent event) {
		String[] split = event.getCursor().split(" ", -1);
		if(split.length < 3) {
			if(event.getCursor().startsWith("/t") || event.getCursor().startsWith("/tch")|| event.getCursor().startsWith("/ch")) {
				ProxiedPlayer player = (ProxiedPlayer) event.getSender();
				PlayerChatManager playerManager = chatManager.getPlayerManager(player);
				
				if(split.length == 2) {
					event.getSuggestions().clear();
					event.getSuggestions().addAll(playerManager.getChannelList(event.getCursor().startsWith("/ch"), false, split[1]));
				} else {
					event.getSuggestions().clear();
					event.getSuggestions().addAll(playerManager.getChannelList(event.getCursor().startsWith("/ch"), false));
				}
			} else if(event.getCursor().startsWith("/join")) {
				ProxiedPlayer player = (ProxiedPlayer) event.getSender();
				PlayerChatManager playerManager = chatManager.getPlayerManager(player);
				
				if(split.length == 2) {
					event.getSuggestions().clear();
					event.getSuggestions().addAll(playerManager.getAvailableChannels(split[1]));
				} else {
					event.getSuggestions().clear();
					event.getSuggestions().addAll(playerManager.getAvailableChannels());
				}
			} else if(event.getCursor().startsWith("/leave") || event.getCursor().startsWith("/close")) {
				ProxiedPlayer player = (ProxiedPlayer) event.getSender();
				PlayerChatManager playerManager = chatManager.getPlayerManager(player);
				
				if(split.length == 2) {
					event.getSuggestions().clear();
					event.getSuggestions().addAll(playerManager.getChannelList(false, true, split[1]));
				} else {
					event.getSuggestions().clear();
					event.getSuggestions().addAll(playerManager.getChannelList(false, true));
				}
			} else if(event.getCursor().startsWith("/party")) {
				String typed = split.length == 2 ? split[1] : "" ;
				String[] partyCommands = new String[]{ "create", "invite", "join", "members", "list", "info" };
				List<String> completions = new ArrayList<String>();
				for(String cmd : partyCommands)
					if(cmd.startsWith(typed))
						completions.add(cmd);
				event.getSuggestions().clear();
				event.getSuggestions().addAll(completions);
			}
		}
	}
}
