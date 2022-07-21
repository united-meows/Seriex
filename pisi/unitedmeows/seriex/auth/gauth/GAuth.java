package pisi.unitedmeows.seriex.auth.gauth;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;

import de.taimos.totp.TOTP;

public class GAuth {
	private static final String IMAGE_URL_FORMAT = "https://www.google.com/chart?chs=130x130&chld=M%%7C0&cht=qr&chl=%s";

	public static String generateSecretKey() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		Base32 base32 = new Base32();
		return base32.encodeToString(bytes);
	}

	public static String getTOTPCode(String secretKey) {
		Base32 base32 = new Base32();
		byte[] bytes = base32.decode(secretKey);
		String hexKey = Hex.encodeHexString(bytes);
		return TOTP.getOTP(hexKey);
	}

	public static String getGoogleAuthenticatorBarCode(String secretKey, String username, String issuer) {
		try {
			return "otpauth://totp/" + URLEncoder.encode(issuer + ":" + username, "UTF-8").replace("+", "%20") + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20") + "&issuer="
						+ URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public static BufferedImage createQRCode(String barCodeData) {
		try {
			String imageUrl = String.format(IMAGE_URL_FORMAT, URLEncoder.encode(barCodeData, "UTF-8"));
			return ImageIO.read(new URL(imageUrl));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void generateAndGiveMap(Player player, BufferedImage image) {
		if (image != null) {
			MapView mapView = Bukkit.getServer().createMap(player.getWorld());
			mapView.getRenderers().forEach(mapView::removeRenderer);
			mapView.addRenderer(new CustomMapRenderer(player.getUniqueId(), image));
			ItemStack mapItem = new ItemStack(Material.MAP, 1, mapView.getId());
			ItemMeta mapMeta = mapItem.getItemMeta();
			mapMeta.setLore(Collections.singletonList("QR Code Map"));
			mapItem.setItemMeta(mapMeta);
			player.sendMap(mapView);
			player.getInventory().addItem(mapItem);
			player.updateInventory();
		}
	}
}
