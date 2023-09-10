package pisi.unitedmeows.seriex.util.unsafe;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.safety.Try;

public class UnsafeIO {
	/**
	 * Returns true if the file has been created.
	 */
	public static boolean createNewFile(File file) {
		if (!file.exists()) {
			try {
				return file.createNewFile();
			}
			catch (Exception e) {}
		}
		return false;
	}

	/**
	 * Returns true if the file has been deleted.
	 */
	public static boolean forceDelete(File file) {
		AtomicBoolean deleted = new AtomicBoolean(true);
		// set file to delete on exit, if everything fails
		file.deleteOnExit();
		try {
			Files.deleteIfExists(file.toPath());
		}
		catch (Exception e) {
			Try.safe(file::delete, () -> {
				try {
					Files.write(file.toPath(), new byte[0]);
				}
				catch (Exception e2) {
					// i give up, also there is a third exception that could be thrown at file::delete but
					// we dont care enough, 2 exception messages should help us (hopefully)
					Seriex.get().logger().error("Could not delete file {} ({} - {})", file, e2.getLocalizedMessage(), e.getLocalizedMessage());
					deleted.set(false);
				}
			});
		}
		return deleted.get();
	}
}
