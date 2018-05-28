import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.security.cert.X509Certificate;

public class Main {

	public static void main(String[] args) {

		if(args.length != 3) {
			System.out.println("Usage: java Main $path_to_KeystoreFile $domain_name $dictionary_path");
			System.exit(0);
		}

		TestJKSCrack m= new TestJKSCrack();
		m.runTest(args[0], args[1], args[2]);
		
	}

}
