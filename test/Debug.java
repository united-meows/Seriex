package test;

import com.google.gson.Gson;
import com.mysql.jdbc.log.Log;
import dev.derklaro.reflexion.Reflexion;
import org.bukkit.Bukkit;
import org.slf4j.LoggerFactory;
import pisi.unitedmeows.seriex.Seriex;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Debug {
	public static final byte HANDSHAKE = 0x00, STATUS = 0x00, PING = 0x01;
	public static final int STATUS_HANDSHAKE = 1;

	public static void main(String... args) throws Exception {
		System.out.print("a" + System.lineSeparator());
		System.out.print("a" + System.lineSeparator());
		System.out.print("a");
	}

	public record AuthRegister(String email, String password) {}

	public static int readVarInt(DataInputStream in) throws IOException {
		int i = 0;
		int j = 0;
		while (true) {
			int k = in.readByte();

			i |= (k & 0x7F) << j++ * 7;

			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}

			if ((k & 0x80) != 128) {
				break;
			}
		}

		return i;
	}

	public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.writeByte(paramInt);
				return;
			}

			out.writeByte(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}
}
