package pisi.unitedmeows.seriex.coin;

// TODO @slowcheetah you can re-code if you want
public class Coin implements ICoin {
	public long hash;
	public String linkedWallet;

	@Override
	public void get() {
		// TODO Auto-generated method stub
	}

	@Override
	public void set(ICoin coin) {
		// TODO Auto-generated method stub
	}

	@Override
	public void calculateHash() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isValidCoin() {
		return (~hash & 0b1000) != 0;
	}
}
