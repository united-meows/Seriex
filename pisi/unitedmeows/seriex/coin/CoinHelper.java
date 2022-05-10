package pisi.unitedmeows.seriex.coin;

import java.util.List;

import pisi.unitedmeows.seriex.database.structs.impl.StructPlayerWallet;

// TODO @slowcheetah you can re-code if you want
public class CoinHelper {
	public static ICoin getCoinFromHash(long hash) {
		return null;
	}

	public List<ICoin> getCoinsFromWallet(String wallet) {
		return null;
	}

	public List<ICoin> getCoinFromPlayer(StructPlayerWallet playerCoinFromDatabase) {
		return getCoinsFromWallet(playerCoinFromDatabase.player_wallet);
	}
}
