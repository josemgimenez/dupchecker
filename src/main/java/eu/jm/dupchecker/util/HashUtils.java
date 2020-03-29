package eu.jm.dupchecker.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

public class HashUtils {
	/*
	 * This amount of bytes will be included in the partial hash.
	 */
	public static final int PARTIAL_HASH_BYTES = 1024 * 1024;
	private static final String ALGORITHM = "SHA-512";

	private HashUtils() {
		// Nobody needs an instance
	}

	public static HashData getHashData(File f, HashType type) {
		try {
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);

			final byte[] b = new byte[PARTIAL_HASH_BYTES];

			final String partialHash;
			final String fullHash;
			try (FileInputStream fi = new FileInputStream(f)) {
				int read = fi.read(b);
				md.update(b);

				partialHash = new String(Hex.encodeHex(((MessageDigest) md.clone()).digest(b)));

				if (read == f.length()) {
					fullHash = partialHash;
				} else if (type == HashType.FULL) {
					while ((read = fi.read(b)) > 0) {
						md.update(b, 0, read);
					}

					fullHash = new String(Hex.encodeHex(md.digest(b)));
				} else {
					fullHash = null;
				}
			}

			return new HashData(partialHash, fullHash);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
