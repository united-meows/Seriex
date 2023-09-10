package pisi.unitedmeows.seriex.util.config.single.util;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

public class ConfigValue<X> {
	private final String key, comment;
	private X value;

	private CommentedFileConfig baseConfig;

	public ConfigValue(String key, X value) {
		this(key, null, value);
	}

	public ConfigValue(String key, String comment, X value) {
		this.key = key;
		this.comment = comment;
		this.value = value;
	}

	public void config(CommentedFileConfig cfg) {
		this.baseConfig = cfg;
	}

	public X value() {
		X cfgValue = baseConfig.get(key); // get config value
		if (cfgValue == null) // if it has no value
			this.value(this.value); // change config value to default value
		else return cfgValue;
		return this.value; // return value
	}

	public void value(X value) {
		this.value = value;
		baseConfig.set(this.key, value);
		if (this.comment != null)
			baseConfig.setComment(this.key, this.comment);
	}

	public String key() {
		return key;
	}

	public String comment() {
		return comment;
	}
}
