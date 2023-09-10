package pisi.unitedmeows.seriex.database.util.reflection;

import java.lang.reflect.Field;

import pisi.unitedmeows.seriex.database.structs.IStruct;

public record DatabaseField(String name, Field field, FieldType type, Class<? extends IStruct> ownerClass, boolean primaryKey, boolean discriminator) {
	public static DatabaseField create(String name, Field field, FieldType fieldType, Class<? extends IStruct> ownerClass, boolean primaryKey, boolean discriminator) {
		return new DatabaseField(name, field, fieldType, ownerClass, primaryKey, discriminator);
	}
}
