package eu.jm.dupchecker.util;

public class Options {
	private HashType hashType = HashType.PARTIAL;
	private int maxDepth = -1;

	public Options() {

	}

	public Options setHashType(HashType hashType) {
		this.hashType = hashType;
		return this;
	}

	public Options setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
		return this;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public HashType getHashType() {
		return this.hashType;
	}
}
