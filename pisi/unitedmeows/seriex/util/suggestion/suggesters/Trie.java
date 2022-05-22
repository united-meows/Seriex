package pisi.unitedmeows.seriex.util.suggestion.suggesters;

import java.util.*;
import java.util.Map.Entry;

import pisi.unitedmeows.seriex.util.collections.GlueList;

public class Trie {
	public static class Node {
		final Map<Character, Node> children;
		boolean isEnd;

		Node() {
			children = new HashMap<>(0);
			isEnd = false;
		}
	}

	private final Node root;
	public static final char WILDCARD = '?';

	public Trie() {
		root = new Node();
	}

	public Trie(Collection<String> col) {
		this();
		for (String s : col) {
			this.add(s);
		}
	}

	public void add(String str) {
		Node curr = root;
		for (int i = 0; i < str.length(); i++) {
			if (curr.children.get(str.charAt(i)) == null) {
				curr.children.put(str.charAt(i), new Node());
			}
			curr = curr.children.get(str.charAt(i));
		}
		curr.isEnd = true;
	}

	public List<String> wildcardMatches(String str) {
		List<String> wildcardMatches = new GlueList<>();
		wildcardTraverse(str, new StringBuilder(), root, 0, wildcardMatches);
		return wildcardMatches;
	}

	private void wildcardTraverse(String pattern, StringBuilder prefix, Node root, int len, List<String> wildcardMatches) {
		if (root == null) return;
		if (len == pattern.length()) {
			if (root.isEnd) {
				wildcardMatches.add(prefix.toString());
			}
			return;
		}
		if (pattern.charAt(len) == WILDCARD) {
			for (Entry<Character, Node> e : root.children.entrySet()) {
				prefix.append(e.getKey());
				wildcardTraverse(pattern, prefix, e.getValue(), len + 1, wildcardMatches);
				prefix.deleteCharAt(prefix.length() - 1);
			}
		} else {
			prefix.append(pattern.charAt(len));
			wildcardTraverse(pattern, prefix, root.children.get(pattern.charAt(len)), len + 1, wildcardMatches);
			prefix.deleteCharAt(prefix.length() - 1);
		}
	}

	private Node getNode(String str) {
		Node node = root;
		for (int i = 0; i < str.length(); i++) {
			Node child = node.children.get(str.charAt(i));
			if (child == null) return null;
			node = child;
		}
		return node;
	}

	public List<String> prefixedWords(String str) {
		Node curr = getNode(str);
		List<String> prefixedWords = new GlueList<>();
		DFS(curr, str, prefixedWords);
		return prefixedWords;
	}

	private static void DFS(Node root, String prefix, List<String> list) {
		if (root == null) return;
		if (root.isEnd) {
			list.add(prefix);
		}
		for (Iterator<Entry<Character, Node>> iterator = root.children.entrySet().iterator(); iterator.hasNext();) {
			Entry<Character, Node> e = iterator.next();
			DFS(e.getValue(), prefix + e.getKey(), list);
		}
	}
}
