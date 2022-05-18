package pisi.unitedmeows.seriex.database.structs.impl.areas;

import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

@Struct(name = "area")
public class StructArea implements IStruct {

	@Column
	public int area_id;
	@Column
	public String area_coid;
	@Column
	public double area_minX;
	@Column
	public double area_minY;
	@Column
	public double area_minZ;
	@Column
	public double area_maxX;
	@Column
	public double area_maxY;
	@Column
	public double area_maxZ;

	@Override
	public String[] getColumns() {
		return DatabaseReflection.getColumnsFromClass(this.getClass()).item1();
	}

	@Override
	public YSQLCommand[] setColumns() {
		return DatabaseReflection.setAndGetColumns(this.getClass());
	}
}
