package net.dotefekts.bungee.dotchat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.Chat;

public class Message {
	private Chat packet;
	private long time;
	
	public Message(Chat packet, long time, boolean addParsedMarker) {
		this.packet = packet;
		this.time = time;
		
		if(addParsedMarker) {
			List<BaseComponent> components = new ArrayList<BaseComponent>(Arrays.asList(ComponentSerializer.parse(packet.getMessage())));
			components.addAll(0, Arrays.asList(TextComponent.fromLegacyText(ChatChannel.PARSED_PREFIX)));
			packet.setMessage(ComponentSerializer.toString(components.toArray(new BaseComponent[components.size()])));
		}
	}
	
	public Chat getPacket() {
		return new Chat(packet.getMessage(), packet.getPosition());
	}
	
	public long getTime() {
		return time;
	}
}
