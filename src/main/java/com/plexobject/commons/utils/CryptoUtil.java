package com.plexobject.commons.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.text.DecimalFormat;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.plexobject.commons.io.IOHelper;

public class CryptoUtil {
	public static boolean useSalt = System.getProperties()
			.getProperty("salt", "true").equals("true");
	public static DecimalFormat hexFormat = new DecimalFormat("%02X ");
	static {
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
	}

	private CryptoUtil() {
	}

	public static KeyPair makeKeyPair()
			throws java.security.NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
		kpg.initialize(1024); // size of key
		return kpg.generateKeyPair();
	}

	public static SecretKey makeSecretKey(byte[] input)
			throws java.security.NoSuchAlgorithmException,
			java.security.InvalidKeyException,
			java.security.spec.InvalidKeySpecException {
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		DESKeySpec spec = new DESKeySpec(input);
		return skf.generateSecret(spec);
	}

	public static byte[] encodeKey(SecretKey key)
			throws java.security.NoSuchAlgorithmException,
			java.security.spec.InvalidKeySpecException {
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		DESKeySpec spec = (DESKeySpec) skf.getKeySpec(key, DESKeySpec.class);
		return spec.getKey();
	}

	public static InputStream decrypt(InputStream in, Key key)
			throws java.io.IOException, java.security.NoSuchAlgorithmException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException {
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new CipherInputStream(in, cipher);
	}

	public static byte[] decrypt(byte[] input, Key key)
			throws java.io.IOException, java.security.NoSuchAlgorithmException,
			java.security.InvalidKeyException,
			javax.crypto.NoSuchPaddingException,
			javax.crypto.BadPaddingException,
			javax.crypto.IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(input);
	}

	public static OutputStream encrypt(OutputStream out, Key key)
			throws java.io.IOException, java.security.NoSuchAlgorithmException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException {
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return new CipherOutputStream(out, cipher);
	}

	public static byte[] encrypt(byte[] input, Key key)
			throws java.io.IOException, java.security.NoSuchAlgorithmException,
			java.security.InvalidKeyException,
			javax.crypto.NoSuchPaddingException,
			javax.crypto.BadPaddingException,
			javax.crypto.IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(input);
	}

	public static byte[] encrypt(File inputFile, String passphrase,
			int iterations) throws java.io.IOException,
			java.security.NoSuchAlgorithmException,
			javax.crypto.NoSuchPaddingException,
			javax.crypto.BadPaddingException,
			java.security.InvalidKeyException,
			javax.crypto.IllegalBlockSizeException,
			java.security.InvalidAlgorithmParameterException,
			java.security.spec.InvalidKeySpecException {
		return encrypt(IOHelper.load(inputFile), passphrase, iterations);
	}

	public static byte[] encrypt(byte[] input, String passphrase, int iterations)
			throws java.security.NoSuchAlgorithmException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException,
			javax.crypto.IllegalBlockSizeException,
			javax.crypto.BadPaddingException,
			java.security.InvalidAlgorithmParameterException,
			java.security.spec.InvalidKeySpecException {
		KeySpec ks = new PBEKeySpec(passphrase.toCharArray());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = skf.generateSecret(ks);
		byte[] salt = new byte[8];

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(passphrase.getBytes());
		md.update(input);
		byte[] digest = md.digest();
		System.arraycopy(digest, 0, salt, 0, 8); // create salt value

		AlgorithmParameterSpec aps = new PBEParameterSpec(salt, iterations);
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.ENCRYPT_MODE, key, aps);
		byte[] cbytes = cipher.doFinal(input);
		byte[] output = new byte[salt.length + cbytes.length];
		System.arraycopy(salt, 0, output, 0, salt.length);
		System.arraycopy(cbytes, 0, output, salt.length, cbytes.length);
		System.err
				.println("CryptoUtil.encrypt got bytes " + cbytes.length
						+ ", output size " + output.length + " = "
						+ new String(cbytes));
		/*
*/
		return output;
	}

	public static byte[] decrypt(File inputFile, String passphrase,
			int iterations) throws java.io.IOException,
			java.security.spec.InvalidKeySpecException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException,
			javax.crypto.BadPaddingException,
			java.security.InvalidAlgorithmParameterException,
			javax.crypto.IllegalBlockSizeException,
			java.security.NoSuchAlgorithmException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				inputFile));
		byte[] salt = new byte[8];
		in.read(salt);

		byte[] buffer = new byte[8192];
		int length;
		while ((length = in.read(buffer)) != -1)
			out.write(buffer, 0, length);
		in.close();
		out.close();
		return decrypt(out.toByteArray(), salt, passphrase, iterations);
	}

	public static byte[] decrypt(byte[] rawInput, String passphrase,
			int iterations) throws java.security.NoSuchAlgorithmException,
			java.security.spec.InvalidKeySpecException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException,
			java.security.InvalidAlgorithmParameterException,
			javax.crypto.BadPaddingException,
			javax.crypto.IllegalBlockSizeException {
		byte[] salt = new byte[8];
		System.arraycopy(rawInput, 0, salt, 0, 8); // copy salt value
		byte[] input = new byte[rawInput.length - 8];
		System.arraycopy(rawInput, 8, input, 0, input.length); // copy salt
																// value
		System.err.println("CryptoUtil.decrypt got raw bytes "
				+ rawInput.length + ", input size " + input.length + " = "
				+ new String(input));
		/*
*/

		return decrypt(input, salt, passphrase, iterations);
	}

	public static byte[] decrypt(byte[] input, byte[] salt, String passphrase,
			int iterations) throws java.security.NoSuchAlgorithmException,
			java.security.spec.InvalidKeySpecException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException,
			java.security.InvalidAlgorithmParameterException,
			javax.crypto.BadPaddingException,
			javax.crypto.IllegalBlockSizeException {
		KeySpec ks = new PBEKeySpec(passphrase.toCharArray());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = skf.generateSecret(ks);

		AlgorithmParameterSpec aps = new PBEParameterSpec(salt, iterations);
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.DECRYPT_MODE, key, aps);
		return cipher.doFinal(input);
	}

	public static void encryptFile(File inFile, File outFile,
			String passphrase, int iterations)
			throws java.security.NoSuchAlgorithmException, java.io.IOException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException,
			javax.crypto.IllegalBlockSizeException,
			javax.crypto.BadPaddingException,
			java.security.InvalidAlgorithmParameterException,
			java.security.spec.InvalidKeySpecException {
		KeySpec ks = new PBEKeySpec(passphrase.toCharArray());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = skf.generateSecret(ks);
		byte[] salt = new byte[8];

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(passphrase.getBytes());
		byte[] buffer = new byte[8192];
		int length;
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				inFile));
		while ((length = in.read(buffer)) != -1) {
			md.update(buffer, 0, length);
		}
		in.close();
		byte[] digest = md.digest();
		System.arraycopy(digest, 0, salt, 0, 8); // create salt value

		AlgorithmParameterSpec aps = new PBEParameterSpec(salt, iterations);
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.ENCRYPT_MODE, key, aps);

		in = new BufferedInputStream(new FileInputStream(inFile));
		BufferedOutputStream bout = new BufferedOutputStream(
				new FileOutputStream(outFile));
		bout.write(salt);

		DataOutputStream out = new DataOutputStream(new CipherOutputStream(
				bout, cipher));

		out.writeInt(digest.length);
		out.write(digest);
		while ((length = in.read(buffer)) != -1)
			out.write(buffer, 0, length);
		in.close();
		out.close();
	}

	public static void decryptFile(File inFile, File outFile,
			String passphrase, int iterations)
			throws java.security.NoSuchAlgorithmException, java.io.IOException,
			java.security.spec.InvalidKeySpecException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException,
			java.security.InvalidAlgorithmParameterException,
			javax.crypto.BadPaddingException,
			javax.crypto.IllegalBlockSizeException {
		KeySpec ks = new PBEKeySpec(passphrase.toCharArray());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = skf.generateSecret(ks);

		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(
				inFile));
		byte[] salt = new byte[8];
		bin.read(salt);

		AlgorithmParameterSpec aps = new PBEParameterSpec(salt, iterations);
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.DECRYPT_MODE, key, aps);

		DataInputStream in = new DataInputStream(new CipherInputStream(bin,
				cipher));

		byte[] odigest = new byte[in.readInt()];
		in.readFully(odigest);

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(passphrase.getBytes());

		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(outFile));
		byte[] buffer = new byte[8192];
		int length;
		while ((length = in.read(buffer)) != -1) {
			out.write(buffer, 0, length);
			md.update(buffer, 0, length);
		}
		byte[] ndigest = md.digest();
		if (odigest.length != ndigest.length) {
			throw new RuntimeException("Digest length not matched");
		}
		for (int i = 0; i < odigest.length; i++) {
			if (odigest[i] != ndigest[i]) {
				throw new RuntimeException("Digest byte at " + i
						+ " offset not matched");
			}
		}

		in.close();
		out.close();
	}

	public static void encryptFile(File inFile, File outFile, String password)
			throws java.io.IOException, java.security.NoSuchAlgorithmException,
			javax.crypto.NoSuchPaddingException,
			java.security.spec.InvalidKeySpecException,
			java.security.InvalidKeyException {
		encryptFile(inFile, outFile, makeSecretKey(password.getBytes()));
	}

	public static void encryptFile(File inFile, File outFile, Key key)
			throws java.io.IOException, java.security.NoSuchAlgorithmException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException {
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				inFile));
		CipherOutputStream out = new CipherOutputStream(
				new BufferedOutputStream(new FileOutputStream(outFile)), cipher);
		byte[] buffer = new byte[8192];
		int length;
		while ((length = in.read(buffer)) != -1)
			out.write(buffer, 0, length);
		in.close();
		out.close();
	}

	public static void decryptFile(File inFile, File outFile, Key key)
			throws java.io.IOException, java.security.NoSuchAlgorithmException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException {
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		CipherInputStream in = new CipherInputStream(new BufferedInputStream(
				new FileInputStream(inFile)), cipher);
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(outFile));
		byte[] buffer = new byte[8192];
		int length;
		while ((length = in.read(buffer)) != -1)
			out.write(buffer, 0, length);
		in.close();
		out.close();
	}

	public static void decryptFile(File inFile, File outFile, String password)
			throws java.io.IOException, java.security.NoSuchAlgorithmException,
			java.security.spec.InvalidKeySpecException,
			javax.crypto.NoSuchPaddingException,
			java.security.InvalidKeyException {
		decryptFile(inFile, outFile, makeSecretKey(password.getBytes()));
	}

	public static byte[] digestSHA(byte[] input)
			throws java.security.NoSuchAlgorithmException {
		return digest(input, "SHA");
	}

	public static byte[] digestMD5(byte[] input)
			throws java.security.NoSuchAlgorithmException {
		return digest(input, "MD5");
	}

	public static byte[] digest(byte[] input, String algorithm)
			throws java.security.NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		md.update(input);
		return md.digest();
	}

	public static byte[] digestSHA(InputStream in) throws java.io.IOException,
			java.security.NoSuchAlgorithmException {
		return digest(in, "SHA");
	}

	public static byte[] digestMD5(InputStream in) throws java.io.IOException,
			java.security.NoSuchAlgorithmException {
		return digest(in, "MD5");
	}

	public static byte[] digest(InputStream in, String algorithm)
			throws java.io.IOException, java.security.NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		DigestInputStream din = new DigestInputStream(in, md);
		byte[] buffer = new byte[8192];
		while (din.read(buffer) != -1)
			;
		return md.digest();
	}

	public static byte[] mac(byte[] input)
			throws java.security.InvalidKeyException,
			java.security.NoSuchAlgorithmException {
		SecureRandom sr = new SecureRandom();
		byte[] keyb = new byte[20];
		sr.nextBytes(keyb);
		SecretKey key = new SecretKeySpec(keyb, "HmacSHA1");
		Mac m = Mac.getInstance("HmacSHA1");
		m.init(key);
		m.update(input);
		return m.doFinal();
	}

	public static byte[] signature(File storefile, File messagefile,
			String alias, String storepass, String keypass)
			throws java.security.SignatureException,
			java.security.NoSuchAlgorithmException,
			java.security.KeyStoreException, java.security.InvalidKeyException,
			java.security.cert.CertificateException,
			java.security.UnrecoverableKeyException, java.io.IOException {
		KeyStore ks = KeyStore.getInstance("JKS"); // SUN provider
		ks.load(new FileInputStream(storefile), storepass.toCharArray());
		return signature(IOHelper.load(messagefile),
				(PrivateKey) ks.getKey(alias, keypass.toCharArray()));
	}

	public static byte[] signature(File messagefile, PrivateKey privkey)
			throws java.security.SignatureException,
			java.security.NoSuchAlgorithmException, java.io.IOException,
			java.security.InvalidKeyException {
		return signature(IOHelper.load(messagefile), privkey);
	}

	public static byte[] signature(byte[] input, PrivateKey privkey)
			throws java.security.SignatureException,
			java.security.InvalidKeyException,
			java.security.NoSuchAlgorithmException {
		Signature s = Signature.getInstance("DSA");
		// signing ...
		s.initSign(privkey);
		s.update(input);
		return s.sign();
	}

	public static boolean verifySignature(File storefile, String alias,
			String storepass, File messagefile, File sigfile)
			throws java.security.SignatureException,
			java.security.KeyStoreException,
			java.security.NoSuchAlgorithmException,
			java.security.cert.CertificateException,
			java.security.InvalidKeyException, java.io.IOException {
		KeyStore ks = KeyStore.getInstance("JKS"); // SUN provider
		ks.load(new FileInputStream(storefile), storepass.toCharArray());
		return verifySignature(IOHelper.load(messagefile),
				IOHelper.load(sigfile), ks.getCertificate(alias).getPublicKey());
	}

	public static boolean verifySignature(File messagefile, File sigfile,
			PublicKey pubkey) throws java.security.SignatureException,
			java.io.IOException, java.security.NoSuchAlgorithmException,
			java.security.InvalidKeyException {
		return verifySignature(IOHelper.load(messagefile),
				IOHelper.load(sigfile), pubkey);
	}

	public static boolean verifySignature(byte[] input, byte[] sig,
			PublicKey pubkey) throws java.security.SignatureException,
			java.security.NoSuchAlgorithmException,
			java.security.InvalidKeyException {
		Signature s = Signature.getInstance("DSA");
		// signing ...
		s.initVerify(pubkey);
		s.update(input);
		return s.verify(sig);
	}

	public static String passwordToSHA1(String username, String password) {
		String passwordHash = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			md.update(username.getBytes("UTF8"));
			md.update(password.getBytes("UTF8"));
			passwordHash = encoder.encode(md.digest());
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return passwordHash;
	}

	public static void printHex(PrintStream out, byte[] input) {
		for (int i = 0; i < input.length; i++) {
			out.print(hexFormat.format(input[i] % 0xFFF));
		}
		out.println();
	}

	public static void main(String[] args) {
		if (args.length < 4) {
			System.err
					.println("Usage: java "
							+ CryptoUtil.class.getName()
							+ "[-encrypt|-decrypt] input-filename out-filename password");
			System.exit(1);
		}
		Provider[] pds = Security.getProviders();
		for (int i = 0; i < pds.length; i++) {
			System.out.println("Provider " + pds[i].getClass().getName() + ":"
					+ pds[i]);
			/*
			 * Iterator iter = pds[i].entrySet().iterator(); while
			 * (iter.hasNext()) { Object next = iter.next();
			 * System.out.println("  " + next.getClass().getName() + ":" +
			 * next); }
			 */
		}

		if (args[0].equals("-encrypt")) {
			try {
				if (useSalt) {
					System.out.println("using salt");
					CryptoUtil.encryptFile(new File(args[1]),
							new File(args[2]), args[3], 3);
				} else {
					System.out.println("not using salt");
					CryptoUtil.encryptFile(new File(args[1]),
							new File(args[2]), args[3]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (args[0].equals("-decrypt")) {
			try {
				if (useSalt) {
					System.out.println("using salt");
					CryptoUtil.decryptFile(new File(args[1]),
							new File(args[2]), args[3], 3);
				} else {
					System.out.println("not using salt");
					CryptoUtil.decryptFile(new File(args[1]),
							new File(args[2]), args[3]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
