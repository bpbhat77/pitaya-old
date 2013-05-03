package com.emo.pitaya.web;

import com.fasterxml.jackson.databind.JsonNode;

public class NodeWrapper {

	private final JsonNode node;

	public NodeWrapper(final JsonNode node) {
		this.node = node;
	}

	public NodeWrapper get(final String name) {
		return new NodeWrapper(this.node.get(name));
	}

	public JsonNode node() {
		return node;
	}

	public String toString() {
		return unquote(node.toString());
	}

	private static String unquote(String s) {

		if (s != null
				&& ((s.startsWith("\"") && s.endsWith("\"")) || (s
						.startsWith("'") && s.endsWith("'")))) {

			s = s.substring(1, s.length() - 1);
		}
		return s;
	}
}
