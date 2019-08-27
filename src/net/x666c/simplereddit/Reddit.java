package net.x666c.simplereddit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class Reddit {
	
	private static final String REDDIT = "https://www.reddit.com/r/";
	private static final String REQUEST = ".json?limit=";
	
	public static Reddit newConnection(String subreddit, Type postType, Time sort, int postLimitRelative) {
		return new Reddit(subreddit, postType, sort, postLimitRelative);
	}
	public static Reddit newConnection(String subreddit, Type postType, int postLimitRelative) {
		return newConnection(subreddit, postType, Time.PastMonth, postLimitRelative);
	}
	
	public static Post newConnection(String postLink) {
		try {
			HttpsURLConnection check = initConnection(postLink + "/.json");
			
			String content = new BufferedReader(new InputStreamReader(check.getInputStream())).lines().reduce("", String::concat);
			
			JSONObject data = new JSONArray(content).getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0);
			return new Post(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private final String name;
	
	private HttpsURLConnection connection;
	
	private int requestedLength, actualLength;
	
	private String jsonLink;
	private JSONObject json;
	
	private List<Post> posts;
	private Post startingPost = null;
	
	private Reddit(String subreddit, Type type, Time sort, int limit) {
		this.name = subreddit;
		
		jsonLink = REDDIT + subreddit + "/" + type + "/" + REQUEST + limit + sort;
		System.out.println(jsonLink);
		
		requestedLength = limit;
		
		refresh();
	}

	public void refresh() {
		try {
			
			if(startingPost != null)
				jsonLink += "&after=" + startingPost.kind() + "_" + startingPost.id();
			
			connection = initConnection(jsonLink); // 1.56 seconds
			
			json = reparseJSON(); // 0.36 seconds
			
			actualLength = data().getInt("dist");
			
			posts = new ArrayList<>(actualLength);
			
			JSONArray children = data().getJSONArray("children");
			for (Object post : children) {
				posts.add(new Post(post)); // Can probably yield and unsorted list, oh well
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static HttpsURLConnection initConnection(String to) throws Exception {
		HttpsURLConnection connection = (HttpsURLConnection) new URL(to).openConnection();
		connection.setRequestProperty("Accept-Language", "en-US,en");
		//connection.setRequestProperty("Connection", "keep-alive"); // Hmmm
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 7.0; WOW64; rv:68.0) Gecko/20100101 Firefox/68.0");
		
		if(connection.getResponseCode() == 429) { // Too many requests
			throw new RuntimeException("Too Many Requests: 429");
		}
		
		return connection;
	}
	
	private Stream<String> getUpdatedStream() throws Exception {
		InputStream in = connection.getInputStream();
		return new BufferedReader(new InputStreamReader(in), Short.MAX_VALUE).lines();
	}
	
	private JSONObject reparseJSON() throws Exception {
		return new JSONObject(getUpdatedStream().reduce("", String::concat));
	}
	
	
	// -------------------------------------- Public ------------------------------------------ //
	
	public JSONObject data() {
		return json.getJSONObject("data");
	}
	
	public String modhash() {
		return data().getString("modhash");
	}
	
	public String before() {
		return data().getString("before");
	}
	
	public String after() {
		return data().getString("after");
	}
	
	public int actualLength() {
		return actualLength;
	}
	
	public int requestedLength() {
		return requestedLength;
	}
	
	/**
	 * Warning: implicitly calls <code>refresh()</code>
	 * @param post Post to start with (exclusive)
	 */
	public void setStartingPost(Post post) {
		startingPost = post;
		refresh();
	}
	
	public Post post(int index) {
		if(index < 0 || index > actualLength) {
			throw new IndexOutOfBoundsException("index Out of bounds: index="+index + " length="+actualLength);
		}
		
		return posts.get(index);
	}
	
	public Post randomPost() {
		return posts.get(new Random().nextInt(actualLength));
	}
	
	public List<Post> posts() {
		ArrayList<Post> ret = new ArrayList<>(posts.size());
		Collections.copy(ret, posts);
		return ret;
	}
	
	public List<Post> postRange(int from, int to) {
		return new ArrayList<>(posts.subList(from, to));
	}
	
	public boolean validSubreddit() {
		return actualLength > 0;
	}
	
	public String name() {
		return name;
	}
	
	public static enum Type {
		Hot,
		New,
		Controversial,
		Top,
		Rising;
		
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	public static enum Time {
		PastHour("&t=hour"),
		PastDay("&t=day"),
		PastWeek("&t=week"),
		PastMonth("&t=month"),
		PastYear("&t=year"),
		OfAllTime("&t=all");
		
		private final String val;
		private Time(String val) {
			this.val = val;
		}
		
		public String toString() {
			return val;
		}
	}
}