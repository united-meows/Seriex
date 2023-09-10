package pisi.unitedmeows.seriex.util.config;

import java.io.File;

import pisi.unitedmeows.seriex.Seriex;


public interface IConfig {
	String name();

	default File parentDirectory() {
		return Seriex.get().plugin().getDataFolder();
	}
	
	default String todo() {
		return FileManager.TODO;
	}
}
