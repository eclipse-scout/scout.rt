package org.eclipse.scout.rt.platform.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Utility class for encryption/decryption, hashing, random number generation and digital signatures.<br>
 * <br>
 * <b>Note:</b> this class requires the following providers to be available and authenticated in the running JRE: SUN,
 * SunJCE, SunEC. See
 * <a href="http://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#SunJCEProvider">Java
 * Cryptography Architecture Oracle Providers Documentation for JDK 8</a>.
 *
 * @since 6.1
 */
@Order(5500)
public class SunSecurityProvider implements ISecurityProvider {

  /**
   * Buffer size for {@link InputStream} read.
   */
  protected static final int BUF_SIZE = 8192;

  @Override
  public byte[] decrypt(byte[] encryptedData, String password, byte[] salt, int keyLen) {
    return doCrypt(encryptedData, password, salt, Cipher.DECRYPT_MODE, keyLen);
  }

  @Override
  public byte[] encrypt(byte[] clearTextData, String password, byte[] salt, int keyLen) {
    return doCrypt(clearTextData, password, salt, Cipher.ENCRYPT_MODE, keyLen);
  }

  protected byte[] doCrypt(byte[] input, String password, byte[] salt, int mode, int keyLen) {
    if (input == null) {
      throw new IllegalArgumentException("input must not be null");
    }
    if (StringUtility.isNullOrEmpty(password)) {
      throw new IllegalArgumentException("password must not be empty.");
    }
    if (salt == null || salt.length < 1) {
      throw new IllegalArgumentException("salt must be provided.");
    }
    if (keyLen != 128 && keyLen != 192 && keyLen != 256) {
      throw new IllegalArgumentException("key length must be 128, 192 or 256.");
    }

    /**
     * Length (in bytes) of Initialization Vector for Galois/Counter Mode (as defined in
     * <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP
     * 800-38D</a> ).
     */
    @SuppressWarnings("squid:S00117")
    final int GCM_INITIALIZATION_VECTOR_LEN = 16;

    /**
     * Length (in bits) of authentication tag T of Initialization Vector for Galois/Counter Mode (as defined in
     * <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP
     * 800-38D</a> ).
     */
    @SuppressWarnings("squid:S00117")
    final int GCM_AUTH_TAG_BIT_LEN = 128;

    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(getSecretKeyAlgorithm(), getCipherAlgorithmProvider());
      KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, getKeyDerivationIterationCount(), keyLen + (GCM_INITIALIZATION_VECTOR_LEN * 8));
      SecretKey tmp = factory.generateSecret(spec);

      // derive Key and Initialization Vector
      byte[] encoded = tmp.getEncoded();
      byte[] iv = new byte[GCM_INITIALIZATION_VECTOR_LEN];
      byte[] key = new byte[keyLen / 8];
      System.arraycopy(encoded, 0, key, 0, key.length);
      System.arraycopy(encoded, key.length, iv, 0, GCM_INITIALIZATION_VECTOR_LEN);

      SecretKey secret = new SecretKeySpec(key, getCipherAlgorithm());
      GCMParameterSpec parameters = new GCMParameterSpec(GCM_AUTH_TAG_BIT_LEN, iv);

      Cipher cipher = Cipher.getInstance(getCipherAlgorithm() + "/" + getCipherAlgorithmMode() + "/" + getCipherAlgorithmPadding(), getCipherAlgorithmProvider());
      cipher.init(mode, secret, parameters);

      return cipher.doFinal(input);
    }
    catch (NoSuchAlgorithmException e) {
      throw new ProcessingException("Unable to crypt data. Algorithm could not be found. Make sure to use JRE 1.8 or newer.", e);
    }
    catch (InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
      throw new ProcessingException("Unable to crypt data.", e);
    }
  }

  @Override
  public SecureRandom createSecureRandom() {
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(new byte[1]); // force self-seed (required for some implementations)
    return secureRandom;
  }

  @Override
  public byte[] createSecureRandomBytes(int numBytes) {
    if (numBytes < 1) {
      throw new IllegalArgumentException(numBytes + " is not a valid number for random bytes.");
    }
    byte[] rnd = new byte[numBytes];
    new SecureRandom().nextBytes(rnd);
    return rnd;
  }

  @Override
  public byte[] createHash(InputStream data, byte[] salt, int iterations) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    try {
      MessageDigest digest = MessageDigest.getInstance(getDigestAlgorithm(), getDigestAlgorithmProvider());
      digest.reset();
      if (salt != null && salt.length > 0) {
        digest.update(salt);
      }

      int n;
      byte[] buf = new byte[BUF_SIZE];
      while ((n = data.read(buf)) >= 0) {
        digest.update(buf, 0, n);
      }

      byte[] key = digest.digest();
      for (int i = 1; i < iterations; i++) {
        key = digest.digest(key);
        digest.reset();
      }
      return key;
    }
    catch (NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
      throw new ProcessingException("Unable to hash.", e);
    }
  }

  @Override
  public KeyPairBytes createKeyPair() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(getKeyPairGenerationAlgorithm(), getSignatureProvider());
      ECGenParameterSpec spec = new ECGenParameterSpec(getEllipticCurveName());
      keyGen.initialize(spec, createSecureRandom());
      KeyPair keyPair = keyGen.generateKeyPair();

      X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
      PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());

      return new KeyPairBytes(pkcs8EncodedKeySpec.getEncoded(), x509EncodedKeySpec.getEncoded());
    }
    catch (NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
      throw new ProcessingException("unable to create a new key-pair", e);
    }
  }

  @Override
  public byte[] createSignature(byte[] privateKey, InputStream data) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    if (privateKey == null) {
      throw new IllegalArgumentException("no private key provided");
    }
    try {
      // create private key from bytes
      KeyFactory keyFactory = KeyFactory.getInstance(getKeyPairGenerationAlgorithm(), getSignatureProvider());
      PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
      PrivateKey priv = keyFactory.generatePrivate(privateKeySpec);

      // create signature
      Signature sig = Signature.getInstance(getSignatureAlgorithm(), getSignatureProvider());
      sig.initSign(priv, createSecureRandom());

      int n;
      byte[] buf = new byte[BUF_SIZE];
      while ((n = data.read(buf)) >= 0) {
        sig.update(buf, 0, n);
      }

      return sig.sign();
    }
    catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException | IOException e) {
      throw new ProcessingException("unable to create signature.", e);
    }
  }

  @Override
  public boolean verifySignature(byte[] publicKey, InputStream data, byte[] signatureToVerify) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    if (publicKey == null) {
      throw new IllegalArgumentException("no public key provided");
    }
    if (signatureToVerify == null) {
      throw new IllegalArgumentException("no signature provided");
    }
    try {
      // create public key from bytes
      KeyFactory keyFactory = KeyFactory.getInstance(getKeyPairGenerationAlgorithm(), getSignatureProvider());
      X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
      PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

      // verify signature
      Signature sig = Signature.getInstance(getSignatureAlgorithm(), getSignatureProvider());
      sig.initVerify(pubKey);

      int n;
      byte[] buf = new byte[BUF_SIZE];
      while ((n = data.read(buf)) >= 0) {
        sig.update(buf, 0, n);
      }

      return sig.verify(signatureToVerify);
    }
    catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | InvalidKeyException | SignatureException | IOException t) {
      throw new ProcessingException("unable to verify signature", t);
    }
  }

  @Override
  public byte[] createMac(byte[] password, InputStream data) {
    if (data == null) {
      throw new IllegalArgumentException("no data provided");
    }
    if (password == null) {
      throw new IllegalArgumentException("no password provided");
    }
    try {
      String algorithm = getMacAlgorithm();
      SecretKeySpec key = new SecretKeySpec(password, 0, password.length, algorithm);
      Mac mac = Mac.getInstance(algorithm, getMacAlgorithmProvider());
      mac.init(key);

      int n;
      byte[] buf = new byte[BUF_SIZE];
      while ((n = data.read(buf)) >= 0) {
        mac.update(buf, 0, n);
      }

      return mac.doFinal();
    }
    catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | IOException | NoSuchProviderException e) {
      throw new ProcessingException("unable to create signature.", e);
    }
  }

  /**
   * @return The MAC algorithm to use.
   */
  protected String getMacAlgorithm() {
    // The HmacSHA256 algorithm (as defined in <a href="http://www.ietf.org/rfc/rfc2104.txt">RFC 2104</a>) with SHA-256 as the message digest algorithm.
    return "HmacSHA256";
  }

  /**
   * @return The provider of the MAC algorithm.
   * @see #getMacAlgorithm()
   */
  protected String getMacAlgorithmProvider() {
    // Sun Java Cryptography Extension Provider
    return "SunJCE";
  }

  /**
   * @return Iteration count for key derivation.
   */
  protected int getKeyDerivationIterationCount() {
    return 3557;
  }

  /**
   * @return The algorithm for digital signatures.
   */
  protected String getSignatureAlgorithm() {
    // The Elliptic Curve Digital Signature Algorithm as defined in ANSI X9.62.
    return "SHA512withECDSA";
  }

  /**
   * @return The provider of the signature algorithm
   * @see #getSignatureAlgorithm()
   */
  protected String getSignatureProvider() {
    // Provider for the Elliptic Curve algorithm
    return "SunEC";
  }

  /**
   * @return The algorithm for public- and private-key-pair generation.
   */
  protected String getKeyPairGenerationAlgorithm() {
    // Generates key-pairs for the Elliptic Curve algorithm.
    return "EC";
  }

  /**
   * @return The standard name of the curve to use. Only used if the key-pair algorithm is EC.
   * @see #getKeyPairGenerationAlgorithm()
   */
  protected String getEllipticCurveName() {
    // Koblitz curve secp256k1 as recommended by <a href="http://www.secg.org/sec2-v2.pdf">Standards for Efficient Cryptography Group</a>.
    return "secp256k1";
  }

  /**
   * @return The algorithm to use for message digest (Hash)
   */
  protected String getDigestAlgorithm() {
    // SHA-512 hash algorithms defined in the <a href="http://csrc.nist.gov/publications/fips/fips180-4/fips-180-4.pdf">FIPS PUB 180-4</a>.
    return "SHA-512";
  }

  /**
   * @return The provider of the digest algorithm.
   * @see #getDigestAlgorithm()
   */
  protected String getDigestAlgorithmProvider() {
    return "SUN";
  }

  /**
   * @return The key-derivation algorithm (algorithm to create a key based on a password) to use for the
   *         encryption/decryption.
   */
  protected String getSecretKeyAlgorithm() {
    // Password-based key-derivation algorithm (<a href="http://tools.ietf.org/search/rfc2898">PKCS #5 2.0</a>)
    // using The HmacSHA algorithm (<a href="http://www.ietf.org/rfc/rfc2104.txt">RFC 2104</a>) as pseudo-random function.
    return "PBKDF2WithHmacSHA256";
  }

  /**
   * @return The algorithm to use for encryption/decryption.
   */
  protected String getCipherAlgorithm() {
    // Advanced Encryption Standard as specified by <a href="http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf">NIST in FIPS 197</a>.
    // Also known as the Rijndael algorithm by Joan Daemen and Vincent Rijmen. AES is a 128-bit block cipher.
    return "AES";
  }

  /**
   * @return The provider of the encryption/decryption cipher.
   * @see #getCipherAlgorithm()
   */
  protected String getCipherAlgorithmProvider() {
    // Sun Java Cryptography Extension Provider
    return "SunJCE";
  }

  /**
   * @return The block mode to use for the encryption/decryption cipher.
   */
  protected String getCipherAlgorithmMode() {
    // Galois/Counter Mode (as defined in <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>).
    return "GCM";
  }

  /**
   * @return the padding algorithm to use for encryption/decryption cipher.
   */
  protected String getCipherAlgorithmPadding() {
    // PKCS5 padding scheme (as defined in <a href="http://tools.ietf.org/html/rfc2898">PKCS #5</a>).
    return "PKCS5Padding";
  }
}
