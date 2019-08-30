package net.x666c.simplereddit.bot.commands.reddit;

import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONObject;

import com.petersamokhin.bots.sdk.clients.Group;

import net.x666c.simplereddit.Post;
import net.x666c.simplereddit.Reddit;

public class MessageResolver {
	
	private HashMap<String, Reddit> redditMap = new HashMap<>();
	private final Group group;
	
	public MessageResolver(Group group) {
		this.group = group;
	}
	
	public String[] tokenize(JSONObject msg) {
		String text = msg.getString("text");
		
		String[] ret = text.split(" ");
		
		if(isMention(ret))
			return Arrays.copyOfRange(ret, 1, ret.length);
		return ret;
	}
	
	
	
	public void resolveReddit(String[] tokens, JSONObject json) {
		System.out.println(isMention(tokens));
		if(isMention(tokens)) {
			tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
		}
		sendPostFromReddit(tokens, json);
	}
	
	private void sendPostFromReddit(String[] tokens, JSONObject json) {
		RedditSender s = new RedditSender(group, json);
		
		Reddit subreddit = null;
		if(tokens[1].equals("random")) {
			subreddit = s.randomSubreddit(redditMap);
		} else {
			subreddit = s.knownSubreddit(redditMap, tokens[1]);
		}
		
		Post post = subreddit.randomPost();
		
		if(tokens[2].equals("photo") || tokens[2].equals("image")) {
			s.sendPhoto(post);
		} else if(tokens[2].equals("text")) {
			s.sendText(post);
		} else if(tokens[2].equals("any")) {
			s.sendAny(post);
		}
	}
	
	
	
	private boolean isMention(String[] tokens) {
		return tokens[0].matches("^\\[club[0-9]{9}\\|@[a-zA-Z]+\\]$");
	}

}
