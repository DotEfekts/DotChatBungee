package net.dotefekts.bungee.dotchat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.Chat;

class ChatUtilities {
	static Chat buildChatPacket(String message, boolean isJson, boolean isSystem) {
		return new Chat(isJson ? message : ComponentSerializer.toString(TextComponent.fromLegacyText(message)), isSystem ? ChatPosition.SYSTEM : ChatPosition.CHAT);
	}
	
	static Chat addChannelPrefix(Chat message, String sourceChannelName) {
		Chat multiMessage = new Chat(message.getMessage(), message.getPosition());
		String chatComponent = multiMessage.getMessage();
		List<BaseComponent> messageComponents = chatComponent != null?
				new ArrayList<BaseComponent>(Arrays.asList(ComponentSerializer.parse(chatComponent)))
				: new ArrayList<BaseComponent>(Arrays.asList(TextComponent.fromLegacyText("")));
		messageComponents.addAll(0, Arrays.asList(TextComponent.fromLegacyText(sourceChannelName)));
		multiMessage.setMessage(ComponentSerializer.toString(messageComponents.toArray(new BaseComponent[messageComponents.size()])));
		
		return multiMessage;
	}
}
