## Reproduction of: "Mind Your Keys? A Security Evaluation of Java Keystores" (NDSS 2018)

In their publication, Focardi et al. have tested a set of seven Key Store
implementations in Java from Oracle and Bouncy Castle, in particular: 

Oracle:

*   JKS ("Java Key Store", default key store in the standard library until at least JDK8_u144
	(Aug 2017, the time the authors submitted their paper to the NDSS 2018 conference).
*	JCEKS ("Java Cryptography Extension Key Store")
*	PKCS12 (An implementation of [PKCS#12](https://en.wikipedia.org/wiki/PKCS_12))
	
Bouncy Castle:
*	UBER (not an abbreviation)
*	BCFKS 
*	BKS 
*	BCPKCS12 (Bouncy Castle PKCS12 implementation)
	
(For an overview of their properties, see Table I of the paper.)
	
The authors found several vulnerabilities. All of them are graphically listed in Table II of the paper.

Especially JKS and JCEKS are vulnerable in terms of confidentiality
(of the stored key data) and integrity (i.e. stored data can be tampered with without
knowledge of the authorized parties) and even allow the injection (and execution) of malicious code into
the key store files under certain circumstances (for details, see: Chapter C ("Threat Model"), Chapter V ("Attacks") in the paper).

They have timely disclosed their findings to Oracle and Bouncy Castle in May 2017 and also documented the 
announced updates by Oracle and BouncyCastle in their paper (see Chapter VII, "Disclosure and Security Updates").

------

In this project, we are going to reproduce one of the mentioned attacks against JKS Keystores:

* Implementation of the Pseudocode: Algorithm 1, JKS One-Block-Crack

The goal of our reproduction is to show the bruteforce speedup gained by the
algorithm (it is referred to as "Algorithm I" in the paper)of Focardi et al.
We created some vulnerable JKS Keystores using "keytool.exe" delivered with JDK 8_u144, implemented the pseudocode in java, and
created two dictionaries to simulate a bruteforce attack.

Then we ran our attack on all keystores and showed the time difference between a conventional and
an optimized bruteforce attempt. We did not use the Standard (closed source) JKS library, but a reverse-engineered version from
Casey Marshall (rsdio@metastatic.org), available at: http://metastatic.org/source/JKS.java (as of 30.4.2018).
We modified this reverse engineered version where it was necessary or useful for us.

## Experiment Setup

### Experiment Content
The experiment consists of two attack attempt. We perform them for every key that is delivered in the "data" directory and compare their speeds.

In the conventional attack attempt, we perform the standard decrypt method (REJKS.decryptKey) with every possible password.

In the optimized version, we perform a known-plaintext-attack on the first 20 Byte Block (called E1, see below) of the encrypted key.
 * E is the whole encrypted bytestring of the key, including checksum and salt. This is what you get from REJKS.getEncryptedPrivKey(String alias).
 * E1 is equal to the first 20 byte block of E. 
 * test_bytes are the three known plaintext bytes XOR the first three bytes of E1 
 * temp is equal to the known plaintext bytes (i.e. the first three bytes) for all X bit RSA Keys. 
	Due to the fact that they differ depending on the keysize, we created a hashmap of these knwon bytes 
	for all conventional keysizes.
 * W1 is a 20 byte Block and equals h(password || salt), where "h" is sha1 and "password" is the password of the current attempt.  
 * A password guess is correct iff the first three bytes of W1 are equal to the test_bytes (with a probability of a false positive of 2^-24)

This should be a lot faster, as one password attempt requires only one sha1 digest and a comparison of three bytes.

### Hardware/Software

#### Our Hardware:
 
The software has successfully run on the following platforms:
* Intel Core i7-6700HQ, 8GB DDR4 UDIMM RAM (NON-ECC)
Win10 home 64Bit

* Intel Core i5-4210U, 12GB DDR3 USIMM RAM (NON-ECC)
Win 10 Pro 64Bit

#### Our Software:
We installed the (vulnerable) JDK version that the authors used: JDK8_144 in order to use it's "keytool.exe" tool, found in the ./bin directory of the 
JDK.

## Additional Prerequisites

To run this experiment, you need:
* JDK installed
* "java" and "javac" must be in your PATH variable
* The runtest.bat is written for windows, it may be altered to a linux shell script with few changes, however.

## Experiment Assumptions
*The reverse engineered JKS implementation equates to the original (closed source) JKS implementation
	in terms of functionality and output for all methods we used.


## Experiment Steps

see runtest.bat

## Results

Depending on the keysize, the speedup lies between one and two orders of magnitude.
The authors are right with their expression that the speedup depends a lot on the size of the key. 
The reason is that the conventional attempt has to decrypt every 20 byte block of the encrypted keystream while the decryption of the one-block-crack approach consumes a constant amount of time.

Within the experiment data set, we delivered two keys ("key_2_1024.jks" and "TestKeyStore_4.jks") for which our testdictionary contains a password that results in the same three known plaintext bytes. Since the checksum shows that it is the wrong password, however, the test program continues it's search through the dictionary until it finds the proper one.


The result-outputs are shown in the ./results/output_all_* files.