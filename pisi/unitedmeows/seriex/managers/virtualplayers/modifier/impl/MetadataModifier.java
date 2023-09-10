package pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.server.v1_8_R3.DataWatcher;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.VirtualModifier;

public class MetadataModifier extends VirtualModifier {

	public MetadataModifier(VirtualPlayer npc) {
		super(npc);
	}

	public boolean getFlag(final int flag) {
		DataWatcher dataWatcher = npc.entityPlayer().getDataWatcher();
		return (dataWatcher.getByte(0) & 0xFF & 1 << flag) != 0;
	}

	public void setFlag(MetadataType type, MetadataFlag flag, boolean state) {
		DataWatcher dataWatcher = npc.entityPlayer().getDataWatcher();
		final byte b0 = dataWatcher.getByte(type.index);

		byte enabled = (byte) (b0 & 0xFF | 1 << flag.offset);
		byte disabled = (byte) (b0 & ~(1 << flag.offset));

		// if we dont cast to byte
		// https://b.cgas.io/_QfBZH9q96rr.png
		// this happens
		dataWatcher.watch(type.index, (byte) (type == MetadataType.SKIN_FLAGS ? flag.offset : state ? enabled : disabled));
	}

	public enum MetadataType {
		PLAYER_FLAGS(0),
		SKIN_FLAGS(10);

		public final int index;
		public MetadataFlag[] flags;

		MetadataType(int index) {
			this.index = index;
		}

		public static MetadataType fromString(String name) {
			for (MetadataType enumValue : MetadataType.values()) {
				if (enumValue.name().equalsIgnoreCase(name)) { return enumValue; }
			}
			return null;
		}

		static {
			for (MetadataType metadataType : values()) {
				metadataType.flags = Arrays.stream(MetadataFlag.values())
							.filter(flag -> flag.type == metadataType)
							.toArray(MetadataFlag[]::new);
			}
		}
	}

	public enum MetadataFlag {

		/*	@DISABLE_FORMATTING
		 * 
		 * how to read hex tutorial!!
		 * lets assume you have
		 * 0x87D
		 * 		
		 *	in hex,
		 *		0 = 0
		 *		1 = 1
		 *		  .
		 *		  .
		 * 	  .
		 * 	9 = 9
		 * 	10 = A
		 * 	11 = B
		 * 	12 = C
		 * 	13 = D
		 * 	14 = E
		 * 	15 = F
		 * 
		 * so in order to calculate 0x87D
		 * 
		 * 0		x	 |		  8			   7				D
		 * 			 |  16^2 * 8		16^1 * 7		just add	
		 * useless  |
		 * 
		 * so you get
		 * 
		 * 16^2 * 8 + 16^1 * 7 + D
		 *  				=
		 * 	  2048 + 112 + 13 
		 * 				=
		 * 			 2173
		 * 
		 * @ENABLE_FORMATTING
		 */

		/*
		 * we have a conflict,
		 * in mcp , 2 is mentioned but 5 is not,
		 * in wiki.vg , 5 is mentioned but 2 is not;
		 * soooooooooooooooooooooooooooooooooooooooooooooooooooooooooo idk LOL
		 * 0) is burning;
		 * 1) is sneaking
		 * 2)* is riding something (not mentioned in wiki.vg)
		 * 3) is sprinting
		 * 4) is eating
		 * 5)* is invisible (not mentioned in documentation)
		 */

		ON_FIRE(MetadataType.PLAYER_FLAGS , 0 /* 0x01 */),
		SNEAKING(MetadataType.PLAYER_FLAGS , 1 /* 0x02 */),
		RIDING(MetadataType.PLAYER_FLAGS , 2 /* ??? */),
		SPRINTING(MetadataType.PLAYER_FLAGS , 3/* 0x08 */),
		USING_ITEM(MetadataType.PLAYER_FLAGS , 4/* 0x10 */),
		INVISIBLE(MetadataType.PLAYER_FLAGS , 5/* 0x20 */),

		ALL_SKIN_FLAGS(MetadataType.SKIN_FLAGS , 0x7F);

		public final MetadataType type;
		public final int offset;

		MetadataFlag(MetadataType type, int offset) {
			this.offset = offset;
			this.type = type;
		}

		public static MetadataFlag fromString(String name) {
			for (MetadataFlag enumValue : MetadataFlag.values()) {
				if (enumValue.name().equalsIgnoreCase(name)) { 
					return enumValue;
				}
			}
			return null;
		}

		public static MetadataFlag[] fromOffset(MetadataType type, int offset) {
			List<MetadataFlag> flags = new ArrayList<>();
			for (MetadataFlag flag : values()) {
				if ((offset & flag.offset) != 0 && type == flag.type) {
					flags.add(flag);
				}
			}
			return flags.toArray(MetadataFlag[]::new);
		}
	}

}
