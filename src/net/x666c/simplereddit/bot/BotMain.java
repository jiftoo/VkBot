package net.x666c.simplereddit.bot;

import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.petersamokhin.bots.sdk.callbacks.Callback;
import com.petersamokhin.bots.sdk.clients.Group;
import com.petersamokhin.bots.sdk.objects.Message;
import com.petersamokhin.bots.sdk.utils.vkapi.CallbackApiSettings;

import net.x666c.simplereddit.Post;
import net.x666c.simplereddit.Reddit;
import net.x666c.simplereddit.Reddit.Time;
import net.x666c.simplereddit.Reddit.Type;

public class BotMain {
	
	private static HashMap<String, Reddit> redditMap = new HashMap<>();
	private static List<String> randomSubreddits;
	static {
		try {
			randomSubreddits = Arrays.asList(SubredditsFiltered.SUBREDDITS.split("\r\n"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		BasicConfigurator.configure();
		
		// Vk ------------------------------------------------------------------
		
		Group group = new Group("6b49194f54d1b9c69717059c7ed7910bda1d548f7da9ce2ef406274a17f3cc7f4abac21fd951cf32a8599");
		group.callbackApi(new CallbackApiSettings("1515c4a2", "localhost", Integer.parseInt(System.getenv("PORT")), "/", true, false));
		
		/*group.onSimpleTextMessage(msg -> {
			System.out.print("Got a message: '" + msg.getText() + "'");
			if (msg.getText().toLowerCase().contains("hmm") || msg.getText().toLowerCase().contains("хмм")) {
				String link = reddit.randomPost().image();
				System.out.println(link);
				
				new Message()
				.from(group)
				.to(msg.authorId())
				.photo(link)
				.send((Callback<Object>[])null);
			} else if(msg.getText().toLowerCase().startsWith("войс ")) {
				File voiceFile = makeVoice(String.valueOf(msg.getMessageId()), msg.getText().split("войс ")[1]);
				deletionQueue.add(voiceFile);
				
				String vpath = voiceFile.getAbsolutePath();
				
				new Message()
				.from(group)
				.to(msg.authorId())
				.sendVoiceMessage(vpath, (Callback<Object>[]) null);
				
				System.out.println("Sent voice response to '" + msg.getText() + "'");
			}
		});*/
		/**/
		group.onMessageNew(json -> {
			try {
				String text = json.getString("text");
				System.out.println("Got a chat message: '" + text + "'");
				
				String mm = text.substring(text.indexOf(']')+2).trim();
				System.out.println(mm);
				
				String[] tokens = mm.split(" ");
				System.out.println(Arrays.toString(tokens));
				
				if (tokens[0].equals("reddit") && tokens.length == 3) {
					Reddit subreddit = null;
					boolean inviteReq = false;
					
					if(tokens[1].equals("random")) {
						String chosen = null;
						do {
							chosen = randomSubreddits.get(new Random().nextInt(randomSubreddits.size()));
						} while(redditMap.containsKey(chosen));
						
						subreddit = Reddit.newConnection(chosen, Type.Top, Time.PastMonth, 1000);
						redditMap.put(chosen, subreddit);
						
						System.out.println("Chose '" + chosen + "' randomly");
						
						new Message()
						.from(group)
						.to(json.getInt("peer_id"))
						.text(inviteReq ? "Invite+required" : "Connected+to+'"+chosen+"'+(random)")
						.send((Callback<Object>[]) null);
					}
					else if(redditMap.containsKey(tokens[1]))
						subreddit = redditMap.get(tokens[1]);
					else {
						try {
							subreddit = Reddit.newConnection(tokens[1], Type.Rising, Time.PastWeek, 100);
						} catch (Exception e) {
							inviteReq = true;
						}
						redditMap.put(tokens[1], subreddit);
						
						new Message()
						.from(group)
						.to(json.getInt("peer_id"))
						.text(inviteReq ? "Invite+required" : "Connected+to+'"+tokens[1]+"'")
						.send((Callback<Object>[]) null);
					}
					
					Post rPost = subreddit.randomPost();
					String titleFix = rPost.title().replaceAll(" ", "+");
					String textFix = rPost.text().replaceAll(" ", "+");
					String linkFix = (rPost.link() == null) ? "" : rPost.link().replaceAll(" ", "+");
					
					if(tokens[2].equals("photo")) {
						new Message()
						.from(group)
						.to(json.getInt("peer_id"))
						.photo(rPost.image() == null ? "No+images+found" : rPost.image())
						.text("Title:"+titleFix)
						.send((Callback<Object>[]) null);
					} else if(tokens[2].equals("text")) {
						new Message()
						.from(group)
						.to(json.getInt("peer_id"))
						.text((rPost.text().isEmpty() ? "Title:+"+titleFix + "+No+text+found" : textFix) + "\n+Link:+" + linkFix)
						.send((Callback<Object>[]) null);
					} else if(tokens[2].equals("any")) {
						System.out.println("Image: " + rPost.image());
						System.out.println("Text: " + rPost.text());
						System.out.println("Video: " + rPost.video());
						
						Message m = new Message()
						.from(group)
						.to(json.getInt("peer_id"))
						.text((rPost.text().isEmpty() ? "Title:+"+titleFix : "Title:+"+titleFix+"+\n"+textFix) + "\n+Link:+" + linkFix);
						
						if(rPost.image() != null) {
							if(rPost.image().endsWith("gif"))
								m.doc(rPost.image());
							else
								m.photo(rPost.image());
						}
						m.send((Callback<Object>[]) null);
					}
				} else if(text.toLowerCase().contains("блять колдун")) {
					System.out.println("blyat koldun");
					
					for (int i = 0; i < 10; i++) {
						new Message()
						.from(group)
						.to(json.getInt("peer_id"))
						.text("блять+колдун")
						.send((Callback<Object>[]) null);
					}
				}
			} catch (Exception e) {
				new Message()
				.from(group)
				.to(json.getInt("peer_id"))
				.text("Exception:"+e.getMessage().replaceAll(" ", "+"))
				.send((Callback<Object>[]) null);
			}
		});
	}
	
}
