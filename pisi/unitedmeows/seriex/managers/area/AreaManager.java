package pisi.unitedmeows.seriex.managers.area;

import java.util.List;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.util.IArea;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.lists.GlueList;

public class AreaManager extends Manager {
	public List<IArea> areaList = new GlueList<>();

	@Override
	public void start(Seriex seriex) {
		areaList.forEach(IArea::enable);
	}

	@Override
	public void cleanup() throws SeriexException {
		areaList.forEach(IArea::disable);
	}
}
