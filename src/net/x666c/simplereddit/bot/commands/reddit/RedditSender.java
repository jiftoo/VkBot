package net.x666c.simplereddit.bot.commands.reddit;

import static net.x666c.simplereddit.bot.BotMain.randomSubs;

import java.util.HashMap;
import java.util.Random;

import org.json.JSONObject;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;

import net.x666c.simplereddit.Post;
import net.x666c.simplereddit.Reddit;
import net.x666c.simplereddit.Reddit.Time;
import net.x666c.simplereddit.Reddit.Type;

public class RedditSender {
	
	private final Group group;
	private final JSONObject json;
	
	public RedditSender(Group g, JSONObject j) {
		group = g;
		json = j;
	}
	
	public Reddit randomSubreddit(HashMap<String, Reddit> redditMap) {
		String chosen = null;
		do {
			chosen = randomSubs.get(new Random().nextInt(randomSubs.size()));
		} while(redditMap.containsKey(chosen));
		
		Reddit ret = null;
		String respText = "Connected+to+'"+chosen+"'+(random)";
		try {
			ret = Reddit.newConnection(chosen, Type.Top, Time.PastMonth, 1000);
			redditMap.put(chosen, ret);
		} catch (Exception e) {
			respText = "Invite+required!";
		}
		System.out.println("Chose '" + chosen + "' randomly");
		
		new Message()
		.from(group)
		.to(json.getInt("peer_id"))
		.text(respText)
		.send((Callback<Object>[]) null);
		
		return ret;
	}
	
	public Reddit knownSubreddit(HashMap<String, Reddit> redditMap, String name) {
		if(redditMap.containsKey(name))
			return redditMap.get(name);
		else {
			Reddit ret = null;
			String respText = "Connected+to+'"+name+"'";
			try {
				ret = Reddit.newConnection(name, Type.Rising, Time.PastWeek, 100);
			} catch (Exception e) {
				respText = "Invite+required!";
			}
			redditMap.put(name, ret);
			
			new Message()
			.from(group)
			.to(json.getInt("peer_id"))
			.text(respText)
			.send((Callback<Object>[]) null);
			
			return ret;
		}
	}
	
	
	
	public void sendPhoto(Post post) {
		String title = post.title().replaceAll(" ", "+");
		
		new Message()
		.from(group)
		.to(json.getInt("peer_id"))
		.photo(post.image() == null ? "No+images+found" : post.image())
		.text("Title:"+title)
		.send((Callback<Object>[]) null);
	}

	public void sendText(Post post) {
		String title = post.title().replaceAll(" ", "+");
		String text = post.text().replaceAll(" ", "+");
		String link = (post.link() == null) ? "" : post.link().replaceAll(" ", "+");
		
		new Message()
		.from(group)
		.to(json.getInt("peer_id"))
		.text((post.text().isEmpty() ? "Title:+"+title + "+No+text+found" : text) + "\n+Link:+" + link)
		.send((Callback<Object>[]) null);
	}

	public void sendAny(Post post) {
		String title = post.title().replaceAll(" ", "+");
		String text = post.text().replaceAll(" ", "+");
		String link = (post.link() == null) ? "" : post.link().replaceAll(" ", "+");
		
		Message m = new Message()
		.from(group)
		.to(json.getInt("peer_id"))
		.text((post.text().isEmpty() ? "Title:+"+title : "Title:+"+title+"+\n"+text) + "\n+Link:+" + link);
		
		if(post.image() != null) {
			if(post.image().endsWith("gif"))
				m.doc(post.image());
			else
				m.photo(post.image());
		}
		m.send((Callback<Object>[]) null);
	}
}
