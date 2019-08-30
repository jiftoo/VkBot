package net.x666c.simplereddit.bot.commands;

import java.util.HashMap;

import org.json.JSONObject;

import com.petersamokhin.bots.sdk.clients.Group;

public class CommandProcessor {
	
	private final HashMap<String, Command> commands = new HashMap<>();
	private final Group group;
	
	public String botPrefix;
	
	public CommandProcessor(Group g, String botPrefix) {
		group = g;
		this.botPrefix = botPrefix;
	}
	
	public void addCommand(String name, Command c) {
		commands.put(name, c);
	}
	
	public Command getCommandObject(String name) {
		return commands.get(name);
	}
	
	public boolean executeCommand(String name, JSONObject o, String... arguments) {
		if(commands.containsKey(name)) {
			getCommandObject(name).execute(o, arguments);
			return true;
		} else {
			return false;
		}
	}

}
