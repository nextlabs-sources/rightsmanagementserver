package com.nextlabs.rms.services.manager.node;

public class NegatePolicyNode implements Node {
	private final Node node;

	public NegatePolicyNode(Node node) {
		this.node = node;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public String toString() {
		return "Not {node: " + node + "}";
	}

	@Override
	public Node negate() {
		return node;
	}
}
