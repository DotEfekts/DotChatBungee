package net.dotefekts.bungee.dotchat;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PmChannel extends ChatChannel {
	private ProxiedPlayer owningPlayer;
	private ProxiedPlayer partnerPlayer;
	
	public PmChannel(ProxiedPlayer owningPlayer, ProxiedPlayer partnerPlayer, int channelOrder, String displayName, String displayNameActive, boolean sendHistory) {
		super("p:" + owningPlayer.getName() + ";" + partnerPlayer.getName(), channelOrder, displayName, displayNameActive, false, false, false, true, true, sendHistory);
		
		this.owningPlayer = owningPlayer;
		this.partnerPlayer = partnerPlayer;
	}
	
	public ProxiedPlayer getOwner() {
		return owningPlayer;
	}
	
	public ProxiedPlayer getPartner() {
		return partnerPlayer;
	}
	
	public boolean isInChat(ProxiedPlayer player) {
		return player == owningPlayer || player == partnerPlayer;
	}
}
