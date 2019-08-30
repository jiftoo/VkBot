package net.x666c.simplereddit.bot.commands;

import java.util.function.BiConsumer;

import org.json.JSONObject;

public class Command {
	
	public final int argsMax;
	private BiConsumer<JSONObject ,String[]> exec;
	
	public Command(int maxArgs, BiConsumer<JSONObject ,String[]> cmd) {
		argsMax = maxArgs;
		exec = cmd;
	}
	
	public void execute(JSONObject o, String[] args) {
		exec.accept(o, args);
	}

}
