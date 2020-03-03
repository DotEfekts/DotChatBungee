package net.dotefekts.bungee.dotchat;

import de.exceptionflug.protocolize.api.event.PacketSendEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import net.md_5.bungee.protocol.packet.Chat;

public class BungeeSendingListener extends PacketAdapter<Chat> {
	private SendingHandler handler;
	
	public BungeeSendingListener(SendingHandler handler) {
		super(Stream.UPSTREAM, Chat.class);
		this.handler = handler;
	}
	
	@Override
	public void send(PacketSendEvent<Chat> event) {
		event.setCancelled(handler.handleChat(event.getPlayer(), event.getPacket())); 
	}
}
