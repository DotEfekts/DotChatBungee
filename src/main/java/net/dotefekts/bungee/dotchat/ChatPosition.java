package net.dotefekts.bungee.dotchat;

import net.md_5.bungee.api.ChatMessageType;

public class ChatPosition {
	public static final byte CHAT = (byte) 0;
	public static final byte SYSTEM = (byte) 1;
	public static final byte ACTION_BAR = (byte) 2;
	
	public static ChatMessageType fromByte(byte position) {
		switch(position) {
			case (byte) 2:
				return ChatMessageType.ACTION_BAR;
			case (byte) 1:
				return ChatMessageType.SYSTEM;
			default:
				return ChatMessageType.CHAT;
		}
	}
}
