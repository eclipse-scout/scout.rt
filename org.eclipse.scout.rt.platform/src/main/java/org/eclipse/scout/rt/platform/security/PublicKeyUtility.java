/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @deprecated Use {@link SecurityUtility#createSignature(byte[], byte[])} and
 *             {@link SecurityUtility#verifySignature(byte[], byte[], byte[])} instead. Please note that existing
 *             signatures are not compatible with the new verification.
 */
@Deprecated
public final class PublicKeyUtility {

  private PublicKeyUtility() {
  }

  /**
   * @param keyAlgorithm
   *          default is "DSA"
   * @param bitSize
   *          default is -1 to create a key pair for asymmmetric encryption.
   * @return two Strings, the public key (index 0) and the private key (index 1)
   * @throws NoSuchAlgorithmException
   */
  public static byte[][]/* public key/private key */ createKeyPair(String keyAlgorithm, int bitSize) throws NoSuchAlgorithmException {
    if (keyAlgorithm == null) {
      keyAlgorithm = "DSA";
    }
    KeyPairGenerator instance = KeyPairGenerator.getInstance(keyAlgorithm);
    if (bitSize > 0) {
      instance.initialize(bitSize);
    }
    KeyPair pair = instance.generateKeyPair();
    PrivateKey privKey = pair.getPrivate();
    PublicKey pubKey = pair.getPublic();
    byte[][] a = new byte[2][];
    a[0] = pubKey.getEncoded();
    a[1] = privKey.getEncoded();
    return a;
  }

  /**
   * @param keyAlgorithm
   *          default is DSA
   * @param signAlgorithm
   *          default is SHA1withDSA
   * @return data signed with the private key
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws InvalidKeyException
   * @throws SignatureException
   */
  public static byte[]/* Signature */ sign(byte[] data, byte[] privateKey, String keyAlgorithm, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    if (keyAlgorithm == null) {
      keyAlgorithm = "DSA";
    }
    if (signAlgorithm == null) {
      signAlgorithm = "SHA1withDSA";
    }

    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
    KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
    PrivateKey privKey = keyFactory.generatePrivate(keySpec);

    Signature sign = Signature.getInstance(signAlgorithm);
    sign.initSign(privKey);
    sign.update(data);

    byte[] signatureData = sign.sign();
    return signatureData;
  }

  /**
   * @param data
   *          to check on
   * @param publicKey
   *          to check with
   * @param signatureData
   *          signature to check against
   * @param keyAlgorithm
   *          default is DSA
   * @param signAlgorithm
   *          default is SHA1withDSA
   * @return true if the verification of the signature on data with the public key succeeds.
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws SignatureException
   * @throws InvalidKeySpecException
   */
  public static boolean verify(byte[] data, byte[] publicKey, byte[] signatureData, String keyAlgorithm, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
    if (keyAlgorithm == null) {
      keyAlgorithm = "DSA";
    }
    if (signAlgorithm == null) {
      signAlgorithm = "SHA1withDSA";
    }
    if (data == null || signatureData == null) {
      return false;
    }

    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
    KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
    PublicKey pubKey = keyFactory.generatePublic(keySpec);

    Signature sign = Signature.getInstance(signAlgorithm);
    sign.initVerify(pubKey);
    sign.update(data);
    boolean ok = sign.verify(signatureData);
    return ok;
  }
}
