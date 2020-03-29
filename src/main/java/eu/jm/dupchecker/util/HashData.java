package eu.jm.dupchecker.util;

public class HashData {
	private final String partialHash;
	private final String fullHash;
	
	public HashData(String partialHash, String fullHash) {
		this.partialHash = partialHash;
		this.fullHash=fullHash;
	}

	public String getFullHash() {
		return fullHash;
	}

	public String getPartialHash() {
		return partialHash;
	}
}
