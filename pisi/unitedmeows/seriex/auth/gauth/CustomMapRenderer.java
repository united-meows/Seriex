package pisi.unitedmeows.seriex.auth.gauth;

import java.awt.image.BufferedImage;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class CustomMapRenderer extends MapRenderer {
	private final UUID uuid;
	private final BufferedImage image;

	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {
		if (player.getUniqueId().equals(uuid)) {
			canvas.drawImage(0, 0, this.image);
		}
	}

	public CustomMapRenderer(UUID uuid, BufferedImage image) {
		this.image = image;
		this.uuid = uuid;
	}
}
