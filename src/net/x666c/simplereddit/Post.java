package net.x666c.simplereddit;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class Post {
	
	private final JSONObject post;
	
	Post(Object data) {
		post = (JSONObject) data;
	}
	
	// --------------------------------------- Content --------------------------------------- //
	
	public String title() {
		return data().getString("title");
	}
	
	public String text() {
		return data().getString("selftext");
	}
	
	public String image() {
		String url = data().getString("url");
		
		if(checkIfURLIsImage(url))
			return url;
		else
			return null;
	}
	
	public String video() {
		return extractVideo();
	}
	
	// If some news article is linked idk
	public String link() {
		String possibleLink = data().getString("url");
		
		if(possibleLink.contains(id())) // No links (be pointing to self)
			return null;
		else
			return possibleLink;
	}
	
	// --------------------------------------- Meta --------------------------------------- //
	
	public int score() {
		return data().getInt("ups") + data().getInt("downs");
	}
	
	public Instant creationTime() {
		return Instant.ofEpochSecond(data().getLong("created_utc"));
	}
	
	public Instant timeSinceCreation() {
		return Instant.now().minusSeconds(creationTime().getEpochSecond());
	}
	
	public String postTime() {
		Instant time = creationTime();
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withLocale(Locale.UK).withZone(ZoneId.systemDefault()).format(time);
	}
	
	public String timeSincePost() {
		Instant time = creationTime();
		return Date.from(time).toString();
	}
	
	public String id() {
		return data().getString("id");
	}
	
	public String kind() {
		return post.getString("kind");
	}
	
	// --------------------------------------- The rest --------------------------------------- //
	
	// No need to implement all json fields as separate methods. Let users do it themselves duh
	/**
	 * @return json with following structure:
	 * {
	 * 		kind: <string>
	 * 		data: {...}
	 * }
	 */
	public JSONObject getJSON() {
		return post;
	}
	
	/**
	 * Same as calling <code>getJSON()</code>, but without <code>kind: <string></code> field
	 * @return json with following structure:
	 * {
	 * 		// lots of metadata crap here
	 * }
	 */
	public JSONObject data() {
		return post.getJSONObject("data");
	}
	
	
	
	private boolean checkIfURLIsImage(String url) {
		try {
			HttpsURLConnection check = Reddit.initConnection(url);
			return check.getContentType().startsWith("image");
		} catch (Exception e) {
			return false;
		}
	}
	
	private String extractVideo() {
		try {
			boolean isCrosspostSource = data().isNull("crosspost_parent_list");// && !data().isNull("media"); // idk if the second is necessary
			
			if (!isCrosspostSource) {
				JSONArray parents = data().getJSONArray("crosspost_parent_list");
				JSONObject crosspostSource = parents.getJSONObject(0);
				if (!crosspostSource.isNull("media")) {
					return parseMediaObject(crosspostSource.getJSONObject("media"));
				} else {
					return null; // Imply that crosspost depth is 1
				}
			} else {
				if(data().isNull("media"))
					return null;
				return parseMediaObject(data().getJSONObject("media"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String parseMediaObject(JSONObject mediaNotNull) {
		if (mediaNotNull.isNull("type")) { // null == hosted on reddit(?)
			return mediaNotNull.getJSONObject("reddit_video").getString("fallback_url");
		} else { // hosted elsewhere
			String mediaType = mediaNotNull.getString("type");
			
			if (mediaType.equals("gfycat.com")) { // if hosted on gfycat.com
				return mediaNotNull.getJSONObject("oembed").getString("thumbnail_url"); // Only returns a thumbnail gif. Later will write a decoder to get a full video link
			}
			// else if(other provider) { // if hosted somewhere else
			// } 
		}
		
		return null; // if none match. should only be executed if encountered an uncpecified hosting
	}
}
