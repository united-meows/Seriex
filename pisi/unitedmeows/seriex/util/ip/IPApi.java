package pisi.unitedmeows.seriex.util.ip;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import pisi.unitedmeows.seriex.Seriex;

public class IPApi {
	private static final Gson gson = new Gson();

	public static IPApiResponse response(String adress) {
		try {
			URL url = new URL("http://ip-api.com/json/" + adress);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
			String currentLine;
			StringBuilder response = new StringBuilder();
			while ((currentLine = reader.readLine()) != null) {
				response.append(currentLine);
			}
			reader.close();
			return gson.fromJson(response.toString(), IPApiResponse.class);
		}
		catch (Exception e) {
			e.printStackTrace();
			Seriex.get().logger().error("Couldnt get response from IP API!");
			return null;
		}
	}
}
