package net.dotefekts.bungee.dotchat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PartyChannel extends ChatChannel {
	private List<ProxiedPlayer> players;
	private List<UUID> invitedPlayers;
	
	public PartyChannel(int channelOrder, String displayName, String displayNameActive, boolean canLeave, boolean sendHistory) {
		super("party", channelOrder, displayName, displayNameActive, false, false, false, canLeave, true, sendHistory);
		
		players = new ArrayList<ProxiedPlayer>();
		invitedPlayers = new ArrayList<UUID>();
	}
	
	public void addPlayer(ProxiedPlayer player) {
		players.add(player);
		invitedPlayers.remove(player.getUniqueId());
	}
	
	public List<ProxiedPlayer> getPlayers() {
		return new ArrayList<ProxiedPlayer>(players);
	}
	
	public void removePlayer(ProxiedPlayer player) {
		players.remove(player);
	}
	
	public void addInvitedPlayer(UUID playerUUID) {
		invitedPlayers.add(playerUUID);
	}
	
	public void removeInvitedPlayer(UUID playerUUID) {
		invitedPlayers.remove(playerUUID);
	}
	
	@Override
	public boolean canJoin(ProxiedPlayer player) {
		return player.hasPermission("dotchat.party.forcejoin") || players.contains(player) || invitedPlayers.contains(player.getUniqueId());
	}

	public boolean isInChat(ProxiedPlayer receiver) {
		boolean inChat = false;
		for(ProxiedPlayer player : players)
			if(player == receiver)
				inChat = true;
		return inChat;
	}
}
