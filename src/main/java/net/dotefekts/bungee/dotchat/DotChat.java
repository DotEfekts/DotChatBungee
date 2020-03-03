package net.dotefekts.bungee.dotchat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import de.exceptionflug.protocolize.api.event.EventManager;
import de.exceptionflug.protocolize.api.protocol.ProtocolAPI;
import net.dotefekts.bungee.dotutils.DotUtilities;
import net.md_5.bungee.api.plugin.Plugin;

public class DotChat extends Plugin {
	private EventManager protocolManager;
	private ChatManager chatManager;
	private SendingHandler sendingHandler;
	private BungeeSendingListener bungeeSendingListener;
	private ServerSendingListener serverSendingListener;
	private ChatFormattingManager chatReplayManager;
	private CommandListener commandListener;
	
    @Override
    public void onEnable() {
    	if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

   
        if (!file.exists()) {
            try (InputStream in = DotChat.class.getResourceAsStream("/dotchat-config.yml")) {
                Files.copy(in, file.toPath());
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    	
    	protocolManager = ProtocolAPI.getEventManager();
    	chatManager = new ChatManager(this);
    	sendingHandler = new SendingHandler(chatManager);
    	bungeeSendingListener = new BungeeSendingListener(sendingHandler);
    	serverSendingListener = new ServerSendingListener(sendingHandler);
    	chatReplayManager = new ChatFormattingManager(this, protocolManager, chatManager);
    	commandListener = new CommandListener(this, chatManager);
    	
    	getProxy().getPluginManager().registerListener(this, chatReplayManager);
    	getProxy().getPluginManager().registerListener(this, commandListener);
		ProtocolAPI.getEventManager().registerListener(bungeeSendingListener);
		ProtocolAPI.getEventManager().registerListener(serverSendingListener);
    	DotUtilities.getCommandHelper().registerCommands(commandListener, this);
    	
    	getLogger().info("DotChat has finished loading.");
    }

    @Override
    public void onDisable() {    	
    	getLogger().info("DotChat has finished disabling.");
    }
}
