package pisi.unitedmeows.seriex.managers.virtualplayers.profile;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;

public class VirtualProfile {
	private String name, textureName;
	private UUID uniqueId;
	private Collection<Property> properties;
	private static final Pattern UNIQUE_ID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
	private static final Type LIST_TYPE = new TypeToken<Set<Property>>() {}.getType();
	private static final Gson GSON = new GsonBuilder().serializeNulls().create();

	public static VirtualProfile create(String displayName) {
		return VirtualProfile.create(displayName, displayName);
	}

	public static VirtualProfile create(String displayName, String texture_ign) {
		JsonElement identifierElement = makeRequest(String.format("https://api.mojang.com/users/profiles/minecraft/%s", texture_ign));
		if (identifierElement == null || !identifierElement.isJsonObject())
			return null;

		JsonObject jsonObject = identifierElement.getAsJsonObject();
		if (jsonObject.has("id")) {
			String uuid = UNIQUE_ID_PATTERN.matcher(jsonObject.get("id").getAsString()).replaceAll("$1-$2-$3-$4-$5");
			JsonElement profileElement = makeRequest(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=%b", uuid.replace("-", ""), false));
			if (profileElement == null || !profileElement.isJsonObject())
				return null;

			JsonObject object = profileElement.getAsJsonObject();
			List<Property> properties = new ArrayList<>();
			if (object.has("name") && object.has("properties")) {
				properties.addAll(GSON.fromJson(object.get("properties"), LIST_TYPE));
			} else return null;

			VirtualProfile virtualProfile	 = new VirtualProfile(
						displayName, 
						UUID.randomUUID(),
//						UUID.fromString(uuid), 
						properties);
			virtualProfile.textureName = texture_ign;
			return virtualProfile;
		}
		return null;
	}

	private VirtualProfile(String name, UUID uuid, Collection<Property> properties) {
		this.name = name;
		this.uniqueId = uuid;
		this.properties = properties;
	}

	public GameProfile toGameProfile() {
		GameProfile gameProfile = new GameProfile(uniqueId, name);
		properties.forEach(prop -> gameProfile.getProperties().put(prop.getName(), new com.mojang.authlib.properties.Property(prop.getName(), prop.getValue(), prop.getSignature())));
		return gameProfile;
	}

	public String getName() { return name; }

	public String getTextureName() { return textureName; }

	protected static JsonElement makeRequest(String apiUrl) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
			connection.setReadTimeout(5000);
			connection.setConnectTimeout(5000);
			connection.setUseCaches(true);
			connection.connect();
			if (connection.getResponseCode() == 200) {
				try (Reader reader = new InputStreamReader(connection.getInputStream(), UTF_8)) {
					return new JsonParser().parse(reader);
				}
			}
			return null;
		}
		catch (IOException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VirtualProfile [");
		if (name != null) builder.append("name=").append(name).append(", ");
		if (uniqueId != null) builder.append("uniqueId=").append(uniqueId).append(", ");
		if (properties != null) builder.append("properties=").append(properties);
		builder.append("]");
		return builder.toString();
	}

	public static class Property {

		private final String name;
		private final String value;
		private final String signature;

		public Property(String name, String value, String signature) {
			this.name = name;
			this.value = value;
			this.signature = signature;
		}

		public String getName() { return this.name; }

		public String getValue() { return this.value; }

		public String getSignature() { return this.signature; }

		public boolean isSigned() { return this.signature != null; }
	}
}
