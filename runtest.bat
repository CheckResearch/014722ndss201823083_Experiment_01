cd code
javac Main.java

echo KeySet one, small number of attempts

java Main ..\data\key_1_512.jks testdomain.com ..\data\dictionary_small.txt
java Main ..\data\key_1.2_768.jks testdomain.com ..\data\dictionary_small.txt
java Main ..\data\key_2_1024.jks testdomain.com ..\data\dictionary_small.txt
java Main ..\data\key_3_2048.jks testdomain.com ..\data\dictionary_small.txt
java Main ..\data\key_4_4096.jks testdomain.com ..\data\dictionary_small.txt
java Main ..\data\key_5_8192.jks testdomain.com ..\data\dictionary_small.txt

echo KeySet one, large number of attempts

java Main ..\data\key_1_512.jks testdomain.com ..\data\dictionary_large.txt
java Main ..\data\key_1.2_768.jks testdomain.com ..\data\dictionary_large.txt
java Main ..\data\key_2_1024.jks testdomain.com ..\data\dictionary_large.txt
java Main ..\data\key_3_2048.jks testdomain.com ..\data\dictionary_large.txt
java Main ..\data\key_4_4096.jks testdomain.com ..\data\dictionary_large.txt
java Main ..\data\key_5_8192.jks testdomain.com ..\data\dictionary_large.txt

echo KeySet two, small number of attempts

java Main ..\data\TestKeyStore_1.jks testdomain.com ..\data\dictionary_small.txt
java Main ..\data\TestKeyStore_2.jks testdomain.com ..\data\dictionary_small.txt
java Main ..\data\TestKeyStore_3.jks testdomain.com ..\data\dictionary_small.txt
java Main ..\data\TestKeyStore_4.jks testdomain.com ..\data\dictionary_small.txt

echo KeySet two, large number of attempts

java Main ..\data\TestKeyStore_1.jks testdomain.com ..\data\dictionary_large.txt
java Main ..\data\TestKeyStore_2.jks testdomain.com ..\data\dictionary_large.txt
java Main ..\data\TestKeyStore_3.jks testdomain.com ..\data\dictionary_large.txt
java Main ..\data\TestKeyStore_4.jks testdomain.com ..\data\dictionary_large.txt


del *.class
