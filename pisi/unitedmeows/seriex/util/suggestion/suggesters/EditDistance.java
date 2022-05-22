package pisi.unitedmeows.seriex.util.suggestion.suggesters;

import pisi.unitedmeows.seriex.util.suggestion.WordList;

public class EditDistance {
	public static int damerauLevenshtein(CharSequence source, CharSequence target) {
		if (source == null || target == null) throw new IllegalArgumentException("Parameter must not be null");
		int sourceLength = source.length();
		int targetLength = target.length();
		if (sourceLength == 0) return targetLength;
		if (targetLength == 0) return sourceLength;
		int[][] dist = new int[sourceLength + 1][targetLength + 1];
		for (int i = 0; i < sourceLength + 1; i++) {
			dist[i][0] = i;
		}
		for (int j = 0; j < targetLength + 1; j++) {
			dist[0][j] = j;
		}
		for (int i = 1; i < sourceLength + 1; i++) {
			for (int j = 1; j < targetLength + 1; j++) {
				int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
				dist[i][j] = Math.min(Math.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1), dist[i - 1][j - 1] + cost);
				if (i > 1 && j > 1 && source.charAt(i - 1) == target.charAt(j - 2) && source.charAt(i - 2) == target.charAt(j - 1)) {
					dist[i][j] = Math.min(dist[i][j], dist[i - 2][j - 2] + 1);
				}
			}
		}
		return dist[sourceLength][targetLength];
	}

	public static int levenshtein(String source, String target) {
		int m = source.length();
		int n = target.length();
		int[][] dp = new int[m + 1][n + 1];
		for (int i = 0; i <= n; i++) {
			dp[0][i] = i;
		}
		for (int i = 0; i <= m; i++) {
			dp[i][0] = i;
		}
		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				if (source.charAt(i - 1) == target.charAt(j - 1)) {
					dp[i][j] = dp[i - 1][j - 1];
				} else {
					dp[i][j] = 1 + WordList.min(dp[i][j - 1], dp[i - 1][j], dp[i - 1][j - 1]);
				}
			}
		}
		return dp[m][n];
	}
}
