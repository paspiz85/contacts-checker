package it.paspiz85.contacts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

public class Facebook {

	public static void main(String[] args) throws Exception {
		Map<String, String> result = new Facebook().readByUrl(new BufferedReader(new FileReader("fbids.txt")));
		for (Entry<String, String> i : result.entrySet()) {
			System.out.println(i.getKey() + "#" + i.getValue());
		}
	}
	
	public void printIds(PrintStream out) throws IOException {
		Map<String, String> result = getIds();
		for (Entry<String, String> i : result.entrySet()) {
			out.println(i.getKey() + "," + Test.URL_FACEBOOK + i.getValue());
		}
	}
	
	public Map<String, String> readByName(BufferedReader in) throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		while (true) {
			String line = in.readLine();
			if (line == null) {
				return result;
			}
			int i = line.lastIndexOf(",");
			String url = line.substring(i + 1);
			String name = line.substring(0, i);
			result.put(name, url);
		}
	}
	
	public Map<String, String> readByUrl(BufferedReader in) throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		while (true) {
			String line = in.readLine();
			if (line == null) {
				return result;
			}
			int i = line.lastIndexOf(",");
			String url = line.substring(i + 1);
			String name = line.substring(0, i);
			result.put(url, name);
		}
	}

	private final String baseParams;

	private final String facebookBaseUrl = "https://graph.facebook.com/v2.2/";

	private final HttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());

	private final String token;

	public Facebook() {
		token = System.getProperty("facebook.token");
		baseParams = "?access_token=" + token + "&format=json&method=get&pretty=1&suppress_http_code=1";
	}

	public Map<String, String> getIds() throws IOException {
		HashMap<String, String> result = new HashMap<String, String>();
		String url = facebookBaseUrl + "me/taggable_friends" + baseParams;
		HttpGet request = new HttpGet(url);
		HttpResponse response = httpClient.execute(request);
		JsonReader reader = Json.createReader(response.getEntity().getContent());
		JsonObject data = reader.readObject();
		for (JsonValue i : data.getJsonArray("data")) {
			JsonObject obj = (JsonObject) i;
			String name = obj.getString("name");
			String pictureUrl = obj.getJsonObject("picture").getJsonObject("data").getString("url");
			url = facebookBaseUrl + "search" + baseParams + "&q=" + URLEncoder.encode(name, "UTF-8") + "&type=user&fields=id,name,picture";
			request = new HttpGet(url);
			response = httpClient.execute(request);
			JsonReader reader2 = Json.createReader(response.getEntity().getContent());
			JsonObject data2 = reader2.readObject();
			String id = null;
			for (JsonValue i2 : data2.getJsonArray("data")) {
				JsonObject obj2 = (JsonObject) i2;
				String pictureUrl2 = obj2.getJsonObject("picture").getJsonObject("data").getString("url");
				if (pictureUrl.equals(pictureUrl2)) {
					id = obj2.getString("id");
					break;
				}
			}
			reader2.close();
			if (id != null) {
				result.put(name, id);
				// printDetails(id);
			}
		}
		reader.close();
		return result;
	}

	void printDetails(String id) throws IOException, ClientProtocolException {
		String url = facebookBaseUrl
				+ id
				+ baseParams
				+ "&fields=id,about,age_range,bio,birthday,context,currency,devices,education,email,favorite_athletes,favorite_teams,gender,hometown,inspirational_people,installed,is_verified,languages,link,locale,location,name,political,quotes,relationship_status,religion,significant_other,timezone,third_party_id,verified,website,work";
		HttpGet request = new HttpGet(url);
		HttpResponse response = httpClient.execute(request);
		IOUtils.copy(response.getEntity().getContent(), System.out);
		System.out.println();
	}
}
