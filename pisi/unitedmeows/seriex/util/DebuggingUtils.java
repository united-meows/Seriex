package pisi.unitedmeows.seriex.util;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

// skidded from /united-meows/complexer
public class DebuggingUtils {
	private DebuggingUtils() {}

	private static boolean containsIgnoreCase(String first, String second) {
		String firstLowercase = first.toLowerCase(Locale.ENGLISH);
		String secondLowercase = second.toLowerCase(Locale.ENGLISH);
		return firstLowercase.contains(secondLowercase);
	}

	/**
	 * Gets the processID from process name
	 */
	public static Optional<Long> getProcessID(final String taskName) {
		return ProcessHandle.allProcesses()
					.filter(p -> p.info().command().filter(cmd -> containsIgnoreCase(cmd, taskName)).isPresent())
					.findFirst()
					.map(ProcessHandle::pid);
	}

	/**
	 * Returns true if the current jar is running from an IDE.
	 */
	public static boolean isRunningFromIDE() {
		final long currentJVMPID = ProcessHandle.current().pid();
		return Set.of("eclipse.exe", "idea64.exe", "idea32.exe").stream()
					.map(DebuggingUtils::getProcessID)
					.filter(Optional::isPresent).map(Optional::get)
					.map(ProcessHandle::of)
					.filter(Optional::isPresent).map(Optional::get)
					.anyMatch(process -> process.descendants().anyMatch(descendant -> descendant.pid() == currentJVMPID));
	}
}
