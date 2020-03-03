package net.dotefekts.bungee.dotchat;

import de.exceptionflug.protocolize.api.event.PacketReceiveEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import net.md_5.bungee.protocol.packet.Chat;

public class ServerSendingListener extends PacketAdapter<Chat> {
	private SendingHandler handler;
	
	
	public ServerSendingListener(SendingHandler handler) {
		super(Stream.DOWNSTREAM, Chat.class);
		this.handler = handler;
	}
	
	@Override
	public void receive(PacketReceiveEvent<Chat> event) {
		event.setCancelled(handler.handleChat(event.getPlayer(), event.getPacket()));
	}
}
