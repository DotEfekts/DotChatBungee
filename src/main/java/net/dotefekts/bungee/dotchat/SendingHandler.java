package net.dotefekts.bungee.dotchat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.Chat;

public class SendingHandler {
	private ChatManager chatManager;
	
	public SendingHandler(ChatManager manager) {
		this.chatManager = manager;
	}
	
	public boolean handleChat(ProxiedPlayer player, Chat packet) {
		byte messageType = packet.getPosition();
		if(messageType != ChatPosition.ACTION_BAR) {
			PlayerChatManager manager = chatManager.getPlayerManager(player);
			String messageJson = packet.getMessage();
			String messagePlain = BaseComponent.toPlainText(ComponentSerializer.parse(messageJson));
			
			if(!messagePlain.contains(ChatChannel.PARSED_PREFIX)) {
				if(messagePlain.contains(ChatChannel.MARKER_PREFIX)) {
					String channelName = messagePlain.substring(
							messagePlain.indexOf(ChatChannel.MARKER_PREFIX) + ChatChannel.MARKER_PREFIX.length(), 
							messagePlain.indexOf(ChatChannel.MARKER_SUFFIX));
					
					ChatChannel sourceChannel;
					if(channelName.equalsIgnoreCase("party")) {
						sourceChannel = manager.getPartyChannel();
					} else if(channelName.startsWith("p:")) {
						String senderName = channelName.split(";")[0].substring(2);
						String recieverName = channelName.split(";")[1];
						
						sourceChannel = manager.getPmChanel(senderName, recieverName);
					} else {
						sourceChannel = chatManager.getChannel(channelName);
					}

					messageJson = messageJson.replace(ChatChannel.MARKER_PREFIX + channelName + ChatChannel.MARKER_SUFFIX, "");
					packet.setMessage(messageJson);
					packet.setPosition(ChatPosition.CHAT);
					
					manager.sendMessage(sourceChannel, packet);
				} else {
					packet.setPosition(ChatPosition.SYSTEM);
					
					ChatChannel systemMessages = chatManager.getSystemMessageChannel();
					
					if(systemMessages != null)
						manager.sendMessage(systemMessages, packet);
					else
						manager.sendAllMessage(packet);
				}
				
				return true;
			} else {
				packet.setMessage(messageJson.replaceAll(ChatChannel.PARSED_PREFIX, ""));
			}
		} 
		
		return false;
	}
}
