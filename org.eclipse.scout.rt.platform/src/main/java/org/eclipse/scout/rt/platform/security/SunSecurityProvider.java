/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Base64Utility;

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
public class SunSecurityProvider implements ISecurityProvider, ILegacySecurityProvider {

  /**
   * Buffer size for {@link InputStream} read.
   */
  protected static final int BUF_SIZE = 8192;

  /**
   * Length (in bytes) of Initialization Vector for Galois/Counter Mode (as defined in
   * <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>
   * ).
   */
  protected static final int GCM_INITIALIZATION_VECTOR_LEN = 16;

  /**
   * Length (in bits) of authentication tag T of Initialization Vector for Galois/Counter Mode (as defined in
   * <a href="http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf">NIST Special Publication SP 800-38D</a>
   * ).
   */
  protected static final int GCM_AUTH_TAG_BIT_LEN = 128;

  @Override
  public EncryptionKey createEncryptionKey(char[] password, byte[] salt, int keyLen) {
    return createEncryptionKeyInternal(
        password,
        salt,
        keyLen,
        getSecretKeyAlgorithm(),
        getCipherAlgorithm(),
        getCipherAlgorithmProvider(),
        GCM_INITIALIZATION_VECTOR_LEN,
        GCM_AUTH_TAG_BIT_LEN,
        getKeyDerivationIterationCount());
  }

  @Override
  public EncryptionKey createDecryptionKey(char[] password, byte[] salt, int keyLen, byte[] compatibilityHeader) {
    String v = compatibilityHeader != null ? new String(compatibilityHeader, StandardCharsets.US_ASCII) : ENCRYPTION_COMPATIBILITY_HEADER_2021_V1;
    if (ENCRYPTION_COMPATIBILITY_HEADER_2021_V1.equals(v)) {
      // legacy
      return createEncryptionKeyInternal(
          password,
          salt,
          keyLen,
          "PBKDF2WithHmacSHA256",
          "AES",
          "SunJCE",
          16,
          128,
          3557);
    }
    if (ENCRYPTION_COMPATIBILITY_HEADER_2023_V1.equals(v)) {
      return createEncryptionKeyInternal(
          password,
          salt,
          keyLen,
          "PBKDF2WithHmacSHA256",
          "AES",
          "SunJCE",
          16,
          128,
          3557);
    }
    if (ENCRYPTION_COMPATIBILITY_HEADER.equals(v)) {
      // latest
      return createEncryptionKey(password, salt, keyLen);
    }
    throw new ProcessingException("Unknown compatibility header {}", v);
  }

  private static EncryptionKey createEncryptionKeyInternal(
      char[] password,
      byte[] salt,
      int keyLen,
      String secretKeyAlgorithm,
      String cipherAlgorithm,
      String cipherAlgorithmProvider,
      int gcmInitVecLen,
      int gcmAuthTagBitLen,
      int keyDerivationIterationCount) {
    assertGreater(assertNotNull(password, "password must not be null.").length, 0, "empty password is not allowed.");
    assertGreater(assertNotNull(salt, "salt must be provided.").length, 0, "empty salt is not allowed.");
    assertTrue(keyLen == 128 || keyLen == 192 || keyLen == 256, "key length must be 128, 192 or 256.");
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyAlgorithm, cipherAlgorithmProvider);
      KeySpec spec = new PBEKeySpec(password, salt, keyDerivationIterationCount, keyLen + (gcmInitVecLen * 8));
      SecretKey tmpSecret = factory.generateSecret(spec);

      // derive Key and Initialization Vector
      byte[] encoded = tmpSecret.getEncoded();
      byte[] iv = new byte[gcmInitVecLen];
      byte[] key = new byte[keyLen / 8];
      System.arraycopy(encoded, 0, key, 0, key.length);
      System.arraycopy(encoded, key.length, iv, 0, gcmInitVecLen);

      SecretKey secretKey = new SecretKeySpec(key, cipherAlgorithm);
      GCMParameterSpec parameters = new GCMParameterSpec(gcmAuthTagBitLen, iv);
      byte[] compatibilityHeader = ("[1:"
          + keyLen
          + "-" + secretKeyAlgorithm
          + "-" + cipherAlgorithm
          + "-" + cipherAlgorithmProvider
          + "-" + gcmInitVecLen
          + "-" + gcmAuthTagBitLen
          + "-" + keyDerivationIterationCount
          + "]").getBytes(StandardCharsets.US_ASCII);
      return new EncryptionKey(secretKey, parameters, compatibilityHeader);
    }
    catch (NoSuchAlgorithmException e) {
      throw new ProcessingException("Unable to create secret. Algorithm could not be found. Make sure to use JRE 1.8 or newer.", e);
    }
    catch (InvalidKeySpecException | NoSuchProviderException e) {
      throw new ProcessingException("Unable to create secret.", e);
    }
  }

  @Override
  public byte[] createPasswordHash(char[] password, byte[] salt) {
    return createPasswordHash(password, salt, MIN_PASSWORD_HASH_ITERATIONS);
  }

  /**
   * @param password
   *          The password to create the hash for. Must not be {@code null} or empty.
   * @param salt
   *          The salt to use. Use {@link #createSecureRandomBytes(int)} to generate a new random salt for each
   *          credential. Do not use the same salt for multiple credentials. The salt should be at least 32 bytes long.
   *          Remember to save the salt with the hashed password! Must not be {@code null} or an empty array.
   * @param iterations
   *          Specifies how many times the method executes its underlying algorithm. A higher value is safer.<br>
   *          While there is a minimum number of iterations recommended to ensure data safety, this value changes every
   *          year as technology improves. As by Aug 2021 at least 120000 iterations are recommended, see
   *          https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html.<br>
   *          Experimentation is important. To provide a good security use an iteration count so that the call to this
   *          method requires one half second to execute (on the production system). Also consider the number of users
   *          and the number of logins executed to find a value that scales in your environment.
   * @return the password hash
   * @throws AssertionException
   *           If one of the following conditions is {@code true}:<br>
   *           <ul>
   *           <li>The password is {@code null} or an empty array</li>
   *           <li>The salt is {@code null} or an empty array</li>
   *           <li>The number of iterations is too small.</li>
   *           </ul>
   * @throws ProcessingException
   *           If there is an error creating the hash. <br>
   */
  public byte[] createPasswordHash(char[] password, byte[] salt, int iterations) {
    assertGreater(assertNotNull(password, "password must not be null.").length, 0, "empty password is not allowed.");
    assertGreater(assertNotNull(salt, "salt must not be null.").length, 0, "empty salt is not allowed.");
    // other checks are done by the PBEKeySpec constructor

    try {
      SecretKeyFactory skf = SecretKeyFactory.getInstance(getPasswordHashSecretKeyAlgorithm(), getCipherAlgorithmProvider());
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
      SecretKey key = skf.generateSecret(spec);
      byte[] res = key.getEncoded();
      return res;
    }
    catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
      throw new ProcessingException("Unable to create password hash.", e);
    }
  }

  @Override
  public boolean verifyPasswordHash(char[] password, byte[] salt, byte[] expectedHash) {
    if (Arrays.equals(expectedHash, createPasswordHash(password, salt, MIN_PASSWORD_HASH_ITERATIONS))) {
      return true;
    }
    if (Arrays.equals(expectedHash, createPasswordHash(password, salt, MIN_PASSWORD_HASH_ITERATIONS_2021))) {
      return true;
    }
    if (Arrays.equals(expectedHash, createPasswordHash(password, salt, MIN_PASSWORD_HASH_ITERATIONS_2019))) {
      return true;
    }
    if (Arrays.equals(expectedHash, createPasswordHash(password, salt, MIN_PASSWORD_HASH_ITERATIONS_2016))) {
      return true;
    }
    //2014 variants
    if (Arrays.equals(expectedHash, createHash(new ByteArrayInputStream(new String(password).getBytes(StandardCharsets.UTF_8)), salt, 3557))) {
      return true;
    }
    if (Arrays.equals(expectedHash, createHash(new ByteArrayInputStream(new String(password).getBytes(StandardCharsets.UTF_16)), salt, 3557))) {
      return true;
    }
    return false;
  }

  @Override
  public void encrypt(InputStream clearTextData, OutputStream encryptedData, EncryptionKey key) {
    doCrypt(clearTextData, encryptedData, key, Cipher.ENCRYPT_MODE);
  }

  @Override
  public void decrypt(InputStream encryptedData, OutputStream clearTextData, EncryptionKey key) {
    doCrypt(encryptedData, clearTextData, key, Cipher.DECRYPT_MODE);
  }

  protected void doCrypt(InputStream input, OutputStream output, EncryptionKey key, int mode) {
    assertNotNull(key, "key must not be null.");
    if (input == null) {
      throw new AssertionException("input must not be null.");
    }
    if (output == null) {
      throw new AssertionException("output must not be null.");
    }

    try {
      Cipher cipher = Cipher.getInstance(getCipherAlgorithm() + "/" + getCipherAlgorithmMode() + "/" + getCipherAlgorithmPadding(), getCipherAlgorithmProvider());
      cipher.init(mode, key.get(), key.params());

      try (OutputStream out = new CipherOutputStream(output, cipher)) {
        int n;
        byte[] buf = new byte[BUF_SIZE];
        while ((n = input.read(buf)) >= 0) {
          out.write(buf, 0, n);
        }
      }
    }
    catch (NoSuchAlgorithmException e) {
      throw new ProcessingException("Unable to crypt data. Algorithm could not be found. Make sure to use JRE 1.8 or newer.", e);
    }
    catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchProviderException | IOException e) {
      throw new ProcessingException("Unable to crypt data.", e);
    }
  }

  @Override
  public SecureRandom createSecureRandom() {
    return new SecureRandom();
  }

  @Override
  public byte[] createSecureRandomBytes(int numBytes) {
    assertGreater(numBytes, 0, "{} is not a valid number for random bytes.", numBytes);
    byte[] rnd = new byte[numBytes];
    createSecureRandom().nextBytes(rnd);
    return rnd;
  }

  @Override
  public DigestInputStream toHashingStream(InputStream stream) {
    try {
      MessageDigest digest = MessageDigest.getInstance(getDigestAlgorithm(), getDigestAlgorithmProvider());
      return new DigestInputStream(stream, digest);
    }
    catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new ProcessingException("Unable to hash.", e);
    }
  }

  @Override
  public DigestOutputStream toHashingStream(OutputStream stream) {
    try {
      MessageDigest digest = MessageDigest.getInstance(getDigestAlgorithm(), getDigestAlgorithmProvider());
      return new DigestOutputStream(stream, digest);
    }
    catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new ProcessingException("Unable to hash.", e);
    }
  }

  @Override
  public byte[] createHash(InputStream data, byte[] salt, int iterations) {
    if (data == null) {
      throw new AssertionException("no data provided");
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
    assertGreater(assertNotNull(privateKey, "no private key provided").length, 0, "empty private key not allowed");
    if (data == null) {
      throw new AssertionException("no data provided");
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
      throw new ProcessingException("Unable to create signature. If the curve is not supported (see cause below), consider creating a new key-pair"
          + " by running '{}' on the command line and configure the properties (e.g. 'scout.auth.publicKey' and 'scout.auth.privateKey') with the new values.", SecurityUtility.class.getName(), e);
    }
  }

  @Override
  public boolean verifySignature(byte[] publicKey, InputStream data, byte[] signatureToVerify) {
    assertGreater(assertNotNull(publicKey, "no public key provided").length, 0, "empty public key not allowed");
    assertGreater(assertNotNull(signatureToVerify, "no signature provided").length, 0, "empty signature not allowed");
    if (data == null) {
      throw new AssertionException("no data provided");
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
    assertGreater(assertNotNull(password, "no password provided").length, 0, "empty password not allowed");
    if (data == null) {
      throw new AssertionException("no data provided");
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
   * @return Iteration count for key derivation. <a href="https://www.baeldung.com/java-secure-aes-key">AES Keys</a>
   *         <p>
   *         2023/05: at least 1000
   *         <p>
   *         Do not confuse this parameter with {@link #MIN_PASSWORD_HASH_ITERATIONS}. This parameter is used to derive
   *         a PBEKey for {@link #encrypt(InputStream, OutputStream, EncryptionKey)} whereas
   *         {@link #MIN_PASSWORD_HASH_ITERATIONS} is used to hash single passwords in a table that is potentially
   *         exposed to a rainbow attack.
   */
  protected int getKeyDerivationIterationCount() {
    return 3557;
  }

  /**
   * @return The algorithm for digital signatures.
   */
  protected String getSignatureAlgorithm() {
    // Use ECDSA for compatibility with Java 11.
    // EdDSA with Curve25519 may be used in Java >= 15
    // Also consider XMSS in future releases if available by JDK
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
    // For compatibility issues see getSignatureAlgorithm.
    // Verified with pentest specialist 2023/05: This curve is still ok since the open source version
    // of scout is running with java 11.
    // Once java 15 is the baseline then the Curve25519 and Ed448-Goldilocks will be incorporated.
    return "secp256r1"; // aka 'prime256v1', aka 'NIST P-256'
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
   * @return The algorithm to use for password hashing.
   */
  protected String getPasswordHashSecretKeyAlgorithm() {
    return "PBKDF2WithHmacSHA512";
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
    return "NoPadding ";
  }

  @Override
  public String keyStoreToHumanReadableText(InputStream keyStoreInput, String storePass, String keyPass) {
    StringWriter sw = new StringWriter();
    try (PrintWriter out = new PrintWriter(sw)) {
      KeyStore ks = KeyStore.getInstance("jks");
      ks.load(keyStoreInput, storePass.toCharArray());
      for (String alias : Collections.list(ks.aliases())) {
        out.println("Alias: " + alias);
        try {
          Certificate cert = ks.getCertificate(alias);
          if (cert != null) {
            out.println(" Certificate");
            out.println("  format: " + cert.getType());
            out.println("  base64: " + Base64Utility.encode(cert.getEncoded()));
            if (cert instanceof X509Certificate) {
              X509Certificate x = (X509Certificate) cert;
              out.println("  issuerDN: " + x.getIssuerDN());
              out.println("  subjectDN: " + x.getSubjectDN());
              out.println("  notBefore: " + x.getNotBefore());
              out.println("  notAfter: " + x.getNotAfter());
              out.println("  sigAlgName: " + x.getSigAlgName());
              out.println("  extendedKeyUsage: " + x.getExtendedKeyUsage());
              out.println("  serialNumber: " + x.getSerialNumber());
              out.println("  version: " + x.getVersion());
            }
            out.println(" PublicKey");
            out.println("  format: " + cert.getPublicKey().getFormat());
            out.println("  base64: " + Base64Utility.encode(cert.getPublicKey().getEncoded()));
            out.println("  algo: " + cert.getPublicKey().getAlgorithm());
          }
        }
        catch (Exception e) {
          out.println("Error reading entry as certificate: " + e);
        }
        if (keyPass != null) {
          try {
            Key key = ks.getKey(alias, keyPass.toCharArray());
            if (key != null) {
              out.println(" PrivateKey");
              out.println("  format: " + key.getFormat());
              out.println("  base64: " + Base64Utility.encode(key.getEncoded()));
              out.println("  algo: " + key.getAlgorithm());
            }
          }
          catch (Exception e) {
            out.println("Error reading entry as key: " + e);
          }
        }
      }
    }
    catch (IOException | GeneralSecurityException | RuntimeException e) {
      return "Error: " + e;
    }
    return sw.toString();
  }

  /**
   * Print security report
   */
  @Override
  public String toString() {
    return "Implementor: " + getClass().getName() + "\n"
        + "MinPasswordHashIterations: " + MIN_PASSWORD_HASH_ITERATIONS + "\n"
        + "MacAlgorithm: " + getMacAlgorithm() + "\n"
        + "MacAlgorithmProvider: " + getMacAlgorithmProvider() + "\n"
        + "KeyDerivationIterationCount (PBE): " + getKeyDerivationIterationCount() + "\n"
        + "SignatureAlgorithm: " + getSignatureAlgorithm() + "\n"
        + "SignatureProvider: " + getSignatureProvider() + "\n"
        + "KeyPairGenerationAlgorithm: " + getKeyPairGenerationAlgorithm() + "\n"
        + "EllipticCurveName: " + getEllipticCurveName() + "\n"
        + "DigestAlgorithm: " + getDigestAlgorithm() + "\n"
        + "DigestAlgorithmProvider: " + getDigestAlgorithmProvider() + "\n"
        + "SecretKeyAlgorithm: " + getSecretKeyAlgorithm() + "\n"
        + "PasswordHashSecretKeyAlgorithm: " + getPasswordHashSecretKeyAlgorithm() + "\n"
        + "CipherAlgorithm: " + getCipherAlgorithm() + "\n"
        + "CipherAlgorithmProvider: " + getCipherAlgorithmProvider() + "\n"
        + "CipherAlgorithmMode: " + getCipherAlgorithmMode() + "\n"
        + "CipherAlgorithmPadding: " + getCipherAlgorithmPadding() + "\n";
  }
}
