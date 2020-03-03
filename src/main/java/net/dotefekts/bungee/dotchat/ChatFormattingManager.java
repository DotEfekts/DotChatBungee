package net.dotefekts.bungee.dotchat;

import de.exceptionflug.protocolize.api.event.EventManager;
import net.dotefekts.bungee.dotutils.UtilityFunctions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ChatFormattingManager implements Listener {
	private DotChat plugin;
	private ChatManager chatManager;
	
	public ChatFormattingManager(DotChat plugin, EventManager protocolManager, ChatManager chatManager) {
		this.plugin = plugin;
		this.chatManager = chatManager;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void checkCanSend(ChatEvent event) {
		if(!event.isCommand() && event.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) event.getSender();
			PlayerChatManager manager = chatManager.getPlayerManager(player);
			ChatChannel currentChannel = manager.getActiveChatChannel();
			
			if(event.getMessage().contains(ChatChannel.MARKER_PREFIX)) {
				String channelName = parseChannelName(event.getMessage());
				if(channelName.startsWith("p:")) {
					if(manager.pmsEnabled()) {
						PmChannel pmChannel = manager.openPmChannel(plugin.getProxy().getPlayer(channelName.split(";")[1]), false);
						if(pmChannel != null && chatManager.getPlayerManager(pmChannel.getPartner()).pmsEnabled()) {
							currentChannel = pmChannel;
						} else {
							event.setCancelled(true);
							UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You cannot message that player.");
						}
					} else {
						event.setCancelled(true);
						UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You currently have PMs disabled.");
					}
				} else {
					ChatChannel newChannel = tryGetChannel(event.getMessage());
					if(newChannel != null) {
						if(manager.inChannel(newChannel))
							currentChannel = newChannel;
						else {
							event.setCancelled(true);
							UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You have not joined that channel.");
							return;
						}
					}
				}
			}
			
			if(!currentChannel.canTalk()) {
				event.setCancelled(true);
				UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You cannot send messages to this channel.");
			} else if(currentChannel instanceof PartyChannel) {
				if(event.getReceiver() instanceof ProxiedPlayer && 
							!((PartyChannel) currentChannel).isInChat((ProxiedPlayer) event.getReceiver()))
							event.setCancelled(true);
			} else if(currentChannel instanceof PmChannel) {
				if(chatManager.getPlayerManager(((PmChannel) currentChannel).getPartner()).pmsEnabled()) {
					if(event.getReceiver() instanceof ProxiedPlayer && 
							!((PmChannel) currentChannel).isInChat((ProxiedPlayer) event.getReceiver()))
							event.setCancelled(true);
				} else {
					event.setCancelled(true);
					UtilityFunctions.sendLegacyMessage(player, ChatColor.RED + "You cannot message that player.");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void chatEvent(ChatEvent event) {
		if(!event.isCommand() && event.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) event.getSender();
			PlayerChatManager manager = chatManager.getPlayerManager(player);
			ChatChannel currentChannel = manager.getActiveChatChannel();
			
			if(event.getMessage().contains(ChatChannel.MARKER_PREFIX)) {
				ChatChannel newChannel = tryGetChannel(event.getMessage());
				if(newChannel != null) {
					currentChannel = newChannel;
				}
				
				event.setMessage(replaceMessageMarker(event.getMessage()));
			}
			
			event.setMessage(ChatChannel.MARKER_PREFIX + currentChannel.getName() + ChatChannel.MARKER_SUFFIX + event.getMessage());
			chatManager.addMessageHistory(currentChannel, ChatUtilities.buildChatPacket(event.getMessage(), false, false));
		}
	}

	private String replaceMessageMarker(String message) {
		String channelName = message.substring(message.indexOf(ChatChannel.MARKER_PREFIX) + ChatChannel.MARKER_PREFIX.length());
		channelName = channelName.substring(0, channelName.indexOf(ChatChannel.MARKER_SUFFIX));
		
		return message.replace(ChatChannel.MARKER_PREFIX + channelName + ChatChannel.MARKER_SUFFIX, "");
	}

	private ChatChannel tryGetChannel(String channelName) {
		return chatManager.getChannel(parseChannelName(channelName));
	}

	private String parseChannelName(String channelName) {
		channelName = channelName.substring(channelName.indexOf(ChatChannel.MARKER_PREFIX) + ChatChannel.MARKER_PREFIX.length());
		channelName = channelName.substring(0, channelName.indexOf(ChatChannel.MARKER_SUFFIX));		
		return channelName;
	}

	@EventHandler
	public void playerJoin(PostLoginEvent event) {
		chatManager.getPlayerManager(event.getPlayer());
	}
	
	@EventHandler
	public void playerLeave(PlayerDisconnectEvent event) {
		chatManager.destroyPlayerManager(event.getPlayer());
	}
}
