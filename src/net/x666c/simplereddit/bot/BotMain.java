package net.x666c.simplereddit.bot;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;
import com.petersamokhin.bots.sdk.utils.vkapi.CallbackApiSettings;

import net.x666c.simplereddit.bot.commands.Command;
import net.x666c.simplereddit.bot.commands.CommandProcessor;
import net.x666c.simplereddit.bot.commands.reddit.MessageResolver;

public class BotMain {
	
	public static final List<String> randomSubs = Arrays.asList(SubredditsFiltered.SUBREDDITS.split("\r\n"));
	
	private static CommandProcessor processor;
	
	public static void main(String[] a) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		BasicConfigurator.configure();
		
		// Vk ------------------------------------------------------------------
		
		Group group = new Group("6b49194f54d1b9c69717059c7ed7910bda1d548f7da9ce2ef406274a17f3cc7f4abac21fd951cf32a8599");
		
		group.callbackApi(new CallbackApiSettings("1515c4a2", "localhost", Integer.parseInt(System.getenv("PORT")), "/", true, false));
		
		// FOR LOCAL TESTING:
		//group.callbackApi(new CallbackApiSettings("1515c4a2", "localhost", 80, "/", true, false));
		
		MessageResolver mr = new MessageResolver(group);
		processor = new CommandProcessor(group, "r3d");
		
		{
			processor.addCommand("list", new Command(0, (o, args) -> {
				sendMessage(group, o, "да йобана мне лень все команды записывать чичас, но тут по идее должен их список быть");
			}));
			processor.addCommand("setprefix", new Command(1, (o, args) -> {
				sendMessage(group, o, "Changed prefix: "+processor.botPrefix+" -> "+args[0]);
				processor.botPrefix = args[0];
			}));
		}
		
		group.onMessageNew(json -> {
			System.out.println(json.toString());
			try {
				System.out.println("Got a chat message: '" + json.getString("text") + "'");
				
				String[] tokens = mr.tokenize(json);
				String message = json.getString("text");
				String messageNoMention = String.join(" ", tokens);
				String messageNoMentionNoPrefix = messageNoMention.replaceFirst(processor.botPrefix, "");
				
				System.out.println(Arrays.toString(tokens));
				
				// Special cases (do not follow the standard)
				if (tokens[1].equals("reddit") && tokens.length >= 3) {
					mr.resolveReddit(tokens, json);
				} else if(message.toLowerCase().contains("блять колдун")) {
					for (int i = 0; i < 10; i++) {
						new Message()
						.from(group)
						.to(json.getInt("peer_id"))
						.text("блять+колдун")
						.send((Callback<Object>[]) null);
					}
				// The rest
				} else {
					if(tokens[0].equals(processor.botPrefix))
						if(!processor.executeCommand(tokens[1], json, Arrays.copyOfRange(tokens, 2, tokens.length)))
							sendMessage(group, json, "Invalid command: " + messageNoMentionNoPrefix);
				}
			} catch (Exception e) {
				sendMessage(group, json, "Exception: " + e.toString());
			}
		});
	}
	
	public static void sendMessage(Group g, JSONObject json, String text) {
		new Message()
		.from(g)
		.to(json.getInt("peer_id"))
		.text(text.replaceAll(" ", "+"))
		.send((Callback<Object>[]) null);
	}
	
	
}
