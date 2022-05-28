package test;

import pisi.unitedmeows.seriex.util.suggestion.suggesters.EditDistance;

public class LevensteinTest {
	public static void main(String... args) {
		String anan = "mal oç ersin puhaha";
		String anan2 = "mal oç ersin puhah123a";
		System.out.println(EditDistance.levenshtein(anan, anan2));
	}
}
