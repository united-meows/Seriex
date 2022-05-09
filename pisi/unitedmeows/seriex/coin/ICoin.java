package pisi.unitedmeows.seriex.coin;

// TODO @slowcheetah you can re-code if you want
public interface ICoin {
	void get();

	void set(ICoin coin);

	void calculateHash();

	boolean isValidCoin();
}
