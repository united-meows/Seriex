package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

public abstract class Test {
	protected Test() {}

	public String[] message = null;

	public TestState run() {
		return null;
	}

	public File testFile(String name) {
		String desktopPath = String.format("%s\\", FileSystemView.getFileSystemView().getHomeDirectory().toString());
		return new File(desktopPath + "\\test\\" + name);
	}

	public void message(Throwable throwable) {
		Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
		dejaVu.add(throwable);
		List<String> string = new ArrayList<>();
		synchronized (this) {
			// Print our stack trace
			string.add(throwable.toString());
			StackTraceElement[] trace = throwable.getStackTrace();
			for (StackTraceElement traceElement : trace) {
				string.add("at " + traceElement);
			}
			// Print suppressed exceptions, if any
			for (Throwable se : throwable.getSuppressed()) {
				string.add(" supressed throwable: ");
				string.add(se.toString());
			}
			// Print cause, if any
			Throwable ourCause = throwable.getCause();
			if (ourCause != null) {
				string.add(" cause: ");
				string.add(ourCause.toString());
			}
		}
		message = string.toArray(new String[0]);
	}

	public void message(String... message) {
		this.message = message;
	}
}
